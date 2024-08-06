#ifndef OPEN5GS_TESTSOCKET_H
#define OPEN5GS_TESTSOCKET_H

/*
#include "ogs-sctp.h"
#include "ogs-app.h"
#include "gmm-build.h"
#include "gmm-handler.h"
#include "nas-path.h"
#include "context.h"
 */

extern bool dlcounter_reset;
extern int server_sock;
extern int auth_count;
extern ogs_sbi_message_t message;
extern pthread_cond_t auth_cond;
extern pthread_mutex_t auth_cond_mutex;
extern pthread_cond_t send_auth_cond;
extern pthread_mutex_t send_auth_cond_mutex;

bool check_int_and_enc(uint8_t* intarr,uint8_t* encarr);
bool check_int(uint8_t* intarr);
bool check_enc(uint8_t* encarr);
bool check_kamf(uint8_t* kamfarr);
bool check_kgnb(uint8_t* kgnb);
void* conn(void* arg);
void thr_connect();



#endif //OPEN5GS_TESTSOCKET_H
