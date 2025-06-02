package com.bobvarioa.buildingpacks;

import com.bobvarioa.buildingpacks.capabilty.BuildingPower;
import com.bobvarioa.buildingpacks.capabilty.BuildingPowerEvents;
import com.bobvarioa.buildingpacks.capabilty.IBuildingPowersHandler;
import com.bobvarioa.buildingpacks.compat.kubejs.BuildingPacksKubeJS;
import com.bobvarioa.buildingpacks.item.BlockPackItem;
import com.bobvarioa.buildingpacks.item.templates.InventoryListener;
import com.bobvarioa.buildingpacks.register.*;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.*;
import org.slf4j.Logger;

import static net.minecraftforge.common.MinecraftForge.EVENT_BUS;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(BuildingPacks.MODID)
public class BuildingPacks {
    public static final String MODID = "buildingpacks";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static IForgeRegistry<BlockPack> BLOCK_PACKS;
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    private static ItemStack blockPackOf(ResourceLocation id) {
        ItemStack item = new ItemStack(ModItems.REG_BLOCK_PACK.get());
        CompoundTag tag = item.getOrCreateTag();
        if (id == null) return item;
        tag.putString("id", id.toString());
        BlockPack blockPack = BLOCK_PACKS.getValue(id);
        if (blockPack == null) return item;
        tag.putInt("material", blockPack.getMaxMaterial());

        return item;
    }

    public static final RegistryObject<CreativeModeTab> BUILDING_PACKS_TAB = CREATIVE_MODE_TABS.register("block_packs", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> blockPackOf(new ResourceLocation("minecraft", "oak")))
            .displayItems((parameters, output) -> {
                output.accept(ModItems.WRENCH.get());
//                output.accept(ModItems.DRAFTING_PENCIL.get());
//                output.accept(ModItems.BLUEPRINT_DESK.get());
//                output.accept(ModItems.HARD_HAT.get());
//                output.accept(ModItems.CLIPBOARD.get());
                BlockPack.blockPacks.forEach((key) -> {
                    output.accept(blockPackOf(key.id));
                });
            })
            .title(Component.translatable("creative.block_packs"))
            .build());

    public BuildingPacks() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(this::createRegistry);
        bus.addListener(this::onRegister);
        bus.addListener(ModPackets::commonSetup);

        ModItems.register(bus);
        ModBlocks.register(bus);
        ModRecipes.register(bus);
        ModBlockEntities.register(bus);
        CREATIVE_MODE_TABS.register(bus);

        EVENT_BUS.addGenericListener(Entity.class, BuildingPowerEvents::attachCaps);
        EVENT_BUS.addListener(BlockPackItem::pickupItem);
        EVENT_BUS.addListener(EventPriority.LOWEST, InventoryListener::onEnterInventory);
        EVENT_BUS.addListener(EventPriority.LOWEST, InventoryListener::onLeaveInventory);
        EVENT_BUS.addListener(EventPriority.LOWEST, InventoryListener::onPlayerClone);
    }

    private void createRegistry(final NewRegistryEvent event) {
        RegistryBuilder<BlockPack> builder = new RegistryBuilder<>();
        builder.setName(new ResourceLocation("buildingpacks:block_packs"));
        event.create(builder, (reg) -> {
            BLOCK_PACKS = reg;
        });
    }

    private static Block getBlock(String id, String namespace) {
        var block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(namespace, id));
        if (block == null) LOGGER.error(namespace + ":" + id + " does not exist!");
        return block;
    }

    public static class PackBuilder {
        PackBuilder() {
        }

        public static String namespace = "minecraft";

        public BlockPack blockPack;

        public static PackBuilder create(String id, int material) {
            var pack = new PackBuilder();
            pack.blockPack = new BlockPack(material, new ResourceLocation(namespace, id));
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

        public PackBuilder createModStoneAdditions(String type) {
            if (ModList.get().isLoaded("create")) {
                var oldNamespace = PackBuilder.namespace;
                PackBuilder.namespace = "create";
                this.put("cut_" + type, 1f)
                    .wallSlabStairs("cut_" + type, false)
                    .put("polished_cut_" + type, 1f)
                    .wallSlabStairs("polished_cut_" + type, false)
                    .put("cut_" + type + "_bricks", 1f)
                    .wallSlabStairs("cut_" + type + "_brick", false)
                    .put("small_" + type + "_bricks", 1f)
                    .wallSlabStairs("small_" + type + "_brick", false)
                    .put("layered_" + type, 1f)
                    .put(type + "_pillar", 1f);
                PackBuilder.namespace = oldNamespace;
            }
            return this;
        }


        public PackBuilder put(String str, float material) {
            blockPack.put(getBlock(str, namespace), material);
            return this;
        }

        public PackBuilder put(String namespace, String str, float material) {
            blockPack.put(getBlock(str, namespace), material);
            return this;
        }

        public PackBuilder put(String str, float material, boolean acceptOnly) {
            blockPack.put(getBlock(str, namespace), material, acceptOnly);
            return this;
        }

        public BlockPack build(RegisterEvent.RegisterHelper<BlockPack> helper) {
            helper.register(blockPack.id, blockPack);
            return blockPack;
        }

        public static BlockPack createWood(RegisterEvent.RegisterHelper<BlockPack> helper, String type) {
            return createWood(helper, type, false);
        }

        public static BlockPack createWood(RegisterEvent.RegisterHelper<BlockPack> helper, String type, boolean isNether) {
            var pack = PackBuilder.create(type, 64 * 4);

            pack.put(type + "_planks", 1f);
//            if (isNether) {
//                pack.put(type + "_stem", 4f, true)
//                        .put("stripped_" + type + "_stem", 4f, true)
//                        .put(type + "_hyphae", 1.4f, true) // 4/3
//                        .put("stripped_" + type + "_hyphae", 1.4f, true); // 4/3
//            } else {
//                pack.put(type + "_log", 4f, true)
//                        .put("stripped_" + type + "_log", 4f, true)
//                        .put(type + "_wood", 1.4f, true) // 4/3
//                        .put("stripped_" + type + "_wood", 1.4f, true); // 4/3
//            }

            pack.wallSlabStairs(type, true)
                    .pressurePlate(type)
                    .button(type)
                    .put(type + "_door", 2f)
                    .put(type + "_sign", 2.2f)
                    .put(type + "_trapdoor", 3f);

            return pack.build(helper);
        }

        public static BlockPack createTConstructWood(RegisterEvent.RegisterHelper<BlockPack> helper, String type) {
            var pack = PackBuilder.create(type, 64 * 4);

            pack.put(type + "_planks", 1f)
                    // why??? no other mod has this naming convention
                    .slabStairs(type + "_planks")
                    .pressurePlate(type)
                    .button(type)
                    .put(type + "_door", 2f)
                    .put(type + "_sign", 2.2f)
                    .put(type + "_trapdoor", 3f)
                    .put(type + "_fence", 1.7f) // 5/3
                    .put(type + "_fence_gate", 4f);

            return pack.build(helper);
        }

        public static BlockPack createStone(RegisterEvent.RegisterHelper<BlockPack> helper, String type, boolean hasCreate) {
            var pack = PackBuilder.create(type, 64 * 4)
                    .put(type, 1f)
                    .wallSlabStairs(type, false)
                    .put("polished_" + type, 1f)
                    .slabStairs("polished_" + type);
            if (hasCreate) {
                pack.createModStoneAdditions(type);
            }
            return pack.build(helper);
        }

        public static BlockPack createStoneCreate(RegisterEvent.RegisterHelper<BlockPack> helper, String type) {
            return PackBuilder.create(type, 64 * 4)
                    .put(type, 1f)
                    .createModStoneAdditions(type)
                    .build(helper);
        }

        public static BlockPack createWallSlabStairs(RegisterEvent.RegisterHelper<BlockPack> helper, String type, boolean addS) {
            return PackBuilder.create(type, 64 * 4)
                    .put(type + (addS ? "s" : ""), 1f)
                    .wallSlabStairs(type, false)
                    .build(helper);
        }

        public static BlockPack createSlabStairs(RegisterEvent.RegisterHelper<BlockPack> helper, String type, boolean addS) {
            return PackBuilder.create(type, 64 * 4)
                    .put(type + (addS ? "s" : ""), 1f)
                    .slabStairs(type)
                    .build(helper);
        }
    }

    private static ResourceKey<Registry<BlockPack>> BLOCK_PACK_REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation("buildingpacks", "block_packs"));
    private void onRegister(final RegisterEvent event) {
        event.register(BLOCK_PACK_REGISTRY_KEY, helper -> {
            PackBuilder.createWood(helper, "oak");
            PackBuilder.createWood(helper, "spruce");
            PackBuilder.createWood(helper, "acacia");
            PackBuilder.createWood(helper, "birch");
            PackBuilder.createWood(helper, "dark_oak");
            PackBuilder.createWood(helper, "jungle");
            PackBuilder.createWood(helper, "mangrove");
            PackBuilder.createWood(helper, "cherry");
            PackBuilder.createWood(helper, "crimson", true);
            PackBuilder.createWood(helper, "warped", true);
            PackBuilder.create("bamboo", 64 * 4)
                    .put("bamboo_block", 2f, true)
                    .put("bamboo_planks", 1f)
                    .wallSlabStairs("bamboo", true)
                    .pressurePlate("bamboo")
                    .button("bamboo")
                    .put("bamboo_door", 2f)
                    .put("bamboo_sign", 2.2f)
                    .put("bamboo_trapdoor", 3f)
                    .build(helper);
            PackBuilder.create("stone", 64 * 4)
                    .put("stone", 1f)
                    .slabStairs("stone")
                    .pressurePlate("stone")
                    .button("stone")
                    .put("stone_bricks", 1f)
                    .wallSlabStairs("stone_brick", false)
                    .put("chiseled_stone_bricks", 1f)
                    .put("smooth_stone", 1f)
                    .put("smooth_stone_slab", 0.5f)
                    .build(helper);
            PackBuilder.create("deepslate", 64 * 4)
                    .put("deepslate", 1f)
                    .put("deepslate_bricks", 1f)
                    .wallSlabStairs("deepslate_brick", false)
                    .put("deepslate_tiles", 1f)
                    .wallSlabStairs("deepslate_tile", false)
                    .put("cobbled_deepslate", 1f)
                    .wallSlabStairs("cobbled_deepslate", false)
                    .put("polished_deepslate", 1f)
                    .slabStairs("polished_deepslate")
                    .createModStoneAdditions("deepslate")
                    .build(helper);
            PackBuilder.createWallSlabStairs(helper, "cobblestone", false);
            PackBuilder.create("blackstone", 64 * 4)
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
                    .build(helper);
            PackBuilder.create("end_stone", 64 * 4)
                    .put("end_stone", 1f)
                    .put("end_stone_bricks", 1f)
                    .wallSlabStairs("end_stone_brick", false)
                    .build(helper);
            PackBuilder.createStone(helper, "granite", true);
            PackBuilder.createStone(helper, "diorite", true);
            PackBuilder.createStone(helper, "andesite", true);
            PackBuilder.create("netherbrick", 64 * 4)
                    .put("nether_bricks", 1f)
                    .wallSlabStairs("nether_brick", false)
                    .put("nether_brick_fence", 1.7f) // 5/3
                    .put("chiseled_nether_bricks", 1f)
                    .put("cracked_nether_bricks", 1f)
                    .build(helper);
            PackBuilder.createWallSlabStairs(helper, "red_nether_brick", true);
            PackBuilder.create("sandstone", 64 * 4)
                    .put("sandstone", 1f)
                    .wallSlabStairs("sandstone", false)
                    .put("cut_sandstone", 1f)
                    .put("cut_sandstone_slab", 0.5f)
                    .put("chiseled_sandstone", 1f)
                    .build(helper);
            PackBuilder.create("red_sandstone", 64 * 4)
                    .put("red_sandstone", 1f)
                    .wallSlabStairs("red_sandstone", false)
                    .put("cut_red_sandstone", 1f)
                    .put("cut_red_sandstone_slab", 0.5f)
                    .put("chiseled_red_sandstone", 1f)
                    .build(helper);
            PackBuilder.create("quartz", 64 * 4)
                    .put("quartz_block", 1f)
                    .slabStairs("quartz")
                    .put("quartz_bricks", 1f)
                    .put("quartz_pillar", 1f)
                    .put("smooth_quartz", 1f)
                    .slabStairs("smooth_quartz")
                    .put("chiseled_quartz_block", 1f)
                    .build(helper);
            PackBuilder.create("purpur", 64 * 4)
                    .put("purpur_block", 1f)
                    .put("purpur_pillar", 1f)
                    .slabStairs("purpur")
                    .build(helper);
            PackBuilder.createWallSlabStairs(helper, "prismarine", false);
            PackBuilder.createSlabStairs(helper, "prismarine_brick", true);
            PackBuilder.createSlabStairs(helper, "dark_prismarine", false);

            PackBuilder.create("mud", 64*4)
                    .put("mud_bricks", 1f)
                    .put("packed_mud", 1f)
                    .wallSlabStairs("mud_brick", false)
                    .build(helper);

            // copper??? waxing and oxidation make this complicated

            if (ModList.get().isLoaded("biomesoplenty")) {
                PackBuilder.namespace = "biomesoplenty";
                PackBuilder.createWood(helper, "fir");
                PackBuilder.createWood(helper, "redwood");
                PackBuilder.createWood(helper, "mahogany");
                PackBuilder.createWood(helper, "palm");
                PackBuilder.createWood(helper, "willow");
                PackBuilder.createWood(helper, "dead");
                PackBuilder.createWood(helper, "jacaranda");
                PackBuilder.createWood(helper, "magic");
                PackBuilder.createWood(helper, "umbran");
                PackBuilder.createWood(helper, "hellbark");
                PackBuilder.create("white_sandstone", 64 * 4)
                        .put("white_sandstone", 1f)
                        .wallSlabStairs("white_sandstone", false)
                        .put("cut_white_sandstone", 1f)
                        .put("cut_white_sandstone_slab", 0.5f)
                        .put("chiseled_white_sandstone", 1f)
                        .build(helper);
                PackBuilder.create("black_sandstone", 64 * 4)
                        .put("black_sandstone", 1f)
                        .wallSlabStairs("black_sandstone", false)
                        .put("cut_black_sandstone", 1f)
                        .put("cut_black_sandstone_slab", 0.5f)
                        .put("chiseled_black_sandstone", 1f)
                        .build(helper);
                PackBuilder.create("brimstone", 64 * 4)
                        .put("brimstone_bricks", 1f)
                        .wallSlabStairs("brimstone_brick", false)
                        .put("chiseled_brimstone_bricks", 1f)
                        .build(helper);
            }
            if (ModList.get().isLoaded("create")) {
                PackBuilder.namespace = "create";
                PackBuilder.createStoneCreate(helper,"veridium");
                PackBuilder.createStoneCreate(helper,"scorchia");
                PackBuilder.createStoneCreate(helper,"ochrum");
                PackBuilder.createStoneCreate(helper,"crimsite");
                PackBuilder.createStoneCreate(helper,"limestone");
                PackBuilder.createStoneCreate(helper,"asurine");

                PackBuilder.create("calcite", 64 * 4)
                        .put("minecraft", "calcite", 1f)
                        .createModStoneAdditions("calcite")
                        .build(helper);
                PackBuilder.create("dripstone", 64 * 4)
                        .put("minecraft", "dripstone_block", 1f)
                        .createModStoneAdditions("dripstone")
                        .build(helper);
                PackBuilder.create("tuff", 64 * 4)
                        .put("minecraft", "tuff", 1f)
                        .createModStoneAdditions("tuff")
                        .build(helper);

                // ['copycats:copycat_block', 'copycats:copycat_slab', 'copycats:copycat_stairs', 'copycats:copycat_vertical_stairs', 'copycats:copycat_fence', 'copycats:copycat_wall', 'copycats:copycat_vertical_step', 'copycats:copycat_beam', 'copycats:copycat_slice', 'copycats:copycat_vertical_slice', 'copycats:copycat_corner_slice', 'copycats:copycat_ghost_block', 'copycats:copycat_layer', 'copycats:copycat_half_panel', 'copycats:copycat_pane', 'copycats:copycat_flat_pane', 'copycats:copycat_byte', 'copycats:copycat_byte_panel', 'copycats:copycat_board', 'copycats:copycat_catwalk', 'copycats:copycat_box', 'copycats:copycat_half_layer', 'copycats:copycat_vertical_half_layer', 'copycats:copycat_stacked_half_layer', 'copycats:copycat_slope', 'copycats:copycat_vertical_slope', 'copycats:copycat_slope_layer', 'copycats:copycat_door', 'copycats:copycat_iron_door', 'copycats:copycat_sliding_door', 'copycats:copycat_folding_door', 'copycats:copycat_fence_gate']
            }
            if (ModList.get().isLoaded("arsnouveau")) {
                PackBuilder.namespace = "arsnouveau";
                PackBuilder.createWood(helper, "archwood");
                // ['ars_nouveau:sourcestone', 'ars_nouveau:sourcestone_mosaic', 'ars_nouveau:sourcestone_basketweave', 'ars_nouveau:sourcestone_alternating', 'ars_nouveau:sourcestone_large_bricks', 'ars_nouveau:sourcestone_small_bricks', 'ars_nouveau:sourcestone_stairs', 'ars_nouveau:sourcestone_mosaic_stairs', 'ars_nouveau:sourcestone_basketweave_stairs', 'ars_nouveau:sourcestone_alternating_stairs', 'ars_nouveau:sourcestone_large_bricks_stairs', 'ars_nouveau:sourcestone_small_bricks_stairs', 'ars_nouveau:sourcestone_slab', 'ars_nouveau:sourcestone_mosaic_slab', 'ars_nouveau:sourcestone_basketweave_slab', 'ars_nouveau:sourcestone_alternating_slab', 'ars_nouveau:sourcestone_large_bricks_slab', 'ars_nouveau:sourcestone_small_bricks_slab', 'ars_nouveau:gilded_sourcestone_mosaic', 'ars_nouveau:gilded_sourcestone_basketweave', 'ars_nouveau:gilded_sourcestone_alternating', 'ars_nouveau:gilded_sourcestone_large_bricks', 'ars_nouveau:gilded_sourcestone_large_bricks_stairs', 'ars_nouveau:gilded_sourcestone_alternating_stairs', 'ars_nouveau:gilded_sourcestone_basketweave_stairs', 'ars_nouveau:gilded_sourcestone_mosaic_stairs', 'ars_nouveau:gilded_sourcestone_mosaic_slab', 'ars_nouveau:gilded_sourcestone_basketweave_slab', 'ars_nouveau:gilded_sourcestone_alternating_slab', 'ars_nouveau:gilded_sourcestone_large_bricks_slab', 'ars_nouveau:gilded_sourcestone_small_bricks', 'ars_nouveau:gilded_sourcestone_small_bricks_stairs', 'ars_nouveau:gilded_sourcestone_small_bricks_slab']
                // ['ars_nouveau:smooth_gilded_sourcestone_mosaic', 'ars_nouveau:smooth_gilded_sourcestone_basketweave', 'ars_nouveau:smooth_gilded_sourcestone_alternating', 'ars_nouveau:smooth_gilded_sourcestone_large_bricks', 'ars_nouveau:smooth_gilded_sourcestone_small_bricks', 'ars_nouveau:smooth_gilded_sourcestone_mosaic_stairs', 'ars_nouveau:smooth_gilded_sourcestone_basketweave_stairs', 'ars_nouveau:smooth_gilded_sourcestone_alternating_stairs', 'ars_nouveau:smooth_gilded_sourcestone_large_bricks_stairs', 'ars_nouveau:smooth_gilded_sourcestone_small_bricks_stairs', 'ars_nouveau:smooth_gilded_sourcestone_mosaic_slab', 'ars_nouveau:smooth_gilded_sourcestone_basketweave_slab', 'ars_nouveau:smooth_gilded_sourcestone_alternating_slab', 'ars_nouveau:smooth_gilded_sourcestone_large_bricks_slab', 'ars_nouveau:smooth_gilded_sourcestone_small_bricks_slab', 'ars_nouveau:smooth_sourcestone_basketweave', 'ars_nouveau:smooth_sourcestone_mosaic', 'ars_nouveau:smooth_sourcestone_alternating', 'ars_nouveau:smooth_sourcestone_large_bricks', 'ars_nouveau:smooth_sourcestone', 'ars_nouveau:smooth_sourcestone_small_bricks', 'ars_nouveau:smooth_sourcestone_basketweave_stairs', 'ars_nouveau:smooth_sourcestone_mosaic_stairs', 'ars_nouveau:smooth_sourcestone_alternating_stairs', 'ars_nouveau:smooth_sourcestone_large_bricks_stairs', 'ars_nouveau:smooth_sourcestone_stairs', 'ars_nouveau:smooth_sourcestone_small_bricks_stairs', 'ars_nouveau:smooth_sourcestone_basketweave_slab', 'ars_nouveau:smooth_sourcestone_mosaic_slab', 'ars_nouveau:smooth_sourcestone_alternating_slab', 'ars_nouveau:smooth_sourcestone_large_bricks_slab', 'ars_nouveau:smooth_sourcestone_slab', 'ars_nouveau:smooth_sourcestone_small_bricks_slab']
            }
            if (ModList.get().isLoaded("tconstruct")) {
                PackBuilder.namespace = "tconstruct";
                PackBuilder.createTConstructWood(helper, "greenheart");
                PackBuilder.createTConstructWood(helper, "skyroot");
                PackBuilder.createTConstructWood(helper, "bloodshroom");
                PackBuilder.createTConstructWood(helper, "enderbark");

                PackBuilder.create("seared", 64 * 4)
                        .put("seared_stone", 1f)
                        .slabStairs("seared_stone")
                        .put("seared_bricks", 1f)
                        .wallSlabStairs("seared_bricks", false)
                        .put("seared_cracked_bricks", 1f)
                        .put("seared_fancy_bricks", 1f)
                        .put("seared_triangle_bricks", 1f)
                        .put("seared_cobble", 1f)
                        .wallSlabStairs("seared_cobble", false)
                        .put("seared_paver", 1f)
                        .slabStairs("seared_paver")
                        .put("seared_ladder", 1f)
                        .build(helper);

                PackBuilder.create("scorched", 64 * 4)
                        .put("scorched_stone", 1f)
                        .put("polished_scorched_stone", 1f)
                        .put("scorched_bricks", 1f)
                        .slabStairs("scorched_bricks")
                        .put("scorched_bricks_fence", 1.7f)
                        .put("chiseled_scorched_bricks", 1f)
                        .put("scorched_road", 1f)
                        .slabStairs("scorched_road")
                        .put("scorched_ladder", 1f)
                        .build(helper);
            }


            if (ModList.get().isLoaded("kubejs")) {
                BuildingPacksKubeJS.load(helper);
            }
        });
    }
}
