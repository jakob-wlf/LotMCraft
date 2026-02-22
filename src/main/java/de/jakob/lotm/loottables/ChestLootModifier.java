package de.jakob.lotm.loottables;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.jakob.lotm.item.ModIngredients;
import de.jakob.lotm.potions.BeyonderCharacteristicItemHandler;
import de.jakob.lotm.potions.PotionItemHandler;
import de.jakob.lotm.potions.PotionRecipeItemHandler;
import de.jakob.lotm.util.BeyonderData;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.function.Supplier;

public class ChestLootModifier extends LootModifier {

    public static final Supplier<MapCodec<ChestLootModifier>> CODEC = () ->
            RecordCodecBuilder.mapCodec(inst -> codecStart(inst).apply(inst, ChestLootModifier::new));

    public ChestLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    private static final Random random = new Random();

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (context.getQueriedLootTableId().getPath().contains("chests/")) {
            if (context.getRandom().nextFloat() < 0.45f) {

                String pathway = BeyonderData.implementedPathways.get(random.nextInt(BeyonderData.implementedPathways.size()));
                int sequence = getWeightedHighSequence();
                Item item = getRandomLoot(pathway, sequence);

                if (item != null) {
                    generatedLoot.add(new ItemStack(item));
                }
            }
        }

        return generatedLoot;
    }

    public static int getWeightedHighSequence() {
        Random random = new Random();
        double normalizedValue = random.nextDouble(); // 0.0 to 1.0

        double weighted = Math.pow(normalizedValue, 0.35); // Lower exponent = stronger bias toward high values

        // Map to range 1-9 (weighted now favors values close to 1.0, which maps to 9)
        return 1 + (int) (weighted * 9);
    }

    public static Item getRandomLoot(String pathway, int sequence) {
        return switch(random.nextInt(4)) {
            case 1 -> ModIngredients.selectRandomIngredientOfPathwayAndSequence(random, pathway, sequence);
            case 2 -> PotionRecipeItemHandler.selectRecipeOfPathwayAndSequence(pathway, sequence);
            case 3 -> BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence(pathway, sequence);
            default -> PotionItemHandler.selectPotionOfPathwayAndSequence(random, pathway, sequence);
        };
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }


}