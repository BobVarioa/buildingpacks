package com.bobvarioa.buildingpacks.client.renderer;

import com.bobvarioa.buildingpacks.BlockPack;
import com.bobvarioa.buildingpacks.client.model.BlockPackBakedModel;
import com.bobvarioa.buildingpacks.client.model.ModModels;
import com.bobvarioa.buildingpacks.item.BlockPackItem;
import com.bobvarioa.buildingpacks.register.ModItems;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

import static com.bobvarioa.buildingpacks.BuildingPacks.MODID;
import static net.minecraft.client.renderer.entity.ItemRenderer.getFoilBuffer;
import static net.minecraft.client.renderer.entity.ItemRenderer.getFoilBufferDirect;

public class BlockPackRenderer extends BlockEntityWithoutLevelRenderer {
    public static boolean removeCameraTransforms = false;
    private static BlockPackRenderer instance;
    private ItemRenderer itemRenderer;
    private ModelManager modelManager;

    public static BlockPackRenderer getInstance() {
        if (instance == null) {
            instance = new BlockPackRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        }
        return instance;
    }

    public BlockPackRenderer(BlockEntityRenderDispatcher pBlockEntityRenderDispatcher, EntityModelSet pEntityModelSet) {
        super(pBlockEntityRenderDispatcher, pEntityModelSet);
        itemRenderer = Minecraft.getInstance().getItemRenderer();
        modelManager = Minecraft.getInstance().getModelManager();
    }


    private void renderItem(PoseStack poseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay, boolean flag1, BakedModel bakedModel, ItemStack bi) {
        for (var model : bakedModel.getRenderPasses(bi, flag1)) {
            for (var rendertype : model.getRenderTypes(bi, flag1)) {
                VertexConsumer vertexconsumer;
                if (flag1) {
                    vertexconsumer = getFoilBufferDirect(pBuffer, rendertype, true, bi.hasFoil());
                } else {
                    vertexconsumer = getFoilBuffer(pBuffer, rendertype, true, bi.hasFoil());
                }
                itemRenderer.renderModelLists(bakedModel, bi, pPackedLight, pPackedOverlay, poseStack, vertexconsumer);
            }
        }
    }

    private void renderItemStack(BakedModel p_model, ItemStack itemStack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        boolean flag1 = true;
        if (displayContext != ItemDisplayContext.GUI && !displayContext.firstPerson()) {
            if (itemStack.getItem() instanceof BlockItem blockItem) {
                Block block = blockItem.getBlock();
                flag1 = !(block instanceof HalfTransparentBlock) && !(block instanceof StainedGlassPaneBlock);
            }
        }

        renderItem(poseStack, bufferSource, combinedLight, combinedOverlay, flag1, p_model, itemStack);

    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return;
        BlockPack data = BlockPackItem.getData(stack);
        if (data == null) return;
        Block block = data.getBlock(tag.getInt("index"));
        if (block == null) return;
        ItemStack bi = new ItemStack(block.asItem());
        Minecraft mc = Minecraft.getInstance();

        if (displayContext == ItemDisplayContext.GUI || displayContext == ItemDisplayContext.GROUND || displayContext == ItemDisplayContext.FIXED) {
            ResourceLocation model = ModModels.toolboxRegModel;
            if (stack.is(ModItems.MED_BLOCK_PACK.get())) {
                model = ModModels.toolboxMedModel;
            } else if (stack.is(ModItems.BIG_BLOCK_PACK.get())) {
                model = ModModels.toolboxBigModel;
            }

            BakedModel base = modelManager.getModel(model);
            poseStack.pushPose();
            Lighting.setupForFlatItems();
            if (displayContext == ItemDisplayContext.GUI) {
                poseStack.translate(0f, 0f, -5f);
            } else {

            }
            renderItem(poseStack, pBuffer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, false, base, bi);
            poseStack.popPose();
        }

        if (displayContext != ItemDisplayContext.GROUND && displayContext != ItemDisplayContext.FIXED) {
            BakedModel bakedModel = itemRenderer.getModel(bi, null, null, 0);
            if (displayContext == ItemDisplayContext.GUI) {
                poseStack.translate(.60f, .60f, 0f);
                poseStack.scale(0.40f, 0.40f, 0.40f);
            }
            poseStack.translate(0.5F, 0.5F, 0.5F);
//            bakedModel = ClientHooks.handleCameraTransforms(poseStack, bakedModel, displayContext, displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND);
            bakedModel.applyTransform(displayContext, poseStack, displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND);
            poseStack.translate(-0.5F, -0.5F, -0.5F);

            poseStack.pushPose();
            renderItemStack(bakedModel, bi, displayContext, poseStack, pBuffer, pPackedLight, pPackedOverlay);
            poseStack.popPose();
        }
    }

}
