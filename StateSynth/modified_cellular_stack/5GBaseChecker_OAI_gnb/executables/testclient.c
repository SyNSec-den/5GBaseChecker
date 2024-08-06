// client to test server
// also act as server to receive response

#include <arpa/inet.h>
#include <stdio.h>
#include <string.h>
#include <sys/socket.h>
#include <unistd.h>

#include <netinet/in.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <unistd.h>
#include <pthread.h>
// #include "testsocket.h"

#define PORT 60001


int main(int argc, char const* argv[])
{
    int sock, valread, client_fd;
    int server_fd, server_sock, server_val;
    struct sockaddr_in serv_addr, address;
    int opt=1;
    int addrlen = sizeof(address);
    char message[100];

    // client to send to open5gs
    if ((sock = socket(AF_INET, SOCK_STREAM, 0)) < 0) {  // socket
        printf("\n Socket creation error \n");
        return -1;
    }

    // as server to receive from gmm-handler
    if ((server_fd = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
        perror("socket failed");
        exit(EXIT_FAILURE);
    }

    // set testclient also as server
    serv_addr.sin_family = AF_INET;
    serv_addr.sin_addr.s_addr = INADDR_ANY;
    serv_addr.sin_port = htons(PORT);
    if (inet_pton(AF_INET, "127.0.0.1", &serv_addr.sin_addr)<= 0) { // set serverip address
        printf("\nInvalid address/ Address not supported \n");
        return -1;
    }

    // set socket option
    if (setsockopt(server_fd, SOL_SOCKET,
                   SO_REUSEADDR | SO_REUSEPORT, &opt,
                   sizeof(opt))) {
        perror("setsockopt");
        exit(EXIT_FAILURE);
    }

    /*
    // bind ip and port
    if (bind(server_fd, (struct sockaddr *) (&serv_addr),  // sockaddr_in --> sockaddr
             sizeof(address)) < 0) {
        perror("bind failed");
        exit(EXIT_FAILURE);
    }

    // listen
    if (listen(server_fd, 3) < 0) {
        perror("listen");
        exit(EXIT_FAILURE);
    }
     */

    address.sin_family = AF_INET;
    address.sin_addr.s_addr = INADDR_ANY;
    address.sin_port = htons(PORT);

    // connect
    printf("message=%s\n",message);
    if ((client_fd = connect(sock, (struct sockaddr*)&serv_addr,sizeof(serv_addr)))< 0) { // connect
        printf("\nConnection Failed \n");
        return -1;
    }

    int message_count=0;
    int flag = 0;
    while (1) {
        flag = 0;
        scanf("%s",message);
        message_count++;
        if(strncmp(message,"0", 1) == 0){
            strcpy(message,"Hello\n");
        }
        if(strncmp(message,"1", 1) == 0){
            flag = 1;
            strcpy(message,"rrc_sm_cmd_plain_text\n");
        }
        if(strncmp(message,"2", 1) == 0){
            flag = 1;
            strcpy(message,"rrc_sm_cmd\n");
        }
        if(strncmp(message,"3", 1) == 0){
            flag = 1;
            strcpy(message,"ue_cap_enquiry_protected\n");
        }
        if(strncmp(message,"4", 1) == 0){
            flag = 1;
            strcpy(message,"rrc_reconf_protected\n");
        }
        if(strncmp(message,"5", 1) == 0){
            strcpy(message,"rrc_resume_protected\n");
            flag = 1;
        }
        if(strncmp(message,"6", 1) == 0){
            strcpy(message,"rrc_ueinfo_req_protected\n");
            flag = 1;
        }
        if(strncmp(message,"7", 1) == 0){
            strcpy(message,"rrc_countercheck_protected\n");
            flag = 1;
        }
        if(strncmp(message,"8", 1) == 0){
            strcpy(message,"rrc_sm_cmd_ns_plain_text\n");
            flag = 1;
        }
        if(strncmp(message,"9", 1) == 0){
            strcpy(message,"ue_cap_enquiry_plain_text\n");
            flag = 1;
        }


        if (strcmp(message,"end")==0) {
            printf("end of message\n");
            break;
        }

        send(sock, message, strlen(message), 0); // send to testsocket
        printf("%s %s\n","message sent",message);

        if(flag == 0) {
            char buffer[1024] = {0};
            valread = read(sock, buffer, 1024); // read (block)
            printf("received: %s\n", buffer);
        }


    }
    return 0;
}