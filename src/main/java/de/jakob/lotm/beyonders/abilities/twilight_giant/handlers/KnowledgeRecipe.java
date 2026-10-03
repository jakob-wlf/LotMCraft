package de.jakob.lotm.beyonders.abilities.twilight_giant.handlers;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.twilight_giant.KnowledgeAbility;
import de.jakob.lotm.beyonders.potions.BeyonderPotion;
import de.jakob.lotm.beyonders.potions.PotionRecipeItem;
import de.jakob.lotm.beyonders.potions.PotionRecipes;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

public class KnowledgeRecipe extends CustomRecipe {

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, LOTMCraft.MOD_ID);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<KnowledgeRecipe>> SERIALIZER =
            SERIALIZERS.register("knowledge", () -> new SimpleCraftingRecipeSerializer<>(KnowledgeRecipe::new));

    private static final int GRID_START = 1;
    private static final int GRID_END = 9;

    public KnowledgeRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (level.isClientSide() || !hasKnowledgeCrafter(input, level)) return false;
        return !result(input).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return result(input);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.getItem() instanceof PotionRecipeItem) {
                remaining.set(i, stack.copyWithCount(1));
            } else if (stack.hasCraftingRemainingItem()) {
                remaining.set(i, stack.getCraftingRemainingItem());
            }
        }
        return remaining;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER.get();
    }

    private static ItemStack result(CraftingInput input) {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < input.size(); i++) {
            if (!input.getItem(i).isEmpty()) items.add(input.getItem(i));
        }
        if (items.size() == 3) return oil(items);
        if (items.size() == 4) return potion(items);
        return ItemStack.EMPTY;
    }

    private static ItemStack oil(List<ItemStack> items) {
        List<Item> kinds = items.stream().map(ItemStack::getItem).toList();
        if (!kinds.contains(DemonHunterOil.BASE)) return ItemStack.EMPTY;
        for (DemonHunterOil.Type type : DemonHunterOil.Type.values()) {
            if (kinds.containsAll(type.ingredients())) return DemonHunterOil.create(type);
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack potion(List<ItemStack> items) {
        PotionRecipeItem recipeItem = null;
        List<ItemStack> ingredients = new ArrayList<>();
        for (ItemStack stack : items) {
            if (stack.getItem() instanceof PotionRecipeItem recipe && recipeItem == null) {
                recipeItem = recipe;
            } else {
                ingredients.add(stack.copyWithCount(1));
            }
        }
        if (recipeItem == null || recipeItem.getRecipe() == null || ingredients.size() != 3) return ItemStack.EMPTY;

        for (int main = 0; main < 3; main++) {
            BeyonderPotion potion = PotionRecipes.getByIngredients(ingredients.get((main + 1) % 3), ingredients.get((main + 2) % 3), ingredients.get(main));
            if (potion == null) continue;
            BeyonderPotion expected = recipeItem.getRecipe().potion();
            if (expected.getSequence() != potion.getSequence() || !expected.getPathway().equals(potion.getPathway())) continue;
            if (!BeyonderData.playerMap.check(potion.getPathway(), potion.getSequence())) return ItemStack.EMPTY;
            return new ItemStack(potion);
        }
        return ItemStack.EMPTY;
    }

    private static boolean hasKnowledgeCrafter(CraftingInput input, Level level) {
        ItemStack sample = ItemStack.EMPTY;
        for (int i = 0; i < input.size() && sample.isEmpty(); i++) sample = input.getItem(i);
        if (sample.isEmpty()) return false;
        for (Player player : level.players()) {
            if (!(player.containerMenu instanceof CraftingMenu menu) || !KnowledgeAbility.isActive(player)) continue;
            for (int slot = GRID_START; slot <= GRID_END; slot++) {
                if (ItemStack.matches(menu.getSlot(slot).getItem(), sample)) return true;
            }
        }
        return false;
    }
}
