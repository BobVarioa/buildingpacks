package com.bobvarioa.buildingpacks.item;

import com.bobvarioa.buildingpacks.capabilty.BuildingPower;
import com.bobvarioa.buildingpacks.capabilty.BuildingPowerItem;
import net.minecraft.world.item.Item;

public class SteelWool extends Item implements BuildingPowerItem {
    public SteelWool(Properties pProperties) {
        super(pProperties);
    }


    @Override
    public BuildingPower getPower() {
        return BuildingPower.PATINA_REMOVE;
    }
}
