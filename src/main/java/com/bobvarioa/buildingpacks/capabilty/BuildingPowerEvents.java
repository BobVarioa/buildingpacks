package com.bobvarioa.buildingpacks.capabilty;


import com.bobvarioa.buildingpacks.network.IndexUpdatePacket;
import com.bobvarioa.buildingpacks.network.PowerUpdatePacket;
import com.bobvarioa.buildingpacks.register.ModCaps;
import com.bobvarioa.buildingpacks.register.ModPackets;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;

public class BuildingPowerEvents {

    public static void attachCaps(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player) {
            event.addCapability(IBuildingPowersHandler.ID, new ICapabilityProvider() {

                LazyOptional<IBuildingPowersHandler> powers = LazyOptional.of(() -> new IBuildingPowersHandler() {
                    private EnumMap<BuildingPower, Boolean> map = new EnumMap<>(BuildingPower.class);

                    @Override
                    public boolean hasPower(BuildingPower power) {
                        return map.getOrDefault(power, false);
                    }

                    @Override
                    public void setPower(BuildingPower power, boolean enabled) {
                        if (map.getOrDefault(power, false) == enabled) return;
                        map.put(power, enabled);

                        if (!player.level().isClientSide) {
                            ModPackets.INSTANCE.send(PacketDistributor.PLAYER.with(() -> ((ServerPlayer)player)), new PowerUpdatePacket(power, enabled));
                        }
                    }
                });

                @Override
                public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                    return ModCaps.BUILDING_POWERS.orEmpty(cap, powers);
                }
            });
        }
    }
}
