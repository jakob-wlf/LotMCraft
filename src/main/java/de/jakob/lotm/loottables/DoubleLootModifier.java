package de.jakob.lotm.loottables;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.jakob.lotm.effect.ModEffects;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DoubleLootModifier extends LootModifier {
    public static final MapCodec<DoubleLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
        codecStart(inst).apply(inst, DoubleLootModifier::new)
    );

    protected DoubleLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }


    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        // Get the player from the loot context
        var player = context.getParamOrNull(LootContextParams.THIS_ENTITY);

        // Check if the entity is a player and has the custom Luck effect
        if (player instanceof Player playerEntity) {
            var luckEffect = playerEntity.getEffect(ModEffects.LUCK);

            if (luckEffect != null) {
                int amplifier = luckEffect.getAmplifier();
                double chance = getExtraLootChance(amplifier);

                // Roll for extra loot
                if (context.getRandom().nextDouble() < chance) {
                    for(int i = 0; i < 2; i++) {
                        // Get the loot table location and convert to ResourceKey
                        var lootTableLocation = context.getQueriedLootTableId();
                        ResourceKey<LootTable> lootTableKey = ResourceKey.create(Registries.LOOT_TABLE, lootTableLocation);

                        var server = context.getLevel().getServer();

                        // Get the loot table
                        LootTable lootTable = server.reloadableRegistries().getLootTable(lootTableKey);

                        // Generate new loot directly without global loot modifiers
                        // This prevents recursive application of modifiers
                        ObjectArrayList<ItemStack> extraLoot = new ObjectArrayList<>();
                        lootTable.getRandomItemsRaw(context, extraLoot::add);

                        // Add the extra loot to the existing loot
                        generatedLoot.addAll(extraLoot);
                    }

                    for(int i = 0; i < generatedLoot.size(); i++) {
                        ItemStack stack = generatedLoot.get(i);
                        if(stack.getMaxStackSize() > 1) {
                            int newCount = Math.min(stack.getCount() * 2, stack.getMaxStackSize());
                            stack.setCount(newCount);
                            generatedLoot.set(i, stack);
                        }
                    }

                    generatedLoot.add(new ItemStack(ChestLootModifier.getRandomLoot()));
                }
            }
        }

        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

    private static double getExtraLootChance(int amplifier) {
        return Math.min(0.06 * (amplifier + 1), 0.95);
    }
}