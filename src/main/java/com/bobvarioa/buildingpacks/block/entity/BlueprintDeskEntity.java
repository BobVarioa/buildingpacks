package com.bobvarioa.buildingpacks.block.entity;

import com.bobvarioa.buildingpacks.capabilty.BuildingPower;
import com.bobvarioa.buildingpacks.register.ModBlockEntities;
import com.bobvarioa.buildingpacks.register.ModCaps;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlueprintDeskEntity extends BlockEntity {
    public List<BlockPos> markerList = new ArrayList<>();
    public AABB range = null;
    public Set<Player> draftingPlayers = new HashSet<>();

    public BlueprintDeskEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.BLUEPRINT_DESK.get(), pPos, pBlockState);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        var list = new ListTag();
        for (var pos : markerList) {
            list.add(NbtUtils.writeBlockPos(pos));
        }
        tag.put("markers",list);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        var list = pTag.getList("markers", Tag.TAG_COMPOUND);
        var markerList = new ArrayList<BlockPos>();
        for (int i = 0; i < list.size(); i++) {
            markerList.add(NbtUtils.readBlockPos(list.getCompound(i)));
        }
        this.markerList = markerList;
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

    public void recalculateAABB() {

    }

    public static void tick(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (blockEntity instanceof BlueprintDeskEntity be) {
            for (var player : be.draftingPlayers) {
                if (player.isRemoved()) {
                    be.draftingPlayers.remove(player);
                    var p = level.getPlayerByUUID(player.getUUID());
                    if (p == null) continue;
                    player = p;
                }

                if (be.range != null && !be.range.contains(player.position())) {
                    var cap = player.getCapability(ModCaps.BUILDING_POWERS);
                    if (cap.isPresent()) {
                        var powers = cap.resolve().get();
                        powers.setPower(BuildingPower.DRAFTING_META, false);
                        if (!player.isCreative()) {
                            player.getAbilities().flying = false;
                            player.getAbilities().mayfly = false;
                            player.onUpdateAbilities();
                        }
                    }
                }
            }
        }
    }
}
