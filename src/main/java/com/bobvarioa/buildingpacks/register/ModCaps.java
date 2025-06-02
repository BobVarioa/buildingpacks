package com.bobvarioa.buildingpacks.register;

import com.bobvarioa.buildingpacks.capabilty.IBuildingPowersHandler;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class ModCaps {
    public static final Capability<IBuildingPowersHandler> BUILDING_POWERS = CapabilityManager.get(new CapabilityToken<>(){});

}
