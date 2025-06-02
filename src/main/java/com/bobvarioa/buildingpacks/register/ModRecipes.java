package com.bobvarioa.buildingpacks.register;

import com.bobvarioa.buildingpacks.BuildingPacks;
import com.bobvarioa.buildingpacks.recipe.BuildingPackUpgradeRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {
    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, BuildingPacks.MODID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, BuildingPacks.MODID);

    public static final RegistryObject<RecipeType<BuildingPackUpgradeRecipe>> UPGRADE = RECIPE_TYPES.register("upgrade", BuildingPackUpgradeRecipe.Type::new);
    public static final RegistryObject<RecipeSerializer<BuildingPackUpgradeRecipe>> UPGRADE_SERIALIZER = RECIPE_SERIALIZERS.register("upgrade", BuildingPackUpgradeRecipe.Serializer::new);

    public static void register(IEventBus bus) {
        RECIPE_TYPES.register(bus);
        RECIPE_SERIALIZERS.register(bus);
    }

}
