//
// Created by kai on 11/12/22.
//

#ifndef SRSRAN_ENODEB_STATELEARNER_H
#define SRSRAN_ENODEB_STATELEARNER_H

#include "srsran/common/buffer_pool.h"
// #include "srsran/common/log.h"
// #include "srsran/common/log_filter.h"
#include "srsran/common/buffer_pool.h"
#include "srsran/common/threads.h"
#include "srsran/interfaces/gnb_interfaces.h"

extern bool mac_valid;

namespace srsenb {
class enodeb_statelearner : public srsran::thread, public enodeb_statelearner_interface_ngap
{
public:
  enodeb_statelearner(); //构造方法
  bool init(ngap_interface_enodeb_statelearner* ngap);
  void stop();
  static enodeb_statelearner* get_instance(void);
  // server
  int statelearner_listen();
  int get_enodeb_statelearner();

  //client
  //bool connect_statelearner();
  bool notify_response(uint8_t *msg, uint16_t len);
  void run_thread();
  void notify_rrc_connection(); //Fuzzing. Wait rrc connection first. After sent rrc_set up, set it as true.
  void RACH_Counter(); // Mainly for plaintext msg which lead to UE disconnect. We only allow UE RACH once within a query
  void RACH_Counter_reset();


private:
  srsran::byte_buffer_pool  *m_pool;
  static enodeb_statelearner *m_instance;

  ngap_interface_enodeb_statelearner* m_ngap;

  in_addr_t m_enodeb_statelearner_ip;
  int m_enodeb_statelearner_sock;


  bool rrc_connected = false; //Fuzzing. Wait rrc connection first
  bool running = false;
  bool statelearner_connected       = false;
  int RACH_Count = 0;

};
} // namespace srsenb

#endif // SRSRAN_ENODEB_STATELEARNER_H
