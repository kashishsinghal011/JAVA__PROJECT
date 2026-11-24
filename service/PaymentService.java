package com.thapar.sportssphere.service;

import com.thapar.sportssphere.exception.InvalidOperationException;
import com.thapar.sportssphere.exception.PaymentFailedException;
import com.thapar.sportssphere.model.Payment;
import com.thapar.sportssphere.model.Registration;
import com.thapar.sportssphere.model.SportsData;
import com.thapar.sportssphere.model.enums.RegistrationStatus;

/** Simulated fee handling. Keeps Registration status and Payment status consistent. */
public class PaymentService {
    private final SportsData data;

    public PaymentService(SportsData data) { this.data = data; }

    /**
     * Pays a PENDING registration. Order matters for consistency:
     * 1) verify the registration can still be confirmed, 2) process the payment,
     * 3) confirm. If step 2 fails, the registration is untouched (still PENDING) and its payment FAILED.
     */
    public void settle(Registration registration, boolean gatewayApproves)
            throws PaymentFailedException, InvalidOperationException {
        if (registration.getStatus() != RegistrationStatus.PENDING) {
            throw new InvalidOperationException("registration " + registration.getRegistrationId()
                    + " is " + registration.getStatus() + "; only PENDING registrations can be paid");
        }
        registration.getPayment().process(gatewayApproves);   // throws PaymentFailedException / duplicate payment
        registration.confirm();                               // cannot fail now: status PENDING and payment SUCCESS
    }

    /** Retry path for a registration that was reserved but not yet paid. */
    public Registration payPending(String registrationId, boolean gatewayApproves)
            throws PaymentFailedException, InvalidOperationException {
        Registration r = data.findRegistration(registrationId);
        settle(r, gatewayApproves);
        return r;
    }

    public String receipt(Registration r) {
        Payment p = r.getPayment();
        return String.format("%s%nTransaction: %s | Amount: Rs %.2f | Status: %s | Registration: %s (%s)",
                Payment.LABEL, p.getTxnId(), p.getAmount(), p.getStatus(), r.getRegistrationId(), r.getStatus());
    }
}
