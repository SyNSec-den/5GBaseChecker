//
// Created by kai on 11/12/22.
//

#include "srsgnb/hdr/stack/enodeb_statelearner.h"
#include <inttypes.h>
#include <iostream>

bool mac_valid = true;

namespace srsenb {
enodeb_statelearner* enodeb_statelearner::m_instance       = nullptr;
pthread_mutex_t      enodeb_statelearner_instance_mutex    = PTHREAD_MUTEX_INITIALIZER;
pthread_mutex_t      enodeb_statelearner_reset_state_mutex = PTHREAD_MUTEX_INITIALIZER;
enodeb_statelearner::enodeb_statelearner() : thread("ENODEB_STATELEARNER")
{
  running                = false;
  statelearner_connected = false;
  return;
}
bool enodeb_statelearner::init(ngap_interface_enodeb_statelearner* ngap)
{
  // is it ok
  // m_pool = srsran::byte_buffer_pool::get_instance();
  m_pool = srsran::byte_buffer_pool::get_instance();
  m_ngap = ngap;
  printf("enodeb-statelearner initialized\n");
  running = false;
  start();
  return true;
}

void enodeb_statelearner::run_thread()
{
  printf("The enodeb_statelearner thread has been started...\n");

  int                sock_fd, err;
  struct sockaddr_in enodeblearnlib_server_addr;

  // socket create and verification
  sock_fd = socket(AF_INET, SOCK_STREAM, 0);
  if (sock_fd == -1) {
    printf("enodeb-statelearner server socket creation failed...\n");
    return;
  }

  bzero(&enodeblearnlib_server_addr, sizeof(enodeblearnlib_server_addr));
  // assign IP, PORT
  enodeblearnlib_server_addr.sin_family      = AF_INET;
  enodeblearnlib_server_addr.sin_addr.s_addr = htonl(INADDR_ANY);
  inet_pton(AF_INET, "127.0.0.1", &(enodeblearnlib_server_addr.sin_addr));
  enodeblearnlib_server_addr.sin_port = htons(60001);
  err = bind(sock_fd, (struct sockaddr*)&enodeblearnlib_server_addr, sizeof(enodeblearnlib_server_addr));
  if (err != 0) {
    close(sock_fd);
    printf("%s\n", strerror(errno));
    printf("Error binding TCP socket for s1ap-learnlib server\n");
    return;
  }
  // Listen for connections
  err = listen(sock_fd, SOMAXCONN);
  if (err != 0) {
    close(sock_fd);
    printf("Error in s1ap-learnlib TCP socket listen\n");
    return;
  }

  srsran::byte_buffer_t* pdu =
      static_cast<srsran::byte_buffer_t*>(m_pool->allocate_node(sizeof(srsran::byte_buffer_t)));
  if (!pdu) {
    printf("Fatal Error: Couldn't allocate buffer in s1ap::run_thread().\n");
    return;
  }

  uint32_t sz = SRSRAN_MAX_BUFFER_SIZE_BYTES - SRSRAN_BUFFER_HEADER_OFFSET;

  m_enodeb_statelearner_sock = accept(sock_fd, (struct sockaddr*)NULL, (socklen_t*)NULL); //will block waiting here. Await a connection on socket FD.
  if (m_enodeb_statelearner_sock < 0) {
    perror("Client accept failed\n");
    return;
  }
  printf("Statelearner is connected\n"); //comment out for local gnb command test.

  running = true; 
  int rd_sz;

  bool ret;

  while (running) {
    // pdu->reset();
    pdu->clear();
    rd_sz = recv(m_enodeb_statelearner_sock, pdu->msg, sz, 0);

    if (rd_sz == -1) {
        printf("ERROR reading from TCP socket\n");
        usleep(10000000);
    }
    if (rd_sz == 0) {
      printf("Client Disconnected. Need to run log_executor again.\n");
      fflush(stdout);
      return;
    } else {
      pdu->N_bytes           = rd_sz;
      pdu->msg[pdu->N_bytes] = '\0';
      printf("\nReceived Query from Statelearner = %s\n", pdu->msg);
      fflush(stdout);
      // enb thread test
//      continue ;

      if (memcmp(pdu->msg, "Hello\n", pdu->N_bytes) == 0) {

        printf("### Received the expected HELLO message ###\n");
        uint8_t response[15] = "ACK from gnb\n";
        uint8_t size        = 15;
        notify_response(response, size);

      } else if(memcmp(pdu->msg, "RACH_Reset\n", pdu->N_bytes) == 0){
              printf("reset RACH Counter!\n");
              enodeb_statelearner::get_instance()->RACH_Counter_reset();
              uint8_t response[7] = "DONE\n";
              uint8_t size         = 7;
              notify_response(response, size);
      }else{
        while (!rrc_connected){

        }



          if (RACH_Count > 1){
              printf("RACH_Count > 1, UE RACH count more than 1, sending null_action back\n");
              uint8_t response[13] = "null_action\n";
              uint8_t size         = 13;
              notify_response(response, size);

          }else{
              ret = m_ngap->execute_command(pdu->msg, pdu->N_bytes);
              if (ret == false) {
                  uint8_t response[13] = "null_action\n";
                  uint8_t size         = 13;
                  notify_response(response, size);
              }
          }

      }
    }
  }
}

void
enodeb_statelearner::stop() {
  if(running) {
    running = false;
    thread_cancel();
    wait_thread_finish();
  }

  if (m_enodeb_statelearner_sock != -1){
    close(m_enodeb_statelearner_sock);
  }
  return;

}

enodeb_statelearner* enodeb_statelearner::get_instance(void) {
  pthread_mutex_lock(&enodeb_statelearner_instance_mutex);
  if (nullptr == m_instance) {
    printf("created a new instance!!\n");
    m_instance = new enodeb_statelearner();
  }
  pthread_mutex_unlock(&enodeb_statelearner_instance_mutex);
  return (m_instance);
}

bool enodeb_statelearner::notify_response(uint8_t *msg, uint16_t len) {


    if(mac_valid == false){
        int lent = len + 3;
        uint8_t *str = static_cast<uint8_t *>(calloc(lent, sizeof(uint8_t)));
        memcpy(str, msg, len);
        memcpy(str+len-1, "_wm\n", 4); //wm -> wrong mac
        free(str);
        printf("Sending back %s \n", str);
        if ((send(m_enodeb_statelearner_sock, msg, lent, 0)) < 0)
//        if ((send(m_enodeb_statelearner_sock, str, lent, 0)) < 0)
        {
            perror("Error in Send to Statelearner");
            exit(0);
        }
  }
    else{
        if ((send(m_enodeb_statelearner_sock, msg, len, 0)) < 0)
        {
            perror("Error in Send to Statelearner");
            exit(0);
        }
    }
  return true;
}

void enodeb_statelearner::notify_rrc_connection(){
  rrc_connected = true;
}

void enodeb_statelearner:: RACH_Counter(){
    RACH_Count++;
}

void enodeb_statelearner:: RACH_Counter_reset(){
    RACH_Count = 0;
}

} // namespace srsenb