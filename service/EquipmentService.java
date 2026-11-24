package com.thapar.sportssphere.service;

import com.thapar.sportssphere.exception.EquipmentNotIssuedException;
import com.thapar.sportssphere.exception.InsufficientEquipmentException;
import com.thapar.sportssphere.exception.InvalidOperationException;
import com.thapar.sportssphere.model.Equipment;
import com.thapar.sportssphere.model.SportsData;
import com.thapar.sportssphere.model.enums.EquipmentCondition;
import com.thapar.sportssphere.util.IdGenerator;

/** Equipment stock set-up, issue and return. A holder is a player id or a team id. */
public class EquipmentService {
    private final SportsData data;

    public EquipmentService(SportsData data) { this.data = data; }

    public Equipment addEquipment(String name, int stock, EquipmentCondition condition) throws InvalidOperationException {
        Equipment e = new Equipment(IdGenerator.next("EQP"), name, stock, condition);
        data.getInventory().addEquipment(e);
        return e;
    }

    public void issue(String equipmentId, String holderId, int quantity)
            throws InsufficientEquipmentException, InvalidOperationException {
        String holder = resolveHolder(holderId);
        data.getInventory().issue(normalise(equipmentId), holder, quantity);
    }

    public void returnItem(String equipmentId, String holderId, int quantity, EquipmentCondition condition)
            throws EquipmentNotIssuedException, InvalidOperationException {
        String holder = resolveHolder(holderId);
        data.getInventory().returnItem(normalise(equipmentId), holder, quantity, condition);
    }

    private static String normalise(String id) { return id == null ? "" : id.trim().toUpperCase(); }

    /** A holder must exist; this stops stock being issued to typos that could never return it. */
    private String resolveHolder(String holderId) throws InvalidOperationException {
        String id = normalise(holderId);
        try {
            return data.findPlayer(id).getId();
        } catch (com.thapar.sportssphere.exception.PlayerNotFoundException notAPlayer) {
            return data.findTeam(id).getTeamId();   // throws InvalidOperationException if not a team either
        }
    }
}
