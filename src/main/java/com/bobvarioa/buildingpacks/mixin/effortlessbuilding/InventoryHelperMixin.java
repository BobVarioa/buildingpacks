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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryHelper.class)
public class InventoryHelperMixin {

    @ModifyVariable(method = "removeFromInventory(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/Item;I)V", at=@At("STORE"), ordinal = 1, remap = false)
    private static int removeFromInventory(int amountFound, Player player, Item item, int amount) {
        if (!player.isCreative()) {
            Block block;
            if (item instanceof BlockItem bi) {
                block = bi.getBlock();
            } else return 0;

            int amountFoundNew = 0;
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
        return 0;
    }

    @Inject(method = "findTotalItemsInInventory(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/Item;)I", at = @At("TAIL"), remap = false, cancellable = true)
    private static void findTotalItemsInInventory(Player player, Item item, CallbackInfoReturnable<Integer> cir) {
        int total = cir.getReturnValue();

        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.getItem() instanceof BlockPackItem bpi) {
                BlockPack data = BlockPackItem.getData(stack);
                CompoundTag tag = stack.getTag();
                if (tag == null) continue;
                if (data == null) continue;
                var index = tag.getInt("index");
                Block block = data.getBlock(index);
                var price = data.getPrice(block);
                if (block.asItem().equals(item)) {
                    total += (int) Math.floor(bpi.getMaterial(stack) / price);
                }
            }
        }

        cir.setReturnValue(total);
    }
}
