package com.bobvarioa.buildingpacks.network;

import com.bobvarioa.buildingpacks.item.BlockPackItem;
import net.minecraft.core.BlockPos;
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

    public record BreakBlockPacket(BlockPos pos, float price) {
        public static BreakBlockPacket decode(FriendlyByteBuf buf) {
            return new BreakBlockPacket(buf.readBlockPos(), buf.readFloat());
        }

        public static void encode(BreakBlockPacket packet, FriendlyByteBuf buf) {
            buf.writeBlockPos(packet.pos);
            buf.writeFloat(packet.price);
        }

        public static void onMessageReceived(final BreakBlockPacket message, Supplier<NetworkEvent.Context> ctxSupplier) {
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

        public static void processMessage(BreakBlockPacket packet, ServerPlayer player) {
            ItemStack stack = player.getMainHandItem();
            if (player.getMainHandItem().getItem() instanceof BlockPackItem bpi) {
                var level = player.level();
                level.destroyBlock(packet.pos, false);
                bpi.addMaterial(stack, packet.price);
            }

        }
    }

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
    }



}
