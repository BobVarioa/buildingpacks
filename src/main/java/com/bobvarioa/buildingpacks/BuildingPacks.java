package com.bobvarioa.buildingpacks;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.*;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(BuildingPacks.MODID)
public class BuildingPacks {
    public static final String MODID = "buildingpacks";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static IForgeRegistry<BlockPack> BLOCK_PACKS;
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final RegistryObject<Item> BLOCK_PACK = ITEMS.register("block_pack", () -> new BlockPackItem(new Item.Properties().stacksTo(1)));

    private static ItemStack blockPackOf(String id) {
        ItemStack item = new ItemStack(BLOCK_PACK.get());
        CompoundTag tag = item.getOrCreateTag();
        tag.putString("id", id);
        ResourceLocation res = ResourceLocation.tryParse(id);
        if (res == null) return item;
        BlockPack blockPack = BLOCK_PACKS.getValue(res);
        if (blockPack == null) return item;
        tag.putInt("material", blockPack.getMaxMaterial());
        return item;
    }

    public static final RegistryObject<CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("block_packs", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> blockPackOf("buildingpacks:oak_wood"))
            .displayItems((parameters, output) -> {
                BlockPack.blockPacks.forEach((key) -> {
                    output.accept(blockPackOf(key.id));
                });
            })
            .title(Component.translatable("creative.block_packs"))
            .build());

    public BuildingPacks() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::createRegistry);
        modEventBus.addListener(this::onRegister);
        modEventBus.addListener(BlockPackPacketHandler::commonSetup);

        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.addListener(BlockPackItem::handleScroll);
        MinecraftForge.EVENT_BUS.addListener(BlockPackItem::pickBlock);
        MinecraftForge.EVENT_BUS.addListener(BlockPackItem::pickupItem);

//        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void createRegistry(final NewRegistryEvent event) {
        RegistryBuilder<BlockPack> builder = new RegistryBuilder<>();
        builder.setName(new ResourceLocation("buildingpacks:block_packs"));
        event.create(builder, (reg) -> {
            BLOCK_PACKS = reg;
        });
    }

    private Block getBlock(String id, String namespace) {
        return ForgeRegistries.BLOCKS.getValue(new ResourceLocation(namespace, id));
    }

    private Block getBlock(String id) {
        return getBlock(id, "minecraft");
    }

    private BlockPack createWood(String type) {
        return createWood(type, false);
    }

    private BlockPack createWood(String type, boolean isNether) {
        var blockPack = new BlockPack(64 * 8, "buildingpacks:" + type + "_wood");
        blockPack.put(getBlock(type + "_planks"), 1f);
        if (isNether) {
            blockPack.put(getBlock(type + "_stem"), 4f, true)
                    .put(getBlock("stripped_" + type + "_stem"), 4f, true)
                    .put(getBlock(type + "_hyphae"), 1.4f, true) // 4/3
                    .put(getBlock("stripped_" + type + "_hyphae"), 1.4f, true); // 4/3
        } else {
            blockPack.put(getBlock(type + "_log"), 4f, true)
                    .put(getBlock("stripped_" + type + "_log"), 4f, true)
                    .put(getBlock(type + "_wood"), 1.4f, true) // 4/3
                    .put(getBlock("stripped_" + type + "_wood"), 1.4f, true); // 4/3
        }

        blockPack.put(getBlock(type + "_stairs"), 1f)
                .put(getBlock(type + "_slab"), 0.5f)
                .put(getBlock(type + "_fence"), 1.7f) // 5/3
                .put(getBlock(type + "_fence_gate"), 4f)
                .put(getBlock(type + "_door"), 2f)
                .put(getBlock(type + "_pressure_plate"), 2f)
                .put(getBlock(type + "_button"), 1f)
                .put(getBlock(type + "_sign"), 2.2f) // 6.5/3
                .put(getBlock(type + "_trapdoor"), 3f);

        return blockPack;
    }

    private void onRegister(final RegisterEvent event) {
        event.register(ResourceKey.createRegistryKey(new ResourceLocation("buildingpacks", "block_packs")), helper -> {
            helper.register("oak_wood", createWood("oak"));
            helper.register("spruce_wood", createWood("spruce"));
            helper.register("acacia_wood", createWood("acacia"));
            helper.register("birch_wood", createWood("birch"));
            helper.register("dark_oak_wood", createWood("dark_oak"));
            helper.register("jungle_wood", createWood("jungle"));
            helper.register("mangrove_wood", createWood("mangrove"));
            helper.register("cherry_wood", createWood("cherry"));
            helper.register("crimson_wood", createWood("crimson", true));
            helper.register("warped_wood", createWood("warped", true));
            helper.register("bamboo_wood", new BlockPack(64 * 8, "buildingpacks:bamboo_wood")
                    .put(Blocks.BAMBOO_BLOCK, 2f, true)
                    .put(Blocks.BAMBOO_PLANKS, 1f)
                    .put(Blocks.BAMBOO_STAIRS, 1f)
                    .put(Blocks.BAMBOO_SLAB, 0.5f)
                    .put(Blocks.BAMBOO_FENCE, 1.7f) // 5/3
                    .put(Blocks.BAMBOO_FENCE_GATE, 4f)
                    .put(Blocks.BAMBOO_DOOR, 2f)
                    .put(Blocks.BAMBOO_PRESSURE_PLATE, 2f)
                    .put(Blocks.BAMBOO_BUTTON, 1f)
                    .put(Blocks.BAMBOO_SIGN, 2.2f) // 6.5/3
                    .put(Blocks.BAMBOO_TRAPDOOR, 3f));
            helper.register("stone", new BlockPack(64 * 8, "buildingpacks:stone")
                    .put(Blocks.STONE, 1f)
                    .put(Blocks.STONE_STAIRS, 1f)
                    .put(Blocks.STONE_SLAB, 0.5f)
                    .put(Blocks.STONE_PRESSURE_PLATE, 2f)
                    .put(Blocks.STONE_BUTTON, 1f)
                    .put(Blocks.STONE_BRICKS, 1f)
                    .put(Blocks.CRACKED_STONE_BRICKS, 1f)
                    .put(Blocks.STONE_BRICK_STAIRS, 1f)
                    .put(Blocks.STONE_BRICK_SLAB, 0.5f)
                    .put(Blocks.STONE_BRICK_WALL, 1f)
                    .put(Blocks.CHISELED_STONE_BRICKS, 1f)
                    .put(Blocks.SMOOTH_STONE, 1f)
                    .put(Blocks.SMOOTH_STONE_SLAB, 0.5f));
            helper.register("deepslate", new BlockPack(64 * 8, "buildingpacks:deepslate")
                    .put(Blocks.DEEPSLATE, 1f)
                    .put(Blocks.DEEPSLATE_BRICKS, 1f)
                    .put(Blocks.CRACKED_DEEPSLATE_BRICKS, 1f)
                    .put(Blocks.DEEPSLATE_BRICK_STAIRS, 1f)
                    .put(Blocks.DEEPSLATE_BRICK_SLAB, 0.5f)
                    .put(Blocks.DEEPSLATE_BRICK_WALL, 1f)
                    .put(Blocks.DEEPSLATE_TILES, 1f)
                    .put(Blocks.DEEPSLATE_TILE_SLAB, 0.5f)
                    .put(Blocks.DEEPSLATE_TILE_STAIRS, 1f)
                    .put(Blocks.DEEPSLATE_TILE_WALL, 1f)
                    .put(Blocks.COBBLED_DEEPSLATE, 1f)
                    .put(Blocks.COBBLED_DEEPSLATE_SLAB, 0.5f)
                    .put(Blocks.COBBLED_DEEPSLATE_STAIRS, 1f)
                    .put(Blocks.COBBLED_DEEPSLATE_WALL, 1f)
                    .put(Blocks.POLISHED_DEEPSLATE, 1f)
                    .put(Blocks.POLISHED_DEEPSLATE_STAIRS, 1f)
                    .put(Blocks.POLISHED_DEEPSLATE_SLAB, 0.5f));
            helper.register("cobblestone", new BlockPack(64 * 8, "buildingpacks:cobblestone")
                    .put(Blocks.COBBLESTONE, 1f)
                    .put(Blocks.COBBLESTONE_STAIRS, 1f)
                    .put(Blocks.COBBLESTONE_SLAB, 0.5f)
                    .put(Blocks.COBBLESTONE_WALL, 1f));
            helper.register("blackstone", new BlockPack(64 * 8, "buildingpacks:blackstone")
                    .put(Blocks.BLACKSTONE, 1f)
                    .put(Blocks.BLACKSTONE_STAIRS, 1f)
                    .put(Blocks.BLACKSTONE_SLAB, 0.5f)
                    .put(Blocks.POLISHED_BLACKSTONE, 1f)
                    .put(Blocks.POLISHED_BLACKSTONE_STAIRS, 1f)
                    .put(Blocks.POLISHED_BLACKSTONE_SLAB, 0.5f)
                    .put(Blocks.POLISHED_BLACKSTONE_WALL, 1f)
                    .put(Blocks.POLISHED_BLACKSTONE_BUTTON, 1f)
                    .put(Blocks.POLISHED_BLACKSTONE_PRESSURE_PLATE, 1f)
                    .put(Blocks.POLISHED_BLACKSTONE_BRICKS, 1f)
                    .put(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS, 1f)
                    .put(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, 1f)
                    .put(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, 0.5f)
                    .put(Blocks.POLISHED_BLACKSTONE_BRICK_WALL, 1f)
                    .put(Blocks.CHISELED_POLISHED_BLACKSTONE, 1f));
            helper.register("end_stone", new BlockPack(64 * 8, "buildingpacks:end_stone")
                    .put(Blocks.END_STONE, 1f)
                    .put(Blocks.END_STONE_BRICKS, 1f)
                    .put(Blocks.END_STONE_BRICK_STAIRS, 1f)
                    .put(Blocks.END_STONE_BRICK_SLAB, 0.5f)
                    .put(Blocks.END_STONE_BRICK_WALL, 1f));
            helper.register("granite", new BlockPack(64 * 8, "buildingpacks:granite")
                    .put(Blocks.GRANITE, 1f)
                    .put(Blocks.GRANITE_STAIRS, 1f)
                    .put(Blocks.GRANITE_SLAB, 0.5f)
                    .put(Blocks.GRANITE_WALL, 1f)
                    .put(Blocks.POLISHED_GRANITE, 1f)
                    .put(Blocks.POLISHED_GRANITE_STAIRS, 1f)
                    .put(Blocks.POLISHED_GRANITE_SLAB, 0.5f));
            helper.register("diorite", new BlockPack(64 * 8, "buildingpacks:diorite")
                    .put(Blocks.DIORITE, 1f)
                    .put(Blocks.DIORITE_STAIRS, 1f)
                    .put(Blocks.DIORITE_SLAB, 0.5f)
                    .put(Blocks.DIORITE_WALL, 1f)
                    .put(Blocks.POLISHED_DIORITE, 1f)
                    .put(Blocks.POLISHED_DIORITE_STAIRS, 1f)
                    .put(Blocks.POLISHED_DIORITE_SLAB, 0.5f));
            helper.register("andesite", new BlockPack(64 * 8, "buildingpacks:andesite")
                    .put(Blocks.ANDESITE, 1f)
                    .put(Blocks.ANDESITE_STAIRS, 1f)
                    .put(Blocks.ANDESITE_SLAB, 0.5f)
                    .put(Blocks.ANDESITE_WALL, 1f)
                    .put(Blocks.POLISHED_ANDESITE, 1f)
                    .put(Blocks.POLISHED_ANDESITE_STAIRS, 1f)
                    .put(Blocks.POLISHED_ANDESITE_SLAB, 0.5f));
            helper.register("netherbrick", new BlockPack(64 * 8, "buildingpacks:netherbrick")
                    .put(Blocks.NETHER_BRICKS, 1f)
                    .put(Blocks.CRACKED_NETHER_BRICKS, 1f)
                    .put(Blocks.NETHER_BRICK_STAIRS, 1f)
                    .put(Blocks.NETHER_BRICK_SLAB, 0.5f)
                    .put(Blocks.NETHER_BRICK_WALL, 1f)
                    .put(Blocks.NETHER_BRICK_FENCE, 1.7f) // 5/3
                    .put(Blocks.CHISELED_NETHER_BRICKS, 1f));
            helper.register("red_netherbrick", new BlockPack(64 * 8, "buildingpacks:red_netherbrick")
                    .put(Blocks.RED_NETHER_BRICKS, 1f)
                    .put(Blocks.RED_NETHER_BRICK_STAIRS, 1f)
                    .put(Blocks.RED_NETHER_BRICK_SLAB, 0.5f)
                    .put(Blocks.RED_NETHER_BRICK_WALL, 1f));
            helper.register("sandstone", new BlockPack(64 * 8, "buildingpacks:sandstone")
                    .put(Blocks.SANDSTONE, 1f)
                    .put(Blocks.SANDSTONE_STAIRS, 1f)
                    .put(Blocks.SANDSTONE_SLAB, 0.5f)
                    .put(Blocks.SANDSTONE_WALL, 1f)
                    .put(Blocks.CUT_SANDSTONE, 1f)
                    .put(Blocks.CHISELED_SANDSTONE, 1f)
                    .put(Blocks.CUT_SANDSTONE_SLAB, 0.5f));
            helper.register("red_sandstone", new BlockPack(64 * 8, "buildingpacks:red_sandstone")
                    .put(Blocks.RED_SANDSTONE, 1f)
                    .put(Blocks.RED_SANDSTONE_STAIRS, 1f)
                    .put(Blocks.RED_SANDSTONE_SLAB, 0.5f)
                    .put(Blocks.RED_SANDSTONE_WALL, 1f)
                    .put(Blocks.CUT_RED_SANDSTONE, 1f)
                    .put(Blocks.CHISELED_RED_SANDSTONE, 1f)
                    .put(Blocks.CUT_RED_SANDSTONE_SLAB, 0.5f));
            helper.register("quartz", new BlockPack(64 * 8, "buildingpacks:quartz")
                    .put(Blocks.QUARTZ_BLOCK, 1f)
                    .put(Blocks.QUARTZ_BRICKS, 1f)
                    .put(Blocks.QUARTZ_PILLAR, 1f)
                    .put(Blocks.QUARTZ_STAIRS, 1f)
                    .put(Blocks.QUARTZ_SLAB, 0.5f)
                    .put(Blocks.SMOOTH_QUARTZ, 1f)
                    .put(Blocks.SMOOTH_QUARTZ_STAIRS, 1f)
                    .put(Blocks.SMOOTH_QUARTZ_SLAB, 0.5f)
                    .put(Blocks.CHISELED_QUARTZ_BLOCK, 1f));
            helper.register("purpur", new BlockPack(64 * 8, "buildingpacks:purpur")
                    .put(Blocks.PURPUR_BLOCK, 1f)
                    .put(Blocks.PURPUR_STAIRS, 1f)
                    .put(Blocks.PURPUR_SLAB, 0.5f)
                    .put(Blocks.PURPUR_PILLAR, 1f));
            helper.register("prismarine", new BlockPack(64 * 8, "buildingpacks:prismarine")
                    .put(Blocks.PRISMARINE, 1f)
                    .put(Blocks.PRISMARINE_STAIRS, 1f)
                    .put(Blocks.PRISMARINE_SLAB, 0.5f)
                    .put(Blocks.PRISMARINE_WALL, 1f));
            helper.register("prismarine_bricks", new BlockPack(64 * 8, "buildingpacks:prismarine_bricks")
                    .put(Blocks.PRISMARINE_BRICKS, 1f)
                    .put(Blocks.PRISMARINE_BRICK_STAIRS, 1f)
                    .put(Blocks.PRISMARINE_BRICK_SLAB, 0.5f));
            helper.register("dark_prismarine", new BlockPack(64 * 8, "buildingpacks:dark_prismarine")
                    .put(Blocks.DARK_PRISMARINE, 1f)
                    .put(Blocks.DARK_PRISMARINE_STAIRS, 1f)
                    .put(Blocks.DARK_PRISMARINE_SLAB, 0.5f));
            // copper??? waxing and oxidation make this complicated

            if (ModList.get().isLoaded("kubejs")) {
                BuildingPacksKubeJS.REGISTER.post(new BuildingPacksKubeJS.BlockPackRegisterEvent(helper));
            }
        });
    }
}
