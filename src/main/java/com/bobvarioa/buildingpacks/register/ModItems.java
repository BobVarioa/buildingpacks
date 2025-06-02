package com.bobvarioa.buildingpacks.register;

import com.bobvarioa.buildingpacks.BuildingPacks;
import com.bobvarioa.buildingpacks.item.BlockPackItem;
import com.bobvarioa.buildingpacks.item.DraftingPencil;
import com.bobvarioa.buildingpacks.item.HardHat;
import com.bobvarioa.buildingpacks.item.Wrench;
import net.minecraft.core.Vec3i;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, BuildingPacks.MODID);
    public static final RegistryObject<BlockPackItem> REG_BLOCK_PACK = ITEMS.register("block_pack", () -> new BlockPackItem(new Item.Properties().stacksTo(1), 1));
    public static final RegistryObject<BlockPackItem> MED_BLOCK_PACK = ITEMS.register("med_block_pack", () -> new BlockPackItem(new Item.Properties().stacksTo(1), 2));
    public static final RegistryObject<BlockPackItem> BIG_BLOCK_PACK = ITEMS.register("big_block_pack", () -> new BlockPackItem(new Item.Properties().stacksTo(1), 4));
    public static final RegistryObject<Wrench> WRENCH = ITEMS.register("wrench", () -> new Wrench(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<DraftingPencil> DRAFTING_PENCIL = ITEMS.register("drafting_pencil", () -> new DraftingPencil(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<HardHat> HARD_HAT = ITEMS.register("hard_hat", () -> new HardHat(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<BlockItem> BLUEPRINT_DESK = ITEMS.register("blueprint_desk", () -> new BlockItem(ModBlocks.BLUEPRINT_DESK.get(), new Item.Properties()));

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
