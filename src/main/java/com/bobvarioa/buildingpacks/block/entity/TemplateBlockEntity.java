package com.bobvarioa.buildingpacks.block.entity;

import com.bobvarioa.buildingpacks.register.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class TemplateBlockEntity extends BlockEntity {
    public BlockState blockState = Blocks.AIR.defaultBlockState();

    public TemplateBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.TEMPLATE_BLOCK.get(), pPos, pBlockState);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!blockState.is(Blocks.AIR)) {
            tag.put("block", NbtUtils.writeBlockState(blockState));
            System.out.println("save");
        }
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        blockState = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), pTag.getCompound("block"));
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        super.onDataPacket(net, pkt);
        CompoundTag tag = pkt.getTag();
        if (tag == null) {
            tag = new CompoundTag();
        }
        load(tag);
    }
}
