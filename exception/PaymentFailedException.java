package com.thapar.sportssphere.exception;

/** Simulated payment could not be completed. */
public class PaymentFailedException extends SportsException {
    private static final long serialVersionUID = 1L;

    public PaymentFailedException(String txnId, String reason) {
        super("Payment " + txnId + " failed (simulated): " + reason);
    }
}
