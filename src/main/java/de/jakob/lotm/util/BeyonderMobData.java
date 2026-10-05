package de.jakob.lotm.util;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.potions.BeyonderCharacteristicItem;
import de.jakob.lotm.beyonders.potions.BeyonderCharacteristicItemHandler;
import de.jakob.lotm.beyonders.potions.PotionRecipeItem;
import de.jakob.lotm.beyonders.potions.PotionRecipeItemHandler;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.entity.goals.AbilityUseGoal;
import de.jakob.lotm.entity.goals.RangedCombatGoal;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public final class BeyonderMobData {
    public static final BeyonderMobData beyonderMobConfig = new BeyonderMobData();
    private static final int RECIPE_DROP_CHANCE = 10;
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    static {
        BUILDER.comment("""
                Mobs that spawn as beyonders. Use the entity id from any mod, namespace:id.
                Fixed makes every spawn of that mob the same pathway and sequence.
                Chance lines are percents from 0 to 100.
                A mob listed in fixed ignores its chance lines.
                Chance example for a zombie that is usually normal, and otherwise Death from sequence 9 down to 4:
                "minecraft:zombie,15,death,9"
                "minecraft:zombie,10,death,8"
                "minecraft:zombie,8,death,7"
                "minecraft:zombie,5,death,6"
                "minecraft:zombie,3,death,5"
                "minecraft:zombie,1,death,4"
                """).push("beyonder_mobs");
    }

    private static final ModConfigSpec.ConfigValue<List<? extends String>> FIXED = BUILDER
            .comment("Format: \"namespace:id,pathway,sequence\". Example: \"minecraft:skeleton,death,8\".")
            .defineListAllowEmpty("fixed", List.of(), () -> "", object -> object instanceof String);
    private static final ModConfigSpec.ConfigValue<List<? extends String>> CHANCE = BUILDER
            .comment("Format: \"namespace:id,chance,pathway,sequence\". Example: \"minecraft:zombie,15,death,9\".")
            .defineListAllowEmpty("chance", List.of(), () -> "", object -> object instanceof String);
    public static final ModConfigSpec SPEC = BUILDER.pop().build();

    private final Map<ResourceLocation, Variant> fixed = new HashMap<>();
    private final Map<ResourceLocation, List<Variant>> chance = new HashMap<>();

    private BeyonderMobData() {
    }

    private record Variant(String pathway, int sequence, float chance) {
    }

    @SubscribeEvent
    public static void onConfig(ModConfigEvent event) {
        if (event.getConfig().getSpec() != SPEC) return;
        beyonderMobConfig.reload();
    }

    @SubscribeEvent
    public static void onSpawn(FinalizeSpawnEvent event) {
        beyonderMobConfig.apply(event.getEntity());
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Mob mob)) return;
        if (!event.loadedFromDisk()) beyonderMobConfig.apply(mob);
        if (mob instanceof BeyonderNPCEntity || !mob.getPersistentData().getBoolean("lotmcraft_beyonder_mob_rolled") || !BeyonderData.isBeyonder(mob)) return;
        allowBeyonderAbilities(mob);
        if (!event.loadedFromDisk() && mob.tickCount == 0) mob.setHealth(mob.getMaxHealth());
    }

    @SubscribeEvent
    public static void onDrops(LivingDropsEvent event) {
        if (event.isCanceled() || !(event.getEntity() instanceof Mob mob) || mob instanceof BeyonderNPCEntity) return;
        if (!mob.getPersistentData().getBoolean("lotmcraft_beyonder_mob_rolled") || !BeyonderData.isBeyonder(mob)) return;
        if (mob.getPersistentData().contains("VoidSummoned")) return;
        String pathway = BeyonderData.getPathway(mob);
        int sequence = BeyonderData.getSequence(mob);
        if (BeyonderData.playerMap == null || !BeyonderData.playerMap.check(pathway, sequence)) return;

        BeyonderCharacteristicItem characteristicItem = BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence(pathway, sequence);
        if (characteristicItem != null) event.getDrops().add(drop(mob, new ItemStack(characteristicItem)));
        if (mob.getRandom().nextInt(RECIPE_DROP_CHANCE) == 0) {
            PotionRecipeItem recipeItem = PotionRecipeItemHandler.selectRecipeOfPathwayAndSequence(pathway, sequence);
            if (recipeItem != null) event.getDrops().add(drop(mob, new ItemStack(recipeItem)));
        }
    }

    private static ItemEntity drop(Mob mob, ItemStack stack) {
        return new ItemEntity(mob.level(), mob.getX(), mob.getY(), mob.getZ(), stack);
    }

    private void reload() {
        fixed.clear();
        chance.clear();
        for (String line : FIXED.get()) readFixed(line);
        for (String line : CHANCE.get()) readChance(line);
    }

    private void readFixed(String line) {
        String[] parts = split(line);
        if (parts.length != 3) return;
        ResourceLocation id = id(parts[0]);
        String pathway = pathway(parts[1]);
        Integer sequence = sequence(parts[2]);
        if (id == null || pathway == null || sequence == null) return;
        fixed.put(id, new Variant(pathway, sequence, 100.0F));
    }

    private void readChance(String line) {
        String[] parts = split(line);
        if (parts.length != 4) return;
        ResourceLocation id = id(parts[0]);
        String pathway = pathway(parts[2]);
        Integer sequence = sequence(parts[3]);
        if (id == null || pathway == null || sequence == null) return;
        float percent;
        try {
            percent = Float.parseFloat(parts[1]);
        } catch (NumberFormatException ignored) {
            return;
        }
        if (percent <= 0.0F || percent > 100.0F) return;
        chance.computeIfAbsent(id, key -> new ArrayList<>()).add(new Variant(pathway, sequence, percent));
    }

    private void apply(Mob mob) {
        if (mob.level().isClientSide() || mob instanceof BeyonderNPCEntity || BeyonderData.isBeyonder(mob)) return;
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        if (!fixed.containsKey(id) && !chance.containsKey(id)) return;
        if (mob.getPersistentData().getBoolean("lotmcraft_beyonder_mob_rolled")) return;
        mob.getPersistentData().putBoolean("lotmcraft_beyonder_mob_rolled", true);
        Variant fixedVariant = fixed.get(id);
        if (fixedVariant != null) {
            BeyonderData.setBeyonder(mob, fixedVariant.pathway, fixedVariant.sequence, true, true, true, false);
            allowBeyonderAbilities(mob);
            return;
        }
        float roll = mob.getRandom().nextFloat() * 100.0F;
        float cursor = 0.0F;
        for (Variant variant : chance.getOrDefault(id, List.of())) {
            cursor += variant.chance;
            if (roll < cursor) {
                BeyonderData.setBeyonder(mob, variant.pathway, variant.sequence, true, true, true, false);
                allowBeyonderAbilities(mob);
                return;
            }
        }
    }

    private static void allowBeyonderAbilities(Mob mob) {
        if (!hasGoal(mob, AbilityUseGoal.class)) mob.goalSelector.addGoal(4, new AbilityUseGoal(mob));
        if (!AbilityUseGoal.hasRangedOption(mob) || hasGoal(mob, RangedCombatGoal.class)) return;
        mob.goalSelector.removeAllGoals(goal -> goal instanceof MeleeAttackGoal);
        mob.goalSelector.addGoal(3, new RangedCombatGoal(mob, 1.0D, 8.0F, 16.0F));
    }

    private static boolean hasGoal(Mob mob, Class<? extends Goal> type) {
        return mob.goalSelector.getAvailableGoals().stream().anyMatch(wrapped -> type.isInstance(wrapped.getGoal()));
    }

    private static String[] split(String line) {
        String[] parts = line.split(",");
        for (int i = 0; i < parts.length; i++) parts[i] = parts[i].trim();
        return parts;
    }

    private static ResourceLocation id(String text) {
        return ResourceLocation.tryParse(text);
    }

    private static String pathway(String text) {
        if (!BeyonderData.pathwayInfos.containsKey(text) || text.equals("none") || text.equals("placeholder") || text.isEmpty()) return null;
        return text;
    }

    private static Integer sequence(String text) {
        try {
            int sequence = Integer.parseInt(text);
            return sequence >= 0 && sequence <= 9 ? sequence : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
