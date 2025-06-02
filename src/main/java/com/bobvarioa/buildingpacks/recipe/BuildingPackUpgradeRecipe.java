package com.bobvarioa.buildingpacks.recipe;

import com.bobvarioa.buildingpacks.item.BlockPackItem;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraftforge.common.crafting.CraftingHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static com.bobvarioa.buildingpacks.BuildingPacks.MODID;

public class BuildingPackUpgradeRecipe extends ShapedRecipe {
    public BuildingPackUpgradeRecipe(ResourceLocation pId, String pGroup, CraftingBookCategory pCategory, int pWidth, int pHeight, NonNullList<Ingredient> pRecipeItems, ItemStack pResult, boolean pShowNotification) {
        super(pId, pGroup, pCategory, pWidth, pHeight, pRecipeItems, pResult, pShowNotification);
    }

    @Override
    public ItemStack assemble(CraftingContainer pContainer, RegistryAccess pRegistryAccess) {
        var res = super.assemble(pContainer, pRegistryAccess);
        ItemStack blockPack = null;

        for (var item : pContainer.getItems()) {
            if (item.getItem() instanceof BlockPackItem bpi) {
                blockPack = item;
                break;
            }
        }

        if (blockPack != null) {
            res.setTag(blockPack.getTag());
        }

        return res;
    }

    public static class Type implements RecipeType<BuildingPackUpgradeRecipe> {
        @Override
        public String toString() {
            return MODID + ":upgrade";
        }
    }

    public static class Serializer implements RecipeSerializer<BuildingPackUpgradeRecipe> {
        public ShapedRecipe.Serializer serializer = new ShapedRecipe.Serializer();

        @Override
        public BuildingPackUpgradeRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
            var recipe = serializer.fromJson(pRecipeId, pSerializedRecipe);
            return new BuildingPackUpgradeRecipe(
                    recipe.getId(),
                    recipe.getGroup(),
                    recipe.category(),
                    recipe.getRecipeWidth(),
                    recipe.getRecipeHeight(),
                    recipe.getIngredients(),
                    CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(pSerializedRecipe, "result"), true, true),
                    recipe.showNotification()
            );
        }

        @Override
        public @Nullable BuildingPackUpgradeRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            int i = pBuffer.readVarInt();
            int j = pBuffer.readVarInt();
            String s = pBuffer.readUtf();
            CraftingBookCategory craftingbookcategory = pBuffer.readEnum(CraftingBookCategory.class);
            NonNullList<Ingredient> nonnulllist = NonNullList.withSize(i * j, Ingredient.EMPTY);

            for (int k = 0; k < nonnulllist.size(); ++k) {
                nonnulllist.set(k, Ingredient.fromNetwork(pBuffer));
            }

            ItemStack itemstack = pBuffer.readItem();
            boolean flag = pBuffer.readBoolean();
            return new BuildingPackUpgradeRecipe(
                    pRecipeId, s, craftingbookcategory, i, j, nonnulllist, itemstack, flag
            );
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, BuildingPackUpgradeRecipe pRecipe) {
            serializer.toNetwork(pBuffer, pRecipe);
        }
    }
}
