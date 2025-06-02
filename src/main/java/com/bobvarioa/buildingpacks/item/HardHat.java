package com.bobvarioa.buildingpacks.item;

import com.bobvarioa.buildingpacks.capabilty.BuildingPower;
import com.bobvarioa.buildingpacks.client.renderer.BlockPackRenderer;
import com.bobvarioa.buildingpacks.item.templates.InventoryListener;
import com.bobvarioa.buildingpacks.register.ModCaps;
import com.bobvarioa.buildingpacks.register.ModItems;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class HardHat extends ArmorItem implements InventoryListener, Equipable {
    public static class HardHatMaterial implements ArmorMaterial {

        @Override
        public int getDurabilityForType(Type pType) {
            return -1;
        }

        @Override
        public int getDefenseForType(Type pType) {
            return 1;
        }

        @Override
        public int getEnchantmentValue() {
            return 0;
        }

        @Override
        public SoundEvent getEquipSound() {
            return SoundEvents.ARMOR_EQUIP_GENERIC;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.EMPTY;
        }

        @Override
        public String getName() {
            return "hard_hat";
        }

        @Override
        public float getToughness() {
            return 0;
        }

        @Override
        public float getKnockbackResistance() {
            return 0.1f;
        }
    }


    public HardHat(Properties pProperties) {
        super(new HardHatMaterial(), Type.HELMET, pProperties);
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.HEAD;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int pSlotId, boolean pIsSelected) {
        super.inventoryTick(stack, level, entity, pSlotId, pIsSelected);
        if (stack.is(ModItems.HARD_HAT.get()) && entity instanceof Player player) {
            var cap = player.getCapability(ModCaps.BUILDING_POWERS).resolve();
            if (!cap.isEmpty()) {
                var powersHandler = cap.get();
                powersHandler.setPower(BuildingPower.DRAFTING_SEE, pSlotId == EquipmentSlot.HEAD.getIndex());
            }
        }
    }

    @Override
    public void leftInventory(Player player) {
        var cap = player.getCapability(ModCaps.BUILDING_POWERS).resolve();
        if (!cap.isEmpty()) {
            var powersHandler = cap.get();
            powersHandler.setPower(BuildingPower.DRAFTING_SEE, false);
        }
    }

    @Override
    public @Nullable String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return "buildingpacks:textures/models/armor/hard_hat_layer_1.png";
    }
}
