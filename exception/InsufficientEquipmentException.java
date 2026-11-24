package com.thapar.sportssphere.exception;

/** Requested quantity exceeds available stock. */
public class InsufficientEquipmentException extends EquipmentException {
    private static final long serialVersionUID = 1L;

    public InsufficientEquipmentException(String item, int requested, int available) {
        super("Cannot issue " + requested + " x " + item + "; only " + available + " available.");
    }
}
