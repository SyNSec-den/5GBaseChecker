
#ifndef OPENAIRINTERFACE_NR_RRC_STATIC_H
#define OPENAIRINTERFACE_NR_RRC_STATIC_H

#include "platform_types.h"
#include "NR_DRB-ToAddModList.h"
#include "NR_SRB-ToAddModList.h"
#include "nr_rrc_defs.h"
#include "rrc_gNB_UE_context.h"
#include "common/ran_context.h"

extern volatile uint16_t init_module_id;
extern volatile uint16_t init_crnti;
extern uint8_t* init_rrc_container;
extern volatile int init_rrc_container_length;
extern uint8_t* du2cu_container;
extern volatile int du2cu_container_length;
extern protocol_ctxt_t *context_in_socket;


void nr_rrc_addmod_drbs(int rnti,
                               const NR_DRB_ToAddModList_t *drb_list,
                               const struct NR_CellGroupConfig__rlc_BearerToAddModList *bearer_list);


void nr_rrc_addmod_srbs(int rnti,
                               const NR_SRB_INFO_TABLE_ENTRY *srb_list,
                               const int nb_srb,
                               const struct NR_CellGroupConfig__rlc_BearerToAddModList *bearer_list);

void apply_macrlc_config(gNB_RRC_INST *rrc, rrc_gNB_ue_context_t *const ue_context_pP, const protocol_ctxt_t *const ctxt_pP);

void rrc_gNB_generate_RRCSetup(instance_t instance,
                               rnti_t rnti,
                               rrc_gNB_ue_context_t *const ue_context_pP,
                               const uint8_t *masterCellGroup,
                               int masterCellGroup_len);

//-----------------------------------------------------------------------------
int rrc_gNB_generate_RRCSetup_for_RRCReestablishmentRequest(module_id_t module_id, rnti_t rnti, const int CC_id);
//-----------------------------------------------------------------------------


void rrc_gNB_generate_RRCReject(module_id_t module_id, rrc_gNB_ue_context_t *const ue_context_pP);
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
void rrc_gNB_generate_defaultRRCReconfiguration(const protocol_ctxt_t *const ctxt_pP, rrc_gNB_ue_context_t *ue_context_pP);
void rrc_gNB_generate_defaultRRCReconfiguration_plain_text(const protocol_ctxt_t *const ctxt_pP, rrc_gNB_ue_context_t *ue_context_pP);
void rrc_gNB_generate_defaultRRCReconfiguration_replay(const protocol_ctxt_t *const ctxt_pP, rrc_gNB_ue_context_t *ue_context_pP);
//-----------------------------------------------------------------------------
#endif // OPENAIRINTERFACE_NR_RRC_STATIC_H
