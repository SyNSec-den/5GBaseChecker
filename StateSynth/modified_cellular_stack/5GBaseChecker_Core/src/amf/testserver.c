// test server
// send msg to open5gs testsocket

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

#define PORT 8081
//#define SERVER_PORT 8080

int main(int argc, char const* argv[])
{
    int sock = 0, valread,server_fd, server_sock;
    struct sockaddr_in serv_addr;
    int opt=1, message_count=0,addrlen = sizeof(serv_addr);
    char message[100];

    // create server_fd
    if ((server_fd = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
        perror("socket failed");
        exit(EXIT_FAILURE);
    }

    // set socket option
    if (setsockopt(server_fd, SOL_SOCKET,
                   SO_REUSEADDR | SO_REUSEPORT, &opt,
                   sizeof(opt))) {
        perror("setsockopt failed");
        exit(EXIT_FAILURE);
    }

    // bind ip and port
    serv_addr.sin_family = AF_INET;
    serv_addr.sin_addr.s_addr = INADDR_ANY;
    serv_addr.sin_port = htons(PORT);
    if (bind(server_fd, (struct sockaddr *) (&serv_addr),  // sockaddr_in --> sockaddr
             sizeof(serv_addr)) < 0) {
        perror("bind failed");
        exit(EXIT_FAILURE);
    }

    // listen and accept
    if (listen(server_fd, 3) < 0) {
        perror("listen failed");
        exit(EXIT_FAILURE);
    }
    if ((server_sock = accept(server_fd, (struct sockaddr *) (&serv_addr),
                         (socklen_t * ) (&addrlen))) < 0) {
        // block, wait for response from gmm-handler
        perror("accept failed");
        exit(EXIT_FAILURE);
    }

    printf("open5gs connected...\n");

    while (1) {

        scanf("%s",message);
        message_count++;
        if(strncmp(message,"1", 1) == 0){
            strcpy(message,"id_request_plain_text");
        }
        if(strncmp(message,"2", 1) == 0){
            strcpy(message,"auth_request_plain_text");
        }
        if(strncmp(message,"3", 1) == 0){
            strcpy(message,"auth_reject");
        }
        if(strncmp(message,"4", 1) == 0){
            strcpy(message,"nas_sm_cmd");
        }
        if(strncmp(message,"5", 1) == 0){
            strcpy(message,"registration_accept_protected");
        }

        if(strncmp(message,"6", 1) == 0){
            strcpy(message,"nas_sm_cmd_plain_text");
        }


        if(strncmp(message,"7", 1) == 0){
            strcpy(message,"nas_sm_cmd_protected");
        }

/*
        if(strncmp(message,"7", 1) == 0){
            strcpy(message,"config_update_cmd_replay");
        }
        */

        if(strncmp(message,"8", 1) == 0){
            strcpy(message,"nas_sm_cmd_replay");
        }

        /*
        if(strncmp(message,"9", 1) == 0){
            strcpy(message,"nas_sm_cmd_null_security");
        }
         */

        if(strncmp(message,"9", 1) == 0){
            strcpy(message,"config_update_cmd_protected");
        }

        if(strcmp(message,"10") == 0){
            strcpy(message,"nas_configuration_update_command_replay");
        }

        /*
        if(strcmp(message,"0") == 0){
            strcpy(message,"nas_registration_accept_plain_text");
        }
*/
        if(strcmp(message,"0") == 0){
            strcpy(message,"registration_reject");
        }

        send(server_sock, message, strlen(message), 0); // send to testsocket
        printf("%d:%s %s\n",message_count,"message sent",message);

        char buffer[1024] = { 0 };
        valread = read(server_sock, buffer, 1024);   // read (block)
        printf("received: %s\n", buffer);
        // closing the connected socket
        // close(client_fd);

    }
    return 0;
}