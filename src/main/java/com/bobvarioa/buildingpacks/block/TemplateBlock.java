package com.bobvarioa.buildingpacks.block;

import com.bobvarioa.buildingpacks.register.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class TemplateBlock extends Block implements EntityBlock {
    public TemplateBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return ModBlockEntities.TEMPLATE_BLOCK.get().create(pPos, pState);
    }


}
