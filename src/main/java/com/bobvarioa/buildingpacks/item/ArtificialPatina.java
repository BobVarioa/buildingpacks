package com.bobvarioa.buildingpacks.item;

import com.bobvarioa.buildingpacks.capabilty.BuildingPower;
import com.bobvarioa.buildingpacks.capabilty.BuildingPowerItem;
import net.minecraft.world.item.Item;

public class ArtificialPatina extends Item implements BuildingPowerItem {
    public ArtificialPatina(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public BuildingPower getPower() {
        return BuildingPower.PATINA_ADD;
    }
}
