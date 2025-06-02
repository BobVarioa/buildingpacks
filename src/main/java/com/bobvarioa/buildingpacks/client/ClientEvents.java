package com.bobvarioa.buildingpacks.client;

import com.bobvarioa.buildingpacks.BlockPack;
import com.bobvarioa.buildingpacks.block.entity.TemplateBlockEntity;
import com.bobvarioa.buildingpacks.capabilty.BuildingPower;
import com.bobvarioa.buildingpacks.client.screens.BlockPackGuiOverlay;
import com.bobvarioa.buildingpacks.item.BlockPackItem;
import com.bobvarioa.buildingpacks.network.ToolIndexUpdatePacket;
import com.bobvarioa.buildingpacks.register.ModCaps;
import com.bobvarioa.buildingpacks.register.ModPackets;
import com.bobvarioa.buildingpacks.network.BreakBlockPacket;
import com.bobvarioa.buildingpacks.network.DropItemPacket;
import com.bobvarioa.buildingpacks.network.IndexUpdatePacket;
import com.bobvarioa.buildingpacks.register.ModBlocks;
import com.bobvarioa.buildingpacks.register.ModItems;
import com.bobvarioa.buildingpacks.utils.WorldUtils;
import com.firemerald.additionalplacements.block.AdditionalPlacementBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import nl.requios.effortlessbuilding.EffortlessBuildingClient;
import nl.requios.effortlessbuilding.buildmode.BuildModeEnum;

import static com.bobvarioa.buildingpacks.BuildingPacks.MODID;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
@OnlyIn(Dist.CLIENT)
public class ClientEvents {
    public static boolean blockPackOpen = false;

    @SubscribeEvent
    public static void handleScrollScreen(ScreenEvent.MouseScrolled.Pre event) {
        if (event.getScreen() instanceof InventoryScreen inv) {
            int sign = (int) Math.signum(event.getScrollDelta());
            Player player = Minecraft.getInstance().player;
            if (player == null) return;

            if (inv.hoveredSlot != null) {
                ItemStack stack = inv.hoveredSlot.getItem();

                if (stack.getItem() instanceof BlockPackItem) {
                    CompoundTag tag = stack.getOrCreateTag();
                    var index = tag.getInt("index");
                    if (sign > 0) {
                        index--;
                        if (index < 0) {
                            index = BlockPackItem.getData(stack).length() - 1;
                        }
                    } else if (sign < 0) {
                        index++;
                        if (index >= BlockPackItem.getData(stack).length()) {
                            index = 0;
                        }
                    }
                    if (sign != 0) {
                        tag.putInt("index", index);
                        ModPackets.INSTANCE.send(PacketDistributor.SERVER.noArg(), new IndexUpdatePacket(index, inv.hoveredSlot.index));
                        event.setCanceled(true);
                    }
                }

            }
        }
    }

    @SubscribeEvent
    public static void handleScroll(InputEvent.MouseScrollingEvent event) {
        int sign = (int) Math.signum(event.getScrollDelta());
        Player player = Minecraft.getInstance().player;
        Options options = Minecraft.getInstance().options;
        if (player == null) return;
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof BlockPackItem) {
            if (blockPackOpen) {
//                if (options.keyShift.isDown()) {
//                    CompoundTag tag = stack.getOrCreateTag();
//                    var toolIndex = tag.getInt("toolIndex");
//
//                    if (sign > 0) {
//                        toolIndex--;
//                        if (toolIndex < 0) {
//                            toolIndex = BuildingPower.ORDER_SET.length - 1;
//                        }
//                    } else if (sign < 0) {
//                        toolIndex++;
//                        if (toolIndex > BuildingPower.ORDER_SET.length - 1) {
//                            toolIndex = 0;
//                        }
//                    }
//
//                    if (sign != 0) {
//                        tag.putInt("toolIndex", toolIndex);
//                        ModPackets.INSTANCE.send(PacketDistributor.SERVER.noArg(), new ToolIndexUpdatePacket(toolIndex));
//                        event.setCanceled(true);
//                    }
//                    return;
//                }
            } else if (!options.keyShift.isDown()) return;

            CompoundTag tag = stack.getOrCreateTag();
            var index = tag.getInt("index");
            if (sign > 0) {
                index--;
                if (index < 0) {
                    index = BlockPackItem.getData(stack).length() - 1;
                }
            } else if (sign < 0) {
                index++;
                if (index >= BlockPackItem.getData(stack).length()) {
                    index = 0;
                }
            }
            if (sign != 0) {
                tag.putInt("index", index);
                ModPackets.INSTANCE.send(PacketDistributor.SERVER.noArg(), new IndexUpdatePacket(index, player.getInventory().selected));
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void pickBlock(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isPickBlock()) return;
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        ItemStack stack = player.getMainHandItem();
        {
            CompoundTag tag = stack.getOrCreateTag();
            HitResult hitResult = Minecraft.getInstance().hitResult;
            HitResult.Type hitresult$type = hitResult.getType();
            if (hitresult$type == HitResult.Type.BLOCK) {
                BlockPos blockpos = ((BlockHitResult) hitResult).getBlockPos();
                Level level = player.level();
                BlockState blockstate = level.getBlockState(blockpos);
                if (blockstate.isAir()) {
                    return;
                }
                Block block = WorldUtils.getRealBlock(level, blockpos, blockstate.getBlock());

                Inventory inventory = player.getInventory();

                for (int i = 0; i < inventory.items.size(); ++i) {
                    ItemStack itemStack = inventory.items.get(i);
                    if (Inventory.isHotbarSlot(i) && itemStack.getItem() instanceof BlockPackItem) {
                        BlockPack data = BlockPackItem.getData(itemStack);
                        int index = data.getBlockIndex(block);
                        if (index != -1) {
                            ModPackets.INSTANCE.send(PacketDistributor.SERVER.noArg(), new IndexUpdatePacket(index, i));
                            player.getInventory().setPickedItem(itemStack);
                            tag.putInt("index", index);
                            event.setCanceled(true);
                            break;
                        }
                    }
                }
            }
        }
//        if (stack.getItem() instanceof BlockPackItem) {
//        }

        if (stack.is(ModItems.DRAFTING_PENCIL.get())) {
            HitResult hitResult = Minecraft.getInstance().hitResult;
            HitResult.Type hitresult$type = hitResult.getType();
            if (hitresult$type == HitResult.Type.BLOCK) {
                BlockPos blockpos = ((BlockHitResult) hitResult).getBlockPos();
                Level level = player.level();
                BlockState blockstate = level.getBlockState(blockpos);
                if (blockstate.isAir()) {
                    return;
                }
                Block block = blockstate.getBlock();
                if (block.equals(ModBlocks.TEMPLATE_BLOCK.get())) {
                    if (level.getBlockEntity(blockpos) instanceof TemplateBlockEntity be) {
                        block = be.blockState.getBlock();
                    }
                }
                if (ModList.get().isLoaded("additionalplacements")) {
                    if (block instanceof AdditionalPlacementBlock<?> apb) {
                        block = apb.parentBlock;
                    }
                }


            }
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide) return;
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof BlockPackItem bpi) {
            if (player.getCooldowns().isOnCooldown(bpi)) return;
            if (ModList.get().isLoaded("effortlessbuilding")) {
                BuildModeEnum buildMode = EffortlessBuildingClient.BUILD_MODES.getBuildMode();
                if (buildMode != BuildModeEnum.DISABLED) {
                    player.getCooldowns().addCooldown(bpi, 5);
                    return;
                }
            }
            var pos = event.getPos();
            var data = BlockPackItem.getData(stack);
            float mat = bpi.getMaterial(stack);
            var level = player.level();
            var blockState = level.getBlockState(pos);
            var block = WorldUtils.getBlock(blockState.getBlock());

            float price = data.getPrice(block);
            if (price != -1) {
                if (player.isCreative() || mat + price <= bpi.getMaxMaterial(stack)) {
                    level.destroyBlock(pos, false);
                    bpi.addMaterial(stack, price);
                    ModPackets.INSTANCE.send(PacketDistributor.SERVER.noArg(), new BreakBlockPacket(pos, price));
                    event.setCanceled(true);
                    player.getCooldowns().addCooldown(bpi, 5);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickWithItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide) return;

        Options options = Minecraft.getInstance().options;
        ItemStack stack = event.getItemStack();
        if (options.keyShift.isDown() && stack.getItem() instanceof BlockPackItem bpi) {
            event.setCanceled(true);
            blockPackOpen = !blockPackOpen;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onKey(InputEvent.Key event) {
        var options = Minecraft.getInstance().options;
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        ItemStack stack = player.getMainHandItem();
        if (!player.isSpectator() && stack.getItem() instanceof BlockPackItem bpi) {
            var tag = stack.getTag();
            var data = BlockPackItem.getData(stack);
            if (tag != null && data != null) {
                var index = tag.getInt("index");
                float mat = bpi.getMaterial(stack);
                Block block = data.getBlock(index);
                float price = data.getPrice(block);
                if (options.keyDrop.isDown()) {
                    while (options.keyDrop.consumeClick()) {
                        var amount = Screen.hasControlDown() ? 64 : 1;
                        amount = Math.min(amount, (int) Math.floor((mat) / price));

                        if (mat - (price * amount) >= 0) {
                            bpi.addMaterial(stack, -price * amount);
                            player.swing(InteractionHand.MAIN_HAND);
                            ModPackets.INSTANCE.send(PacketDistributor.SERVER.noArg(), new DropItemPacket(amount));
                            continue;
                        }
                        // normal behavior
                        if (!player.drop(amount == 64)) {
                            player.swing(InteractionHand.MAIN_HAND);
                        }
                    }
                }

                if (blockPackOpen) {
                    int len = options.keyHotbarSlots.length;
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

                    for (int i = 0; i < len; i++) {
                        if (options.keyHotbarSlots[i].consumeClick()) {
                            tag.putInt("index", min + i);
                            ModPackets.INSTANCE.send(PacketDistributor.SERVER.noArg(), new IndexUpdatePacket(min + i, player.getInventory().selected));
                        }
                    }
                }
            }
        }
    }

    private static ResourceLocation HOTBAR = new ResourceLocation("minecraft", "hotbar");

    @SubscribeEvent
    public static void preOverlayRendered(RenderGuiOverlayEvent.Pre event) {

        if (event.getOverlay().id().equals(HOTBAR)) {
            if (blockPackOpen) {
                event.setCanceled(true);
                return;
            }
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                var cap = player.getCapability(ModCaps.BUILDING_POWERS);
                if (cap.isPresent() && cap.resolve().get().hasPower(BuildingPower.DRAFTING_META)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        @SubscribeEvent
        public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
            event.registerAboveAll("block_pack", new BlockPackGuiOverlay());
        }
    }

}
