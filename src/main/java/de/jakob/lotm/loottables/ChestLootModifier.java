package de.jakob.lotm.loottables;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.jakob.lotm.beyonders.rituals.RitualManager;
import de.jakob.lotm.gui.custom.ritualistic_table.RitualMenu;
import de.jakob.lotm.item.ModIngredients;
import de.jakob.lotm.beyonders.potions.BeyonderCharacteristicItemHandler;
import de.jakob.lotm.beyonders.potions.PotionItemHandler;
import de.jakob.lotm.beyonders.potions.PotionRecipeItemHandler;
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
                ItemStack item = getRandomLoot(pathway, sequence);

                if (item != null && (sequence >= 7)) {
                    generatedLoot.add(item);
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

    public static ItemStack getRandomLoot(String pathway, int sequence) {
        return switch(random.nextInt(8)) {
            case 1,2 -> {
                Item item = ModIngredients.selectRandomIngredientOfPathwayAndSequence(random, pathway, sequence);
                yield item != null ? new ItemStack(item) : null;
            }
            case 3,4 -> {
                Item item = PotionRecipeItemHandler.selectRecipeOfPathwayAndSequence(pathway, sequence);
                yield item != null ? new ItemStack(item) : null;
            }
            case 5,6 -> {
                Item item = BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence(pathway, sequence);
                yield item != null ? new ItemStack(item) : null;
            }
            case 7 -> RitualManager.getRandomRitualBook();
            default -> {
                Item item = PotionItemHandler.selectPotionOfPathwayAndSequence(random, pathway, sequence);
                yield item != null ? new ItemStack(item) : null;
            }
        };
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }


}