package com.bobvarioa.buildingpacks.item.templates;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface BlockItemExtensions{
    boolean buildingpacks$placeBlock(BlockPlaceContext pContext, BlockState pState);
    SoundEvent buildingpacks$getPlaceSound(BlockState state, Level world, BlockPos pos, Player entity);
    BlockState buildingpacks$getPlacementState(BlockPlaceContext pContext);
    boolean buildingpacks$canPlace(BlockPlaceContext pContext, BlockState pState);
}
