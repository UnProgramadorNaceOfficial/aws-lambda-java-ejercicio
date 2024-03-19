package com.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * P = Monto del préstamo
 * i = Tasa de interés mensual
 * n = Plazo del crédito en meses
 * <p>
 * Cuota mensual = (P * i) / (1 - (1 + i) ^ (-n))
 */
public class LambdaBank implements RequestHandler<BankRequest, BankResponse> {

    @Override
    public BankResponse handleRequest(BankRequest bankRequest, Context context) {

        MathContext mathContext = MathContext.DECIMAL128;
        BigDecimal amount = bankRequest.getAmount().setScale(2, RoundingMode.HALF_UP);

        BigDecimal monthlyRate = bankRequest.getRate()
                                            .setScale(2, RoundingMode.HALF_UP)
                                            .divide(BigDecimal.valueOf(100), mathContext);

        BigDecimal monthlyRateWithAccount = bankRequest.getRate()
                                                       .subtract(BigDecimal.valueOf(0.2), mathContext)
                                                       .setScale(2, RoundingMode.HALF_UP)
                                                       .divide(BigDecimal.valueOf(100), mathContext);
        Integer term = bankRequest.getTerm();

        BigDecimal monthlyPayment = this.calculateQuota(amount, monthlyRate, term, mathContext);
        BigDecimal monthlyPaymentWithAccount = this.calculateQuota(amount, monthlyRateWithAccount, term, mathContext);

        BankResponse bankResponse = new BankResponse();
        bankResponse.setQuota(monthlyPayment);
        bankResponse.setRate(monthlyRate);
        bankResponse.setTerm(term);
        bankResponse.setQuotaWithAccount(monthlyPaymentWithAccount);
        bankResponse.setRateWithAccount(monthlyRateWithAccount);
        bankResponse.setTermWithAccount(term);

        return bankResponse;
    }

    public BigDecimal calculateQuota(BigDecimal amount, BigDecimal monthlyRate, Integer term, MathContext mathContext){

        // Calcular (1 + i)
        BigDecimal onePlusRate = monthlyRate.add(BigDecimal.ONE, mathContext);

        // Calcular (1 + i) ^ n y luego tomar el recíproco para obtener (1 + i) ^ -n
        BigDecimal onePlusRateToN = onePlusRate.pow(term, mathContext);
        BigDecimal onePlusRateToNegativeN = BigDecimal.ONE.divide(onePlusRateToN, mathContext);

        // Calcular la cuota mensual
        BigDecimal numerator = amount.multiply(monthlyRate, mathContext);
        BigDecimal denominator = BigDecimal.ONE.subtract(onePlusRateToNegativeN, mathContext);
        BigDecimal monthlyPayment = numerator.divide(denominator, mathContext);

        // Establecer el resultado a 2 decimales
        monthlyPayment = monthlyPayment.setScale(2, RoundingMode.HALF_UP);
        return monthlyPayment;
    }
}