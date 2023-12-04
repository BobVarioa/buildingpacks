package com.bobvarioa.buildingpacks;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

import static com.bobvarioa.buildingpacks.BuildingPacks.MODID;

public class BlockPackPacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static SimpleChannel INSTANCE;

    public record IndexUpdatePacket(int index) {
        public static IndexUpdatePacket decode(FriendlyByteBuf buf) {
            return new IndexUpdatePacket(buf.readInt());
        }

        public static void encode(IndexUpdatePacket packet, FriendlyByteBuf buf) {
            buf.writeInt(packet.index);
        }
    }

    public static void commonSetup(FMLCommonSetupEvent event) {
        INSTANCE = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(MODID, "main"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
        );

        INSTANCE.registerMessage(100, IndexUpdatePacket.class, IndexUpdatePacket::encode, IndexUpdatePacket::decode, BlockPackPacketHandler::onMessageReceived);
    }

    public static void onMessageReceived(final IndexUpdatePacket message, Supplier<NetworkEvent.Context> ctxSupplier) {
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

    public static void processMessage(IndexUpdatePacket packet, ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("index", packet.index);
    }

}
