package com.bobvarioa.buildingpacks;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.ResourceLocation;

import static com.bobvarioa.buildingpacks.BuildingPacks.BLOCK_PACK;
import static com.bobvarioa.buildingpacks.BuildingPacks.MODID;

@JeiPlugin
public class BlockPackJEIPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(MODID, "jei_plugin");
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(BLOCK_PACK.get(), (ingredient, context) -> {
            return ingredient.getOrCreateTag().getString("id");
        });
    }
}
