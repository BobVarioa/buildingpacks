package com.bobvarioa.buildingpacks.client.renderer;

import com.bobvarioa.buildingpacks.block.entity.TemplateBlockEntity;
import com.bobvarioa.buildingpacks.capabilty.BuildingPower;
import com.bobvarioa.buildingpacks.register.ModBlockEntities;
import com.bobvarioa.buildingpacks.register.ModCaps;
import com.bobvarioa.buildingpacks.register.ModItems;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterNamedRenderTypesEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static com.bobvarioa.buildingpacks.BuildingPacks.MODID;


@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@OnlyIn(Dist.CLIENT)
public class TemplateBlockRenderer implements BlockEntityRenderer<TemplateBlockEntity> {
    private static TemplateBlockRenderer instance;
    private static BlockRenderDispatcher blockRenderDispatcher;
    private static RenderType blueprintType;

    public static TemplateBlockRenderer getInstance() {
        if (instance == null) {
            instance = new TemplateBlockRenderer();
            blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();
        }
        return instance;
    }

    @Override
    public void render(TemplateBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {

        var player = Minecraft.getInstance().player;

        pPoseStack.pushPose();
        BlockState blockState = pBlockEntity.blockState;
        if (player != null) {
            var cap = player.getCapability(ModCaps.BUILDING_POWERS);
            if (cap.isPresent() && cap.resolve().get().hasPower(BuildingPower.DRAFTING_SEE)) {
                blockRenderDispatcher.renderSingleBlock(blockState, pPoseStack, pBuffer, pPackedLight, pPackedOverlay, ModelData.EMPTY, RenderType.translucent());
            } else {
                pPoseStack.translate(0.1f, 0.1f, 0.1f);
                pPoseStack.scale(0.8f, 0.8f, 0.8f);
                blockRenderDispatcher.renderSingleBlock(blockState, pPoseStack, pBuffer, pPackedLight, pPackedOverlay, ModelData.EMPTY, blueprintType);
            }

        }

        pPoseStack.popPose();
    }

    @SubscribeEvent
    public static void registerBlockEntityRenderer(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.TEMPLATE_BLOCK.get(), (ctx) -> TemplateBlockRenderer.getInstance());
    }

    @SubscribeEvent
    public static void registerShaderEvent(RegisterShadersEvent event) {
        AtomicReference<ShaderInstance> blueprint = new AtomicReference<>();
        try {
            event.registerShader(new ShaderInstance(event.getResourceProvider(), new ResourceLocation(MODID, "rendertype_blueprint"), DefaultVertexFormat.BLOCK), blueprint::set);
        } catch (IOException e) {
            throw new RuntimeException("could not reload shaders", e);
        }

        blueprintType = RenderType.create(
                "blueprint",
                DefaultVertexFormat.BLOCK,
                VertexFormat.Mode.QUADS,
                2097152,
                true,
                true,
                RenderType.translucentState(new RenderStateShard.ShaderStateShard(blueprint::get))
        );
    }

}
