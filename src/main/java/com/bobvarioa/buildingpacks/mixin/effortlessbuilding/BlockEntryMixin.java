package com.bobvarioa.buildingpacks.mixin.effortlessbuilding;

import com.bobvarioa.buildingpacks.BlockPack;
import com.bobvarioa.buildingpacks.item.BlockPackItem;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import nl.requios.effortlessbuilding.utilities.BlockEntry;
import nl.requios.effortlessbuilding.utilities.MyPlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntry.class)
public class BlockEntryMixin {
    @Shadow(remap = false)
    private Direction applyMirror(Direction direction) {
        throw new IllegalStateException("Mixin failed to shadow applyMirror");
    }

    @Shadow(remap = false)
    private void applyMirrorToBlockState() {
        throw new IllegalStateException("Mixin failed to shadow applyMirrorToBlockState");
    }

    @Inject(method = "setItemAndFindNewBlockState(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/Direction;Lnet/minecraft/core/Direction;Lnet/minecraft/world/phys/Vec3;)V", at=@At("HEAD"), cancellable = true, remap = false)
    public void setItemAndFindNewBlockState(ItemStack itemStack, Level world, Direction originalDirection, Direction clickedFace, Vec3 relativeHitVec, CallbackInfo ci) {
        if (itemStack.getItem() instanceof BlockPackItem bpi) {
            BlockEntry _this = (BlockEntry)(Object)this;
            CompoundTag tag = itemStack.getTag();
            if (tag == null) return;
            BlockPack data = BlockPackItem.getData(itemStack);
            var index = tag.getInt("index");
            if (data == null) return;
            Block block = data.getBlock(index);
            _this.item = block.asItem();
            Direction direction = originalDirection;
            if (_this.rotation != null) {
                direction = _this.rotation.rotate(originalDirection);
            }

            direction = this.applyMirror(direction);
            ItemStack stack = new ItemStack(block.asItem());

            MyPlaceContext blockPlaceContext = new MyPlaceContext(world, _this.blockPos, direction, stack, clickedFace, relativeHitVec);
            _this.newBlockState = block.getStateForPlacement(blockPlaceContext);
            this.applyMirrorToBlockState();

            ci.cancel();
        }

    }
}
