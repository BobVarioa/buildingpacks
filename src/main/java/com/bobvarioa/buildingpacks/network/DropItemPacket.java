package com.bobvarioa.buildingpacks.network;

import com.bobvarioa.buildingpacks.item.BlockPackItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record DropItemPacket(int amount) {
    public static DropItemPacket decode(FriendlyByteBuf buf) {
        return new DropItemPacket(buf.readInt());
    }

    public static void encode(DropItemPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.amount);
    }

    public static void onMessageReceived(final DropItemPacket message, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
        ctx.setPacketHandled(true);

        if (sideReceived != LogicalSide.SERVER) {
            return;
        }

        final ServerPlayer sendingPlayer = ctx.getSender();
        if (sendingPlayer == null) return;

        ctx.enqueueWork(() -> processMessage(message, sendingPlayer));
    }

    public static void processMessage(DropItemPacket packet, ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (player.getMainHandItem().getItem() instanceof BlockPackItem bpi) {
            bpi.dropItem(stack, player, packet.amount);
        }

    }
}
