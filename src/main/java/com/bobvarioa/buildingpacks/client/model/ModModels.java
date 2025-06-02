package com.bobvarioa.buildingpacks.client.model;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

import static com.bobvarioa.buildingpacks.BuildingPacks.MODID;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModModels {
    public static final ResourceLocation toolboxRegModel = new ResourceLocation(MODID, "item/toolbox_reg");
    public static final ResourceLocation toolboxMedModel = new ResourceLocation(MODID, "item/toolbox_med");
    public static final ResourceLocation toolboxBigModel = new ResourceLocation(MODID, "item/toolbox_big");

    @SubscribeEvent
    public static void onModelBake(ModelEvent.ModifyBakingResult event) {
        Map<ResourceLocation, BakedModel> models = event.getModels();
        ModelResourceLocation location = new ModelResourceLocation(MODID, "block_pack", "inventory");
        ModelResourceLocation medLocation = new ModelResourceLocation(MODID, "med_block_pack", "inventory");
        ModelResourceLocation bigLocation = new ModelResourceLocation(MODID, "big_block_pack", "inventory");
        models.put(location, new BlockPackBakedModel(models.get(toolboxRegModel)));
        models.put(medLocation, new BlockPackBakedModel(models.get(toolboxMedModel)));
        models.put(bigLocation, new BlockPackBakedModel(models.get(toolboxBigModel)));
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(toolboxRegModel);
        event.register(toolboxMedModel);
        event.register(toolboxBigModel);
    }
}
