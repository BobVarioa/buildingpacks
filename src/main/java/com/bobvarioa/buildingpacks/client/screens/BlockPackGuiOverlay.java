package com.bobvarioa.buildingpacks.client.screens;

import com.bobvarioa.buildingpacks.client.BlockPackClientEvents;
import com.bobvarioa.buildingpacks.item.BlockPackItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import static com.bobvarioa.buildingpacks.BuildingPacks.MODID;

@OnlyIn(Dist.CLIENT)
public class BlockPackGuiOverlay implements IGuiOverlay {
    protected static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation(MODID, "textures/gui/widgets.png");
    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        Player player = gui.getMinecraft().player;
        if (player != null && !player.isSpectator() && BlockPackClientEvents.blockPackOpen) {
            ItemStack stack = player.getMainHandItem();

            if (stack.getItem() instanceof BlockPackItem bpi) {
                var tag = stack.getTag();
                var data = BlockPackItem.getData(stack);
                if (tag != null && data != null) {
                    var index = tag.getInt("index");

                    int min = index - 4;
                    int max = index + 4;
                    while (max > data.length() - 1) {
                        max--;
                        min--;
                    }
                    while (min < 0) {
                        min++;
                        max++;
                    }

                    int adjIndex = index - min;

                    int offhandOffset = 110;

                    HumanoidArm humanoidarm = player.getMainArm().getOpposite();
                    int i = screenWidth / 2;
                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().translate(0.0F, 0.0F, -90.0F);
                    guiGraphics.blit(WIDGETS_LOCATION, i - 91, screenHeight - 22, 0, 0, 182, 22);
                    guiGraphics.blit(WIDGETS_LOCATION, i - 91 - 1 + adjIndex * 20, screenHeight - 22 - 1, 0, 22, 24, 22);


                    if (!stack.isEmpty()) {
                        if (humanoidarm == HumanoidArm.LEFT) {
                            guiGraphics.blit(WIDGETS_LOCATION, i - offhandOffset - 29, screenHeight - 23, 24, 22, 29, 24);
                        } else {
                            guiGraphics.blit(WIDGETS_LOCATION, i + offhandOffset, screenHeight - 23, 53, 22, 29, 24);
                        }
                    }

                    guiGraphics.pose().popPose();
                    int l = 1;

                    for(int i1 = 0; i1 < 9; ++i1) {
                        int j1 = i - 90 + i1 * 20 + 2;
                        int k1 = screenHeight - 16 - 3;
                        if (min + i1 > data.length() - 1) continue;
                        this.renderSlot(guiGraphics, j1, k1, partialTick, player, data.getBlock(min + i1).asItem().getDefaultInstance(), l++);
                    }

                    if (!stack.isEmpty()) {
                        int i2 = screenHeight - 16 - 3;
                        if (humanoidarm == HumanoidArm.LEFT) {
                            this.renderSlot(guiGraphics, i - offhandOffset - 26, i2, partialTick, player, stack, l++);
                        } else {
                            this.renderSlot(guiGraphics, i + offhandOffset + 10, i2, partialTick, player, stack, l++);
                        }
                    }

                }
            } else {
                BlockPackClientEvents.blockPackOpen = false;
            }
        }
    }

    private void renderSlot(GuiGraphics pGuiGraphics, int pX, int pY, float pPartialTick, Player pPlayer, ItemStack pStack, int pSeed) {
        if (!pStack.isEmpty()) {
            float f = (float)pStack.getPopTime() - pPartialTick;
            if (f > 0.0F) {
                float f1 = 1.0F + f / 5.0F;
                pGuiGraphics.pose().pushPose();
                pGuiGraphics.pose().translate((float)(pX + 8), (float)(pY + 12), 0.0F);
                pGuiGraphics.pose().scale(1.0F / f1, (f1 + 1.0F) / 2.0F, 1.0F);
                pGuiGraphics.pose().translate((float)(-(pX + 8)), (float)(-(pY + 12)), 0.0F);
            }

            pGuiGraphics.renderItem(pPlayer, pStack, pX, pY, pSeed);
            if (f > 0.0F) {
                pGuiGraphics.pose().popPose();
            }

            pGuiGraphics.renderItemDecorations(Minecraft.getInstance().font, pStack, pX, pY);
        }
    }
}
