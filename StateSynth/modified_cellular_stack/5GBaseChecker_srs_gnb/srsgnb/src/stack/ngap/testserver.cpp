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
#include <cerrno>
// #include "testsocket.h"

#define PORT 60001
#define SERVER_PORT 8080

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
    printf("message=\n");
    if ((client_fd = connect(sock, (struct sockaddr*)&serv_addr,sizeof(serv_addr)))< 0) { // connect
        printf("%s", strerror(errno));
        printf("\nConnection Failed \n");
        return -1;
    }

    int message_count=0;
    int flag = 0;
    while (1) {

        int len = scanf("%s",message);
        message_count++;
        if(strncmp(message,"01", 2) == 0){
            flag = 0;
            strcpy(message,"rrc_sm_cmd\n");
        }
        if(strncmp(message,"02", 2) == 0){
            flag = 0;
            strcpy(message,"ue_cap_enquiry_protected\n");
        }
        if(strncmp(message,"03", 2) == 0){
            flag = 0;
            strcpy(message,"rrc_reconf_protected\n");
        }
        if(strncmp(message,"4", 1) == 0){
            strcpy(message,"rrc_release\n");
        }
        if(strncmp(message,"5", 1) == 0){
            strcpy(message,"rrc_reest_protected\n");
        }


        // attack symbols
        if(strncmp(message,"6", 1) == 0){
            strcpy(message,"rrc_sm_cmd_replay\n");
        }

        if(strncmp(message,"7", 1) == 0){
            strcpy(message,"rrc_sm_cmd_protected\n");
        }

        if(strncmp(message,"8", 1) == 0){
            strcpy(message,"rrc_sm_cmd_plain_text\n");
        }

        if(strncmp(message,"9", 1) == 0){
            strcpy(message,"rrc_sm_cmd_null_security\n");
        }

        if(strncmp(message,"10", 2) == 0){
            strcpy(message,"rrc_reconf_replay\n");
        }

        if(strncmp(message,"11", 2) == 0){
            strcpy(message,"rrc_reconf_plain_text\n");
        }

        if(strncmp(message,"12", 2) == 0){
            strcpy(message,"rrc_reest_plain_text\n");
        }

        if(strncmp(message,"13", 2) == 0){
            strcpy(message,"ue_cap_enquiry_plaintext\n");
        }


        send(sock, message, strlen(message), 0); // send to testsocket
        printf("%s %s\n","message sent",message);

    if(flag == 1){
        char buffer[1024] = { 0 };
        valread = read(sock, buffer, 1024);   // read (block)
        printf("received: %s\n", buffer);
    }else{
        continue;
    }

        // closing the connected socket

    }
    return 0;
}
