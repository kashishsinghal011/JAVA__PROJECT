package com.thapar.sportssphere.web.persistence;

import jakarta.persistence.*;

/**
 * One row per (holder, equipment, quantity). The domain EquipmentInventory
 * keeps an
 * in-memory map holderId -> (equipmentId -> qty); this table is its durable
 * form.
 */
@Entity
@Table(name = "equipment_issued", uniqueConstraints = @UniqueConstraint(columnNames = { "holder_id", "equipment_id" }))
public class EquipmentIssuedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "holder_id", nullable = false, length = 32)
    private String holderId;

    @Column(name = "equipment_id", nullable = false, length = 32)
    private String equipmentId;

    @Column(nullable = false)
    private int quantity;

    public EquipmentIssuedEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHolderId() {
        return holderId;
    }

    public void setHolderId(String holderId) {
        this.holderId = holderId;
    }

    public String getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(String equipmentId) {
        this.equipmentId = equipmentId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
