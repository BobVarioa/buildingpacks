package com.bobvarioa.buildingpacks.item;

import com.bobvarioa.buildingpacks.capabilty.BuildingPower;
import com.bobvarioa.buildingpacks.item.templates.InventoryListener;
import com.bobvarioa.buildingpacks.register.ModCaps;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public class WaxedPaintbrush extends Item implements InventoryListener {
    public WaxedPaintbrush(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void enteredInventory(Player player) {
        var cap = player.getCapability(ModCaps.BUILDING_POWERS).resolve();
        if (!cap.isEmpty()) {
            var powersHandler = cap.get();
            powersHandler.setPower(BuildingPower.WAX_ADD, true);
            powersHandler.setPower(BuildingPower.WAX_REMOVE, true);
        }
    }

    @Override
    public void leftInventory(Player player) {
        var cap = player.getCapability(ModCaps.BUILDING_POWERS).resolve();
        if (!cap.isEmpty()) {
            var powersHandler = cap.get();
            powersHandler.setPower(BuildingPower.WAX_ADD, false);
            powersHandler.setPower(BuildingPower.WAX_REMOVE, false);
        }
    }
}
