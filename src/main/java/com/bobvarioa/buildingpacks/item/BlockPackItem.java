package com.bobvarioa.buildingpacks.item;

import com.bobvarioa.buildingpacks.BlockPack;
import com.bobvarioa.buildingpacks.BuildingPacks;
import com.bobvarioa.buildingpacks.client.renderer.BlockPackRenderer;
import com.bobvarioa.buildingpacks.item.templates.BlockItemExtensions;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class BlockPackItem extends BlockItem {
    public final int mult;

    public BlockPackItem(Properties pProperties, int mult) {
        super(Blocks.AIR, pProperties);
        this.mult = mult;
    }

    @Override
    public Block getBlock() {
        return super.getBlock();
    }

    @Override
    public Component getName(ItemStack pStack) {
        ResourceLocation id = ResourceLocation.tryParse(pStack.getOrCreateTag().getString("id"));
        if (id == null) return Component.translatable("item.buildingpacks.pack");
        return Component.translatable("item.buildingpacks.pack." + id.toLanguageKey()).append(" ").append(Component.translatable("item.buildingpacks.pack"));
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        super.appendHoverText(pStack, pLevel, pTooltip, pFlag);

        pTooltip.add(
                Component.translatable("item.buildingpacks.tooltip.material")
                        .append(": ")
                        .append(((Float) getMaterial(pStack)).toString())
                        .append("/")
                        .append(((Integer) getMaxMaterial(pStack)).toString()));
    }

    public static BlockPack getData(ItemStack stack) {
        ResourceLocation id = ResourceLocation.tryParse(stack.getOrCreateTag().getString("id"));
        if (id != null) {
            BlockPack blockPack = BuildingPacks.BLOCK_PACKS.getValue(id);
            return blockPack;
        }
        return null;
    }

    public int getMaxMaterial(ItemStack stack) {
        BlockPack blockPack = getData(stack);
        if (blockPack == null) return 0;
        return blockPack.getMaxMaterial() * this.mult;
    }

    public float getMaterial(ItemStack stack) {
        return stack.getOrCreateTag().getFloat("material");
    }

    public static Block getSelectedBlock(ItemStack stack) {
        var tag = stack.getTag();
        var data = getData(stack);
        if (tag != null && data != null) {
            var index = tag.getInt("index");
            return data.getBlock(index);
        }
        return Blocks.AIR;
    }

    @Override
    public boolean isBarVisible(ItemStack pStack) {
        return true;
    }

    public float addMaterial(ItemStack stack, float amount) {
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

    public static void pickupItem(EntityItemPickupEvent event) {
        Player player = event.getEntity();
        Inventory inventory = player.getInventory();
        ItemStack stack = event.getItem().getItem();
        if (stack.getItem() instanceof BlockItem item) {
            Block block = item.getBlock();
            for (int i = 0; i < inventory.items.size(); ++i) {
                ItemStack itemStack = inventory.items.get(i);
                if (itemStack.getItem() instanceof BlockPackItem bpi) {
                    BlockPack data = getData(itemStack);
                    float mat = bpi.getMaterial(itemStack);
                    float price = data.getPrice(block);

                    if (price != -1) {
                        int times = Math.min(stack.getCount(), (int)Math.floor((bpi.getMaxMaterial(itemStack) - mat) / price));
                        if (times > 0) {
                            bpi.addMaterial(itemStack, price * times);
                            stack.setCount(stack.getCount() - times);
                            player.playSound(SoundEvents.ITEM_PICKUP, .25f, .75f);
                            if (stack.getCount() == 0) {
                                event.setCanceled(true);
                            }
                        }
                    }
                }
            }

        }
    }

    public void dropItem(ItemStack stack, Player player, int amount) {
        var data = getData(stack);
        if (data != null) {
            float mat = getMaterial(stack);
            Block block = getSelectedBlock(stack);
            float price = data.getPrice(block);
            amount = Math.min(amount, (int)Math.floor((mat) / price));

            if (mat - (price * amount) >= 0) {
                addMaterial(stack, -price * amount);
                var itemStack = new ItemStack(block.asItem());
                itemStack.setCount(amount);
                player.drop(itemStack, true);
                return;
            }
        }
        player.drop(stack, true);
    }

    @Override
    public InteractionResult place(BlockPlaceContext pContext) {
        BlockPlaceContext blockplacecontext = this.updatePlacementContext(pContext);
        if (blockplacecontext != null) {
            var stack = pContext.getItemInHand();
            var data = getData(stack);
            float mat = getMaterial(stack);
            var tag = stack.getOrCreateTag();

            if (!(mat > 0)) return InteractionResult.FAIL;

            var index = tag.getInt("index");
            if (index >= 0 && index < data.length()) {
                Block block = data.getBlock(index);
                if (block.asItem() instanceof BlockItem bi) {
                    var bie = (BlockItemExtensions)bi;
                    float price = data.getPrice(block);
                    if (mat - price >= 0) {
                        BlockState blockstate = bie.buildingpacks$getPlacementState(pContext);
                        if (blockstate != null && bie.buildingpacks$canPlace(pContext, blockstate)) {
                            if (!bie.buildingpacks$placeBlock(blockplacecontext, blockstate)) {
                                return InteractionResult.FAIL;
                            } else {
                                BlockPos blockpos = blockplacecontext.getClickedPos();
                                Level level = blockplacecontext.getLevel();
                                Player player = blockplacecontext.getPlayer();
                                ItemStack itemstack = blockplacecontext.getItemInHand();
                                BlockState blockState = level.getBlockState(blockpos);
                                if (blockState.is(blockstate.getBlock())) {
                                    blockState.getBlock().setPlacedBy(level, blockpos, blockState, player, itemstack);
                                    if (player instanceof ServerPlayer) {
                                        CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer) player, blockpos, itemstack);
                                    }
                                }

                                SoundType soundtype = blockState.getSoundType(level, blockpos, pContext.getPlayer());
                                level.playSound(player, blockpos, bie.buildingpacks$getPlaceSound(blockState, level, blockpos, pContext.getPlayer()), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                                level.gameEvent(GameEvent.BLOCK_PLACE, blockpos, GameEvent.Context.of(player, blockState));
                                if (!player.isCreative() && !level.isClientSide()) {
                                    addMaterial(stack, -price);
                                }

                                return InteractionResult.sidedSuccess(level.isClientSide);
                            }
                        } else {
                            return InteractionResult.FAIL;
                        }
                    }
                }

            }

            return InteractionResult.FAIL;

        }
        return InteractionResult.FAIL;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack pStack, ItemStack pOther, Slot pSlot, ClickAction pAction, Player pPlayer, SlotAccess pAccess) {
        if (pAction == ClickAction.SECONDARY) {
            if (pOther.getItem() instanceof BlockPackItem bpi) {
                BlockPack data = getData(pStack);
                BlockPack data2 = getData(pOther);
                if (data == null || data2 == null) return false;
                float mat = getMaterial(pStack);
                float mat2 = getMaterial(pOther);
                if (data.id.equals(data2.id)) {
                    float maxAmount = Math.min(bpi.getMaxMaterial(pOther) - mat2, mat);
                    bpi.addMaterial(pOther, maxAmount);
                    addMaterial(pStack, -maxAmount);
                    return true;
                }

                return false;
            }

            if (pOther.getItem() instanceof BlockItem bi) {
                BlockPack data = getData(pStack);
                if (data == null) return false;
                float mat = getMaterial(pStack);
                float price = data.getPrice(bi.getBlock());
                if (price != -1) {
                    int times = Math.min(pOther.getCount(), (int)Math.floor((getMaxMaterial(pStack) - mat) / price));
                    if (times > 0) {
                        addMaterial(pStack, price * times);
                        pOther.setCount(pOther.getCount() - times);
                        return pOther.getCount() == 0;
                    }
                }
            }

        }

        return false;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack pStack, Slot pSlot, ClickAction pAction, Player pPlayer) {
        if (pAction == ClickAction.SECONDARY) {
            if (pSlot.getItem().isEmpty()) {
                var data = getData(pStack);
                if (data != null) {
                    float mat = getMaterial(pStack);
                    Block block = getSelectedBlock(pStack);
                    float price = data.getPrice(block);
                    int amount = Math.min(64, (int)Math.floor((mat) / price));

                    if (mat - (price * amount) >= 0) {
                        addMaterial(pStack, -price * amount);
                        var itemStack = new ItemStack(block.asItem());
                        itemStack.setCount(amount);
                        pSlot.set(itemStack);
                        return true;
                    }
                }
            } else {
                return this.overrideOtherStackedOnMe(pStack, pSlot.getItem(), pSlot, pAction, pPlayer, SlotAccess.NULL);
            }

        }
        return false;
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

    @Override
    public ItemStack getDefaultInstance() {
        return super.getDefaultInstance();
    }
}
