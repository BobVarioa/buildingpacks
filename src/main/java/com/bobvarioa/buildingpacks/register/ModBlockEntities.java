package com.bobvarioa.buildingpacks.register;

import com.bobvarioa.buildingpacks.BuildingPacks;
import com.bobvarioa.buildingpacks.block.entity.BlueprintDeskEntity;
import com.bobvarioa.buildingpacks.block.entity.TemplateBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, BuildingPacks.MODID);
    public static final RegistryObject<BlockEntityType<TemplateBlockEntity>> TEMPLATE_BLOCK = BLOCK_ENTITIES.register("template_block", () ->
            BlockEntityType.Builder.of(TemplateBlockEntity::new, ModBlocks.TEMPLATE_BLOCK.get()).build(null)
    );
    public static final RegistryObject<BlockEntityType<BlueprintDeskEntity>> BLUEPRINT_DESK = BLOCK_ENTITIES.register("blueprint_desk", () ->
            BlockEntityType.Builder.of(BlueprintDeskEntity::new, ModBlocks.BLUEPRINT_DESK.get()).build(null)
    );

    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }
}
