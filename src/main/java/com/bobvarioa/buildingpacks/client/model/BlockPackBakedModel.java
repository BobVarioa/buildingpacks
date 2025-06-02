package com.bobvarioa.buildingpacks.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.client.model.BakedModelWrapper;

public class BlockPackBakedModel extends BakedModelWrapper<BakedModel> {

    public BlockPackBakedModel(BakedModel originalModel) {
        super(originalModel);
    }

    @Override
    public boolean isCustomRenderer() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public BakedModel applyTransform(ItemDisplayContext displayContext, PoseStack poseStack, boolean leftHand) {
        if (displayContext == ItemDisplayContext.GROUND || displayContext == ItemDisplayContext.FIXED) {
            return super.applyTransform(displayContext, poseStack, leftHand);
        }
        return this;
    }
}
