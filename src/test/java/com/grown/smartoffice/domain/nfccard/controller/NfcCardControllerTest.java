package com.grown.smartoffice.domain.nfccard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grown.smartoffice.domain.nfccard.dto.NfcCardRegisterRequest;
import com.grown.smartoffice.domain.nfccard.dto.NfcCardRegisterResponse;
import com.grown.smartoffice.domain.nfccard.entity.NfcCard;
import com.grown.smartoffice.domain.nfccard.entity.NfcCardStatus;
import com.grown.smartoffice.domain.nfccard.service.NfcCardService;
import com.grown.smartoffice.domain.user.entity.User;
import com.grown.smartoffice.support.TestSecurityConfig;
import com.grown.smartoffice.support.WithMockAdminUser;
import com.grown.smartoffice.support.WithMockEmployeeUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NfcCardController.class)
@Import(TestSecurityConfig.class)
class NfcCardControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean NfcCardService nfcCardService;

    @Test
    @DisplayName("NFC 카드 등록 - ADMIN 성공")
    @WithMockAdminUser
    void registerCard_asAdmin_success() throws Exception {
        NfcCardRegisterRequest req = NfcCardRegisterRequest.builder()
                .userId(1L)
                .uid("UID-001")
                .cardType("EMPLOYEE")
                .build();

        User user = User.builder().employeeName("박성종").build();
        NfcCard card = NfcCard.builder().user(user).cardUid("UID-001").cardType("EMPLOYEE").build();
        org.springframework.test.util.ReflectionTestUtils.setField(card, "cardId", 1L);
        
        given(nfcCardService.registerCard(any())).willReturn(new NfcCardRegisterResponse(card));

        mockMvc.perform(post("/api/v1/nfc-cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.uid").value("UID-001"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("NFC 카드 등록 - EMPLOYEE 거부")
    @WithMockEmployeeUser
    void registerCard_asEmployee_forbidden() throws Exception {
        NfcCardRegisterRequest req = NfcCardRegisterRequest.builder()
                .userId(1L)
                .uid("UID-001")
                .cardType("EMPLOYEE")
                .build();

        mockMvc.perform(post("/api/v1/nfc-cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("NFC 카드 목록 조회 - ADMIN 성공")
    @WithMockAdminUser
    void getAllCards_asAdmin_success() throws Exception {
        mockMvc.perform(get("/api/v1/nfc-cards"))
                .andExpect(status().isOk());
    }
}
