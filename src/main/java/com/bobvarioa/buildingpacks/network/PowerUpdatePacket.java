package com.bobvarioa.buildingpacks.network;

import com.bobvarioa.buildingpacks.capabilty.BuildingPower;
import com.bobvarioa.buildingpacks.register.ModCaps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record PowerUpdatePacket(BuildingPower power, boolean state) {
    public static PowerUpdatePacket decode(FriendlyByteBuf buf) {
        return new PowerUpdatePacket(buf.readEnum(BuildingPower.class), buf.readBoolean());
    }

    public static void encode(PowerUpdatePacket packet, FriendlyByteBuf buf) {
        buf.writeEnum(packet.power());
        buf.writeBoolean(packet.state());
    }

    public static void onMessageReceived(final PowerUpdatePacket message, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
        ctx.setPacketHandled(true);

        if (sideReceived != LogicalSide.CLIENT) {
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        ctx.enqueueWork(() -> processMessage(message, player));
    }

    public static void processMessage(PowerUpdatePacket packet, LocalPlayer player) {
        var cap = player.getCapability(ModCaps.BUILDING_POWERS).resolve();
        if (!cap.isEmpty()) {
            var powersHandler = cap.get();
            powersHandler.setPower(packet.power, packet.state);
        }
    }
}
