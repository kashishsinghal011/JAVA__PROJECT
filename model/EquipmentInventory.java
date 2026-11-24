package com.thapar.sportssphere.model;

import com.thapar.sportssphere.exception.EquipmentNotIssuedException;
import com.thapar.sportssphere.exception.InsufficientEquipmentException;
import com.thapar.sportssphere.exception.InvalidOperationException;
import com.thapar.sportssphere.model.contract.Reportable;
import com.thapar.sportssphere.model.enums.EquipmentCondition;
import com.thapar.sportssphere.util.Validator;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Stock ledger. COMPOSITION: owns its Equipment items.
 * items: LinkedHashMap keeps insertion order for reports while giving O(1) lookup by id.
 * issuedTo: holderId -> (equipmentId -> quantity), so a return can be checked per holder.
 */
public class EquipmentInventory implements Serializable, Reportable {
    private static final long serialVersionUID = 1L;

    private final Map<String, Equipment> items = new LinkedHashMap<>();
    private final Map<String, Map<String, Integer>> issuedTo = new HashMap<>();

    public void addEquipment(Equipment equipment) throws InvalidOperationException {
        Validator.requireNonNull(equipment, "Equipment");
        if (items.containsKey(equipment.getEquipmentId())) {
            throw new InvalidOperationException("equipment " + equipment.getEquipmentId() + " already exists");
        }
        items.put(equipment.getEquipmentId(), equipment);
    }

    /**
     * Persistence support: inserts an already-built Equipment row while loading saved
     * data, preserving its exact stock counters (which the constructor cannot express).
     * The saved state is trusted to be consistent because it can only come from a
     * domain that enforces available <= total.
     */
    public void restoreItem(Equipment equipment) throws InvalidOperationException {
        Validator.requireNonNull(equipment, "Equipment");
        if (items.containsKey(equipment.getEquipmentId())) {
            throw new InvalidOperationException("equipment " + equipment.getEquipmentId() + " already exists");
        }
        items.put(equipment.getEquipmentId(), equipment);
    }

    public Equipment find(String equipmentId) throws InvalidOperationException {
        Equipment e = items.get(equipmentId);
        if (e == null) {
            throw new InvalidOperationException("unknown equipment id " + equipmentId);
        }
        return e;
    }

    /** Stock check happens inside Equipment.issue, so the ledger is only updated on success. */
    public void issue(String equipmentId, String holderId, int quantity)
            throws InsufficientEquipmentException, InvalidOperationException {
        Validator.requireNonBlank(holderId, "Holder id");
        Equipment e = find(equipmentId);
        e.issue(quantity);
        issuedTo.computeIfAbsent(holderId, k -> new HashMap<>()).merge(equipmentId, quantity, Integer::sum);
    }

    /** Validates against what THIS holder was issued, then restores stock and updates the ledger. */
    public void returnItem(String equipmentId, String holderId, int quantity, EquipmentCondition condition)
            throws EquipmentNotIssuedException, InvalidOperationException {
        Validator.requirePositive(quantity, "Quantity");
        Validator.requireNonNull(condition, "Condition");
        Equipment e = find(equipmentId);
        int issued = getIssuedQuantity(equipmentId, holderId);
        if (issued == 0 || quantity > issued) {
            throw new EquipmentNotIssuedException(e.getName(), holderId, quantity, issued);
        }
        e.restore(quantity, condition);
        Map<String, Integer> byItem = issuedTo.get(holderId);
        int remaining = issued - quantity;
        if (remaining == 0) {
            byItem.remove(equipmentId);
            if (byItem.isEmpty()) issuedTo.remove(holderId);
        } else {
            byItem.put(equipmentId, remaining);
        }
    }

    public int getIssuedQuantity(String equipmentId, String holderId) {
        Map<String, Integer> byItem = issuedTo.get(holderId);
        return byItem == null ? 0 : byItem.getOrDefault(equipmentId, 0);
    }

    public Collection<Equipment> getItems() { return Collections.unmodifiableCollection(items.values()); }

    /**
     * Read-only snapshot of the issuance ledger (holderId -> equipmentId -> quantity).
     * Used by persistence adapters; the internal map is never handed out mutable.
     */
    public Map<String, Map<String, Integer>> getIssuedLedger() {
        Map<String, Map<String, Integer>> copy = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, Integer>> e : issuedTo.entrySet()) {
            copy.put(e.getKey(), Collections.unmodifiableMap(new LinkedHashMap<>(e.getValue())));
        }
        return Collections.unmodifiableMap(copy);
    }

    /**
     * Persistence support: rebuilds one ledger row while loading saved data.
     * Validates that the item exists and that the restored quantities for that
     * item never exceed the stock actually issued, so a corrupted ledger cannot
     * put the inventory into an impossible state.
     */
    public void restoreIssuance(String equipmentId, String holderId, int quantity)
            throws InvalidOperationException {
        Validator.requireNonBlank(holderId, "Holder id");
        Validator.requirePositive(quantity, "Quantity");
        Equipment e = find(equipmentId);
        int alreadyLedgered = 0;
        for (Map<String, Integer> byItem : issuedTo.values()) {
            Integer q = byItem.get(equipmentId);
            if (q != null) alreadyLedgered += q;
        }
        if (alreadyLedgered + quantity > e.getIssuedCount()) {
            throw new InvalidOperationException("ledger for " + equipmentId + " (" + (alreadyLedgered + quantity)
                    + ") exceeds issued stock " + e.getIssuedCount());
        }
        issuedTo.computeIfAbsent(holderId, k -> new HashMap<>()).merge(equipmentId, quantity, Integer::sum);
    }

    @Override
    public String generateReport() {
        StringBuilder sb = new StringBuilder("EQUIPMENT AVAILABILITY\n");
        if (items.isEmpty()) sb.append("   (no equipment)\n");
        for (Equipment e : items.values()) {
            sb.append("   ").append(e).append('\n');
        }
        return sb.toString();
    }
}
