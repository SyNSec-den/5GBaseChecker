/*
 * Copyright (C) 2019,2020 by Sukchan Lee <acetcom@gmail.com>
 *
 * This file is part of Open5GS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

#include "nudm-build.h"

ogs_sbi_request_t *amf_nudm_uecm_build_registration(
        amf_ue_t *amf_ue, void *data)
{
    ogs_sbi_message_t message;
    ogs_sbi_header_t header;
    ogs_sbi_request_t *request = NULL;
    ogs_sbi_server_t *server = NULL;

    OpenAPI_amf3_gpp_access_registration_t Amf3GppAccessRegistration;

    ogs_warn("amf_ue->supi in nudm-build: %s", amf_ue->supi);

//     amf_ue->supi = "imsi-001011234567895"; //srsue bug?
    ogs_assert(amf_ue);
    ogs_assert(amf_ue->supi);

    ogs_warn("supi: %s", amf_ue->supi);

    memset(&message, 0, sizeof(message));
    message.h.method = (char *)OGS_SBI_HTTP_METHOD_PUT;
    message.h.service.name = (char *)OGS_SBI_SERVICE_NAME_NUDM_UECM;
    message.h.api.version = (char *)OGS_SBI_API_V1;
    message.h.resource.component[0] = amf_ue->supi;
    message.h.resource.component[1] =
            (char *)OGS_SBI_RESOURCE_NAME_REGISTRATIONS;
    message.h.resource.component[2] =
            (char *)OGS_SBI_RESOURCE_NAME_AMF_3GPP_ACCESS;

    memset(&Amf3GppAccessRegistration, 0, sizeof(Amf3GppAccessRegistration));

    ogs_assert(ogs_sbi_self()->nf_instance);
    Amf3GppAccessRegistration.amf_instance_id = ogs_sbi_self()->nf_instance->id;

    server = ogs_list_first(&ogs_sbi_self()->server_list);
    ogs_assert(server);

    memset(&header, 0, sizeof(header));
    header.service.name = (char *)OGS_SBI_SERVICE_NAME_NAMF_CALLBACK;
    header.api.version = (char *)OGS_SBI_API_V1;
    header.resource.component[0] = amf_ue->supi;
    header.resource.component[1] = (char *)OGS_SBI_RESOURCE_NAME_DEREG_NOTIFY;
    Amf3GppAccessRegistration.dereg_callback_uri =
                        ogs_sbi_server_uri(server, &header);
    ogs_assert(Amf3GppAccessRegistration.dereg_callback_uri);

    Amf3GppAccessRegistration.guami = ogs_sbi_build_guami(amf_ue->guami);
    Amf3GppAccessRegistration.rat_type = amf_ue_rat_type(amf_ue);
    ogs_assert(Amf3GppAccessRegistration.rat_type != OpenAPI_rat_type_NULL);

    message.Amf3GppAccessRegistration = &Amf3GppAccessRegistration;

    request = ogs_sbi_build_request(&message);
    ogs_assert(request);

    if (Amf3GppAccessRegistration.guami)
        ogs_sbi_free_guami(Amf3GppAccessRegistration.guami);
    ogs_free(Amf3GppAccessRegistration.dereg_callback_uri);

    return request;
}

ogs_sbi_request_t *amf_nudm_uecm_build_registration_delete(
        amf_ue_t *amf_ue, void *data)
{
    ogs_sbi_message_t message;
    ogs_sbi_request_t *request = NULL;

    OpenAPI_amf3_gpp_access_registration_modification_t
        Amf3GppAccessRegistrationModification;

    ogs_assert(amf_ue);
    ogs_assert(amf_ue->supi);

    memset(&message, 0, sizeof(message));
    message.h.method = (char *)OGS_SBI_HTTP_METHOD_PATCH;
    message.h.service.name = (char *)OGS_SBI_SERVICE_NAME_NUDM_UECM;
    message.h.api.version = (char *)OGS_SBI_API_V1;
    message.h.resource.component[0] = amf_ue->supi;
    message.h.resource.component[1] =
            (char *)OGS_SBI_RESOURCE_NAME_REGISTRATIONS;
    message.h.resource.component[2] =
            (char *)OGS_SBI_RESOURCE_NAME_AMF_3GPP_ACCESS;

    memset(&Amf3GppAccessRegistrationModification, 0,
            sizeof(Amf3GppAccessRegistrationModification));

    Amf3GppAccessRegistrationModification.guami =
            ogs_sbi_build_guami(amf_ue->guami);
    Amf3GppAccessRegistrationModification.is_purge_flag = true;
    Amf3GppAccessRegistrationModification.purge_flag = 1;

    message.Amf3GppAccessRegistrationModification =
            &Amf3GppAccessRegistrationModification;

    request = ogs_sbi_build_request(&message);
    ogs_assert(request);

    if (Amf3GppAccessRegistrationModification.guami)
        ogs_sbi_free_guami(Amf3GppAccessRegistrationModification.guami);

    return request;
}

ogs_sbi_request_t *amf_nudm_sdm_build_get(amf_ue_t *amf_ue, void *data)
{
    ogs_sbi_message_t message;
    ogs_sbi_request_t *request = NULL;

    ogs_assert(amf_ue);
    ogs_assert(amf_ue->supi);

    memset(&message, 0, sizeof(message));
    message.h.method = (char *)OGS_SBI_HTTP_METHOD_GET;
    message.h.service.name = (char *)OGS_SBI_SERVICE_NAME_NUDM_SDM;
    message.h.api.version = (char *)OGS_SBI_API_V2;
    message.h.resource.component[0] = amf_ue->supi;
    message.h.resource.component[1] = data;

    request = ogs_sbi_build_request(&message);
    ogs_assert(request);

    return request;
}
