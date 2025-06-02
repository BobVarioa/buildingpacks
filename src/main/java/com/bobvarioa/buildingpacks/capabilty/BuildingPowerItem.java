package com.bobvarioa.buildingpacks.capabilty;

import com.bobvarioa.buildingpacks.item.templates.InventoryListener;
import com.bobvarioa.buildingpacks.register.ModCaps;
import net.minecraft.world.entity.player.Player;

public interface BuildingPowerItem extends InventoryListener {
    BuildingPower getPower();

    default void enteredInventory(Player player) {
        var cap = player.getCapability(ModCaps.BUILDING_POWERS).resolve();
        if (!cap.isEmpty()) {
            var powersHandler = cap.get();
            powersHandler.setPower(getPower(), true);
        }
    }

    default void leftInventory(Player player) {
        var cap = player.getCapability(ModCaps.BUILDING_POWERS).resolve();
        if (!cap.isEmpty()) {
            var powersHandler = cap.get();
            powersHandler.setPower(getPower(), false);
        }
    }
}
