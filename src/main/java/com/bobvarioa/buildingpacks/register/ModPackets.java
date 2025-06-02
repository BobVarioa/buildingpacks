package com.bobvarioa.buildingpacks.register;

import com.bobvarioa.buildingpacks.network.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import static com.bobvarioa.buildingpacks.BuildingPacks.MODID;

public class ModPackets {
    private static final String PROTOCOL_VERSION = "1";
    public static SimpleChannel INSTANCE;

    public static void commonSetup(FMLCommonSetupEvent event) {
        INSTANCE = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(MODID, "main"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
        );

        INSTANCE.registerMessage(100, IndexUpdatePacket.class, IndexUpdatePacket::encode, IndexUpdatePacket::decode, IndexUpdatePacket::onMessageReceived);
        INSTANCE.registerMessage(101, BreakBlockPacket.class, BreakBlockPacket::encode, BreakBlockPacket::decode, BreakBlockPacket::onMessageReceived);
        INSTANCE.registerMessage(102, DropItemPacket.class, DropItemPacket::encode, DropItemPacket::decode, DropItemPacket::onMessageReceived);
        INSTANCE.registerMessage(103, PowerUpdatePacket.class, PowerUpdatePacket::encode, PowerUpdatePacket::decode, PowerUpdatePacket::onMessageReceived);
        INSTANCE.registerMessage(104, ToolIndexUpdatePacket.class, ToolIndexUpdatePacket::encode, ToolIndexUpdatePacket::decode, ToolIndexUpdatePacket::onMessageReceived);
    }
}