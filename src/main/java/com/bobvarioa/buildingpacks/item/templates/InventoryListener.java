package com.bobvarioa.buildingpacks.item.templates;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

public interface InventoryListener {
    default void enteredInventory(Player player) {};

    default void leftInventory(Player player) {};

    static void onEnterInventory(PlayerEvent.ItemPickupEvent event) {
        ItemStack stack = event.getStack();
        if (!event.isCanceled() && !event.getOriginalEntity().isRemoved() && !stack.isEmpty() && stack.getItem() instanceof InventoryListener listener) {
            listener.enteredInventory(event.getEntity());
        }
    }

    static void onPlayerClone(PlayerEvent.Clone event) {
        for (var item : event.getEntity().getInventory().items) {
            if (item.getItem() instanceof InventoryListener listener) {
                listener.enteredInventory(event.getEntity());
            }
        }
    }

    static void onLeaveInventory(ItemTossEvent event) {
        ItemStack stack = event.getEntity().getItem();
        if (!event.isCanceled() && !event.getEntity().isRemoved() && !stack.isEmpty() && stack.getItem() instanceof InventoryListener listener) {
            listener.leftInventory(event.getPlayer());
        }
    }
}
