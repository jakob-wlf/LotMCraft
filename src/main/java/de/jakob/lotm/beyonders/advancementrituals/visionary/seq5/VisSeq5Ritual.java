package de.jakob.lotm.beyonders.advancementrituals.visionary.seq5;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.advancementrituals.AdvancementRitualManager;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.entity.custom.spirits.*;
import de.jakob.lotm.item.ModIngredients;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Sequence 6 -> 5 Visionary "waking dream" advancement ritual. */
@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public final class VisSeq5Ritual {
    private static final String PATHWAY = "visionary";
    private static final int TARGET_SEQUENCE = 5;
    private static final int FEATHER_COOLDOWN_TICKS = 20 * 60 * 60;
    private static final int MINIMUM_DREAM_TICKS = 20 * 60;
    private static final int DREAM_WINDOW_TICKS = 20 * 60 * 10;

    private static final Map<UUID, Long> featherCooldowns = new HashMap<>();
    // Presence in this map is the source of truth for "dream in progress".
    private static final Map<UUID, Long> dreamDeadlines = new HashMap<>();

    private VisSeq5Ritual() {
    }

    @SubscribeEvent
    public static void onSpiritInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!isSpiritCreature(event.getTarget())) return;
        if (!isCandidate(player)) return;

        long now = player.level().getGameTime();
        Long cooldownEnd = featherCooldowns.get(player.getUUID());
        if (cooldownEnd != null && now < cooldownEnd) {
            player.sendSystemMessage(Component.literal(
                    "The spirits are wary of you; wait before seeking another feather.")
                    .withStyle(ChatFormatting.RED));
            event.setCanceled(true);
            return;
        }

        if (hasFeather(player)) {
            player.sendSystemMessage(Component.literal(
                    "You may only carry one black feather of the monster bird at a time.")
                    .withStyle(ChatFormatting.RED));
            event.setCanceled(true);
            return;
        }

        ItemStack feather = new ItemStack(ModIngredients.BLACK_FEATHER_OF_MONSTER_BIRD.get());
        if (!player.getInventory().add(feather)) player.drop(feather, false);
        featherCooldowns.put(player.getUUID(), now + FEATHER_COOLDOWN_TICKS);
        player.sendSystemMessage(Component.literal(
                "The spirit creature grants you a black feather of the monster bird.")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        boolean candidate = isCandidate(player);

        UUID uuid = player.getUUID();
        if (!dreamDeadlines.containsKey(uuid)) {
            if (candidate && hasFeather(player) && hasQualifyingEmotion(player)
                    && player.hasEffect(ModEffects.ASLEEP)
                    && AdvancementRitualManager.startRitual(player, PATHWAY, TARGET_SEQUENCE, DREAM_WINDOW_TICKS * 10)) {
                dreamDeadlines.put(uuid, player.level().getGameTime() + DREAM_WINDOW_TICKS);
                AdvancementRitualManager.syncProgress(player, 0, MINIMUM_DREAM_TICKS,
                        "Ritual minimum time", true);
            }
            return;
        }

        if (!candidate || !hasFeather(player) || !hasQualifyingEmotion(player)
                || !player.hasEffect(ModEffects.ASLEEP)) {
            failRitual(player, "The ritual conditions were broken.");
            return;
        }

        if (player.level().getGameTime() >= dreamDeadlines.get(uuid)) {
            failRitual(player, "You were not awakened before the ten-minute limit.");
            return;
        }

        if (player.tickCount % 10 == 0) {
            long startedAt = dreamDeadlines.get(uuid) - DREAM_WINDOW_TICKS;
            int elapsedTicks = (int) Math.min(MINIMUM_DREAM_TICKS,
                    player.level().getGameTime() - startedAt);
            AdvancementRitualManager.syncProgress(player, elapsedTicks, MINIMUM_DREAM_TICKS,
                    "Ritual minimum time", true);
        }
    }

    // HIGH priority: must read ASLEEP before AsleepEffect's normal-priority handler strips it.
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onWokenByPlayer(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!dreamDeadlines.containsKey(player.getUUID())) return;

        long deadline = dreamDeadlines.get(player.getUUID());
        if (player.level().getGameTime() >= deadline) {
            failRitual(player, "The ten-minute limit expired before you were awakened.");
            return;
        }

        long startedAt = deadline - DREAM_WINDOW_TICKS;
        long elapsedTicks = player.level().getGameTime() - startedAt;
        if (elapsedTicks < MINIMUM_DREAM_TICKS) {
            long secondsRemaining = (MINIMUM_DREAM_TICKS - elapsedTicks + 19) / 20;
            failRitual(player, "You were hit too early; " + secondsRemaining
                    + " more seconds were required.");
            return;
        }

        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof Player) || attacker == player) return;

        if (!isCandidate(player) || !hasFeather(player)
                || !hasQualifyingEmotion(player) || !player.hasEffect(ModEffects.ASLEEP)) {
            failRitual(player, "The ritual conditions were broken before you were awakened.");
            return;
        }

        dreamDeadlines.remove(player.getUUID());
        if (!AdvancementRitualManager.completeRitual(player)) {
            failRitual(player, "The active ritual could not be completed.");
            return;
        }
        player.removeEffect(ModEffects.JOY);
        player.removeEffect(ModEffects.ANGER);
    }

    private static void failRitual(ServerPlayer player, String reason) {
        dreamDeadlines.remove(player.getUUID());
        AdvancementRitualManager.cancelRitual(player);
        BeyonderData.setCorruption(player, 1.0f);
        player.sendSystemMessage(Component.literal(
            "Ritual failed: " + reason + " Corruption is now 100%.")
                .withStyle(ChatFormatting.RED));
    }

    private static boolean isCandidate(ServerPlayer player) {
        return PATHWAY.equals(BeyonderData.getPathway(player))
                && BeyonderData.getSequence(player) == TARGET_SEQUENCE + 1;
    }

    private static boolean hasFeather(ServerPlayer player) {
        return player.getInventory().findSlotMatchingItem(
                new ItemStack(ModIngredients.BLACK_FEATHER_OF_MONSTER_BIRD.get())) != -1;
    }

    private static boolean hasQualifyingEmotion(ServerPlayer player) {
        return player.hasEffect(ModEffects.JOY) || player.hasEffect(ModEffects.ANGER);
    }

    private static boolean isSpiritCreature(Entity entity) {
        return entity instanceof SpiritBaneEntity
                || entity instanceof SpiritBizarroBaneEntity
                || entity instanceof SpiritBlueWizardEntity
                || entity instanceof SpiritBubblesEntity
                || entity instanceof SpiritDervishEntity
                || entity instanceof SpiritGhostEntity
                || entity instanceof SpiritMalmouthEntity
                || entity instanceof SpiritTranslucentWizardEntity;
    }
}
