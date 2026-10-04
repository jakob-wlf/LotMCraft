package de.jakob.lotm.util.helper;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.FogComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.fool.marionettes.ControllingUtils;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.events.custom.StartAdvanceSequencePathwayEvent;
import de.jakob.lotm.beyonders.potions.BeyonderPotion;
import de.jakob.lotm.beyonders.potions.PotionItemHandler;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.*;

import static de.jakob.lotm.util.BeyonderData.*;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class AdvancementUtil {

    private static final HashMap<UUID, BeyonderPotion> activeAdvancements = new HashMap<>();

    private static final List<HashSet<String>> PATHWAY_DOMAINS = List.of(
            new HashSet<>(Set.of("fool", "error", "door")),
            new HashSet<>(Set.of("red_priest", "demoness")),
            new HashSet<>(Set.of("sun", "tyrant", "visionary")),
            new HashSet<>(Set.of("darkness", "death", "twilight_giant"))
    );


    @SubscribeEvent
    public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        if (!activeAdvancements.containsKey(player.getUUID())) return;

        BeyonderPotion potion = activeAdvancements.get(player.getUUID());
        if (!player.getInventory().add(potion.getDefaultInstance()))
            player.drop(potion.getDefaultInstance(), false);
        activeAdvancements.remove(player.getUUID());

        int index = player.getInventory().findSlotMatchingItem(PotionItemHandler.EMPTY_BOTTLE.get().getDefaultInstance());
        if (index != -1) player.getInventory().removeItem(index, 1);
    }

    public static void advance(LivingEntity entity, String pathway, int sequence) {
        if(playerMap == null) return;

        if (entity instanceof Player player && ControllingUtils.isControlling(player)) {
            entity.hurt(ModDamageTypes.source(entity.level(), ModDamageTypes.LOOSING_CONTROL), Float.MAX_VALUE);
            return;
        }

        if (entity instanceof Player player && player.isCreative()) {
            setBeyonder(entity, pathway, sequence);
            return;
        }

        if (entity instanceof Player player) {
            activeAdvancements.put(player.getUUID(), PotionItemHandler.selectPotionOfPathwayAndSequence(null, pathway, sequence));
        }

        float sanity = entity.getData(ModAttachments.SANITY_COMPONENT).getSanity();

        if (!isBeyonder(entity)) {
            advanceFirstTime(entity, pathway, sequence, sanity);
            return;
        }

        String prevPathway = getPathway(entity);
        int prevSequence = getSequence(entity);

        if (!prevPathway.equals(pathway)) {
            advancePathwaySwitch(entity, pathway, sequence, prevPathway, prevSequence);
            return;
        }

        if (prevSequence < sequence) return;

        int difference = Math.abs(prevSequence - sequence);
        double failureChance = difference >= 2? 1.0f : calculateFailureChance(difference, sanity);
        if (BeyonderData.hasSwitchedPathway(entity)) failureChance = Math.min(1.0, failureChance + 0.1);

        if(prevSequence == sequence) {
            advanceSameSequence(entity, pathway, sequence, failureChance);
            return;
        }

        executeAdvancement(entity, pathway, sequence, failureChance, null);
    }

    private static void advanceFirstTime(LivingEntity entity, String pathway, int sequence, float sanity) {
        double failureChance = calculateFailureChanceForFirstTime(sequence, sanity);
        executeAdvancement(entity, pathway, sequence, failureChance, null);
    }

    private static void advancePathwaySwitch(LivingEntity entity, String pathway, int sequence,
                                             String prevPathway, int prevSequence) {
        boolean isSameDomainSwitch = prevSequence <= 5 && sequence == (prevSequence - 1) && sameDomain(prevPathway, pathway);
        double failureChance = isSameDomainSwitch ? 0.0 : 1.0;

        Runnable onSuccess = isSameDomainSwitch
                ? () -> playerMap.recordPathwaySwitch(entity, prevSequence, prevPathway)
                : null;

        executeAdvancement(entity, pathway, sequence, failureChance, onSuccess);
    }

    private static void advanceSameSequence(LivingEntity entity, String pathway, int sequence, double failureChance) {
        int duration = calculateAdvancementDuration(sequence);
        StartAdvanceSequencePathwayEvent event = postAdvancementEvent(entity, sequence, pathway, failureChance, duration);

        double finalFailureChance = event.getFailureChance();

        activeAdvancements.remove(entity.getUUID());

        if (finalFailureChance >= 1.0 || Math.random() < finalFailureChance) {
            if (!entity.isDeadOrDying())
                entity.hurt(ModDamageTypes.source(entity.level(), ModDamageTypes.LOOSING_CONTROL), Float.MAX_VALUE);
            return;
        }

        if (entity.isDeadOrDying()) return;
        BeyonderData.addCharStack(entity, sequence);
        if (entity instanceof Player p) BeyonderData.setDigestionProgress(p, 1.0f);
    }

    // Fires the event, then immediately resolves failure-death or success-setBeyonder.
    // onSuccessPreSet runs before setBeyonder if non-null.
    private static void executeAdvancement(LivingEntity entity, String pathway, int sequence,
                                           double failureChance, Runnable onSuccessPreSet) {
        int duration = calculateAdvancementDuration(sequence);
        StartAdvanceSequencePathwayEvent event = postAdvancementEvent(entity, sequence, pathway, failureChance, duration);

        String finalPathway = event.getPathway();
        int finalSequence = event.getSequence();
        double finalFailureChance = event.getFailureChance();

        activeAdvancements.remove(entity.getUUID());

        if (finalFailureChance >= 1.0 || Math.random() < finalFailureChance) {
            if (!entity.isDeadOrDying())
                entity.hurt(ModDamageTypes.source(entity.level(), ModDamageTypes.LOOSING_CONTROL), Float.MAX_VALUE);
            return;
        }

        if (entity.isDeadOrDying()) return; // died before advancement could resolve; don't apply the sequence-up
        if (onSuccessPreSet != null) onSuccessPreSet.run();
        setBeyonder(entity, finalPathway, finalSequence);
        if (entity instanceof Player p) BeyonderData.setDigestionProgress(p, 1.0f);
    }

    private static StartAdvanceSequencePathwayEvent postAdvancementEvent(LivingEntity entity, int sequence,
                                                                         String pathway, double failureChance, int duration) {
        StartAdvanceSequencePathwayEvent event = new StartAdvanceSequencePathwayEvent(entity, sequence, pathway, failureChance, duration);
        NeoForge.EVENT_BUS.post(event);
        return event;
    }

    private static boolean sameDomain(String pathway1, String pathway2) {
        for (HashSet<String> domain : PATHWAY_DOMAINS) {
            if (domain.contains(pathway1) && domain.contains(pathway2)) return true;
        }
        return false;
    }

    private static int calculateAdvancementDuration(int sequence) {
        int baseSeconds = 5 + (9 - sequence) * 3;
        return 20 * baseSeconds;
    }

    private static double calculateFailureChanceForFirstTime(int sequence, float sanity) {
        if (sequence >= 5) return 0.0;

        if (sanity < 0.2f) return 1.0;

        double baseChance = 1.0;
        double sanityPenalty = sanity < 0.8f ? (0.8f - sanity) * 0.4 : 0;

        return Math.min(1.0, Math.max(0.0, baseChance + sanityPenalty));
    }

    private static double calculateFailureChance(int sequenceDifference, float sanity) {
        if (sanity < 0.2f || sequenceDifference > 2) return 1.0;

        double baseChance;
        if (sequenceDifference <= 1) {
            boolean goodSanity = sanity >= 0.8f;
            baseChance = goodSanity ? 0.0 : 0.6;
        } else {
            baseChance = 0.9;
        }

        double sanityPenalty = sanity < 0.8f ? (0.8f - sanity) * 0.5 : 0;

        return Math.min(1.0, Math.max(0.0, baseChance + sanityPenalty));
    }
}