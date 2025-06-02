package com.bobvarioa.buildingpacks.item;

import com.bobvarioa.buildingpacks.block.entity.TemplateBlockEntity;
import com.bobvarioa.buildingpacks.capabilty.BuildingPower;
import com.bobvarioa.buildingpacks.capabilty.BuildingPowerItem;
import com.bobvarioa.buildingpacks.register.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class DraftingPencil extends BlockItem implements BuildingPowerItem {
    public DraftingPencil(Properties properties) {
        super(ModBlocks.TEMPLATE_BLOCK.get(), properties);
    }

    @Override
    public Component getName(ItemStack pStack) {
        ResourceLocation id = ResourceLocation.tryParse(pStack.getOrCreateTag().getString("id"));
        if (id == null) return Component.translatable("item.buildingpacks.pack");
        return Component.translatable("item.buildingpacks.drafting_pencil");
    }

    @Override
    public InteractionResult place(BlockPlaceContext pContext) {
        if (!pContext.canPlace()) {
            return InteractionResult.FAIL;
        }
        BlockPlaceContext blockplacecontext = this.updatePlacementContext(pContext);
        if (blockplacecontext != null) {
            Player player = pContext.getPlayer();
            if (player != null) {
                var stack = player.getItemInHand(InteractionHand.OFF_HAND);

                if (stack.getItem() instanceof BlockItem bi) {
                    Block block = bi.getBlock();
                    BlockState blockstate = block.getStateForPlacement(pContext);
                    BlockState templateBlock = ModBlocks.TEMPLATE_BLOCK.get().defaultBlockState();
                    Level level = pContext.getLevel();
                    BlockPos pos = pContext.getClickedPos();
                    if (blockstate != null && this.canPlace(pContext, blockstate)) {
                        if (!this.placeBlock(pContext, templateBlock)) {
                            return InteractionResult.FAIL;
                        } else {
                            if (level.getExistingBlockEntity(pos) instanceof TemplateBlockEntity be) {
                                be.blockState = blockstate;
                                be.setChanged();
                            }
                        }

                        return InteractionResult.SUCCESS;
                    }
                }
            }

            return InteractionResult.FAIL;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        return super.onEntityItemUpdate(stack, entity);
    }

    @Override
    public BuildingPower getPower() {
        return BuildingPower.DRAFTING_PLACE;
    }
}
