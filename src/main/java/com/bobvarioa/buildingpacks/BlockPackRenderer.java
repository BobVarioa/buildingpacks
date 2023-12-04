package com.bobvarioa.buildingpacks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.bobvarioa.buildingpacks.BuildingPacks.MODID;
import static net.minecraft.client.renderer.entity.ItemRenderer.*;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BlockPackRenderer extends BlockEntityWithoutLevelRenderer {
    private static BlockPackRenderer instance;
    private ItemRenderer itemRenderer;

    public static BlockPackRenderer getInstance() {
        if (instance == null) {
            instance = new BlockPackRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        }
        return instance;
    }

    public BlockPackRenderer(BlockEntityRenderDispatcher pBlockEntityRenderDispatcher, EntityModelSet pEntityModelSet) {
        super(pBlockEntityRenderDispatcher, pEntityModelSet);
        itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    public static class BlockPackBakedModel extends BakedModelWrapper<BakedModel> {

        public BlockPackBakedModel(BakedModel originalModel) {
            super(originalModel);
        }

        @Override
        public boolean isCustomRenderer() {
            return true;
        }

        @Override
        public boolean isGui3d() {
            return true;
        }

        @Override
        public BakedModel applyTransform(ItemDisplayContext cameraTransformType, PoseStack poseStack, boolean applyLeftHandTransform) {
            super.applyTransform(cameraTransformType, poseStack, applyLeftHandTransform);
            return this;
        }
    }

    @SubscribeEvent
    public static void onModelBake(ModelEvent.ModifyBakingResult event) {
        ResourceLocation location = new ModelResourceLocation(MODID, "block_pack", "inventory");
        event.getModels().put(location, new BlockPackBakedModel(event.getModels().get(location)));
    }

    private static ResourceLocation toolboxModel = new ResourceLocation(MODID, "item/toolbox");
    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(toolboxModel);
    }

    @Override
    public void renderByItem(ItemStack pStack, ItemDisplayContext pTransformType, PoseStack poseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        CompoundTag tag = pStack.getTag();
        if (tag == null) return;
        BlockPack data = BlockPackItem.getData(pStack);
        if (data == null) return;
        Block block = data.getBlock(tag.getInt("index"));
        if (block == null) return;
        ItemStack bi = new ItemStack(block.asItem());
        Minecraft mc = Minecraft.getInstance();

        if (pTransformType == ItemDisplayContext.GUI) {
            BakedModel base = mc.getModelManager().getModel(toolboxModel);
            poseStack.pushPose();
            poseStack.translate(0f, 0f, -5f);
            renderItem(poseStack, pBuffer, pPackedLight, pPackedOverlay, false, base, bi);
            poseStack.popPose();
        }

        poseStack.pushPose();
        BakedModel bakedModel = itemRenderer.getModel(bi, null, null, 0);

        if (pTransformType == ItemDisplayContext.GUI) {
            poseStack.scale(0.5f, 0.5f, 1f);
            poseStack.translate(1.5f, 1.5f, 0f);
            itemRenderer.render(bi, pTransformType, false, poseStack, pBuffer, pPackedLight, pPackedOverlay, bakedModel);
        } else {
            bakedModel = bakedModel.applyTransform(pTransformType, poseStack, false);
            if (!pTransformType.firstPerson()) {
                poseStack.translate(0f, 1f, 0f);
                poseStack.scale(1.9f, 1.9f, 1.9f);
                poseStack.translate(0f, -0.5f, 0f);
            } else {
                poseStack.translate(-1f, 1f, 1f);
                poseStack.mulPose(Axis.XP.rotationDegrees(25));
                poseStack.mulPose(Axis.ZN.rotationDegrees(25));
                poseStack.mulPose(Axis.YN.rotationDegrees(10));
                poseStack.scale(1.2f, 1.2f, 1.2f);
            }
            boolean flag1;
            if (!pTransformType.firstPerson()) {
                flag1 = !(block instanceof HalfTransparentBlock) && !(block instanceof StainedGlassPaneBlock);
            } else {
                flag1 = true;
            }
            renderItem(poseStack, pBuffer, pPackedLight, pPackedOverlay, flag1, bakedModel, bi);
        }
        poseStack.popPose();
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
}
