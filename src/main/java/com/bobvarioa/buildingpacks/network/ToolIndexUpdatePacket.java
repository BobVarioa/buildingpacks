package com.bobvarioa.buildingpacks.network;

import com.bobvarioa.buildingpacks.item.BlockPackItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ToolIndexUpdatePacket(int index, int inventoryIndex) {
    public static ToolIndexUpdatePacket decode(FriendlyByteBuf buf) {
        return new ToolIndexUpdatePacket(buf.readInt(), buf.readInt());
    }

    public static void encode(ToolIndexUpdatePacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.index);
        buf.writeInt(packet.inventoryIndex);
    }

    public static void onMessageReceived(final ToolIndexUpdatePacket message, Supplier<NetworkEvent.Context> ctxSupplier) {
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

    public static void processMessage(ToolIndexUpdatePacket packet, ServerPlayer player) {
        ItemStack stack = player.getInventory().getItem(packet.inventoryIndex);
        if (stack.getItem() instanceof BlockPackItem) {
            CompoundTag tag = stack.getOrCreateTag();
            tag.putInt("toolIndex", packet.index);
        }
    }
}
