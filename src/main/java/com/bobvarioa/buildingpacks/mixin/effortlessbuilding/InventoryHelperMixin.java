package com.bobvarioa.buildingpacks.mixin.effortlessbuilding;

import com.bobvarioa.buildingpacks.BlockPack;
import com.bobvarioa.buildingpacks.item.BlockPackItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import nl.requios.effortlessbuilding.utilities.InventoryHelper;
import org.checkerframework.checker.units.qual.A;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryHelper.class)
public class InventoryHelperMixin {

    @ModifyConstant(method = "removeFromInventory(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/Item;I)V", constant = @Constant(intValue = 0, ordinal = 0), remap = false)
    private static int removeFromInventory(int amountFound, Player player, Item item, int amount) {
        if (!player.isCreative()) {
            Block block;
            if (item instanceof BlockItem bi) {
                block = bi.getBlock();
            } else return amountFound;

            int amountFoundNew = amountFound;
            int preferredSlot = player.getInventory().selected;
            ItemStack itemstack = player.getInventory().getItem(preferredSlot);
            if (itemstack.getItem() instanceof BlockPackItem bpi) {
                BlockPack data = BlockPackItem.getData(itemstack);
                if (data == null) return 0;
                var price = data.getPrice(block);
                var mat = bpi.getMaterial(itemstack);
                int i = 1;
                for (; i < amount && price * i < mat; i++) {}
                if (price * i > mat) i --;
                bpi.addMaterial(itemstack, -(price * i));
                amountFoundNew += i;
            }

            return amountFoundNew;
        }
        return amountFound;
    }

    @Inject(method = "findTotalItemsInInventory(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/Item;)I", at = @At("TAIL"), remap = false, cancellable = true)
    private static void findTotalItemsInInventory(Player player, Item item, CallbackInfoReturnable<Integer> cir) {
        int total = cir.getReturnValue();

        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.getItem() instanceof BlockPackItem bpi) {
                BlockPack data = BlockPackItem.getData(stack);
                if (data != null) {
                    Block block = BlockPackItem.getSelectedBlock(stack);
                    var price = data.getPrice(block);
                    if (block.asItem().equals(item)) {
                        total += (int) Math.floor(bpi.getMaterial(stack) / price);
                    }
                }
            }
        }

        cir.setReturnValue(total);
    }
}
