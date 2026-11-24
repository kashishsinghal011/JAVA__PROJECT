package com.thapar.sportssphere.exception;

/** Return of equipment that was not issued (or more than was issued). */
public class EquipmentNotIssuedException extends EquipmentException {
    private static final long serialVersionUID = 1L;

    public EquipmentNotIssuedException(String item, String holder, int returning, int issued) {
        super("Cannot return " + returning + " x " + item + " for " + holder + "; only " + issued + " issued.");
    }
}
