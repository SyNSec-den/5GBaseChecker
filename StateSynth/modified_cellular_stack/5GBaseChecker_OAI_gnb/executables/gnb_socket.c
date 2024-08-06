#include "gnb_socket.h"
#include "gtp_itf.h"
#include "common/utils/LOG/log.h"
#include "openair2/LAYER2/nr_pdcp/nr_pdcp_oai_api.h"
#include "openair2/RRC/NR/rrc_gNB_NGAP.h"
#include "openair2/LAYER2/nr_pdcp/nr_pdcp_entity.h"

int server_sock = 0;
bool rrc_sm_cmd_sent = 0;
bool rrc_reconf_sent = 0;
bool has_key = false;
int RACH_Count = 0;
int sec_type; 

bool check_int(uint8_t* kRRCint) {
    bool is_int=false;
    int i;
    for (i=0;i<16;i++) {
        if (kRRCint[i]!=0)
            is_int=true;
    }

    return is_int;
}

bool check_enc(uint8_t* kRRCenc) {
    bool is_int=false;
    int i;
    for (i=0;i<16;i++) {
        if (kRRCenc[i]!=0)
            is_int=true;
    }

    return is_int;
}


void *conn(void *arg)
{
  printf("try to launch socket server...\n");
  struct sockaddr_in serv_addr;
  int opt = 1, addrlen = sizeof(serv_addr), message_count = 0, valread, server_fd;

  // create server_fd
  if ((server_fd = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
    perror("socket failed");
    exit(EXIT_FAILURE);
  }

  // set socket option
  if (setsockopt(server_fd, SOL_SOCKET, SO_REUSEADDR | SO_REUSEPORT, &opt, sizeof(opt))) {
    perror("setsockopt");
    exit(EXIT_FAILURE);
  }

  // bind ip and port
  serv_addr.sin_family = AF_INET;
  serv_addr.sin_addr.s_addr = INADDR_ANY;
  serv_addr.sin_port = htons(PORT);
  if (inet_pton(AF_INET, "127.0.0.1", &serv_addr.sin_addr) <= 0) { // set serverip address
    printf("\nInvalid address/ Address not supported \n");
    return (void *)-1;
  }
  // bind ip and port, and listen
  if (bind(server_fd,
           (struct sockaddr *)(&serv_addr), // sockaddr_in --> sockaddr
           sizeof(serv_addr))
      < 0) {
    perror("bind failed");
    exit(EXIT_FAILURE);
  }
  if (listen(server_fd, 3) < 0) { // no block
    perror("listen");
    exit(EXIT_FAILURE);
  }

  printf("OAI gnb socket initialized!!\n");
  if ((server_sock = accept(server_fd, (struct sockaddr *)(&serv_addr),
                            (socklen_t *)(&addrlen))) < 0) { // return a new_socket fd
    perror("accept");
    exit(EXIT_FAILURE);
  }

  while (1) {
    char buffer[1024] = {0};
        valread = read(server_sock, buffer, 1024); // read (block)
        if (strcmp(buffer, "Hello\n") == 0) {
          printf("received:Hello\n");
          char* response = "ACK from OAI gnb\n";
          send_message(response);
          continue;
        }

        if (strcmp(buffer, "RACH_Reset\n") == 0) {
          printf("received:RACH_Reset\n");
          RACH_Count = 0;
          char* response = "DONE\n";
          send_message(response);
          continue;
        }

        if(RACH_Count > 1){
          printf("RACH count more than 1, sending null_action back\n");
          char* response = "null_action\n";
          send_message(response);
          continue;
        }

        if (strcmp(buffer, "rrc_sm_cmd\n") == 0) {
          printf("received command: rrc_sm_cmd\n");
          gNB_RRC_INST *gnb_rrc_inst = RC.nrrrc[context_in_socket->module_id];
          rrc_gNB_ue_context_t *ue_context_p = rrc_gNB_get_ue_context_by_rnti(gnb_rrc_inst, context_in_socket->rntiMaybeUEid);
          if(!gnb_rrc_inst || !ue_context_p || !has_key){
            printf("sending null action back!\n");
            send_message("null_action\n");
          } else{
            sec_type = 1;
            rrc_sm_cmd_sent = 1;
            rrc_gNB_generate_SecurityModeCommand(context_in_socket, ue_context_p);
          }
        } else if(strcmp(buffer, "rrc_sm_cmd_replay\n") == 0) {
          printf("received command: rrc_sm_cmd_replay\n");
          gNB_RRC_INST *gnb_rrc_inst = RC.nrrrc[context_in_socket->module_id];
          rrc_gNB_ue_context_t *ue_context_p = rrc_gNB_get_ue_context_by_rnti(gnb_rrc_inst, context_in_socket->rntiMaybeUEid);
          if(!gnb_rrc_inst || !ue_context_p || !rrc_sm_cmd_sent){
            printf("sending null action back!\n");
            send_message("null_action\n");
          } else{
            sec_type = 1;
            rrc_gNB_generate_SecurityModeCommand_replay(context_in_socket, ue_context_p);
            printf("rrc_sm_cmd_replay called!\n");
          }
        }else if (strcmp(buffer, "rrc_sm_cmd_ns\n") == 0) {
          printf("received command: rrc_sm_cmd_ns\n");
          gNB_RRC_INST *gnb_rrc_inst = RC.nrrrc[context_in_socket->module_id];
          rrc_gNB_ue_context_t *ue_context_p = rrc_gNB_get_ue_context_by_rnti(gnb_rrc_inst, context_in_socket->rntiMaybeUEid);
          if(!gnb_rrc_inst || !ue_context_p || !has_key){
            printf("sending null action back!\n");
            send_message("null_action\n");
          } else{
            sec_type = 1;
            rrc_gNB_generate_SecurityModeCommand_ns(context_in_socket, ue_context_p);
            printf("rrc_sm_cmd_ns called!\n");
          }
        }else if (strcmp(buffer, "rrc_sm_cmd_ns_plain_text\n") == 0) {
          printf("received command: rrc_sm_cmd_ns_plain_text\n");
          gNB_RRC_INST *gnb_rrc_inst = RC.nrrrc[context_in_socket->module_id];
          rrc_gNB_ue_context_t *ue_context_p = rrc_gNB_get_ue_context_by_rnti(gnb_rrc_inst, context_in_socket->rntiMaybeUEid);
          if(!gnb_rrc_inst || !ue_context_p){
            printf("sending null action back!\n");
            send_message("null_action\n");
          } else{
            sec_type =0;
            rrc_gNB_generate_SecurityModeCommand_ns(context_in_socket, ue_context_p); //todo: change to ns_plain_text
            printf("rrc_sm_cmd_ns_plain_text called!\n");
          }
        } else if (strcmp(buffer, "ue_cap_enquiry_protected\n") == 0){
          printf("received command: ue_cap_enquiry_protected\n");
          gNB_RRC_INST *gnb_rrc_inst = RC.nrrrc[context_in_socket->module_id];
          rrc_gNB_ue_context_t *ue_context_p = rrc_gNB_get_ue_context_by_rnti(gnb_rrc_inst, context_in_socket->rntiMaybeUEid);
          if(!gnb_rrc_inst || !ue_context_p || !has_key){
            printf("sending null action back!\n");
            send_message("null_action\n");
          } else{
           sec_type = 3;
           rrc_gNB_generate_UECapabilityEnquiry(context_in_socket, ue_context_p);
           printf("ue_cap_enquiry_protected called!\n");
          }
        } else if (strcmp(buffer, "ue_cap_enquiry_plain_text\n") == 0){
          gNB_RRC_INST *gnb_rrc_inst = RC.nrrrc[context_in_socket->module_id];
          rrc_gNB_ue_context_t *ue_context_p = rrc_gNB_get_ue_context_by_rnti(gnb_rrc_inst, context_in_socket->rntiMaybeUEid);
          if(!gnb_rrc_inst || !ue_context_p){
            printf("sending null action back!\n");
            send_message("null_action\n");
          } else{
            sec_type = 0;
            rrc_gNB_generate_UECapabilityEnquiry_plain_text(context_in_socket, ue_context_p);
            printf("ue_cap_enquiry_plain_text called!\n");
          }
        } else if (strcmp(buffer, "rrc_reconf_protected\n") == 0){
          printf("received command: rrc_reconf_protected\n");
          gNB_RRC_INST *gnb_rrc_inst = RC.nrrrc[context_in_socket->module_id];
          rrc_gNB_ue_context_t *ue_context_p = rrc_gNB_get_ue_context_by_rnti(gnb_rrc_inst, context_in_socket->rntiMaybeUEid);
          if(!gnb_rrc_inst || !ue_context_p || !has_key){
            printf("sending null action back!\n");
            send_message("null_action\n");
          } else{
            sec_type = 3;
            rrc_gNB_generate_defaultRRCReconfiguration(context_in_socket, ue_context_p);
            rrc_reconf_sent = 1;
            printf("rrc_reconf_protected called!\n");
          }
        } else if (strcmp(buffer, "rrc_reconf_plain_text\n") == 0){
          gNB_RRC_INST *gnb_rrc_inst = RC.nrrrc[context_in_socket->module_id];
          rrc_gNB_ue_context_t *ue_context_p = rrc_gNB_get_ue_context_by_rnti(gnb_rrc_inst, context_in_socket->rntiMaybeUEid);
          if (!gnb_rrc_inst || !ue_context_p){
            printf("sending null action back!\n");
            send_message("null_action\n");
          } else{
            sec_type = 0;
            rrc_gNB_generate_defaultRRCReconfiguration_plain_text(context_in_socket, ue_context_p);
            printf("rrc_reconf_plain_text called!\n");
          }
        } else if (strcmp(buffer, "rrc_reconf_replay\n") == 0){
          gNB_RRC_INST *gnb_rrc_inst = RC.nrrrc[context_in_socket->module_id];
          rrc_gNB_ue_context_t *ue_context_p = rrc_gNB_get_ue_context_by_rnti(gnb_rrc_inst, context_in_socket->rntiMaybeUEid);
          if(!gnb_rrc_inst || !ue_context_p ||!rrc_reconf_sent){
            printf("context check failed, sending null action back!\n");
            send_message("null_action\n");
          } else{
            sec_type = 3;
            rrc_gNB_generate_defaultRRCReconfiguration_replay(context_in_socket, ue_context_p);
            printf("rrc_reconf_replay called!\n");
          }
        }else if(strcmp(buffer, "rrc_countercheck_protected\n") == 0){
          gNB_RRC_INST *gnb_rrc_inst = RC.nrrrc[context_in_socket->module_id];
          rrc_gNB_ue_context_t *ue_context_p = rrc_gNB_get_ue_context_by_rnti(gnb_rrc_inst, context_in_socket->rntiMaybeUEid);
          if(!gnb_rrc_inst || !ue_context_p || !has_key){
            printf("sending null action back!\n");
            send_message("null_action\n");
          } else{
            sec_type = 3;
            rrc_gNB_generate_RRCCounterCheck(context_in_socket, ue_context_p);
            printf("rrc_countercheck_protected called!\n");
          }
        }else if(strcmp(buffer, "rrc_countercheck_plain_text\n") == 0){
          gNB_RRC_INST *gnb_rrc_inst = RC.nrrrc[context_in_socket->module_id];
          rrc_gNB_ue_context_t *ue_context_p = rrc_gNB_get_ue_context_by_rnti(gnb_rrc_inst, context_in_socket->rntiMaybeUEid);
          if(!gnb_rrc_inst || !ue_context_p){
            printf("sending null action back!\n");
            send_message("null_action\n");
          } else{
            sec_type = 0;
            rrc_gNB_generate_RRCCounterCheck(context_in_socket, ue_context_p);
            printf("rrc_countercheck_plain_text called!\n");
          }
        }else if(strcmp(buffer, "rrc_ueinfo_req_protected\n") == 0){
          gNB_RRC_INST *gnb_rrc_inst = RC.nrrrc[context_in_socket->module_id];
          rrc_gNB_ue_context_t *ue_context_p = rrc_gNB_get_ue_context_by_rnti(gnb_rrc_inst, context_in_socket->rntiMaybeUEid);
          if(!gnb_rrc_inst || !ue_context_p || !has_key){
            printf("sending null action back!\n");
            send_message("null_action\n");
          } else{
            sec_type = 3;
            rrc_gNB_generate_RRCUeInfoReq(context_in_socket, ue_context_p);
            printf("rrc_ueinfo_req_protected called!\n");
          }
        }else if(strcmp(buffer, "rrc_ueinfo_req_plain_text\n") == 0){
          gNB_RRC_INST *gnb_rrc_inst = RC.nrrrc[context_in_socket->module_id];
          rrc_gNB_ue_context_t *ue_context_p = rrc_gNB_get_ue_context_by_rnti(gnb_rrc_inst, context_in_socket->rntiMaybeUEid);
          if(!gnb_rrc_inst || !ue_context_p){
            printf("context check failed, sending null action back!\n");
            send_message("null_action\n");
          } else{
            sec_type = 0; //check this
            rrc_gNB_generate_RRCUeInfoReq(context_in_socket, ue_context_p);
            printf("rrc_ueinfo_req_plain_text called!\n");
          }
        }

  }// end for while(1)
} // end for conn(void *arg)


void statelearner_connect()
{
  printf("The gnb_socket thread has been started...\n");
  pthread_t thrConn;
  int i = 0, ret = 0;
  ret = pthread_create(&thrConn, NULL, conn, NULL);
  if (ret == -1) {
    printf("Failed to create socket\n");
    printf("Create thread error\n");
    return;
  }
  printf("New socket created\n");
}

void send_message(char* message) {
      send(server_sock, message, strlen(message), 0); // send
}