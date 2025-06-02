package com.bobvarioa.buildingpacks.mixin.create;

import com.bobvarioa.buildingpacks.item.BlockPackItem;
import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementOffset;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.annotation.Target;
import java.util.function.Predicate;

@Mixin(value = IPlacementHelper.class)
public interface IPlacementHelperMixin {
    @Shadow  Predicate<ItemStack> getItemPredicate();
    @Shadow  PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos, BlockHitResult ray);

    /**
     * @author Bob Varioa
     * @reason cannot inject into default interface methods
     */
    @Overwrite(remap = false)
    default boolean matchesItem(ItemStack item) {
        if (item.getItem() instanceof BlockPackItem) {
            var block = BlockPackItem.getSelectedBlock(item);
            if (getItemPredicate().test(block.asItem().getDefaultInstance())) {
                return true;
            }
        }
        return getItemPredicate().test(item);
    }

    /**
     * @author Bob Varioa
     * @reason cannot inject into default interface methods
     */
    @Overwrite(remap = false)
    default PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos, BlockHitResult ray, ItemStack heldItem) {
        PlacementOffset offset = getOffset(player, world, state, pos, ray);
        if (heldItem.getItem() instanceof BlockPackItem) {
            var block = BlockPackItem.getSelectedBlock(heldItem);
            if (getItemPredicate().test(block.asItem().getDefaultInstance())) {
                offset = offset.withGhostState(block.defaultBlockState());
            }
        } else if (heldItem.getItem() instanceof BlockItem blockItem) {
            offset = offset.withGhostState(blockItem.getBlock().defaultBlockState());
        }
        return offset;
    }
}
