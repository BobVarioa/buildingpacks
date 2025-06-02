package com.bobvarioa.buildingpacks.mixin.create;

import com.bobvarioa.buildingpacks.item.BlockPackItem;
import net.createmod.catnip.placement.PlacementOffset;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = PlacementOffset.class, remap = false)
public class PlacementOffsetMixin {
    @Unique
    @Redirect(method = "placeInWorld(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/BlockItem;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BlockItem;getBlock()Lnet/minecraft/world/level/block/Block;"), remap = false)
    public Block placeInWorld(BlockItem instance) {
        if (instance instanceof BlockPackItem) {
            return null;
        }
        return null;
    }
}
