package com.bobvarioa.buildingpacks.utils;

import com.bobvarioa.buildingpacks.block.entity.TemplateBlockEntity;
import com.bobvarioa.buildingpacks.register.ModBlocks;
import com.firemerald.additionalplacements.block.AdditionalPlacementBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.ModList;

public class WorldUtils {
    public static Block getRealBlock(Level level, BlockPos pos, Block block) {
        if (block.equals(ModBlocks.TEMPLATE_BLOCK.get())) {
            if (level.getBlockEntity(pos) instanceof TemplateBlockEntity be) {
                block = be.blockState.getBlock();
            }
        }
        return getBlock(block);
    }

    public static Block getBlock(Block block) {
        if (ModList.get().isLoaded("additionalplacements")) {
            if (block instanceof AdditionalPlacementBlock<?> apb) {
                block = apb.parentBlock;
            }
        }
        // to handle stuff like signs where they are secretly two blocks
        if (block.asItem() instanceof BlockItem bi) {
            block = bi.getBlock();
        }
        return block;
    }
}
