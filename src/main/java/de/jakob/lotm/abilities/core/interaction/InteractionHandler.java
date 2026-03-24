package de.jakob.lotm.abilities.core.interaction;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.abyss.DefilingSeedAbility;
import de.jakob.lotm.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.abilities.demoness.ThreadManipulationAbility;
import de.jakob.lotm.abilities.tyrant.TorrentialDownpourAbility;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.HashSet;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class InteractionHandler {

    private static final HashSet<Interaction> recentInteractions = new HashSet<>();

    @SubscribeEvent
    public static void onAbilityUsed(AbilityUsedEvent event) {
        recentInteractions.add(new Interaction(event, event.getLevel().getGameTime(), event.getInteractionCacheTime()));
        if (event.getInteractionFlags().contains("freezing"))     handleFreezingInteractions(event);
        if (event.getInteractionFlags().contains("purification")) handlePurificationInteractions(event);
        if (event.getInteractionFlags().contains("burn"))         handleBurnInteractions(event);
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

    private record Interaction(AbilityUsedEvent event, long gameTimeOnInteraction, int interactionCacheTime) {}
}
