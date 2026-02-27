package com.bank.transfer.app.controller;

import com.bank.transfer.app.dto.TransferRequest;
import com.bank.transfer.app.entity.Account;
import com.bank.transfer.app.repository.AccountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
//@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TransferControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    private Account alice;
    private Account bob;

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();
        alice = accountRepository.save(Account.builder()
                .accountNumber("1111111111")
                .accountName("Alice")
                .balance(new BigDecimal("100000.00"))
                .build());
        bob = accountRepository.save(Account.builder()
                .accountNumber("2222222222")
                .accountName("Bob")
                .balance(new BigDecimal("50000.00"))
                .build());
    }

    @Test
    @DisplayName("POST /api/v1/transfers - successful transfer returns 201")
    void transfer_success() throws Exception {
        TransferRequest request = TransferRequest.builder()
                .sourceAccountNumber("1111111111")
                .destinationAccountNumber("2222222222")
                .amount(new BigDecimal("1000.00"))
                .description("Test")
                .build();

        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("SUCCESSFUL"))
                .andExpect(jsonPath("$.data.transactionReference").isNotEmpty())
                .andExpect(jsonPath("$.data.transactionFee").value(5.0));
    }

    @Test
    @DisplayName("POST /api/v1/transfers - insufficient funds returns 422")
    void transfer_insufficientFunds() throws Exception {
        TransferRequest request = TransferRequest.builder()
                .sourceAccountNumber("1111111111")
                .destinationAccountNumber("2222222222")
                .amount(new BigDecimal("999999.00"))
                .build();

        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/v1/transfers - missing amount returns 400")
    void transfer_validationFailure() throws Exception {
        TransferRequest request = TransferRequest.builder()
                .sourceAccountNumber("1111111111")
                .destinationAccountNumber("2222222222")
                .build();

        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.amount").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/v1/transfers - same account returns 400")
    void transfer_sameAccount() throws Exception {
        TransferRequest request = TransferRequest.builder()
                .sourceAccountNumber("1111111111")
                .destinationAccountNumber("1111111111")
                .amount(new BigDecimal("1000.00"))
                .build();

        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/transfers - returns paginated results")
    void getTransactions_success() throws Exception {
        mockMvc.perform(get("/api/v1/transfers")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/summaries/daily - returns summary for today")
    void getDailySummary_today() throws Exception {
        mockMvc.perform(get("/api/v1/summaries/daily"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.summaryDate").isNotEmpty())
                .andExpect(jsonPath("$.data.totalTransactions").isNumber());
    }
}