package com.bobvarioa.buildingpacks.compat.additionalplacements;

import com.bobvarioa.buildingpacks.BlockPack;
import com.bobvarioa.buildingpacks.item.BlockPackItem;
import com.firemerald.additionalplacements.block.interfaces.IPlacementBlock;
import com.firemerald.additionalplacements.config.APConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import static com.bobvarioa.buildingpacks.BuildingPacks.MODID;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
@OnlyIn(Dist.CLIENT)
public class BlockPackHighlightEvent {
    @SubscribeEvent
    public static void onHighlightBlock(RenderHighlightEvent.Block event) {
        if (ModList.get().isLoaded("additionalplacements")) {
            if (!APConfigs.client().enablePlacementHighlight.get()) return;
            Player player = Minecraft.getInstance().player;
            if (player == null) return;
            ItemStack stack = player.getMainHandItem();
            if (stack.isEmpty()) stack = player.getOffhandItem();
            if (stack.getItem() instanceof BlockPackItem) {
                Block block = BlockPackItem.getSelectedBlock(stack);
                if (block instanceof IPlacementBlock<?> verticalBlock) {
                    if (verticalBlock.hasAdditionalStates())
                        verticalBlock.renderHighlight(event.getPoseStack(), event.getMultiBufferSource().getBuffer(RenderType.LINES), player, event.getTarget(), event.getCamera(), event.getPartialTick());
                }
            }
        }
    }
}
