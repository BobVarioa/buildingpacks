package com.bobvarioa.buildingpacks;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.Pack;
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

    private static Block getBlock(String id, String namespace) {
        return ForgeRegistries.BLOCKS.getValue(new ResourceLocation(namespace, id));
    }

    private static Block getBlock(String id) {
        return getBlock(id, "minecraft");
    }


    public static class PackBuilder {
        PackBuilder() {
        }

        public String namespace = "minecraft";

        public BlockPack blockPack;

        public static PackBuilder create(String id, int material) {
            var pack = new PackBuilder();
            pack.blockPack = new BlockPack(material, "buildingpacks:"+ id);
            return pack;
        }

        public PackBuilder slabStairs(String type) {
            put(type + "_stairs", 1f);
            put(type + "_slab", 0.5f);
            return this;
        }

        public PackBuilder wallSlabStairs(String type, boolean isFence) {
            slabStairs(type);

            if (isFence) {
                put(type + "_fence", 1.7f); // 5/3
                put(type + "_fence_gate", 4f);
            } else {
                this.put(type + "_wall", 1f);
            }

            return this;
        }

        public PackBuilder pressurePlate(String type) {
            put(type + "_pressure_plate", 2f);
            return this;
        }

        public PackBuilder button(String type) {
            put(type + "_button", 1f);
            return this;
        }

        public PackBuilder put(String str, float material) {
            blockPack.put(getBlock(str, namespace), material);
            return this;
        }

        public PackBuilder put(String str, float material, boolean acceptOnly) {
            blockPack.put(getBlock(str, namespace), material, acceptOnly);
            return this;
        }

        public BlockPack build() {
            return blockPack;
        }

        public static BlockPack createWood(String type) {
            return createWood(type, false);
        }

        public static BlockPack createWood(String type, boolean isNether) {
            var pack = PackBuilder.create("buildingpacks:" + type + "_wood", 64 * 8);

            pack.put(type + "_planks", 1f);
            if (isNether) {
                pack.put(type + "_stem", 4f, true)
                        .put("stripped_" + type + "_stem", 4f, true)
                        .put(type + "_hyphae", 1.4f, true) // 4/3
                        .put("stripped_" + type + "_hyphae", 1.4f, true); // 4/3
            } else {
                pack.put(type + "_log", 4f, true)
                        .put("stripped_" + type + "_log", 4f, true)
                        .put(type + "_wood", 1.4f, true) // 4/3
                        .put("stripped_" + type + "_wood", 1.4f, true); // 4/3
            }

            pack.wallSlabStairs(type, true)
                    .pressurePlate(type)
                    .button(type)
                    .put(type + "_door", 2f)
                    .put(type + "_sign", 2.2f)
                    .put(type + "_trapdoor", 3f);

            return pack.build();
        }

        public static BlockPack createStone(String type) {
            return PackBuilder.create("buildingpacks:" + type, 64 * 8)
                    .put(type, 1f)
                    .wallSlabStairs(type, false)
                    .put("polished_" + type, 1f)
                    .slabStairs("polished_" + type)
                    .build();
        }

        public static BlockPack createWallSlabStairs(String type, boolean addS) {
            return PackBuilder.create("buildingpacks:" + type, 64 * 8)
                    .put(type + (addS ? "s" : ""), 1f)
                    .wallSlabStairs(type, false)
                    .build();
        }

        public static BlockPack createSlabStairs(String type, boolean addS) {
            return PackBuilder.create("buildingpacks:" + type, 64 * 8)
                    .put(type + (addS ? "s" : ""), 1f)
                    .slabStairs(type)
                    .build();
        }
    }

    private void onRegister(final RegisterEvent event) {
        event.register(ResourceKey.createRegistryKey(new ResourceLocation("buildingpacks", "block_packs")), helper -> {
            helper.register("oak_wood", PackBuilder.createWood("oak"));
            helper.register("spruce_wood", PackBuilder.createWood("spruce"));
            helper.register("acacia_wood", PackBuilder.createWood("acacia"));
            helper.register("birch_wood", PackBuilder.createWood("birch"));
            helper.register("dark_oak_wood", PackBuilder.createWood("dark_oak"));
            helper.register("jungle_wood", PackBuilder.createWood("jungle"));
            helper.register("mangrove_wood", PackBuilder.createWood("mangrove"));
            helper.register("cherry_wood", PackBuilder.createWood("cherry"));
            helper.register("crimson_wood", PackBuilder.createWood("crimson", true));
            helper.register("warped_wood", PackBuilder.createWood("warped", true));
            helper.register("bamboo_wood", PackBuilder.create("bamboo_wood", 64 * 8)
                    .put("bamboo_block", 2f, true)
                    .put("bamboo_planks", 1f)
                    .wallSlabStairs("bamboo", true)
                    .pressurePlate("bamboo")
                    .button("bamboo")
                    .put("bamboo_door", 2f)
                    .put("bamboo_sign", 2.2f)
                    .put("bamboo_trapdoor", 3f)
                    .build());
            helper.register("stone", PackBuilder.create("stone", 64 * 8)
                    .put("stone", 1f)
                    .slabStairs("stone")
                    .pressurePlate("stone")
                    .button("stone")
                    .put("stone_bricks", 1f)
                    .wallSlabStairs("stone_brick", false)
                    .put("chiseled_stone_bricks", 1f)
                    .put("smooth_stone", 1f)
                    .put("smooth_stone_slab", 0.5f)
                    .build());
            helper.register("deepslate", PackBuilder.create("deepslate", 64 * 8)
                    .put("deepslate", 1f)
                    .put("deepslate_bricks", 1f)
                    .wallSlabStairs("deepslate_brick", false)
                    .put("deepslate_tiles", 1f)
                    .wallSlabStairs("deepslate_tile", false)
                    .put("cobbled_deepslate", 1f)
                    .wallSlabStairs("cobbled_deepslate", false)
                    .put("polished_deepslate", 1f)
                    .slabStairs("polished_deepslate")
                    .build());
            helper.register("cobblestone", PackBuilder.createWallSlabStairs("cobblestone", false));
            helper.register("blackstone", PackBuilder.create("blackstone", 64 * 8)
                    .put("blackstone", 1f)
                    .slabStairs("blackstone")
                    .put("polished_blackstone", 1f)
                    .put("chiseled_polished_blackstone", 1f)
                    .wallSlabStairs("polished_blackstone", false)
                    .button("polished_blackstone")
                    .pressurePlate("polished_blackstone")
                    .put("polished_blackstone_bricks", 1f)
                    .put("cracked_polished_blackstone_bricks", 1f)
                    .wallSlabStairs("polished_blackstone_brick", false)
                    .build());
            helper.register("end_stone", PackBuilder.create("end_stone", 64 * 8)
                    .put("end_stone", 1f)
                    .put("end_stone_bricks", 1f)
                    .wallSlabStairs("end_stone_brick", false));
            helper.register("granite", PackBuilder.createStone("granite"));
            helper.register("diorite", PackBuilder.createStone("diorite"));
            helper.register("andesite", PackBuilder.createStone("andesite"));
            helper.register("netherbrick", PackBuilder.create("netherbrick", 64 * 8)
                    .put("nether_bricks", 1f)
                    .wallSlabStairs("nether_brick", false)
                    .put("nether_brick_fence", 1.7f) // 5/3
                    .put("chiseled_nether_bricks", 1f)
                    .put("cracked_nether_bricks", 1f));
            helper.register("red_netherbrick", PackBuilder.createWallSlabStairs("red_netherbrick", true));
            helper.register("sandstone", PackBuilder.create("sandstone", 64 * 8)
                    .put("sandstone", 1f)
                    .wallSlabStairs("sandstone", false)
                    .put("cut_sandstone", 1f)
                    .put("cut_sandstone_slab", 0.5f)
                    .put("chiseled_sandstone", 1f));
            helper.register("red_sandstone", PackBuilder.create("red_sandstone", 64 * 8)
                    .put("red_sandstone", 1f)
                    .wallSlabStairs("red_sandstone", false)
                    .put("cut_red_sandstone", 1f)
                    .put("cut_red_sandstone_slab", 0.5f)
                    .put("chiseled_red_sandstone", 1f));
            helper.register("quartz", PackBuilder.create("quartz", 64 * 8)
                    .put("quartz_block", 1f)
                    .slabStairs("quartz")
                    .put("quartz_bricks", 1f)
                    .put("quartz_pillar", 1f)
                    .put("smooth_quartz", 1f)
                    .slabStairs("smooth_quartz")
                    .put("chiseled_quartz_block", 1f));
            helper.register("purpur", PackBuilder.create("purpur", 64 * 8)
                    .put("purpur", 1f)
                    .put("purpur_pillar", 1f)
                    .slabStairs("purpur"));
            helper.register("prismarine", PackBuilder.createWallSlabStairs("prismarine", false));
            helper.register("prismarine_bricks", PackBuilder.createSlabStairs("prismarine_bricks", true));
            helper.register("dark_prismarine", PackBuilder.createSlabStairs("dark_prismarine", false));
            // copper??? waxing and oxidation make this complicated

            if (ModList.get().isLoaded("kubejs")) {
                BuildingPacksKubeJS.REGISTER.post(new BuildingPacksKubeJS.BlockPackRegisterEvent(helper));
            }
        });
    }
}
