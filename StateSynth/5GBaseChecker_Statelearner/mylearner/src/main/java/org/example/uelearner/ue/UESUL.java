package org.example.uelearner.ue;

/*
 *  Copyright (c) 2016 Joeri de Ruiter
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */


import de.learnlib.api.SUL;
import net.automatalib.words.impl.SimpleAlphabet;
import org.example.uelearner.StateLearnerSUL;
import org.example.uelearner.devices.DeviceSUL;
import org.example.uelearner.devices.DeviceSULFactory;
import org.example.uelearner.tgbot.NotificationBot;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;


public class UESUL implements StateLearnerSUL<String, String> {
    private static final String[] WIN_RUNTIME = {"cmd.exe", "/C"};
    private static final String[] OS_LINUX_RUNTIME = {"/bin/bash", "-l", "-c"};
    private static final List<String> expectedResults = Arrays.asList(
            "registration_request",
            "registration_request_guti",
            "id_response",
            "auth_response",
            "nas_sm_complete",
            "nas_sm_reject",
            "registration_complete",
            "config_update_complete",
            "deregistration_accept",
            "rrc_reconf_complete",
            "rrc_sm_complete",
            "rrc_sm_failure",
            "rrc_ue_cap_info",
            "rrc_ueinfo_response",
            "rrc_countercheck_response",
            "null_action",
            "DONE");
    private static final List<String> enable_s1_expectedResults = Arrays.asList("registration_request", "registration_request_guti", "null_action");
    private static final List<String> id_request_expectedResults = Arrays.asList("id_response", "null_action");
    private static final List<String> auth_request_expectedResults = Arrays.asList("auth_response", "null_action");
    private static final List<String> nas_sm_cmd_expectedResults = Arrays.asList("nas_sm_complete", "nas_sm_reject", "null_action");
    private static final List<String> registration_accept_expectedResults = Arrays.asList("registration_complete", "null_action");
    private static final List<String> deregistration_req_expectedResults = Arrays.asList("deregistration_accept", "null_action");
    private static final List<String> config_update_cmd_expectedResults = Arrays.asList("config_update_complete", "null_action");
    private static final List<String> rrc_reconf_expectedResults = Arrays.asList("rrc_reconf_complete", "null_action");
    private static final List<String> rrc_sm_cmd_expectedResults = Arrays.asList("rrc_sm_complete", "rrc_sm_failure", "null_action");
    private static final List<String> ue_cap_enquiry_expectedResults = Arrays.asList("rrc_ue_cap_info", "null_action");
    private static final List<String> rrc_countercheck_expectedResults = Arrays.asList("rrc_countercheck_response", "null_action");
    private static final List<String> rrc_ueinfo_req_expectedResults = Arrays.asList("rrc_ueinfo_response", "null_action");
    private final DeviceSUL deviceSUL;
    public static LTEUEConfig config;
    public Socket amf_socket, enodeb_socket, ue_socket;
    public BufferedWriter amf_out, enodeb_out, ue_out;
    public BufferedReader amf_in, enodeb_in, ue_in;
    public int enable_s1_timeout_count = 0;
    public Boolean pre_needed = true;
    public Map<String, Boolean> Inputmap = new HashMap<>();
    public Map<String, Boolean> Outputmap = new HashMap<>();
    public SimpleAlphabet<String> alphabet;
    int enable_s1_count = 0;
    public int auth_response_counter = 0;
    public int nas_sm_complete_counter = 0;
    boolean sqn_synchronized = false;
    int reset_amf_count = 0;
    int unexpected = 0;

    public UESUL(LTEUEConfig config) throws Exception {
        this.config = config;
        alphabet = new SimpleAlphabet<String>(Arrays.asList(config.alphabet.split(" ")));

        Input_dic_init();
        Output_dic_init();

        System.out.println("Starting Core & gNodeB");
        start_core_enb();
        System.out.println("Finished starting up Core & gNodeB");
        init_core_enb_con();
        init_ue_con();
        System.out.println("Done with initializing the connection with UE, gNodeB, and Core.");
        if(config.tgbot_enable == 1){
            NotificationBot.sendToTelegram("Start Learning " + config.device);
        }
        this.deviceSUL = DeviceSULFactory.getSULByDevice(config.device, this);
        if (deviceSUL == null) {
            System.out.println("config.device is wrong, or not handled in Device_SUL_Factory. Exiting...");
            System.exit(1);
        }
    }

    public void start_eNodeB() {
        String command = String.format("echo \"%s\" | sudo -S ./start_gnb.sh", config.password); // ./start_srs_gnb.sh -> srsue
        String password1 = config.password;
        System.out.println("password1: " + password1);
        System.out.println("Command: " + command);
        runProcess(false, command);
//        runProcess(false, "echo \"USENIX24\" | sudo -S ./start_gnb.sh"); // commercial UE
//        runProcess(false, "echo \"USENIX24\" | sudo -S ./start_srs_gnb.sh"); // srsue
    }

    public static void start_core() {
        String command = String.format("echo \"%s\" | sudo -S ./start_core.sh", config.password);
        String password1 = config.password;
        System.out.println("password1: " + password1);
        System.out.println("Command: " + command);
        runProcess(false, command);
    }


    public static void kill_eNodeb() {
        String command = String.format("echo \"%s\" | sudo -S ./kill_gnb.sh", config.password);
        runProcess(false, command);
    }

    public static void kill_eNodeb_handle() {
        String command = String.format("echo \"%s\" | sudo -S ./kill_gnb_handle.sh", config.password);
        runProcess(false, command);
    }

    public static void kill_core() {
        String command = String.format("echo \"%s\" | sudo -S ./kill_core.sh", config.password);
        runProcess(false, command);
    }

    public static void kill_core_handle() {
        String command = String.format("echo \"%s\" | sudo -S ./kill_core_handle.sh", config.password);
        runProcess(false, command);
    }


    public static void kill_uecontroller() {
        String command = String.format("echo \"%s\" | sudo -S ./kill_uecontroller.sh", config.password);
        runProcess(false, command);
    }


    public static void kill_evt() {
        String command = String.format("echo \"%s\" | sudo -S ./kill_evt.sh", config.password);
        runProcess(false, command);
    }

    public static void start_ue() {
        String command = String.format("echo \"%s\" | sudo -S ./start_ue.sh", config.password);
        runProcess(false, command);
    }

    public static void start_uecontroller() {
        runProcess(false, "python3 ./UEController/UEController.py");
    }

    public static void runProcess(boolean isWin, String... command) {
        System.out.print("command to run: ");
        for (String s : command) {
            System.out.print(s);
        }
        System.out.print("\n");
        String[] allCommand = null;
        try {
            if (isWin) {
                allCommand = concat(WIN_RUNTIME, command);
            } else {
                allCommand = concat(OS_LINUX_RUNTIME, command);
            }
            ProcessBuilder pb = new ProcessBuilder(allCommand);
            pb.redirectErrorStream(true);
            Process p = pb.start();

        } catch (IOException e) {
            System.out.println("ERROR: " + command + " is not running after invoking script");
            System.out.println("Attempting again...");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public SimpleAlphabet<String> getAlphabet() {
        return alphabet;
    }

    public boolean canFork() {
        return false;
    }

    public SUL<String, String> fork() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Cannot fork SocketSUL");
    }

    public void post() {
        deviceSUL.post();
    }

    public String step(String symbol) {
        return deviceSUL.step(symbol);
    }

    public void pre() {
        deviceSUL.pre();
    }


    public void handle_enb_core_failure() {
        String result = "";

        try {
            restart_core_enb();
            enable_s1_timeout_count = 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("gNB Core FAILURE HANDLING DONE.");

    }

    public void Input_dic_init() {
        Inputmap.put("enable_s1", false);
        Inputmap.put("rrc_countercheck_protected",false);
        Inputmap.put("rrc_countercheck_plain_text",false);
        Inputmap.put("rrc_ueinfo_req_protected",false);
        Inputmap.put("rrc_ueinfo_req_plain_text",false);
        Inputmap.put("rrc_sm_cmd", false);
        Inputmap.put("rrc_sm_cmd_ns",false);
        Inputmap.put("rrc_sm_cmd_ns_plain_text",false);
        Inputmap.put("rrc_sm_cmd_replay", false);
        Inputmap.put("rrc_reconf_protected", false);
        Inputmap.put("rrc_reconf_replay", false);
        Inputmap.put("rrc_reconf_plain_text", false);
        Inputmap.put("ue_cap_enquiry_protected", false);
        Inputmap.put("ue_cap_enquiry_plain_text", false);
        Inputmap.put("id_request_plain_text", false);
        Inputmap.put("auth_request_plain_text", false);
        Inputmap.put("nas_sm_cmd", false);
        Inputmap.put("nas_sm_cmd_ns",false);
        Inputmap.put("nas_sm_cmd_ns_plain_text",false);
        Inputmap.put("nas_sm_cmd_replay", false);
        Inputmap.put("nas_sm_cmd_protected", false);
        Inputmap.put("registration_accept_plain_text", false);
        Inputmap.put("registration_accept_h4_plain_text",false);
        Inputmap.put("registration_accept_protected", false);
        Inputmap.put("deregistration_req_protected",false);
        Inputmap.put("deregistration_req_plain_text",false);
        Inputmap.put("config_update_cmd_protected", false);
        Inputmap.put("config_update_cmd_replay", false);
        Inputmap.put("config_update_cmd_plain_text", false);
    }

    public void Output_dic_init() {
        Outputmap.put("rrc_reconf_complete", false);
        Outputmap.put("rrc_sm_complete", false);
        Outputmap.put("rrc_sm_failure", false);
        Outputmap.put("rrc_ue_cap_info", false);
        Outputmap.put("rrc_countercheck_response", false);
        Outputmap.put("rrc_ueinfo_response", false);
        Outputmap.put("registration_request", false);
        Outputmap.put("registration_request_guti", false);
        Outputmap.put("deregistration_accept", false);
        Outputmap.put("auth_response", false);
        Outputmap.put("nas_sm_complete", false);
        Outputmap.put("nas_sm_reject", false);
        Outputmap.put("registration_complete", false);
        Outputmap.put("id_response", false);
        Outputmap.put("config_update_complete", false);
    }

    /**
     * Methods to kill and restart Core and gNB
     */

    public void start_core_enb() {
        // kill and start the processes
        try {
            start_core();
            sleep(4 * 1000);
            start_eNodeB();
            sleep(8 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void restart_core_enb() {
        try {

            amf_out.close();
            amf_in.close();
            amf_socket.close();

            enodeb_out.close();
            enodeb_in.close();
            enodeb_socket.close();

            sleep(1000);


            start_core_enb();

            amf_socket = new Socket(config.mme_controller_ip_address, config.mme_port);
            amf_socket.setTcpNoDelay(true);
            amf_out = new BufferedWriter(new OutputStreamWriter(amf_socket.getOutputStream()));
            amf_in = new BufferedReader(new InputStreamReader(amf_socket.getInputStream()));

            enodeb_socket = new Socket(config.enodeb_controller_ip_address, config.enodeb_port);
            enodeb_socket.setTcpNoDelay(true);
            enodeb_out = new BufferedWriter(new OutputStreamWriter(enodeb_socket.getOutputStream()));
            enodeb_in = new BufferedReader(new InputStreamReader(enodeb_socket.getInputStream()));


        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init_core_enb_con() {
        try {
            System.out.println("Connecting to core..");
            amf_socket = new Socket(config.mme_controller_ip_address, config.mme_port);
            amf_socket.setTcpNoDelay(true);
            amf_out = new BufferedWriter(new OutputStreamWriter(amf_socket.getOutputStream()));
            amf_in = new BufferedReader(new InputStreamReader(amf_socket.getInputStream()));
            System.out.println("Connected with core.");

            System.out.println("Connecting to srsgNB...");
            enodeb_socket = new Socket(config.enodeb_controller_ip_address, config.enodeb_port);
            enodeb_socket.setTcpNoDelay(true);
            enodeb_out = new BufferedWriter(new OutputStreamWriter(enodeb_socket.getOutputStream()));
            enodeb_in = new BufferedReader(new InputStreamReader(enodeb_socket.getInputStream()));
            System.out.println("Connected with srsgNB.");

            String result = "";
            try {
                sleep(2 * 1000);
                amf_out.write("Hello\n");
                amf_out.flush();
                String result_core = amf_in.readLine();
                System.out.println("Received = " + result_core);


                sleep(2 * 1000);
                enodeb_out.write("Hello\n");
                enodeb_out.flush();
                result = enodeb_in.readLine();
                System.out.println("Received = " + result);

//                sleep(2 * 1000);
//                amf_out.write("Hello\n");
//                amf_out.flush();
//                String result_core = amf_in.readLine();
//                System.out.println("Received = " + result_core);
            } catch (SocketException e) {
                e.printStackTrace();
                start_core_enb();
                init_core_enb_con();
            } catch (IOException e) {
                e.printStackTrace();
                start_core_enb();
                init_core_enb_con();
            } catch (Exception e) {
                e.printStackTrace();
                start_core_enb();
                init_core_enb_con();
            }
            sleep(3 * 1000);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            start_core_enb();
            init_core_enb_con();
        } catch (SocketException e) {
            e.printStackTrace();
            start_core_enb();
            init_core_enb_con();
        } catch (Exception e) {
            e.printStackTrace();
            start_core_enb();
            init_core_enb_con();
        }
        System.out.println("Connected to gNB and Core.");

    }

    public void init_ue_con() {
        try {
            System.out.println("Connecting to UE Controller...");
            System.out.println("UE controller IP Address: " + config.ue_controller_ip_address);
            start_uecontroller();
            sleep(1000);
            ue_socket = new Socket(config.ue_controller_ip_address, config.ue_port);
            ue_socket.setTcpNoDelay(true);
            System.out.println("Initializing Buffers for UE...");
            ue_out = new BufferedWriter(new OutputStreamWriter(ue_socket.getOutputStream()));
            ue_in = new BufferedReader(new InputStreamReader(ue_socket.getInputStream()));
            System.out.println("Initialized Buffers for UE");
            System.out.println("Connected to UE Controller");

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public boolean enb_alive() {
//        String result = "";
//        try {
//            enodeb_socket.setSoTimeout(5 * 1000);
//            enodeb_out.write("Hello\n");
//            enodeb_out.flush();
//            result = enodeb_in.readLine();
//            System.out.println("Received from Hello message in enb alive = " + result);
//            enodeb_socket.setSoTimeout(30 * 1000);
//        } catch (SocketTimeoutException e) {
//            e.printStackTrace();
//            return false;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//
//        if (result.contains("ACK")) {
//            System.out.println("PASSED: Testing the connection between the statelearner and the srsENB");
//            return true;
//        } else {
//            System.out.println("FAILED: Testing the connection between the statelearner and the srsENB");
//            return false;
//        }
//    }


    public boolean radio_check() {
        return Inputmap.get("enable_s1");
    }


    public boolean context_check(String symbol) {
        if (symbol.startsWith("enable_s1")) {
            return true;
        }

        if (!radio_check()) {
            System.out.println("logexecutor: " + symbol + "context(radio) check failed.");
            return false;
        }

        if(nas_sm_complete_counter > 1 && auth_response_counter >= 2){
            System.out.println("logexecutor: " + symbol + "context check (auth_response_counter >= 2) failed.");
            return false;
        }


        if (symbol.startsWith("rrc_reconf_protected")  || symbol.startsWith("ue_cap_enquiry_protected") || symbol.startsWith("rrc_countercheck_protected") || symbol.startsWith("rrc_ueinfo_req_protected") || symbol.startsWith("rrc_sm_cmd_ns")) {
            if (!(Inputmap.get("auth_request_plain_text"))) {
                return false;
            }
        }

        // if no auth_request, key will not get derived.
        if (symbol.startsWith("rrc_sm_cmd")) {
            if (!(Inputmap.get("auth_request_plain_text"))) {
                return false;
            }
        }

        // nas protected packets should be sent only after nas security ctx established.
        if (symbol.startsWith("nas_sm_cmd_protected") || symbol.startsWith("registration_accept_protected") || symbol.startsWith("config_update_cmd_protected")) {
            if (!(Inputmap.get("auth_request_plain_text"))) {
                return false;
            }
        }

        // replay packets should be sent after original one.
        if (symbol.startsWith("rrc_sm_cmd_replay")) {
            if (!Inputmap.get("rrc_sm_cmd")) {
                return false;
            }
        }

        // replay packets should also be sent after security context established.
        if (symbol.startsWith("rrc_recconf_replay")) {
            if (!Inputmap.get("rrc_reconf_protected")) {
                return false;
            }
        }

        if (symbol.startsWith("nas_sm_cmd_replay")) {
            if (!Inputmap.get("nas_sm_cmd")) {
                return false;
            }
        }

        if (symbol.startsWith("config_update_cmd_replay")) {
            return Inputmap.get("config_update_cmd_protected");
        }

        return true;
    } //end for context_check

    public void kill_process(String path, String nameOfProcess) {
        ProcessBuilder pb = new ProcessBuilder(path);
        Process p;
        try {
            p = pb.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Killed " + nameOfProcess);
        System.out.println("Waiting a second");
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String line;
        try {
            Process temp = Runtime.getRuntime().exec("pidof " + nameOfProcess);
            BufferedReader input = new BufferedReader(new InputStreamReader(temp.getInputStream()));
            line = input.readLine();
            if (line != null) {
                System.out.println("ERROR: " + nameOfProcess + " is still running after invoking kill script");
                System.out.println("Attempting termination again...");
                kill_process(path, nameOfProcess);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(nameOfProcess + " has been killed");
    }

    private void start_process(String path, String nameOfProcess) {
        ProcessBuilder pb = new ProcessBuilder(path);
        Process p;
        try {
            p = pb.start();
            System.out.println(nameOfProcess + " process has started");
            System.out.println("Waiting a second");
            TimeUnit.SECONDS.sleep(2);
        } catch (IOException e) {
            System.out.println("IO Exception");
            System.out.println("ERROR: " + nameOfProcess + " is not running after invoking script");
            System.out.println("Attempting again...");
            start_process(path, nameOfProcess);
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("Timer Exception");
            System.out.println("ERROR: " + nameOfProcess + " is not running after invoking script");
            System.out.println("Attempting again...");
            start_process(path, nameOfProcess);
            e.printStackTrace();
        }


        String line;
        try {
            Process temp = Runtime.getRuntime().exec("pidof " + nameOfProcess);
            BufferedReader input = new BufferedReader(new InputStreamReader(temp.getInputStream()));

            line = input.readLine();
            if (line == null) {
                System.out.println("ERROR: " + nameOfProcess + " is not running after invoking script");
                System.out.println("Attempting again...");
                start_process(path, nameOfProcess);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(nameOfProcess + " has started...");
    }

    public int minimum(int a, int b, int c) {
        return Math.min(Math.min(a, b), c);
    }

    public int computeLevenshteinDistance(CharSequence lhs, CharSequence rhs) {
        int[][] distance = new int[lhs.length() + 1][rhs.length() + 1];

        for (int i = 0; i <= lhs.length(); i++)
            distance[i][0] = i;
        for (int j = 1; j <= rhs.length(); j++)
            distance[0][j] = j;

        for (int i = 1; i <= lhs.length(); i++)
            for (int j = 1; j <= rhs.length(); j++)
                distance[i][j] = minimum(
                        distance[i - 1][j] + 1,
                        distance[i][j - 1] + 1,
                        distance[i - 1][j - 1] + ((lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1));

        return distance[lhs.length()][rhs.length()];
    }

    public String getClosests(String result) {
        System.out.println("Getting closest of " + result);
        if (expectedResults.contains(result)) {
            return result;
        }

        int minDistance = Integer.MAX_VALUE;
        String correctWord = null;


        for (String word : expectedResults) {
            int distance = computeLevenshteinDistance(result, word);

            if (distance < minDistance) {
                correctWord = word;
                minDistance = distance;
            }
        }
        return correctWord;
    }

    public String getExpectedResult(String symbol, String result) {

        String final_result = "";

        if (symbol.contains("enable_s1")){
            if (enable_s1_expectedResults.contains(result)){
                final_result = result;
            }
        } else if (symbol.contains("id_request_plain_text")){
            if (id_request_expectedResults.contains(result)){
                final_result = result;
            }
        } else if (symbol.contains("auth_request_plain_text")){
            if (auth_request_expectedResults.contains(result)){
                final_result = result;
            }
        } else if (symbol.contains("rrc_sm_cmd")){
            if (rrc_sm_cmd_expectedResults.contains(result)){
                final_result = result;
            }
        } else if (symbol.contains("nas_sm_cmd")){
            if (nas_sm_cmd_expectedResults.contains(result)){
                final_result = result;
            }
        } else if (symbol.contains("registration_accept")){
            if (registration_accept_expectedResults.contains(result)){
                final_result = result;
            }
        } else if (symbol.contains("deregistration_req")){
            if (deregistration_req_expectedResults.contains(result)){
                final_result = result;
            }
        } else if (symbol.contains("rrc_reconf")){
            if (rrc_reconf_expectedResults.contains(result)){
                final_result = result;
            }
        } else if (symbol.contains("ue_cap_enquiry")){
            if (ue_cap_enquiry_expectedResults.contains(result)){
                final_result = result;
            }
        }  else if (symbol.contains("config_update_cmd")){
            if (config_update_cmd_expectedResults.contains(result)){
                final_result = result;
            }
        }
        else if (symbol.contains("rrc_countercheck")){
            if(rrc_countercheck_expectedResults.contains(result)){
                final_result = result;
            }
        }
        else if (symbol.contains("rrc_ueinfo_req")){
            if(rrc_ueinfo_req_expectedResults.contains(result)){
                final_result = result;
            }
        }

        return final_result;
    }
}
