package com.bobvarioa.buildingpacks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Consumer;

public class BlockPackItem extends Item {

    public BlockPackItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public Component getName(ItemStack pStack) {
        ResourceLocation id = ResourceLocation.tryParse(pStack.getOrCreateTag().getString("id"));
        if (id == null) return Component.translatable("item.buildingpacks.pack");
        return Component.translatable("item.buildingpacks.pack." + id.toLanguageKey());
    }

    public static BlockPack getData(ItemStack stack) {
        ResourceLocation id = ResourceLocation.tryParse(stack.getOrCreateTag().getString("id"));
        if (id != null) {
            BlockPack blockPack = BuildingPacks.BLOCK_PACKS.getValue(id);
            return blockPack;
        }
        return null;
    }

    public static int getMaxMaterial(ItemStack stack) {
        BlockPack blockPack = getData(stack);
        if (blockPack == null) return 0;
        return blockPack.getMaxMaterial();
    }


    public static float getMaterial(ItemStack stack) {
        return stack.getOrCreateTag().getFloat("material");
    }

    @Override
    public boolean isBarVisible(ItemStack pStack) {
        return true;
    }

    public static float addMaterial(ItemStack stack, float amount) {
        CompoundTag tag = stack.getOrCreateTag();
        float mat = tag.getFloat("material");
        float change = Mth.clamp(mat + amount, 0, getMaxMaterial(stack));
        tag.putFloat("material", change);
        return change;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return BlockPackRenderer.getInstance();
            }
        });

    }

    public static void handleScroll(InputEvent.MouseScrollingEvent event) {
        int sign = (int) Math.signum(event.getScrollDelta());
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        ItemStack stack = player.getMainHandItem();
        if (player.isCrouching() && stack.getItem() instanceof BlockPackItem) {
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
            tag.putInt("index", index);
            BlockPackPacketHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(), new BlockPackPacketHandler.IndexUpdatePacket(index));
            event.setCanceled(true);
        }
    }

    public static void pickBlock(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isPickBlock()) return;
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof BlockPackItem) {
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
                Block block = blockstate.getBlock();

                Inventory inventory = player.getInventory();

                for (int i = 0; i < inventory.items.size(); ++i) {
                    ItemStack itemStack = inventory.items.get(i);
                    if (Inventory.isHotbarSlot(i) && itemStack.getItem() instanceof BlockPackItem) {
                        BlockPack data = getData(itemStack);
                        int index = data.getBlockIndex(block);
                        if (index != -1) {
                            inventory.selected = i;
                            tag.putInt("index", index);
                            BlockPackPacketHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(), new BlockPackPacketHandler.IndexUpdatePacket(index));
                            event.setCanceled(true);
                            break;
                        }
                    }
                }

            }
        }
    }

    public static void pickupItem(EntityItemPickupEvent event) {
        Player player = event.getEntity();
        Inventory inventory = player.getInventory();
        ItemStack stack = event.getItem().getItem();
        if (stack.getItem() instanceof BlockItem item) {
            Block block = item.getBlock();
            top: for (int i = 0; i < inventory.items.size(); ++i) {
                ItemStack itemStack = inventory.items.get(i);
                if (itemStack.getItem() instanceof BlockPackItem) {
                    BlockPack data = getData(itemStack);
                    float mat = getMaterial(itemStack);
                    float price = data.getPrice(block);
                    if (price != -1) {
                        for (int j = 1; j < stack.getCount() + 1; j++) {
                            if (mat + price <= data.getMaxMaterial()) {
                                mat = addMaterial(itemStack, price);
                            } else {
                                stack.setCount(0);
                                event.setCanceled(true);
                                break top;
                            }
                        }
                    }
                }
            }

        }
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        var stack = pContext.getItemInHand();
        var data = getData(stack);
        float mat = getMaterial(stack);
        Level level = pContext.getLevel();
        CompoundTag tag = stack.getOrCreateTag();
        if (pContext.getHand() == InteractionHand.MAIN_HAND) {
            Player player = pContext.getPlayer();
            if (player == null) return InteractionResult.PASS;
            if (player.isCrouching()) {
                var pos = pContext.getClickedPos();
                var block = level.getBlockState(pos);
                float price = data.getPrice(block.getBlock());
                if (price != -1) {
                    if (player.isCreative() || mat + price <= data.getMaxMaterial()) {
                        level.destroyBlock(pos, false);
                        addMaterial(stack, price);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
            if (mat > 0) {
                var index = tag.getInt("index");
                if (index >= 0 && index < data.length()) {
                    Block block = data.getBlock(index);
                    float price = data.getPrice(block);
                    BlockItem item = (BlockItem) block.asItem();
                    if (mat - price >= 0) {
                        BlockPlaceContext cxt = new BlockPlaceContext(pContext.getLevel(), pContext.getPlayer(), pContext.getHand(), new ItemStack(item), new BlockHitResult(pContext.getClickLocation(), pContext.getClickedFace(), pContext.getClickedPos(), pContext.isInside()));
                        var res = item.place(cxt);

                        if (!player.isCreative() && !level.isClientSide() && res != InteractionResult.FAIL) {
                            addMaterial(stack, -price);
                        }
                        return InteractionResult.SUCCESS;
                    }
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public int getBarWidth(ItemStack pStack) {
        float maxMaterial = this.getMaxMaterial(pStack);
        float material = maxMaterial - getMaterial(pStack);
        return Math.round(13.0F - material * 13.0F / maxMaterial);
    }

    @Override
    public int getBarColor(ItemStack pStack) {
        // 239, 67, 82
        // 203, 67, 0.82
        float maxMaterial = this.getMaxMaterial(pStack);
        float material = getMaterial(pStack);
        float f = Math.max(0.0F, (maxMaterial - material) / maxMaterial);
        return Mth.hsvToRgb((239 - 36 * f) / 360, 0.67F, 0.82F);
    }


}
