package com.bobvarioa.buildingpacks.mixin.effortlessbuilding;

import com.bobvarioa.buildingpacks.compat.effortlessbuilding.BuilderChainExtensions;
import com.bobvarioa.buildingpacks.item.BlockPackItem;
import com.bobvarioa.buildingpacks.register.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import nl.requios.effortlessbuilding.EffortlessBuildingClient;
import nl.requios.effortlessbuilding.buildmode.BuildModeEnum;
import nl.requios.effortlessbuilding.capability.CapabilityHandler;
import nl.requios.effortlessbuilding.compatibility.CompatHelper;
import nl.requios.effortlessbuilding.network.PacketHandler;
import nl.requios.effortlessbuilding.network.ServerBreakBlocksPacket;
import nl.requios.effortlessbuilding.systems.BuilderChain;
import nl.requios.effortlessbuilding.utilities.BlockEntry;
import nl.requios.effortlessbuilding.utilities.BlockSet;
import nl.requios.effortlessbuilding.utilities.ClientBlockUtilities;
import org.openjdk.nashorn.internal.objects.annotations.Setter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BuilderChain.class)
public abstract class BuilderChainMixin implements BuilderChainExtensions {
    @Accessor(remap = false)
    public abstract void setBuildingState(BuilderChain.BuildingState buildingState);

    @Inject(method= "determineItemStack(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;", at=@At("HEAD"), remap = false, cancellable = true)
    private void determineItemStack(Player player, ItemStack heldItem, CallbackInfoReturnable<ItemStack> cir) {
        if (heldItem.getItem() instanceof BlockPackItem) {
            cir.setReturnValue(CompatHelper.getItemBlockFromStack(heldItem));
        }
    }
    
    @Inject(method = "onLeftClick()V", at = @At("HEAD"), remap = false, cancellable = true)
    public void onLeftClick(CallbackInfo ci) {
        BuilderChain _this = (BuilderChain)(Object) this;
        if (_this.getAbilitiesState() != BuilderChain.AbilitiesState.NONE && _this.getBuildingState() != BuilderChain.BuildingState.PLACING) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                if (CapabilityHandler.canBreakFar(player) || player.getMainHandItem().getItem() instanceof BlockPackItem) {
                    BlockSet blocks = _this.getBlocks();
                    if (_this.getBuildingState() == BuilderChain.BuildingState.IDLE) {
                        this.setBuildingState(BuilderChain.BuildingState.BREAKING);
                        blocks.setStartPos(new BlockEntry(_this.getStartPosForBreaking()));
                        EffortlessBuildingClient.BUILD_MODIFIERS.findCoordinates(blocks, player);
                        EffortlessBuildingClient.BUILDER_FILTER.filterOnCoordinates(blocks, player);
                        Level level = player.level();
                        for (BlockEntry blockEntry : blocks) {
                            blockEntry.existingBlockState = level.getBlockState(blockEntry.blockPos);
                        }
                        EffortlessBuildingClient.BUILDER_FILTER.filterOnExistingBlockStates(blocks, player);
                    }

                    BuildModeEnum buildMode = EffortlessBuildingClient.BUILD_MODES.getBuildMode();
                    if (buildMode.instance.onClick(blocks)) {
                        this.setBuildingState(BuilderChain.BuildingState.IDLE);
                        if (!blocks.isEmpty()) {
                            EffortlessBuildingClient.BLOCK_PREVIEWS.onBlocksBroken(blocks);
                            ClientBlockUtilities.playSoundIfFurtherThanNormal(player, blocks.getLastBlockEntry(), true);
                            player.swing(InteractionHand.MAIN_HAND);
                            blocks.skipFirst = buildMode == BuildModeEnum.DISABLED;
                            PacketHandler.INSTANCE.sendToServer(new ServerBreakBlocksPacket(blocks));
                        }
                    }
                }
            }
        } else {
            _this.cancel();
        }
        
        ci.cancel();
    }
}
