package com.bobvarioa.buildingpacks.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import static com.bobvarioa.buildingpacks.BuildingPacks.MODID;
import static com.bobvarioa.buildingpacks.register.ModItems.*;

@JeiPlugin
public class BlockPackJEIPlugin implements IModPlugin {
    private static String subtypeInterpret(ItemStack ingredient, UidContext context) {
        return ingredient.getOrCreateTag().getString("id");
    }

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(MODID, "jei_plugin");
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(REG_BLOCK_PACK.get(), BlockPackJEIPlugin::subtypeInterpret);
        registration.registerSubtypeInterpreter(MED_BLOCK_PACK.get(), BlockPackJEIPlugin::subtypeInterpret);
        registration.registerSubtypeInterpreter(BIG_BLOCK_PACK.get(), BlockPackJEIPlugin::subtypeInterpret);
    }
}
