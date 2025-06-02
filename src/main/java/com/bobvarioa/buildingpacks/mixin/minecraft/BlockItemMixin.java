package com.bobvarioa.buildingpacks.mixin.minecraft;

import com.bobvarioa.buildingpacks.item.templates.BlockItemExtensions;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin implements BlockItemExtensions {

    @Shadow
    protected abstract boolean placeBlock(BlockPlaceContext pContext, BlockState pState);

    @Shadow protected abstract SoundEvent getPlaceSound(BlockState state, Level world, BlockPos pos, Player entity);

    @Shadow @Nullable protected abstract BlockState getPlacementState(BlockPlaceContext pContext);

    @Shadow protected abstract boolean canPlace(BlockPlaceContext pContext, BlockState pState);

    @Override
    public boolean buildingpacks$placeBlock(BlockPlaceContext pContext, BlockState pState) {
        return this.placeBlock(pContext, pState);
    }

    @Override
    public SoundEvent buildingpacks$getPlaceSound(BlockState state, Level world, BlockPos pos, Player entity) {
        return this.getPlaceSound(state, world, pos, entity);
    }

    @Override
    public BlockState buildingpacks$getPlacementState(BlockPlaceContext pContext) {
        return this.getPlacementState(pContext);
    }

    @Override
    public boolean buildingpacks$canPlace(BlockPlaceContext pContext, BlockState pState) {
        return this.canPlace(pContext, pState);
    }


}
