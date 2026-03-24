package de.jakob.lotm.abilities.core.interaction;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.abyss.DefilingSeedAbility;
import de.jakob.lotm.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.abilities.demoness.CharmAbility;
import de.jakob.lotm.abilities.demoness.ThreadManipulationAbility;
import de.jakob.lotm.abilities.tyrant.TorrentialDownpourAbility;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.HashSet;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class InteractionHandler {

    private static final HashSet<Interaction> recentInteractions = new HashSet<>();

    @SubscribeEvent
    public static void onAbilityUsed(AbilityUsedEvent event) {
        recentInteractions.add(new Interaction(event, event.getLevel().getGameTime(), event.getInteractionCacheTime()));
        if (event.getInteractionFlags().contains("freezing"))     handleFreezingInteractions(event);
        if (event.getInteractionFlags().contains("purification")) handlePurificationInteractions(event);
        if (event.getInteractionFlags().contains("burn"))         handleBurnInteractions(event);
        if (event.getInteractionFlags().contains("charm"))        handleCharmInteractions(event);
        if (event.getInteractionFlags().contains("cleansing"))    handleCleansingInteractions(event);
    }

    private static void handleBurnInteractions(AbilityUsedEvent event) {
    }

    private static void handleFreezingInteractions(AbilityUsedEvent event) {
        // Freezing Torrential Downpour
        TorrentialDownpourAbility.getActiveDownpours().stream()
                .filter(downpour -> downpour.loc().isInSameLevel(event.getLevel()))
                .filter(downpour -> downpour.loc().getDistanceTo(event.getPosition()) < event.getInteractionRadius())
                .filter(downpour -> isInteractionOfSameOrHigherSequence(event, downpour.sequence()))
                .forEach(d -> TorrentialDownpourAbility.freezeDownpour(d.downpourId()));
    }

    private static void handlePurificationInteractions(AbilityUsedEvent event) {
        ServerLevel level = event.getLevel();
        double radius = event.getInteractionRadius();

        // Purifying Defiling Seed
        level.getEntitiesOfClass(
                LivingEntity.class,
                AABB.ofSize(event.getPosition(), radius * 2, radius * 2, radius * 2),
                e -> DefilingSeedAbility.getDefiledEntities().containsKey(e.getUUID())
        ).forEach(afflicted -> {
            int casterSequence = DefilingSeedAbility.getDefiledEntities()
                    .get(afflicted.getUUID()).casterSequence();

            if (isInteractionOfSameOrHigherSequence(event, casterSequence)) {
                DefilingSeedAbility.purify(afflicted, event.getEntity(), level);
            }
        });

        // Purification suppresses LOOSING_CONTROL (Frenzy) on nearby entities
        level.getEntitiesOfClass(
                LivingEntity.class,
                AABB.ofSize(event.getPosition(), radius * 2, radius * 2, radius * 2),
                e -> e.hasEffect(ModEffects.LOOSING_CONTROL)
        ).forEach(afflicted -> {
            if(isInteractionOfSameOrHigherSequence(event, BeyonderData.getSequence(afflicted))) {
                afflicted.removeEffect(ModEffects.LOOSING_CONTROL);
            }
        });

        // Purification suppresses MENTAL_PLAGUE on nearby entities
        level.getEntitiesOfClass(
                LivingEntity.class,
                AABB.ofSize(event.getPosition(), radius * 2, radius * 2, radius * 2),
                e -> e.hasEffect(ModEffects.MENTAL_PLAGUE)
        ).forEach(afflicted -> {
            if(isInteractionOfSameOrHigherSequence(event, BeyonderData.getSequence(afflicted))) {
                afflicted.removeEffect(ModEffects.MENTAL_PLAGUE);
            }
        });
    }

    private static void handleCharmInteractions(AbilityUsedEvent event) {
        // Charm interactions are handled within CharmAbility itself
    }

    private static void handleCleansingInteractions(AbilityUsedEvent event) {
        ServerLevel level = event.getLevel();
        double radius = event.getInteractionRadius();

        // Cleansing removes charm from nearby entities
        level.getEntitiesOfClass(
                LivingEntity.class,
                AABB.ofSize(event.getPosition(), radius * 2, radius * 2, radius * 2),
                e -> CharmAbility.getCharmed().containsKey(e.getUUID())
        ).forEach(charmedEntity -> {
            UUID charmCasterUUID = CharmAbility.getCharmed().get(charmedEntity.getUUID());
            if(charmCasterUUID != null) {
                Entity charmCasterEntity = level.getEntity(charmCasterUUID);
                int charmCasterSeq = charmCasterEntity instanceof LivingEntity livingCharmCaster ? BeyonderData.getSequence(livingCharmCaster) : LOTMCraft.NON_BEYONDER_SEQ;
                if(isInteractionOfSameOrHigherSequence(event, charmCasterSeq)) {
                    CharmAbility.removeCharm(charmedEntity.getUUID());
                }
            }
        });
    }

    private static boolean isInteractionOfSameOrHigherSequence(AbilityUsedEvent event, int sequenceToCompare) {
        return BeyonderData.getSequence(event.getEntity()) <= sequenceToCompare;
    }

    public static void cleanupInteractions() {
        recentInteractions.removeIf(interaction -> {
            long gameTime = interaction.event.getLevel().getGameTime();
            return gameTime > interaction.gameTimeOnInteraction + interaction.interactionCacheTime;
        });
    }

    public static boolean isInteractionPossible(Location location, String interactionFlag, int sequence, boolean requireSameOrHigherSequence) {
        return recentInteractions.stream()
                .filter(interaction -> interaction.event.getInteractionFlags().contains(interactionFlag))
                .filter(interaction -> interaction.event.getPosition().distanceTo(location.getPosition()) < interaction.event.getInteractionRadius())
                .anyMatch(interaction -> isInteractionOfSameOrHigherSequence(interaction.event, sequence) || !requireSameOrHigherSequence);
    }

    public static boolean isInteractionPossible(Location location, String interactionFlag, int sequence) {
        return isInteractionPossible(location, interactionFlag, sequence, true);
    }

    public static boolean isInteractionPossible(Location location, String interactionFlag) {
        return isInteractionPossible(location, interactionFlag, LOTMCraft.NON_BEYONDER_SEQ, false);
    }

    /**
     * Check if an interaction is possible with the caster being at least {@code margin} sequences
     * higher (numerically lower) than the given sequence.
     * For example, with margin=1 and sequence=4, only interactions from casters with seq <= 3 will match.
     */
    public static boolean isInteractionPossibleStrictlyHigher(Location location, String interactionFlag, int sequence, int margin) {
        return recentInteractions.stream()
                .filter(interaction -> interaction.event.getInteractionFlags().contains(interactionFlag))
                .filter(interaction -> interaction.event.getPosition().distanceTo(location.getPosition()) < interaction.event.getInteractionRadius())
                .anyMatch(interaction -> BeyonderData.getSequence(interaction.event.getEntity()) + margin <= sequence);
    }

    private record Interaction(AbilityUsedEvent event, long gameTimeOnInteraction, int interactionCacheTime) {}
}
