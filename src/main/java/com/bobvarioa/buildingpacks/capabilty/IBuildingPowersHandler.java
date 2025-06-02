package com.bobvarioa.buildingpacks.capabilty;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

import static com.bobvarioa.buildingpacks.BuildingPacks.MODID;

@AutoRegisterCapability
public interface IBuildingPowersHandler {
    ResourceLocation ID = new ResourceLocation(MODID, "building_powers");

    boolean hasPower(BuildingPower power);
    void setPower(BuildingPower power, boolean enabled);

}
