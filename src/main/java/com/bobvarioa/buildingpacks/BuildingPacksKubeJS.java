package com.bobvarioa.buildingpacks;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegisterEvent;

import java.util.ArrayList;
import java.util.List;

public class BuildingPacksKubeJS extends KubeJSPlugin {
    public static final EventGroup MAIN = EventGroup.of("BuildingPacks");

    public static final List<BlockPack> kubeBlockPacks = new ArrayList<>();

    public static class BlockPackRegisterEvent extends EventJS {

        private final RegisterEvent.RegisterHelper<Object> helper;

        public BlockPackRegisterEvent(RegisterEvent.RegisterHelper<Object> helper) {
            this.helper = helper;
        }

        public BlockPack create(ResourceLocation id, int maxMaterial) {
            var blockPack = new BlockPack(maxMaterial, id.toString());
            helper.register(id, blockPack);
            return blockPack;
        }
    }

    public static final EventHandler REGISTER = MAIN.startup("register", () -> BlockPackRegisterEvent.class);

    @Override
    public void registerEvents() {
        MAIN.register();
    }
}
