package com.bobvarioa.buildingpacks.register;

import com.bobvarioa.buildingpacks.BuildingPacks;
import com.bobvarioa.buildingpacks.block.BlueprintDesk;
import com.bobvarioa.buildingpacks.block.TemplateBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, BuildingPacks.MODID);
    public static final RegistryObject<Block> TEMPLATE_BLOCK = BLOCKS.register("template_block", () -> new TemplateBlock(
            Block.Properties.of().noCollission().noOcclusion().noLootTable().instabreak()
    ));
    public static final RegistryObject<Block> BLUEPRINT_DESK = BLOCKS.register("blueprint_desk", () -> new BlueprintDesk(
            Block.Properties.of().noOcclusion()
                    //.dynamicShape()
    ));

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
