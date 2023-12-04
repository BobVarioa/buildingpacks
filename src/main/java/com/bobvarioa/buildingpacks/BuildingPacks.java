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
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
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

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void createRegistry(final NewRegistryEvent event) {
        RegistryBuilder<BlockPack> builder = RegistryBuilder.of("buildingpacks:block_packs");
        event.create(builder, (reg) -> {
            BLOCK_PACKS = reg;
        });
    }

    private void onRegister(final RegisterEvent event) {
        event.register(ResourceKey.createRegistryKey(new ResourceLocation("buildingpacks", "block_packs")), helper -> {
            helper.register("oak_wood", new BlockPack(64*8, "buildingpacks:oak_wood")
                    .put(Blocks.OAK_PLANKS, 1f)
                    .put(Blocks.OAK_LOG, 4f, true)
                    .put(Blocks.STRIPPED_OAK_LOG, 4f, true)
                    .put(Blocks.OAK_WOOD, 1.4f, true) // 4/3
                    .put(Blocks.STRIPPED_OAK_WOOD, 1.4f, true) // 4/3
                    .put(Blocks.OAK_STAIRS, 1f)
                    .put(Blocks.OAK_SLAB, 0.5f)
                    .put(Blocks.OAK_FENCE, 1.7f) // 5/3
                    .put(Blocks.OAK_FENCE_GATE, 4f)
                    .put(Blocks.OAK_DOOR, 2f)
                    .put(Blocks.OAK_PRESSURE_PLATE, 2f)
                    .put(Blocks.OAK_BUTTON, 1f)
                    .put(Blocks.OAK_SIGN, 2.2f) // 6.5/3
                    .put(Blocks.OAK_TRAPDOOR, 3f));
            helper.register("spruce_wood", new BlockPack(64*8, "buildingpacks:spruce_wood")
                    .put(Blocks.SPRUCE_PLANKS, 1f)
                    .put(Blocks.SPRUCE_LOG, 4f, true)
                    .put(Blocks.STRIPPED_SPRUCE_LOG, 4f, true)
                    .put(Blocks.SPRUCE_WOOD, 1.4f, true) // 4/3
                    .put(Blocks.STRIPPED_SPRUCE_WOOD, 1.4f, true) // 4/3
                    .put(Blocks.SPRUCE_STAIRS, 1f)
                    .put(Blocks.SPRUCE_SLAB, 0.5f)
                    .put(Blocks.SPRUCE_FENCE, 1.7f) // 5/3
                    .put(Blocks.SPRUCE_FENCE_GATE, 4f)
                    .put(Blocks.SPRUCE_DOOR, 2f)
                    .put(Blocks.SPRUCE_PRESSURE_PLATE, 2f)
                    .put(Blocks.SPRUCE_BUTTON, 1f)
                    .put(Blocks.SPRUCE_SIGN, 2.2f) // 6.5/3
                    .put(Blocks.SPRUCE_TRAPDOOR, 3f));
            helper.register("acacia_wood", new BlockPack(64*8, "buildingpacks:acacia_wood")
                    .put(Blocks.ACACIA_PLANKS, 1f)
                    .put(Blocks.ACACIA_LOG, 4f, true)
                    .put(Blocks.STRIPPED_ACACIA_LOG, 4f, true)
                    .put(Blocks.OAK_WOOD, 1.4f, true) // 4/3
                    .put(Blocks.STRIPPED_ACACIA_WOOD, 1.4f, true) // 4/3
                    .put(Blocks.ACACIA_STAIRS, 1f)
                    .put(Blocks.ACACIA_SLAB, 0.5f)
                    .put(Blocks.ACACIA_FENCE, 1.7f) // 5/3
                    .put(Blocks.ACACIA_FENCE_GATE, 4f)
                    .put(Blocks.ACACIA_DOOR, 2f)
                    .put(Blocks.ACACIA_PRESSURE_PLATE, 2f)
                    .put(Blocks.ACACIA_BUTTON, 1f)
                    .put(Blocks.ACACIA_SIGN, 2.2f) // 6.5/3
                    .put(Blocks.ACACIA_TRAPDOOR, 3f));
            helper.register("birch_wood", new BlockPack(64*8, "buildingpacks:birch_wood")
                    .put(Blocks.BIRCH_PLANKS, 1f)
                    .put(Blocks.BIRCH_LOG, 4f, true)
                    .put(Blocks.STRIPPED_BIRCH_LOG, 4f, true)
                    .put(Blocks.OAK_WOOD, 1.4f, true) // 4/3
                    .put(Blocks.STRIPPED_BIRCH_WOOD, 1.4f, true) // 4/3
                    .put(Blocks.BIRCH_STAIRS, 1f)
                    .put(Blocks.BIRCH_SLAB, 0.5f)
                    .put(Blocks.BIRCH_FENCE, 1.7f) // 5/3
                    .put(Blocks.BIRCH_FENCE_GATE, 4f)
                    .put(Blocks.BIRCH_DOOR, 2f)
                    .put(Blocks.BIRCH_PRESSURE_PLATE, 2f)
                    .put(Blocks.BIRCH_BUTTON, 1f)
                    .put(Blocks.BIRCH_SIGN, 2.2f) // 6.5/3
                    .put(Blocks.BIRCH_TRAPDOOR, 3f));
            helper.register("dark_oak_wood", new BlockPack(64*8, "buildingpacks:dark_oak_wood")
                    .put(Blocks.DARK_OAK_PLANKS, 1f)
                    .put(Blocks.DARK_OAK_LOG, 4f, true)
                    .put(Blocks.STRIPPED_DARK_OAK_LOG, 4f, true)
                    .put(Blocks.OAK_WOOD, 1.4f, true) // 4/3
                    .put(Blocks.STRIPPED_DARK_OAK_WOOD, 1.4f, true) // 4/3
                    .put(Blocks.DARK_OAK_STAIRS, 1f)
                    .put(Blocks.DARK_OAK_SLAB, 0.5f)
                    .put(Blocks.DARK_OAK_FENCE, 1.7f) // 5/3
                    .put(Blocks.DARK_OAK_FENCE_GATE, 4f)
                    .put(Blocks.DARK_OAK_DOOR, 2f)
                    .put(Blocks.DARK_OAK_PRESSURE_PLATE, 2f)
                    .put(Blocks.DARK_OAK_BUTTON, 1f)
                    .put(Blocks.DARK_OAK_SIGN, 2.2f) // 6.5/3
                    .put(Blocks.DARK_OAK_TRAPDOOR, 3f));
            helper.register("jungle_wood", new BlockPack(64*8, "buildingpacks:jungle_wood")
                    .put(Blocks.JUNGLE_PLANKS, 1f)
                    .put(Blocks.JUNGLE_LOG, 4f, true)
                    .put(Blocks.STRIPPED_JUNGLE_LOG, 4f, true)
                    .put(Blocks.OAK_WOOD, 1.4f, true) // 4/3
                    .put(Blocks.STRIPPED_JUNGLE_WOOD, 1.4f, true) // 4/3
                    .put(Blocks.JUNGLE_STAIRS, 1f)
                    .put(Blocks.JUNGLE_SLAB, 0.5f)
                    .put(Blocks.JUNGLE_FENCE, 1.7f) // 5/3
                    .put(Blocks.JUNGLE_FENCE_GATE, 4f)
                    .put(Blocks.JUNGLE_DOOR, 2f)
                    .put(Blocks.JUNGLE_PRESSURE_PLATE, 2f)
                    .put(Blocks.JUNGLE_BUTTON, 1f)
                    .put(Blocks.JUNGLE_SIGN, 2.2f) // 6.5/3
                    .put(Blocks.JUNGLE_TRAPDOOR, 3f));
            helper.register("mangrove_wood", new BlockPack(64*8, "buildingpacks:mangrove_wood")
                    .put(Blocks.MANGROVE_PLANKS, 1f)
                    .put(Blocks.MANGROVE_LOG, 4f, true)
                    .put(Blocks.STRIPPED_MANGROVE_LOG, 4f, true)
                    .put(Blocks.OAK_WOOD, 1.4f, true) // 4/3
                    .put(Blocks.STRIPPED_MANGROVE_WOOD, 1.4f, true) // 4/3
                    .put(Blocks.MANGROVE_STAIRS, 1f)
                    .put(Blocks.MANGROVE_SLAB, 0.5f)
                    .put(Blocks.MANGROVE_FENCE, 1.7f) // 5/3
                    .put(Blocks.MANGROVE_FENCE_GATE, 4f)
                    .put(Blocks.MANGROVE_DOOR, 2f)
                    .put(Blocks.MANGROVE_PRESSURE_PLATE, 2f)
                    .put(Blocks.MANGROVE_BUTTON, 1f)
                    .put(Blocks.MANGROVE_SIGN, 2.2f) // 6.5/3
                    .put(Blocks.MANGROVE_TRAPDOOR, 3f));
            helper.register("cherry_wood", new BlockPack(64*8, "buildingpacks:cherry_wood")
                    .put(Blocks.CHERRY_PLANKS, 1f)
                    .put(Blocks.CHERRY_LOG, 4f, true)
                    .put(Blocks.STRIPPED_CHERRY_LOG, 4f, true)
                    .put(Blocks.OAK_WOOD, 1.4f, true) // 4/3
                    .put(Blocks.STRIPPED_CHERRY_WOOD, 1.4f, true) // 4/3
                    .put(Blocks.CHERRY_STAIRS, 1f)
                    .put(Blocks.CHERRY_SLAB, 0.5f)
                    .put(Blocks.CHERRY_FENCE, 1.7f) // 5/3
                    .put(Blocks.CHERRY_FENCE_GATE, 4f)
                    .put(Blocks.CHERRY_DOOR, 2f)
                    .put(Blocks.CHERRY_PRESSURE_PLATE, 2f)
                    .put(Blocks.CHERRY_BUTTON, 1f)
                    .put(Blocks.CHERRY_SIGN, 2.2f) // 6.5/3
                    .put(Blocks.CHERRY_TRAPDOOR, 3f));
            helper.register("crimson_wood", new BlockPack(64*8, "buildingpacks:crimson_wood")
                    .put(Blocks.CRIMSON_PLANKS, 1f)
                    .put(Blocks.CRIMSON_STEM, 4f, true)
                    .put(Blocks.STRIPPED_CRIMSON_STEM, 4f, true)
                    .put(Blocks.CRIMSON_HYPHAE, 1.4f, true) // 4/3
                    .put(Blocks.STRIPPED_CRIMSON_HYPHAE, 1.4f, true) // 4/3
                    .put(Blocks.CRIMSON_STAIRS, 1f)
                    .put(Blocks.CRIMSON_SLAB, 0.5f)
                    .put(Blocks.CRIMSON_FENCE, 1.7f) // 5/3
                    .put(Blocks.CRIMSON_FENCE_GATE, 4f)
                    .put(Blocks.CRIMSON_DOOR, 2f)
                    .put(Blocks.CRIMSON_PRESSURE_PLATE, 2f)
                    .put(Blocks.CRIMSON_BUTTON, 1f)
                    .put(Blocks.CRIMSON_SIGN, 2.2f) // 6.5/3
                    .put(Blocks.CRIMSON_TRAPDOOR, 3f));
            helper.register("warped_wood", new BlockPack(64*8, "buildingpacks:warped_wood")
                    .put(Blocks.WARPED_PLANKS, 1f)
                    .put(Blocks.WARPED_STEM, 4f, true)
                    .put(Blocks.STRIPPED_WARPED_STEM, 4f, true)
                    .put(Blocks.WARPED_HYPHAE, 1.4f, true) // 4/3
                    .put(Blocks.STRIPPED_WARPED_HYPHAE, 1.4f, true) // 4/3
                    .put(Blocks.WARPED_STAIRS, 1f)
                    .put(Blocks.WARPED_SLAB, 0.5f)
                    .put(Blocks.WARPED_FENCE, 1.7f) // 5/3
                    .put(Blocks.WARPED_FENCE_GATE, 4f)
                    .put(Blocks.WARPED_DOOR, 2f)
                    .put(Blocks.WARPED_PRESSURE_PLATE, 2f)
                    .put(Blocks.WARPED_BUTTON, 1f)
                    .put(Blocks.WARPED_SIGN, 2.2f) // 6.5/3
                    .put(Blocks.WARPED_TRAPDOOR, 3f));
            helper.register("bamboo_wood", new BlockPack(64*8, "buildingpacks:bamboo_wood")
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
            helper.register("stone", new BlockPack(64*8, "buildingpacks:stone")
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
            helper.register("deepslate", new BlockPack(64*8, "buildingpacks:deepslate")
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
            helper.register("cobblestone", new BlockPack(64*8, "buildingpacks:cobblestone")
                    .put(Blocks.COBBLESTONE, 1f)
                    .put(Blocks.COBBLESTONE_STAIRS, 1f)
                    .put(Blocks.COBBLESTONE_SLAB, 0.5f)
                    .put(Blocks.COBBLESTONE_WALL, 1f));
            helper.register("blackstone", new BlockPack(64*8, "buildingpacks:blackstone")
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
            helper.register("end_stone", new BlockPack(64*8, "buildingpacks:end_stone")
                    .put(Blocks.END_STONE, 1f)
                    .put(Blocks.END_STONE_BRICKS, 1f)
                    .put(Blocks.END_STONE_BRICK_STAIRS, 1f)
                    .put(Blocks.END_STONE_BRICK_SLAB, 0.5f)
                    .put(Blocks.END_STONE_BRICK_WALL, 1f));
            helper.register("granite", new BlockPack(64*8, "buildingpacks:granite")
                    .put(Blocks.GRANITE, 1f)
                    .put(Blocks.GRANITE_STAIRS, 1f)
                    .put(Blocks.GRANITE_SLAB, 0.5f)
                    .put(Blocks.GRANITE_WALL, 1f)
                    .put(Blocks.POLISHED_GRANITE, 1f)
                    .put(Blocks.POLISHED_GRANITE_STAIRS, 1f)
                    .put(Blocks.POLISHED_GRANITE_SLAB, 0.5f));
            helper.register("diorite", new BlockPack(64*8, "buildingpacks:diorite")
                    .put(Blocks.DIORITE, 1f)
                    .put(Blocks.DIORITE_STAIRS, 1f)
                    .put(Blocks.DIORITE_SLAB, 0.5f)
                    .put(Blocks.DIORITE_WALL, 1f)
                    .put(Blocks.POLISHED_DIORITE, 1f)
                    .put(Blocks.POLISHED_DIORITE_STAIRS, 1f)
                    .put(Blocks.POLISHED_DIORITE_SLAB, 0.5f));
            helper.register("andesite", new BlockPack(64*8, "buildingpacks:andesite")
                    .put(Blocks.ANDESITE, 1f)
                    .put(Blocks.ANDESITE_STAIRS, 1f)
                    .put(Blocks.ANDESITE_SLAB, 0.5f)
                    .put(Blocks.ANDESITE_WALL, 1f)
                    .put(Blocks.POLISHED_ANDESITE, 1f)
                    .put(Blocks.POLISHED_ANDESITE_STAIRS, 1f)
                    .put(Blocks.POLISHED_ANDESITE_SLAB, 0.5f));
            helper.register("netherbrick", new BlockPack(64*8, "buildingpacks:netherbrick")
                    .put(Blocks.NETHER_BRICKS, 1f)
                    .put(Blocks.CRACKED_NETHER_BRICKS, 1f)
                    .put(Blocks.NETHER_BRICK_STAIRS, 1f)
                    .put(Blocks.NETHER_BRICK_SLAB, 0.5f)
                    .put(Blocks.NETHER_BRICK_WALL, 1f)
                    .put(Blocks.NETHER_BRICK_FENCE, 1.7f) // 5/3
                    .put(Blocks.CHISELED_NETHER_BRICKS, 1f));
            helper.register("red_netherbrick", new BlockPack(64*8, "buildingpacks:red_netherbrick")
                    .put(Blocks.RED_NETHER_BRICKS, 1f)
                    .put(Blocks.RED_NETHER_BRICK_STAIRS, 1f)
                    .put(Blocks.RED_NETHER_BRICK_SLAB, 0.5f)
                    .put(Blocks.RED_NETHER_BRICK_WALL, 1f));
            helper.register("sandstone", new BlockPack(64*8, "buildingpacks:sandstone")
                    .put(Blocks.SANDSTONE, 1f)
                    .put(Blocks.SANDSTONE_STAIRS, 1f)
                    .put(Blocks.SANDSTONE_SLAB, 0.5f)
                    .put(Blocks.SANDSTONE_WALL, 1f)
                    .put(Blocks.CUT_SANDSTONE, 1f)
                    .put(Blocks.CHISELED_SANDSTONE, 1f)
                    .put(Blocks.CUT_SANDSTONE_SLAB, 0.5f));
            helper.register("red_sandstone", new BlockPack(64*8, "buildingpacks:red_sandstone")
                    .put(Blocks.RED_SANDSTONE, 1f)
                    .put(Blocks.RED_SANDSTONE_STAIRS, 1f)
                    .put(Blocks.RED_SANDSTONE_SLAB, 0.5f)
                    .put(Blocks.RED_SANDSTONE_WALL, 1f)
                    .put(Blocks.CUT_RED_SANDSTONE, 1f)
                    .put(Blocks.CHISELED_RED_SANDSTONE, 1f)
                    .put(Blocks.CUT_RED_SANDSTONE_SLAB, 0.5f));
            helper.register("quartz", new BlockPack(64*8, "buildingpacks:quartz")
                    .put(Blocks.QUARTZ_BLOCK, 1f)
                    .put(Blocks.QUARTZ_BRICKS, 1f)
                    .put(Blocks.QUARTZ_PILLAR, 1f)
                    .put(Blocks.QUARTZ_STAIRS, 1f)
                    .put(Blocks.QUARTZ_SLAB, 0.5f)
                    .put(Blocks.SMOOTH_QUARTZ, 1f)
                    .put(Blocks.SMOOTH_QUARTZ_STAIRS, 1f)
                    .put(Blocks.SMOOTH_QUARTZ_SLAB, 0.5f)
                    .put(Blocks.CHISELED_QUARTZ_BLOCK, 1f));
            helper.register("purpur", new BlockPack(64*8, "buildingpacks:purpur")
                    .put(Blocks.PURPUR_BLOCK, 1f)
                    .put(Blocks.PURPUR_STAIRS, 1f)
                    .put(Blocks.PURPUR_SLAB, 0.5f)
                    .put(Blocks.PURPUR_PILLAR, 1f));
            helper.register("prismarine", new BlockPack(64*8, "buildingpacks:prismarine")
                    .put(Blocks.PRISMARINE, 1f)
                    .put(Blocks.PRISMARINE_STAIRS, 1f)
                    .put(Blocks.PRISMARINE_SLAB, 0.5f)
                    .put(Blocks.PRISMARINE_WALL, 1f));
            helper.register("prismarine_bricks", new BlockPack(64*8, "buildingpacks:prismarine_bricks")
                    .put(Blocks.PRISMARINE_BRICKS, 1f)
                    .put(Blocks.PRISMARINE_BRICK_STAIRS, 1f)
                    .put(Blocks.PRISMARINE_BRICK_SLAB, 0.5f));
            helper.register("dark_prismarine", new BlockPack(64*8, "buildingpacks:dark_prismarine")
                    .put(Blocks.DARK_PRISMARINE, 1f)
                    .put(Blocks.DARK_PRISMARINE_STAIRS, 1f)
                    .put(Blocks.DARK_PRISMARINE_SLAB, 0.5f));
            // copper??? waxing and oxidation make this complicated
        });
    }
}
