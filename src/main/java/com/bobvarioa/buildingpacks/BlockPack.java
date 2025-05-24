package com.bobvarioa.buildingpacks;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockPack {
    private final int maxMaterial;
    private final Map<Block, Float> priceMap;
    private final List<Block> blocks;
    public static Map<Block, Integer> revBlock = new HashMap<>();

    public static List<BlockPack> blockPacks = new ArrayList<>();
    public ResourceLocation id;

    public BlockPack(int pMaxMaterial, ResourceLocation id) {
        maxMaterial = pMaxMaterial;
        priceMap = new HashMap<>();
        blocks = new ArrayList<>();
        this.id = id;
        blockPacks.add(this);
    }

    public int getMaxMaterial() {
        return maxMaterial;
    }

    public float getPrice(Block block) {
        return priceMap.getOrDefault(block, -1f);
    }

    public BlockPack put(Block block, float price) {
        put(block, price, false);
        return this;
    }
    public BlockPack put(Block block, float price, boolean acceptOnly) {
        priceMap.put(block, price);
        if (!acceptOnly) {
            blocks.add(block);
            revBlock.put(block, blocks.size() - 1);
        };
        return this;
    }

    public Block getBlock(int n) {
        if (n >= blocks.size()) return Blocks.AIR;
        return blocks.get(n);
    }
    public int getBlockIndex(Block block) {
        return revBlock.getOrDefault(block, -1);
    }

    public int length() {
        return blocks.size();
    }
}
