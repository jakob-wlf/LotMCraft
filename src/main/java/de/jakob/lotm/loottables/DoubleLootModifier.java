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

        if (player instanceof Player) {
            var luckEffect = ((Player) player).getEffect(ModEffects.LUCK);

            if (luckEffect != null) {
                int amplifier = luckEffect.getAmplifier();
                double chance = getExtraLootChance(amplifier);

                // Roll for extra loot
                if (context.getRandom().nextDouble() < chance) {
                    // Get the loot table location and convert to ResourceKey
                    var lootTableLocation = context.getQueriedLootTableId();
                    ResourceKey<LootTable> lootTableKey = ResourceKey.create(Registries.LOOT_TABLE, lootTableLocation);

                    var server = context.getLevel().getServer();

                    // Get the loot table and roll it again
                    LootTable lootTable = server.reloadableRegistries().getLootTable(lootTableKey);

                    // Generate new loot from the same table with the same context
                    // Use getRandomItems with a Consumer
                    lootTable.getRandomItems(context, generatedLoot::add);
                    lootTable.getRandomItems(context, generatedLoot::add);
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