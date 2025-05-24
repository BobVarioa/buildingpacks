package com.bobvarioa.buildingpacks.mixin.effortlessbuilding;

import com.bobvarioa.buildingpacks.BlockPack;
import com.bobvarioa.buildingpacks.item.BlockPackItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import nl.requios.effortlessbuilding.compatibility.CompatHelper;
import nl.requios.effortlessbuilding.item.AbstractRandomizerBagItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CompatHelper.class)
public class CompatHelperMixin {

    @Inject(method = "isItemBlockProxy(Lnet/minecraft/world/item/ItemStack;Z)Z", at = @At("HEAD"), remap = false, cancellable = true)
    private static void isItemBlockProxy(ItemStack stack, boolean seeBlockItemsAsProxies, CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof BlockPackItem) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getItemBlockFromStack(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;", at = @At("HEAD"), remap = false, cancellable = true)
    private static void getItemBlockFromStack(ItemStack proxy, CallbackInfoReturnable<ItemStack> cir) {
        if (proxy.getItem() instanceof BlockPackItem) {
            CompoundTag tag = proxy.getTag();
            if (tag == null) return;
            BlockPack data = BlockPackItem.getData(proxy);
            if (data == null) return;
            var index = tag.getInt("index");
            ItemStack stack = new ItemStack(data.getBlock(index).asItem());

            cir.setReturnValue(stack);
        }
    }


}
