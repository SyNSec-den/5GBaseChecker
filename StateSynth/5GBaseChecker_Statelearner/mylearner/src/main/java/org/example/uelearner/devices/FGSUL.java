package org.example.uelearner.devices;

import org.example.uelearner.ue.UESUL;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;
import static org.example.uelearner.tgbot.NotificationBot.sendToTelegram;
import static org.example.uelearner.ue.UESUL.*;

public class FGSUL extends DeviceSUL {
    public FGSUL(UESUL uesul) { // FiveG SUL
        super(uesul);
    }

    @Override
    public void pre() {
        System.out.println("pre!");
        if (!uesul.pre_needed) {
            System.out.println("pre doesn't need!");
            uesul.Input_dic_init();
            uesul.Output_dic_init();
            return;
        }
        int flag = 0;
        uesul.Input_dic_init();
        uesul.Output_dic_init();
        uesul.auth_response_counter = 0;
        uesul.nas_sm_complete_counter = 0;
        try {
            if (!uesul.config.combine_query) {
                String result = "";
                boolean reset_done = false;
                System.out.println("---- Starting RESET ----");

                System.out.println("Sending symbol: RESET to UE controller");
                uesul.ue_out.write("RESET\n");
                uesul.ue_out.flush();
                result = "";
                result = uesul.ue_in.readLine();

                sleep(2000);

                // pre first restart gNB and Core
                System.out.println("---- Starting restart gNB and Core  ----");
                System.out.println("---- Starting clean core context ----");
                if (uesul.amf_socket != null) {
                    uesul.amf_socket.close();
                }
                sleep(1000);
                start_core();
                sleep(7000); //just for test

                //connect to core again.
                System.out.println("Connecting to core..");
                uesul.amf_socket = new Socket(uesul.config.mme_controller_ip_address, uesul.config.mme_port);
                uesul.amf_socket.setTcpNoDelay(true);
                uesul.amf_out = new BufferedWriter(new OutputStreamWriter(uesul.amf_socket.getOutputStream()));
                uesul.amf_in = new BufferedReader(new InputStreamReader(uesul.amf_socket.getInputStream()));
                uesul.amf_out.write("hello\n");
                uesul.amf_out.flush();
                result = uesul.amf_in.readLine();
                System.out.println("Connected with core after pre: " + result);

                System.out.println("---- Starting clean enb context ----");
                if (uesul.enodeb_socket != null) {
                    uesul.enodeb_socket.close();
                }
                uesul.start_eNodeB();
                sleep(8 * 1000);
                System.out.println("Connecting to OAI gNB...");

                uesul.enodeb_socket = new Socket(uesul.config.enodeb_controller_ip_address, uesul.config.enodeb_port);
                uesul.enodeb_socket.setTcpNoDelay(true);
                uesul.enodeb_out = new BufferedWriter(new OutputStreamWriter(uesul.enodeb_socket.getOutputStream()));
                uesul.enodeb_in = new BufferedReader(new InputStreamReader(uesul.enodeb_socket.getInputStream()));
                uesul.enodeb_out.write("Hello\n");
                uesul.enodeb_out.flush();
                result = uesul.enodeb_in.readLine();
                System.out.println("Connected with OAI gNB after pre: " + result);



                System.out.println("---- Starting clean ue context ----"); // I think this part is not needed here.
//                System.out.println("Sending symbol: RESET to UE controller"); //turn on airplane mode on phone.
//                uesul.ue_out.write("RESET\n");
//                uesul.ue_out.flush();
//                result = "";
//                result = uesul.ue_in.readLine();
//                sleep(2000);


                // kill and restart core to reset core. We can also restart AMF to save time. I think we don't need it here.
                //start_core();
                /* The EPC needs to synchronize the sqn value with UE.
                 * Thats why statelearner+srsEPC needs to perform authentication procedure for RESET operation
                 */
                do {
                    try {
                        uesul.amf_socket.setSoTimeout(30 * 1000); //special value for HUAWEI P40 pro
                        System.out.println("Sending symbol: enable_s1 to UE controller");

                        //before send enable_s1 to uecontroller, reset RACH_counter
                        Reset_Rach();
                        sleep(500);

                        uesul.ue_out.write("enable_s1\n");
                        uesul.ue_out.flush();
                        sleep(1000); //just for test
                        result = uesul.amf_in.readLine(); //expect registration_request_guti
                        System.out.println("Received " + result);
                        //do we need this?
                        //result = new String(Arrays.copyOfRange(result.getBytes(), 1, result.getBytes().length)); //what does this line do? Does it contain any \n or other symbol so need specific handle?
                        System.out.println("enable_s1 in pre: " + result);
                        uesul.amf_socket.setSoTimeout(2 * 1000);
                        if (result.contains("deregistration_request")) {
                            System.out.println("Caught detach_request and sending enable_s1 again!");
                            pre();
                        }
                        if (flag == 0 && result.contains("registration_request")) { //expect registration_request_guti
                            Reset_Rach();
                            sleep(500);
                            uesul.amf_out.write("registration_reject\n"); //with cause #11
                            uesul.amf_out.flush();
                            TimeUnit.SECONDS.sleep(1); // Should we use sleep here? Value here can be adjusted.
                            System.out.println("registration_request caught and registration_reject(#11 pre) sent!!\n");
                        }
                        // Probably, we don't need to do this.
//                        System.out.println("---- Starting sync ue sequence number ----");
//                      //After clean the ue ctx, reset ue again. turn on airplane mode on phone.
                        System.out.println("Sending symbol: RESET to UE controller");
                        uesul.ue_out.write("RESET\n");
                        uesul.ue_out.flush();
                        result = "";
                        result = uesul.ue_in.readLine();
                        //clear context in core.
                        System.out.println("---- Starting clean core context ----");
                        if (uesul.amf_socket != null) {
                            uesul.amf_socket.close();
                        }
                        sleep(1000);
                        start_core();
                        sleep(7000);
                        //connect to core again.
                        System.out.println("Connecting to core..");
                        uesul.amf_socket = new Socket(uesul.config.mme_controller_ip_address, uesul.config.mme_port);
                        uesul.amf_socket.setTcpNoDelay(true);
                        uesul.amf_out = new BufferedWriter(new OutputStreamWriter(uesul.amf_socket.getOutputStream()));
                        uesul.amf_in = new BufferedReader(new InputStreamReader(uesul.amf_socket.getInputStream()));
                        uesul.amf_out.write("hello\n");
                        uesul.amf_out.flush();
                        result = uesul.amf_in.readLine();
                        System.out.println("Connected with core after pre: " + result);
                        System.out.println("---- Starting clean enb context ----");
                        if (uesul.enodeb_socket != null) {
                            uesul.enodeb_socket.close();
                        }
                        uesul.start_eNodeB();
                        sleep(8 * 1000);
                        System.out.println("Connecting to OAI gNB...");
                        uesul.enodeb_socket = new Socket(uesul.config.enodeb_controller_ip_address, uesul.config.enodeb_port);
                        uesul.enodeb_socket.setTcpNoDelay(true);
                        uesul.enodeb_out = new BufferedWriter(new OutputStreamWriter(uesul.enodeb_socket.getOutputStream()));
                        uesul.enodeb_in = new BufferedReader(new InputStreamReader(uesul.enodeb_socket.getInputStream()));
                        uesul.enodeb_out.write("Hello\n");
                        uesul.enodeb_out.flush();
                        result = uesul.enodeb_in.readLine();
                        System.out.println("Connected with OAI gNB after pre: " + result);
                        System.out.println("---- Restart UE Controller ----");
                        if (uesul.ue_socket != null) {
                            uesul.ue_socket.close();
                        }
                        kill_uecontroller();
                        sleep(500);
                        start_uecontroller();
                        sleep(1000);
                        uesul.ue_socket = new Socket(uesul.config.ue_controller_ip_address, uesul.config.ue_port);
                        uesul.ue_socket.setTcpNoDelay(true);
                        System.out.println("Initializing Buffers for UE...");
                        uesul.ue_out = new BufferedWriter(new OutputStreamWriter(uesul.ue_socket.getOutputStream()));
                        uesul.ue_in = new BufferedReader(new InputStreamReader(uesul.ue_socket.getInputStream()));
                        System.out.println("Initialized Buffers for UE");
                        System.out.println("Pre: Connected to UE Controller");
                        sleep(1000);
                        uesul.amf_socket.setSoTimeout(120 * 1000);
                        System.out.println("Sending symbol: enable s1 to UE controller"); //turn on airplane mode on phone.
                        uesul.ue_out.write("enable_s1\n");
                        uesul.ue_out.flush();
                        result = uesul.amf_in.readLine();
                        if (result.equals("registration_request")) { //compensate for deregistration request??
                            Reset_Rach();
                            sleep(500);
                            uesul.amf_socket.setSoTimeout(3 * 1000);
                            if (flag == 0) {
                                flag = 1;
                                reset_done = true; //for don't need seq sync
                                System.out.println("UE pre success! Sending symbol: RESET to UE controller");
                                uesul.ue_out.write("RESET\n");
                                uesul.ue_out.flush();
                                result = "";
                                result = uesul.ue_in.readLine();
                                sleep(1000);
                                uesul.pre_needed = false;
                                System.out.println("reset_done = true");
                                continue;
                            }
                        }else{
                            System.out.println("UE pre failed!");
                            if(uesul.config.tgbot_enable == 1){
                                sendToTelegram("UE pre failed!");
                            }
                            reset_ue();
                            kill_core_handle();
                            kill_eNodeb_handle();
                            kill_uecontroller();
                            sleepp(15 * 1000);
                            uesul.start_core_enb();
                            uesul.start_uecontroller();
                            uesul.init_core_enb_con();
                            uesul.init_ue_con();
                            pre();
                            if(uesul.config.tgbot_enable == 1){
                                sendToTelegram("May be UE pre failed handled!");
                            }
                        }
                    } catch (SocketTimeoutException e) {
                        uesul.enable_s1_timeout_count++;
                        System.out.println("Timeout occured for enable_s1");
                        System.out.println("Sleeping for a while...");
                        //Does the logic correct here?
                        sleep((long) uesul.enable_s1_timeout_count * 1000);
                        pre();
                        //System.out.println("Rebooting ADB Server");
                        if (uesul.enable_s1_timeout_count == 10) {
                            //System.out.println("Rebooting UE");
                            //tgbot send notification here.
                            uesul.handle_enb_core_failure();
                        }
                    }
                } while (!reset_done);
                System.out.println("---- RESET DONE ----");
            }
        } catch (Exception e) {
            sendToTelegram("May be connection refused handled!");
            reset_ue();
            kill_core_handle();
            kill_eNodeb_handle();
            kill_uecontroller();
            sleepp(15 * 1000);
            uesul.start_core_enb();
            uesul.start_uecontroller();
            uesul.init_core_enb_con();
            uesul.init_ue_con();
            pre();
            System.out.println("connection refused handled!");
//            e.printStackTrace();
        }
    }// end for pre

    public void sleepp(int sec) { //just used in pre catch
        try {
            sleep(sec);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void reset_ue() {
        try{
            System.out.println("Sending symbol: RESET to UE controller");
            uesul.ue_out.write("RESET\n");
            uesul.ue_out.flush();
            String result = "";
            result = uesul.ue_in.readLine();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void post() {
        System.out.println("---- Starting post ----");
    }

    @Override
    public String step(String symbol) {
        int rrc_amf = 0; //decide which socket to read. 0 read from enodeb socket. 1 read from core socket.
        int socket_wait_time = 1000;
        //If it is the first message, set a longer time for timeout.
        String result = ""; // return value for step function

        try { //log executor context check
            if (uesul.context_check(symbol)) {
                System.out.println("Log executor: " + symbol + " Context check passed");
                if(!symbol.startsWith("enable_s1")){
                    uesul.pre_needed = true;
                }
            } else {
                System.out.println("Log executor: " + symbol + "Context check failed");
                return "null_action";
            }
        } catch (Exception e) {
            System.out.println("context check exception!!");
            return "null_action";
        }

        try {
            sleep(800);

            if (symbol.startsWith("enable_s1")) {
                uesul.nas_sm_complete_counter = 0;
                uesul.auth_response_counter = 0;
            }

            if(uesul.config.DIKEUE_compare == 0 && uesul.nas_sm_complete_counter >= 1 && uesul.auth_response_counter >= 2){
                System.out.println("logexecutor: " + symbol + "context check (auth_response_counter >= 2) failed.");
                return "null_action";
            }

            if (symbol.startsWith("enable_s1")) {
                while (!result.contains("registration_request")) {

                    //For enable_s1 new strategy
                    if(uesul.pre_needed == true){
                        pre();
                    } else {
                        System.out.println("pre doesn't needed when sending enable_s1");
                    }

                    rrc_amf = 2;
                    System.out.println("Log executor: sending enable_s1.");
                    uesul.amf_socket.setSoTimeout(120 * 1000); //Depends on the speed of UE searching.
                    //reset RACH_count before sending enable_s1
                    Reset_Rach();
                    sleep(500);
                    uesul.ue_out.write(symbol + "\n");
                    uesul.ue_out.flush();
                    uesul.Inputmap.put("enable_s1", true);

                    System.out.println("Reading from amf");
                    result = uesul.amf_in.readLine();
                    System.out.println("Log executor: received(raw) " + result + " from amf.");
                    result = uesul.getClosests(result);
                }
            } else if (symbol.startsWith("rrc_sm_cmd")) {
                rrc_amf = 0;
                System.out.println("Log executor: sending rrc_sm_cmd.");
                uesul.enodeb_socket.setSoTimeout(socket_wait_time);
                uesul.enodeb_out.write(symbol + "\n");
                uesul.enodeb_out.flush();
                uesul.Inputmap.put("rrc_sm_cmd", true);
            } else if (symbol.startsWith("rrc_sm_cmd_replay")) {
                rrc_amf = 0;
                System.out.println("Log executor: sending rrc_sm_cmd_replay.");
                uesul.enodeb_socket.setSoTimeout(socket_wait_time);
                uesul.enodeb_out.write(symbol + "\n");
                uesul.enodeb_out.flush();
                uesul.Inputmap.put("rrc_sm_cmd_replay", true);
            }else if (symbol.startsWith("rrc_sm_cmd_ns")) {
                rrc_amf = 0;
                System.out.println("Log executor: sending rrc_sm_cmd_ns.");
                uesul.enodeb_socket.setSoTimeout(socket_wait_time);
                uesul.enodeb_out.write(symbol + "\n");
                uesul.enodeb_out.flush();
                uesul.Inputmap.put("rrc_sm_cmd_ns", true);
            }else if (symbol.equals("rrc_sm_cmd_ns_plain_text")) {
                rrc_amf = 0;
                System.out.println("Log executor: sending rrc_sm_cmd_ns_plain_text.");
                uesul.enodeb_socket.setSoTimeout(socket_wait_time);
                uesul.enodeb_out.write(symbol + "\n");
                uesul.enodeb_out.flush();
                uesul.Inputmap.put("rrc_sm_cmd_ns_plain_text",true);
            } else if (symbol.startsWith("rrc_countercheck_protected")) {
                rrc_amf = 0;
                System.out.println("Log executor: sending rrc_countercheck_protected.");
                uesul.enodeb_socket.setSoTimeout(socket_wait_time);
                uesul.enodeb_out.write(symbol + "\n");
                uesul.enodeb_out.flush();
                uesul.Inputmap.put("rrc_countercheck_protected",true);
            } else if (symbol.startsWith("rrc_countercheck_plain_text")) {
                rrc_amf = 0;
                System.out.println("Log executor: sending rrc_countercheck_plain_text.");
                uesul.enodeb_socket.setSoTimeout(socket_wait_time);
                uesul.enodeb_out.write(symbol + "\n");
                uesul.enodeb_out.flush();
                uesul.Inputmap.put("rrc_countercheck_plain_text",true);
            } else if (symbol.startsWith("rrc_ueinfo_req_protected")) {
                rrc_amf = 0;
                System.out.println("Log executor: sending rrc_ueinfo_req_protected.");
                uesul.enodeb_socket.setSoTimeout(socket_wait_time);
                uesul.enodeb_out.write(symbol + "\n");
                uesul.enodeb_out.flush();
                uesul.Inputmap.put("rrc_ueinfo_req_protected",true);
            } else if (symbol.startsWith("rrc_ueinfo_req_plain_text")) {
                rrc_amf = 0;
                System.out.println("Log executor: sending rrc_ueinfo_req_plain_text.");
                uesul.enodeb_socket.setSoTimeout(socket_wait_time);
                uesul.enodeb_out.write(symbol + "\n");
                uesul.enodeb_out.flush();
                uesul.Inputmap.put("rrc_ueinfo_req_plain_text",true);
            } else if (symbol.startsWith("ue_cap_enquiry_protected")) {
                rrc_amf = 0;
                System.out.println("Log executor: sending rrc_ue_cap_enquiry_protected.");
                uesul.enodeb_socket.setSoTimeout(socket_wait_time);
                uesul.enodeb_out.write(symbol + "\n");
                uesul.enodeb_out.flush();
                uesul.Inputmap.put("ue_cap_enquiry_protected", true);
            } else if (symbol.startsWith("ue_cap_enquiry_plain_text")) {
                rrc_amf = 0;
                System.out.println("Log executor: sending ue_cap_enquiry_plaintext.");
                uesul.enodeb_socket.setSoTimeout(socket_wait_time);
                uesul.enodeb_out.write(symbol + "\n");
                uesul.enodeb_out.flush();
                uesul.Inputmap.put("ue_cap_enquiry_plain_text", true);
            } else if (symbol.startsWith("rrc_reconf_protected")) {
                rrc_amf = 0;
                System.out.println("Log executor: sending rrc_reconf_protected.");
                uesul.enodeb_socket.setSoTimeout(socket_wait_time);
                uesul.enodeb_out.write(symbol + "\n");
                uesul.enodeb_out.flush();
                uesul.Inputmap.put("rrc_reconf_protected", true);
            } else if (symbol.startsWith("rrc_reconf_plain_text")) {
                rrc_amf = 0;
                System.out.println("Log executor: sending rrc_reconf_plain_text.");
                uesul.enodeb_socket.setSoTimeout(socket_wait_time);
                uesul.enodeb_out.write(symbol + "\n");
                uesul.enodeb_out.flush();
                uesul.Inputmap.put("rrc_reconf_plain_text", true);
            } else if (symbol.startsWith("rrc_reconf_replay")) {
                rrc_amf = 0;
                System.out.println("Log executor: sending rrc_reconf_replay.");
                uesul.enodeb_socket.setSoTimeout(socket_wait_time);
                uesul.enodeb_out.write(symbol + "\n");
                uesul.enodeb_out.flush();
                uesul.Inputmap.put("rrc_reconf_replay", true);
            } else if (symbol.startsWith("id_request_plain_text")) {
                rrc_amf = 1;
                System.out.println("Log executor: sending id_request_plain_text.");
                uesul.amf_socket.setSoTimeout(socket_wait_time);
                uesul.amf_out.write(symbol + "\n");
                uesul.amf_out.flush();
                uesul.Inputmap.put("id_request_plain_text", true);
            } else if (symbol.startsWith("auth_request_plain_text")) {
                rrc_amf = 1;
                System.out.println("Log executor: sending auth_request_plain_text.");
                uesul.amf_socket.setSoTimeout(1500);
                uesul.amf_out.write(symbol + "\n");
                uesul.amf_out.flush();
                uesul.Inputmap.put("auth_request_plain_text", true);
            } else if (symbol.equals("nas_sm_cmd")) {
                rrc_amf = 1;
                System.out.println("Log executor: sending nas_sm_cmd.");
                uesul.amf_socket.setSoTimeout(socket_wait_time);
                uesul.amf_out.write(symbol + "\n");
                uesul.amf_out.flush();
                uesul.Inputmap.put("nas_sm_cmd", true);
            } else if (symbol.startsWith("nas_sm_cmd_replay")) {
                rrc_amf = 1;
                System.out.println("Log executor: sending nas_sm_cmd_replay.");
                uesul.amf_socket.setSoTimeout(socket_wait_time);
                uesul.amf_out.write(symbol + "\n");
                uesul.amf_out.flush();
                uesul.Inputmap.put("nas_sm_cmd_replay", true);
            } else if (symbol.startsWith("nas_sm_cmd_protected")) {
                rrc_amf = 1;
                System.out.println("Log executor: sending nas_sm_cmd_protected.");
                uesul.amf_socket.setSoTimeout(socket_wait_time);
                uesul.amf_out.write(symbol + "\n");
                uesul.amf_out.flush();
                uesul.Inputmap.put("nas_sm_cmd_protected", true);
            } else if (symbol.startsWith("nas_sm_cmd_ns")) {
                rrc_amf = 1;
                System.out.println("Log executor: sending nas_sm_cmd_ns.");
                uesul.amf_socket.setSoTimeout(socket_wait_time);
                uesul.amf_out.write(symbol + "\n");
                uesul.amf_out.flush();
                uesul.Inputmap.put("nas_sm_cmd_ns", true);
            } else if(symbol.startsWith("nas_sm_cmd_ns_plain_text")) {
                rrc_amf = 1;
                System.out.println("Log executor: sending nas_sm_cmd_ns_plain_text.");
                uesul.amf_socket.setSoTimeout(socket_wait_time);
                uesul.amf_out.write(symbol + "\n");
                uesul.amf_out.flush();
                uesul.Inputmap.put("nas_sm_cmd_ns_plain_text", true);
            } else if (symbol.startsWith("registration_accept_protected")) {
                rrc_amf = 1;
                System.out.println("Log executor: sending registration_accept_protected.");
                uesul.amf_socket.setSoTimeout(socket_wait_time);
                uesul.amf_out.write(symbol + "\n");
                uesul.amf_out.flush();
                uesul.Inputmap.put("registration_accept_protected", true);
            }else if (symbol.startsWith("deregistration_req_protected")) {
                rrc_amf = 1;
                System.out.println("Log executor: sending deregistration_req_protected.");
                uesul.amf_socket.setSoTimeout(socket_wait_time);
                uesul.amf_out.write(symbol + "\n");
                uesul.amf_out.flush();
                uesul.Inputmap.put("deregistration_req_protected", true);
            } else if (symbol.startsWith("deregistration_req_plain_text")) {
                rrc_amf = 1;
                System.out.println("Log executor: sending deregistration_req_plain_text.");
                uesul.amf_socket.setSoTimeout(socket_wait_time);
                uesul.amf_out.write(symbol + "\n");
                uesul.amf_out.flush();
                uesul.Inputmap.put("deregistration_req_plain_text", true);
            } else if (symbol.startsWith("registration_accept_plain_text")) {
                rrc_amf = 1;
                System.out.println("Log executor: sending registration_accept_plain_text.");
                uesul.amf_socket.setSoTimeout(socket_wait_time);
                uesul.amf_out.write(symbol + "\n");
                uesul.amf_out.flush();
                uesul.Inputmap.put("registration_accept_plain_text", true);
            } else if(symbol.startsWith("registration_accept_h4_plain_text")){
                rrc_amf = 1;
                System.out.println("Log executor: sending registration_accept_h4_plain_text.");
                uesul.amf_socket.setSoTimeout(socket_wait_time);
                uesul.amf_out.write(symbol + "\n");
                uesul.amf_out.flush();
                uesul.Inputmap.put("registration_accept_h4_plain_text",true);
            } else if (symbol.equals("config_update_cmd_protected")) {
                rrc_amf = 1;
                System.out.println("Log executor: sending config_update_cmd_protected.");
                uesul.amf_socket.setSoTimeout(socket_wait_time);
                uesul.amf_out.write(symbol + "\n");
                uesul.amf_out.flush();
                uesul.Inputmap.put("config_update_cmd_protected", true);
            } else if (symbol.equals("config_update_cmd_plain_text")) {
                rrc_amf = 1;
                System.out.println("Log executor: sending config_update_cmd_plain_text.");
                uesul.amf_socket.setSoTimeout(socket_wait_time);
                uesul.amf_out.write(symbol + "\n");
                uesul.amf_out.flush();
//                uesul.Inputmap.put("config_update_cmd_protected", true);
            } else if (symbol.equals("config_update_cmd_replay")) {
                rrc_amf = 1;
                System.out.println("Log executor: sending config_update_cmd_replay.");
                uesul.amf_socket.setSoTimeout(socket_wait_time);
                uesul.amf_out.write(symbol + "\n");
                uesul.amf_out.flush();
                uesul.Inputmap.put("config_update_cmd_replay", true);
            }
        } catch (SocketTimeoutException e) {
            System.out.println("Timeout occured for " + symbol);
            return "null_action";
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Attempting to restart device and reset core. Also restarting query.");
            uesul.handle_enb_core_failure();
            return "null_action";
        }

        while (result.equals("")) {
            //Get reply from gnb or core.
            try {
                if (rrc_amf == 0) {
                    System.out.println("Reading from rrc");
                    result = uesul.enodeb_in.readLine();
                    System.out.println("Log executor: received(raw) " + result + " from rrc.");
                    result = uesul.getClosests(result);
                } else if (rrc_amf == 1) {
                    System.out.println("Reading from amf");
                    result = uesul.amf_in.readLine();
                    System.out.println("Log executor: received(raw) " + result + " from amf.");
                    result = uesul.getClosests(result);
                    if (result.equals("nas_sm_complete")){
                        uesul.nas_sm_complete_counter++;
                    }
                    if (result.equals("auth_response")){
                        uesul.auth_response_counter++;
                    }
                }
            } catch (SocketTimeoutException e) {
                System.out.println("Timeout occured for " + symbol);
                return "null_action";
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Attempting to restart device and reset core. Also restarting query.");
                uesul.handle_enb_core_failure();
                return "null_action"; //check this
                //return "null_action";
            }
            result = uesul.getExpectedResult(symbol, result);
            if (result.equals("")) {
                System.out.println("Get " + result + " for " + symbol + ". Retrying...");
            }
        }

        uesul.Outputmap.put(result, true);
        System.out.println("####" + symbol + "/" + result + "####");
        return result;

    }//end for step

    public void Reset_Rach() {
        try{
            String amf_result = "";
            String gnb_result = "";
            //before send enable_s1 to uecontroller, reset RACH_counter
            uesul.amf_out.write("RACH_Reset" + "\n");
            uesul.amf_out.flush();
            amf_result = uesul.amf_in.readLine();
            System.out.println("Log executor: received(raw) " + amf_result + " for RACH_Reset from amf.");

            uesul.enodeb_out.write("RACH_Reset" + "\n");
            uesul.enodeb_out.flush();
            gnb_result = uesul.enodeb_in.readLine();
            System.out.println("Log executor: received(raw) " + amf_result + " for RACH_Reset from gnb.");
        }catch (Exception e){
            e.printStackTrace();
        }
    }


}
