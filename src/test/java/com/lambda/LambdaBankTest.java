package com.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LambdaBankTest {

    @Mock
    private Context mockContext;
    private LambdaBank lambdaBank;

    @BeforeEach
    public void setUp() {
        lambdaBank = new LambdaBank();
    }

    @Test
    public void testHandleRequest() {
        // Given
        BankRequest request = new BankRequest();
        request.setAmount(BigDecimal.valueOf(2000000));
        request.setRate(BigDecimal.valueOf(1.4));
        request.setTerm(48);

        // When
        BankResponse response = lambdaBank.handleRequest(request, mockContext);
        System.out.println(response);

        // Then
        BigDecimal expectedQuota = BigDecimal.valueOf(57503.35).setScale(2, RoundingMode.HALF_UP);
        BigDecimal expectedQuotaWithAccount = BigDecimal.valueOf(55055.10).setScale(2, RoundingMode.HALF_UP);

        BigDecimal rateExpected = BigDecimal.valueOf(0.014);
        BigDecimal rateExpectedWithAccount = BigDecimal.valueOf(0.012);

        assertEquals(expectedQuota, response.getQuota());
        assertEquals(expectedQuotaWithAccount, response.getQuotaWithAccount());
        assertEquals(rateExpected, response.getRate());
        assertEquals(rateExpectedWithAccount, response.getRateWithAccount());
    }
}