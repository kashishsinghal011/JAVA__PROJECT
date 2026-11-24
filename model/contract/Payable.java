package com.thapar.sportssphere.model.contract;

/** Anything that owes a registration fee. Lets fee logic be polymorphic. */
public interface Payable {
    double calculateFee();
}
