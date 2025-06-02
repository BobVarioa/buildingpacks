package com.bobvarioa.buildingpacks.mixin.effortlessbuilding;

import com.bobvarioa.buildingpacks.BlockPack;
import com.bobvarioa.buildingpacks.item.BlockPackItem;
import com.bobvarioa.buildingpacks.utils.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import nl.requios.effortlessbuilding.capability.CapabilityHandler;
import nl.requios.effortlessbuilding.utilities.BlockEntry;
import nl.requios.effortlessbuilding.utilities.BlockPlacerHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockPlacerHelper.class)
public class BlockPlacerHelperMixin {
    @Inject(method = "breakBlock(Lnet/minecraft/world/entity/player/Player;Lnl/requios/effortlessbuilding/utilities/BlockEntry;)Z", at = @At("HEAD"), remap = false, cancellable = true)
    private static void breakBlock(Player player, BlockEntry blockEntry, CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = player.getMainHandItem();
        if (!player.isCreative() && !CapabilityHandler.canBreakFar(player) && stack.getItem() instanceof BlockPackItem) {
            BlockPack data = BlockPackItem.getData(stack);
            if (data == null) return;

            if (!(data.getBlockIndex(WorldUtils.getBlock(blockEntry.existingBlockState.getBlock())) > -1)) {
                cir.setReturnValue(false);
            }
        }
    }
}
