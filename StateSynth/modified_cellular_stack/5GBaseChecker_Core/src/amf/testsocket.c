#include <netinet/in.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <unistd.h>
#include <pthread.h>

#include "amf-sm.h"
#include "testsocket.h"
#include "gmm-build.h"
#include "gmm-handler.h"
#include "nas-path.h"
#include "context.h"
#include "nausf-build.h"
#include "nausf-handler.h"
#include "nudm-build.h"
#include "nudm-handler.h"
#include "sbi-path.h"
#include "ogs-sbi.h"
#include "../ausf/event.h"
#include "../ausf/ausf-sm.h"

#define PORT 60000

int auth_count=0;
int server_sock=0;
ogs_sbi_message_t message;
extern int regis_count;
pthread_cond_t send_auth_cond = PTHREAD_COND_INITIALIZER;
pthread_mutex_t send_auth_cond_mutex = PTHREAD_MUTEX_INITIALIZER;
pthread_mutex_t auth_cond_mutex = PTHREAD_MUTEX_INITIALIZER;
pthread_cond_t auth_cond = PTHREAD_COND_INITIALIZER;

bool check_kamf(uint8_t* kamfarr) {
    bool flag=false;
    int i;
    for (i=0;i<OGS_SHA256_DIGEST_SIZE;i++) {
        if (kamfarr[i]!=0)
            flag=true;
    }
    return flag;
}
bool check_int(uint8_t* intarr) {
    bool is_int=false;
    int i;
    for (i=0;i<OGS_SHA256_DIGEST_SIZE/2;i++) {
        if (intarr[i]!=0)
            is_int=true;
    }

    return is_int;
}

bool check_enc(uint8_t* encarr) {
    bool is_enc=false;
    int i;
    for (i=0;i<OGS_SHA256_DIGEST_SIZE/2;i++) {
        if (encarr[i]!=0)
            is_enc=true;
    }

    return is_enc;
}

bool check_int_and_enc(uint8_t* intarr,uint8_t* encarr) {
    bool is_int=false,is_enc=false;
    int i;
    for (i=0;i<OGS_SHA256_DIGEST_SIZE/2;i++) {
        if (intarr[i]!=0)
            is_int=true;
        if (encarr[i]!=0)
            is_enc=true;
    }
    return (is_int&&is_enc);
}

bool check_kgnb(uint8_t* kgnb) {
    bool have_gnb=false;
    int i;
    for (i=0;i<OGS_SHA256_DIGEST_SIZE;i++) {
        if (kgnb[i] != 0)
            have_gnb = true;
    }
    return have_gnb;
}

void* conn(void* arg) {

    struct sockaddr_in serv_addr;
    int opt = 1,addrlen = sizeof(serv_addr),message_count=0,valread, server_fd;
    int smd_replayed=0, conf_upd_replayed=0;

    if ((server_fd = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
        perror("socket failed");
        exit(EXIT_FAILURE);
    }

    if (setsockopt(server_fd, SOL_SOCKET,
                   SO_REUSEADDR | SO_REUSEPORT, &opt,
                   sizeof(opt))) {
        perror("setsockopt");
        exit(EXIT_FAILURE);
    }

    serv_addr.sin_family = AF_INET;
    serv_addr.sin_addr.s_addr = INADDR_ANY;
    serv_addr.sin_port = htons(PORT);
    if (inet_pton(AF_INET, "127.0.0.1", &serv_addr.sin_addr)<= 0) { 
        printf("\nInvalid address/ Address not supported \n");
        return -1;
    }

    if (bind(server_fd, (struct sockaddr *) (&serv_addr),  
             sizeof(serv_addr)) < 0) {
        perror("bind failed");
        exit(EXIT_FAILURE);
    }
    if (listen(server_fd, 3) < 0) { 
        perror("listen");
        exit(EXIT_FAILURE);
    }

    ogs_info("Connected to client_log/log_executor");
    if ((server_sock = accept(server_fd, (struct sockaddr *) (&serv_addr),
                              (socklen_t * ) (&addrlen))) < 0) {  
        perror("accept");
        exit(EXIT_FAILURE);
    }

    char test_buffer[1024] = {0};
    valread = read(server_sock, test_buffer, 1024);  
    ogs_info("buffer:%s",test_buffer);
    char* response="ACK from core\n";
    send_message(response);

    while (1) {
        char buffer[1024] = {0};
        valread = read(server_sock, buffer, 1024);  
        amf_ue_t* amf_ue = ogs_list_first(&amf_self()->amf_ue_list);
        int count = ogs_list_count(&amf_self()->amf_ue_list);

        if (strcmp(buffer,"RACH_Reset\n")==0) {
            ogs_warn("received:RACH_Reset");
            regis_count = 0;
            send_message("DONE\n");
            continue;
        }

        if(regis_count > 1){
            ogs_warn("registration request count more than 1, UE disconnect before, sending null_action back.");
            char* response="null_action\n";
            send_message(response);
            continue;
        }

        if (strcmp(buffer,"id_request_plain_text\n")==0) {
            ogs_info("received:id_request_plain_text");
            ogs_assert(amf_ue->kamf);
            CLEAR_AMF_UE_TIMER(amf_ue->t3570); 
            int rv= nas_5gs_send_identity_request(amf_ue);
        }

        if (strcmp(buffer,"auth_request_plain_text\n")==0) {
            ogs_info("received:auth_request_plain_text");
            auth_count=0;
            // amf_sbi_send_release_all_sessions(
            //         amf_ue, AMF_RELEASE_SM_CONTEXT_NO_STATE);
            ogs_assert(true ==
                       amf_ue_sbi_discover_and_send(OpenAPI_nf_type_AUSF, NULL,
                                                    amf_nausf_auth_build_authenticate, amf_ue, NULL));
            pthread_mutex_lock(&auth_cond_mutex);
            pthread_cond_wait(&auth_cond,&auth_cond_mutex);
            pthread_mutex_unlock(&auth_cond_mutex);
            ogs_sbi_message_t *test_message=event_auth->h.sbi.message;

            int rv2 = amf_nausf_auth_handle_authenticate(amf_ue, test_message);
            pthread_mutex_lock(&send_auth_cond_mutex);
            pthread_cond_signal(&send_auth_cond);
            pthread_mutex_unlock(&send_auth_cond_mutex);
        }

        if (strcmp(buffer,"auth_reject\n")==0) {
            ogs_info("received:auth_reject");
            int rv= nas_5gs_send_authentication_reject(amf_ue);
        }

        if (strcmp(buffer,"nas_sm_cmd\n")==0) {
            ogs_info("received:nas_sm_cmd");
            // CLEAR_AMF_UE_TIMER(amf_ue->t3560); 
            // ogs_assert(OGS_OK ==nas_5gs_send_security_mode_command(amf_ue));
            if (!check_kamf(amf_ue->kamf)) {
                send_message("null_action\n");
                continue;
            }
            int rv=nas_5gs_send_security_mode_command(amf_ue);
            if (rv==-5) {
                ogs_info("no integrity in smd");
                send_message("null_action\n");
                continue;
            }
            smd_replayed=1;
        }

        if (strcmp(buffer,"nas_sm_cmd_protected\n")==0) {
            ogs_info("received:nas_sm_cmd");
            // CLEAR_AMF_UE_TIMER(amf_ue->t3560); 
            // ogs_assert(OGS_OK ==nas_5gs_send_security_mode_command(amf_ue));
            if (!check_int_and_enc(amf_ue->knas_int,amf_ue->knas_enc)) {
                ogs_info("no integrity/encryption in smd_protected");
                send_message("null_action\n");
                continue;
            }
            int rv=nas_5gs_send_security_mode_command_protected(amf_ue);
            if (rv==-5) {
                ogs_info("no integrity/encryption in smd_protected");
                send_message("null_action\n");
                continue;
            }
        }

        if (strcmp(buffer,"nas_sm_cmd_replay\n")==0) {
            ogs_info("received:nas_sm_cmd_replay");
            if (!smd_replayed) {
                ogs_info("no smd_replay msg");
                send_message("null_action\n");
                continue;
            }
            if (!check_int(amf_ue->knas_int)) {
                ogs_info("no integrity in smd replay");
                send_message("null_action\n");
                continue;
            }
            // CLEAR_AMF_UE_TIMER(amf_ue->t3560); 
            // ogs_assert(OGS_OK ==nas_5gs_send_security_mode_command_replay(amf_ue));
            int rv=nas_5gs_send_security_mode_command_replay(amf_ue);
        }

        if (strcmp(buffer,"nas_sm_cmd_ns_plain_text\n")==0) {
            ogs_info("received:nas_sm_cmd_ns_plain_text");
            // CLEAR_AMF_UE_TIMER(amf_ue->t3560); 
            // ogs_assert(OGS_OK ==nas_5gs_send_security_mode_command_null_security(amf_ue));
            int rv=nas_5gs_send_security_mode_command_null_security(amf_ue); // This msg is not int protected
        }

        if (strcmp(buffer,"nas_sm_cmd_ns\n")==0) { //This msg itself is int protected
            ogs_info("received:nas_sm_cmd_ns");
            // CLEAR_AMF_UE_TIMER(amf_ue->t3560); 
            // ogs_assert(OGS_OK ==nas_5gs_send_security_mode_command_null_security(amf_ue));
            if (!check_kamf(amf_ue->kamf)) {
                ogs_info("no amf_ue_kamf");
                send_message("null_action\n");
                continue;
            }
            int rv=nas_5gs_send_security_mode_command_null_security_int(amf_ue);
            if (rv==-5) {
                ogs_info("no integrity in smd_ns");
                send_message("null_action\n");
                continue;
            }
        }

        if (strcmp(buffer,"registration_accept_protected\n")==0) {
            if (!check_kgnb(amf_ue->kgnb)) {
                ogs_info("protected msg, should after smd");
                send_message("null_action\n");
                continue;
            }

            ogs_assert(true ==
                       amf_ue_sbi_discover_and_send(OpenAPI_nf_type_UDM, NULL,
                                                    amf_nudm_uecm_build_registration, amf_ue, NULL));
            ogs_info("received:registration_accept_protected");
            int rv= nas_5gs_send_registration_accept_dlnas(amf_ue);
        }

        if (strcmp(buffer,"registration_accept_plain_text\n")==0) {
            ogs_assert(true ==
                       amf_ue_sbi_discover_and_send(OpenAPI_nf_type_UDM, NULL,
                                                    amf_nudm_uecm_build_registration, amf_ue, NULL));
            ogs_info("received:registration_accept_plain_text");
            int rv= nas_5gs_send_registration_accept_plain_text_dlnas(amf_ue);
        }

        if (strcmp(buffer,"registration_accept_h4_plain_text\n")==0) {
            ogs_assert(true ==
                       amf_ue_sbi_discover_and_send(OpenAPI_nf_type_UDM, NULL,
                                                    amf_nudm_uecm_build_registration, amf_ue, NULL));
            ogs_info("received:registration_accept_h4_plain_text");
            int rv= nas_5gs_send_registration_accept_h4_plain_text_dlnas(amf_ue);
        }

        if (strcmp(buffer,"config_update_cmd_protected\n")==0) {
            ogs_info("received:config_update_cmd_protected");
            if (!check_int_and_enc(amf_ue->knas_int,amf_ue->knas_enc)) {
                ogs_info("protected msg, should after smd");
                send_message("null_action\n");
                continue;
            }
            // CLEAR_AMF_UE_TIMER(amf_ue->t3560); 
            gmm_configuration_update_command_param_t param;
            memset(&param, 0, sizeof(param));
            param.nitz = 1;
            //ogs_assert(OGS_OK == nas_5gs_send_configuration_update_command(amf_ue,&param));
            int rv=nas_5gs_send_configuration_update_command(amf_ue,&param);
            conf_upd_replayed=1;
        }

        if (strcmp(buffer,"config_update_cmd_plain_text\n")==0) {
            ogs_info("received:config_update_cmd_plain_text");
            gmm_configuration_update_command_param_t param;
            memset(&param, 0, sizeof(param));
            param.nitz = 1;
            int rv=nas_5gs_send_configuration_update_command_plain_text(amf_ue,&param);
        }

        if (strcmp(buffer,"config_update_cmd_replay\n")==0) {
            ogs_info("received:config_update_cmd_replay");
            if (!check_int_and_enc(amf_ue->knas_int,amf_ue->knas_enc)) {
                ogs_info("protected msg, should after smd");
                send_message("null_action\n");
                continue;
            }
            if (!conf_upd_replayed) {
                ogs_info("no conf_upd_replay msg");
                send_message("null_action\n");
                continue;
            }
            // CLEAR_AMF_UE_TIMER(amf_ue->t3560); 
            gmm_configuration_update_command_param_t param;
            memset(&param, 0, sizeof(param));
            param.nitz = 1;
            // ogs_assert(OGS_OK == nas_5gs_send_configuration_update_command_replay(amf_ue,&param));
            int rv=nas_5gs_send_configuration_update_command_replay(amf_ue,&param);
        }

        if (strcmp(buffer,"registration_reject\n")==0) {
            ogs_info("received:registration_reject");
            // CLEAR_AMF_UE_TIMER(amf_ue->t3570);
            int rv= nas_5gs_send_registration_reject(amf_ue,OGS_5GMM_CAUSE_PLMN_NOT_ALLOWED);
            amf_sbi_send_release_all_sessions(
                     amf_ue, AMF_RELEASE_SM_CONTEXT_NO_STATE);
        }

        if (strcmp(buffer,"deregistration_req_protected\n")==0) {
            ogs_info("received:deregistration_req_protected");
            if (!check_int_and_enc(amf_ue->knas_int,amf_ue->knas_enc)) {
                ogs_info("protected msg, should after smd");
                send_message("null_action\n");
                continue;
            }
            int rv= nas_5gs_send_de_registration_request(amf_ue);
        }

        if (strcmp(buffer,"deregistration_req_plain_text\n")==0) {
            ogs_info("received:deregistration_req_plain_text");
            int rv= nas_5gs_send_de_registration_request_plain_text(amf_ue);
        }

        if (strcmp(buffer,"end")==0) {
            printf("received:end message\n");
            break;
        }
    }

    return NULL;

}

void thr_connect() {

    pthread_t thrConn;
    int i=0, ret=0;
    ret = pthread_create(&thrConn , NULL , conn, NULL);
    if(ret == -1){
        ogs_error("Failed to create socket");
        printf("Create thread error\n");
        return;
    }
    ogs_info("New socket created");

}
