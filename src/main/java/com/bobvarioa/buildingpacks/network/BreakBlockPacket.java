package com.bobvarioa.buildingpacks.network;

import com.bobvarioa.buildingpacks.item.BlockPackItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record  BreakBlockPacket(BlockPos pos, float price) {
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
            Level level = player.level();
            if (level instanceof ServerLevel serverLevel) {
                bpi.addMaterial(stack, packet.price);
                destroyBlock(serverLevel, packet.pos, false, player, 512);
            }
        }

    }

    // turns out there's not a good way to destroy a block on the server and the client without triggering two sound events, so reimplementation of vanilla's code
    public static boolean destroyBlock(ServerLevel level, BlockPos pPos, boolean pDropBlock, ServerPlayer player, int pRecursionLeft) {
        BlockState blockstate = level.getBlockState(pPos);
        if (blockstate.isAir()) {
            return false;
        } else {
            FluidState fluidstate = level.getFluidState(pPos);
            if (!(blockstate.getBlock() instanceof BaseFireBlock)) {
                // key change from vanilla logic, just added the player field here
                level.levelEvent(player, 2001, pPos, Block.getId(blockstate));
            }

            if (pDropBlock) {
                BlockEntity blockentity = blockstate.hasBlockEntity() ? level.getBlockEntity(pPos) : null;
                Block.dropResources(blockstate, level, pPos, blockentity, player, ItemStack.EMPTY);
            }

            boolean flag = level.setBlock(pPos, fluidstate.createLegacyBlock(), 3, pRecursionLeft);
            if (flag) {
                level.gameEvent(GameEvent.BLOCK_DESTROY, pPos, GameEvent.Context.of(player, blockstate));
            }

            return flag;
        }
    }
}
