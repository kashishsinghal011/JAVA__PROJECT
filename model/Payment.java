package com.thapar.sportssphere.model;

import com.thapar.sportssphere.exception.InvalidOperationException;
import com.thapar.sportssphere.exception.PaymentFailedException;
import com.thapar.sportssphere.model.enums.PaymentStatus;
import com.thapar.sportssphere.util.Validator;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * SIMULATED fee transaction. No card, UPI or bank data is ever collected; the caller just
 * says whether the pretend gateway approves it.
 */
public class Payment implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String LABEL = "SIMULATED PAYMENT - no real money involved";

    private final String txnId;
    private final double amount;
    private PaymentStatus status = PaymentStatus.PENDING;
    private LocalDateTime processedAt;

    public Payment(String txnId, double amount) {
        this.txnId = Validator.requireNonBlank(txnId, "Transaction id");
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must not be negative.");
        }
        this.amount = amount;
    }

    /**
     * PENDING or FAILED payments may be (re)tried; a SUCCESS payment cannot be processed again,
     * which is what prevents double charging.
     */
    public void process(boolean gatewayApproves) throws PaymentFailedException, InvalidOperationException {
        if (status == PaymentStatus.SUCCESS) {
            throw new InvalidOperationException("payment " + txnId + " is already completed");
        }
        processedAt = LocalDateTime.now();
        if (!gatewayApproves) {
            status = PaymentStatus.FAILED;
            throw new PaymentFailedException(txnId, "the simulated gateway declined the transaction");
        }
        status = PaymentStatus.SUCCESS;
    }

    public boolean isSuccessful() { return status == PaymentStatus.SUCCESS; }

    /**
     * Persistence support: restores the exact saved state of a processed payment while
     * loading data (the live process() stamps the clock and can only succeed once).
     */
    public void restore(double savedAmount, PaymentStatus savedStatus, LocalDateTime savedProcessedAt) {
        if (savedAmount < 0) {
            throw new IllegalArgumentException("corrupted payment amount for " + txnId);
        }
        Validator.requireNonNull(savedStatus, "Saved payment status");
        status = savedStatus;
        processedAt = savedProcessedAt;
    }
    public String getTxnId() { return txnId; }
    public double getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public LocalDateTime getProcessedAt() { return processedAt; }

    @Override
    public String toString() {
        return String.format("%s Rs %.2f [%s]", txnId, amount, status);
    }
}
