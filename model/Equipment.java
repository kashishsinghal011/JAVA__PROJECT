package com.thapar.sportssphere.model;

import com.thapar.sportssphere.exception.InsufficientEquipmentException;
import com.thapar.sportssphere.model.enums.EquipmentCondition;
import com.thapar.sportssphere.util.Validator;

import java.io.Serializable;

/** One kind of equipment (e.g. "Football size 5") with stock counts. */
public class Equipment implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String equipmentId;
    private final String name;
    private int totalStock;
    private int available;
    private EquipmentCondition condition;

    public Equipment(String equipmentId, String name, int totalStock, EquipmentCondition condition) {
        this.equipmentId = Validator.requireNonBlank(equipmentId, "Equipment id");
        this.name = Validator.requireNonBlank(name, "Equipment name");
        this.totalStock = Validator.requireNonNegative(totalStock, "Stock");
        this.available = totalStock;
        this.condition = Validator.requireNonNull(condition, "Condition");
    }

    /** Reduces available stock. Nothing changes if the request is too large. */
    public void issue(int quantity) throws InsufficientEquipmentException {
        Validator.requirePositive(quantity, "Quantity");
        if (quantity > available) {
            throw new InsufficientEquipmentException(name, quantity, available);
        }
        available -= quantity;
    }

    /**
     * Puts items back. The inventory guarantees quantity never exceeds what was issued, so
     * overflow here is a programming error (IllegalStateException), not a user error.
     */
    public void restore(int quantity, EquipmentCondition returnedCondition) {
        Validator.requirePositive(quantity, "Quantity");
        if (available + quantity > totalStock) {
            throw new IllegalStateException("Restoring " + quantity + " would exceed total stock of " + name);
        }
        available += quantity;
        // Track the worst condition seen: enum order is best -> worst.
        if (returnedCondition.ordinal() > condition.ordinal()) {
            condition = returnedCondition;
        }
    }

    public void addStock(int quantity) {
        Validator.requirePositive(quantity, "Quantity");
        totalStock += quantity;
        available += quantity;
    }

    public int getIssuedCount() { return totalStock - available; }
    public String getEquipmentId() { return equipmentId; }
    public String getName() { return name; }
    public int getTotalStock() { return totalStock; }
    public int getAvailable() { return available; }
    public EquipmentCondition getCondition() { return condition; }

    @Override
    public String toString() {
        return String.format("%s %-22s available %d/%d  condition %s", equipmentId, name, available, totalStock, condition);
    }
}
