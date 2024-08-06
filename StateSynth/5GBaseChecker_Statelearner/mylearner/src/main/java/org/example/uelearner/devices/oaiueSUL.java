package org.example.uelearner.devices;

import org.example.uelearner.ue.UESUL;

import java.net.SocketTimeoutException;

import static java.lang.Thread.sleep;
import static org.example.uelearner.tgbot.NotificationBot.sendToTelegram;
import static org.example.uelearner.ue.UESUL.kill_evt;
import static org.example.uelearner.ue.UESUL.start_ue;

public class oaiueSUL extends DeviceSUL {
    public oaiueSUL(UESUL uesul) {
        super(uesul);
    }

    @Override
    public void pre() {
        System.out.println("pre!");

        uesul.Input_dic_init();
        uesul.Output_dic_init();
        uesul.auth_response_counter = 0;
        uesul.nas_sm_complete_counter = 0;

        try{
            if (uesul.amf_socket != null) {
                uesul.amf_socket.close();
            }

            if (uesul.enodeb_socket != null) {
                uesul.enodeb_socket.close();
            }

        }catch (Exception e){
            System.out.println("oaiuesul line 36");
        }



        kill_evt();
        sleepp(500);


        try {
            if (!uesul.config.combine_query) {
                uesul.start_core_enb();
                uesul.init_core_enb_con();
            }
        } catch (Exception e) {
//            sendToTelegram("May be connection refused handled!");
            sleepp(5 * 1000);
            pre();
            System.out.println("connection refused handled!");
        }
    }// end for pre


    public void pree() {
        System.out.println("pre!");
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
            sleep(500); //This is very important.

            if (symbol.startsWith("enable_s1")) {
                uesul.nas_sm_complete_counter = 0;
                uesul.auth_response_counter = 0;
            }

            if(uesul.config.DIKEUE_compare == 0 && uesul.nas_sm_complete_counter >= 1 && uesul.auth_response_counter >= 2){
                System.out.println("logexecutor: " + symbol + "context check (auth_response_counter >= 2) failed.");
                return "null_action";
            }


            if (symbol.startsWith("enable_s1")) {
                while (!result.contains("registration_request")) { // probably we don't need context check here.

                    pre(); // bring gnb and core to S1, then start ue


                    rrc_amf = 2;
                    System.out.println("Log executor: sending enable_s1.");
//                    uesul.amf_socket.setSoTimeout(10 * 1000); //Depends on the speed of UE searching.
//                    Reset_Rach();
                    sleep(500);

                    uesul.Inputmap.put("enable_s1", true);

                    start_ue(); //real enable_s1
                    sleep(1 * 1000);

                    System.out.println("Reading from amf");
                    result = uesul.amf_in.readLine();
                    System.out.println("Log executor: received(raw) " + result + " from amf.");
                    result = uesul.getClosests(result);
                }
            } else if (symbol.startsWith("rrc_setup_plain_text")) {
                rrc_amf = 0;
                System.out.println("Log executor: sending rrc_setup_plain_text.");
                uesul.enodeb_socket.setSoTimeout(3 * 1000);
                uesul.enodeb_out.write(symbol + "\n");
                uesul.enodeb_out.flush();
                uesul.Inputmap.put("rrc_setup_plain_text", true);
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
            } else if (symbol.startsWith("rrc_sm_cmd_ns")) {
                rrc_amf = 0;
                System.out.println("Log executor: sending rrc_sm_cmd_ns.");
                uesul.enodeb_socket.setSoTimeout(socket_wait_time);
                uesul.enodeb_out.write(symbol + "\n");
                uesul.enodeb_out.flush();
                uesul.Inputmap.put("rrc_sm_cmd_ns", true);
            }else if (symbol.startsWith("rrc_sm_cmd_ns_plain_text")) {
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
            } else if (symbol.startsWith("rrc_resume_protected")) {
                rrc_amf = 0;
                System.out.println("Log executor: sending rrc_resume_protected.");
                uesul.enodeb_socket.setSoTimeout(socket_wait_time);
                uesul.enodeb_out.write(symbol + "\n");
                uesul.enodeb_out.flush();
                uesul.Inputmap.put("rrc_resume_protected",true);
            } else if (symbol.startsWith("rrc_resume_plain_text")) {
                rrc_amf = 0;
                System.out.println("Log executor: sending rrc_resume_protected.");
                uesul.enodeb_socket.setSoTimeout(socket_wait_time);
                uesul.enodeb_out.write(symbol + "\n");
                uesul.enodeb_out.flush();
                uesul.Inputmap.put("rrc_resume_plain_text",true);
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
            } else if (symbol.startsWith("rrc_reest_protected")) {
                rrc_amf = 0;
                System.out.println("Log executor: sending rrc_reest_protected.");
                uesul.enodeb_socket.setSoTimeout(socket_wait_time);
                uesul.enodeb_out.write(symbol + "\n");
                uesul.enodeb_out.flush();
                uesul.Inputmap.put("rrc_reest_protected", true);
            } else if (symbol.startsWith("rrc_reest_plain_text")) {
                rrc_amf = 0;
                System.out.println("Log executor: sending rrc_reest_plain_text.");
                uesul.enodeb_socket.setSoTimeout(socket_wait_time);
                uesul.enodeb_out.write(symbol + "\n");
                uesul.enodeb_out.flush();
                uesul.Inputmap.put("rrc_reest_plain_text", true);
            } else if (symbol.startsWith("rrc_release_protected")) {
                rrc_amf = 0;
                System.out.println("Log executor: sending rrc_release_protected.");
                uesul.enodeb_socket.setSoTimeout(1 * 1000); //we don't need any listen for rrc release
                uesul.enodeb_out.write(symbol + "\n");
                uesul.enodeb_out.flush();
                uesul.Inputmap.put("rrc_release_protected",true);
            } else if (symbol.startsWith("rrc_release_suspend")) {
                rrc_amf = 0;
                System.out.println("Log executor: sending rrc_release_suspend.");
                uesul.enodeb_socket.setSoTimeout(1 * 1000); //we don't need any listen for rrc release
                uesul. enodeb_out.write(symbol + "\n");
                uesul.enodeb_out.flush();
                uesul.Inputmap.put("rrc_release_suspend",true);
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
            } else if (symbol.startsWith("config_update_cmd_protected")) {
                rrc_amf = 1;
                System.out.println("Log executor: sending config_update_cmd_protected.");
                uesul.amf_socket.setSoTimeout(socket_wait_time);
                uesul.amf_out.write(symbol + "\n");
                uesul.amf_out.flush();
                uesul.Inputmap.put("config_update_cmd_protected", true);
            } else if (symbol.startsWith("config_update_cmd_plain_text")) {
                rrc_amf = 1;
                System.out.println("Log executor: sending config_update_cmd_plain_text.");
                uesul.amf_socket.setSoTimeout(socket_wait_time);
                uesul.amf_out.write(symbol + "\n");
                uesul.amf_out.flush();
//                uesul.Inputmap.put("config_update_cmd_protected", true);
            } else if (symbol.startsWith("config_update_cmd_protected_ack")) {
                rrc_amf = 1;
                System.out.println("Log executor: sending config_update_cmd_protected_ack.");
                uesul.amf_socket.setSoTimeout(socket_wait_time);
                uesul.amf_out.write(symbol + "\n");
                uesul.amf_out.flush();
//                uesul.Inputmap.put("config_update_cmd_protected", true);
            } else if (symbol.startsWith("config_update_cmd_replay")) {
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