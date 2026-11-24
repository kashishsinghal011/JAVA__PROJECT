package com.thapar.sportssphere.web.persistence;

import com.thapar.sportssphere.model.enums.EquipmentCondition;
import jakarta.persistence.*;

/**
 * Equipment stock row. The issued-to ledger is stored as one row per
 * holder+item.
 */
@Entity
@Table(name = "equipment")
public class EquipmentEntity {

    @Id
    @Column(name = "equipment_id", length = 32)
    private String equipmentId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "total_stock", nullable = false)
    private int totalStock;

    @Column(nullable = false)
    private int available;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EquipmentCondition condition;

    public EquipmentEntity() {
    }

    public String getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(String equipmentId) {
        this.equipmentId = equipmentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTotalStock() {
        return totalStock;
    }

    public void setTotalStock(int totalStock) {
        this.totalStock = totalStock;
    }

    public int getAvailable() {
        return available;
    }

    public void setAvailable(int available) {
        this.available = available;
    }

    public EquipmentCondition getCondition() {
        return condition;
    }

    public void setCondition(EquipmentCondition condition) {
        this.condition = condition;
    }
}
