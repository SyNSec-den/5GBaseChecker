#ifndef OPENAIRINTERFACE_GNB_SOCKET_H
#define OPENAIRINTERFACE_GNB_SOCKET_H

#define PORT 60001

#include <stdio.h>
#include <pthread.h>
#include <sys/socket.h>
#include <stdlib.h>
#include <netinet/in.h>
#include <string.h>
#include <unistd.h>
#include <arpa/inet.h>
#include "openair2/RRC/NR/nr_rrc_static.h"
#include "openair2/RRC/NR/nr_rrc_proto.h"

extern bool has_key;
extern int RACH_Count;

void statelearner_connect();

void send_message(char* message);

#endif // OPENAIRINTERFACE_GNB_SOCKET_H
