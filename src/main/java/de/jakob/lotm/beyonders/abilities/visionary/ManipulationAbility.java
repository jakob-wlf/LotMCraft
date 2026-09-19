package de.jakob.lotm.beyonders.abilities.visionary;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.EntityControllingComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.beyonders.abilities.fool.marionettes.ControllingUtils;
import de.jakob.lotm.beyonders.abilities.visionary.handlers.VisionaryHandler;
import de.jakob.lotm.beyonders.abilities.visionary.passives.MetaAwarenessAbility;
import de.jakob.lotm.beyonders.advancementrituals.visionary.seq2.VisSeq2Ritual;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncIntrospectMenuPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import de.jakob.lotm.util.playerMap.Characteristic;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ManipulationAbility extends SelectableAbility {

    public ManipulationAbility(String id) {
        super(id, 5);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 4));
    }

    @Override
    public float getSpiritualityCost() {
        return 1150;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.manipulation.group_incite",
                "ability.lotmcraft.manipulation.control",
                "ability.lotmcraft.manipulation.stop_control"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);

        if(VisionaryHandler.shouldBeAffectedWithMindWorldSeal(entitySeq)){
            AbilityUtil.sendActionBar(entity,
                    Component.translatable("ability.lotmcraft.mind_world_authority_ability.is_sealed")
                            .withColor(0xFFff124d));
            return;
        }

        switch (abilityIndex) {
            case 0 -> groupIncite(level, entity);
            case 1 -> control(level, entity);
            case 2 -> stopControl(level, entity);
        }
    }

    // Caster UUID -> the target's true pathway/sequence/characteristics at the moment control started, so we
    // can force-restore a player target's real data on release instead of trusting ControllingUtil.reset()'s
    // originalBody-based restore path (which is built for the caster's own body, not a possessed player target).
    private static final Map<UUID, TargetSnapshot> targetSnapshotByPlayer = new HashMap<>();
    private static final Set<UUID> activeManipulationControls = new HashSet<>();

    private record TargetSnapshot(String pathway, int sequence, List<Characteristic> characteristics) {}

    private final DustParticleOptions dust = new DustParticleOptions(
            new Vector3f(250 / 255f, 201 / 255f, 102 / 255f),
            1.5f
    );

    public void groupIncite(Level level, LivingEntity entity) {
        if (level.isClientSide) {
            LivingEntity target = AbilityUtil.getTargetEntity(entity, (int) (20*multiplier(entity)), 2);
            if(target == null) return;
            ParticleUtil.spawnSphereParticles((ClientLevel) level, ParticleTypes.SMOKE, target.getEyePosition(), 1, 30);
            ParticleUtil.spawnParticles((ClientLevel) level, dust,  target.getEyePosition(), 40, .5);
            return;
        }
        if (!(level instanceof ServerLevel serverLevel)) return;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, (int) (20 *multiplier(entity)), 2);
        if (target == null) {
            AbilityUtil.sendActionBar(entity,
                    Component.translatable("ability.lotmcraft.frenzy.no_target").withColor(0xFFff124d));
            return;
        }

        int casterSeq = AbilityUtil.getSeqWithArt(entity, this);
        List<LivingEntity> nearby = AbilityUtil.getNearbyEntities(
                entity, serverLevel, entity.position(), 20, false, true);

        if(VisionaryHandler.shouldFailAndTrigger(casterSeq, entity, target, this)){
            return;
        }

        for (LivingEntity nearby_entity : nearby) {
            if (nearby_entity.getUUID().equals(entity.getUUID())) continue;
            if (nearby_entity.getUUID().equals(target.getUUID())) continue;

            if (nearby_entity instanceof ServerPlayer nearbyPlayer) {
                // Force beyonder players of lower sequence to use abilities
                if (!BeyonderData.isBeyonder(nearbyPlayer)) continue;

                if(VisionaryHandler.shouldFailAndTrigger(casterSeq, entity, nearby_entity, this)){
                   continue;
                }

                if (BeyonderData.getSequence(nearbyPlayer) < casterSeq) continue;
                forcePlayerAbilities(nearbyPlayer, target, serverLevel);
            } else if (nearby_entity instanceof Mob mob) {
                // For beyonder mobs, check sequence. For non-beyonder mobs, always incite.
                if (BeyonderData.isBeyonder(mob) && BeyonderData.getSequence(mob) < casterSeq) continue;

                LivingEntity originalTarget = mob.getTarget();
                mob.setTarget(target);

                ServerScheduler.scheduleDelayed(20 * 10, () -> {
                    if (!mob.isRemoved()) {
                        mob.setTarget(originalTarget != null && originalTarget.isAlive()
                                ? originalTarget : null);
                    }
                });
            }
        }
    }

    private void forcePlayerAbilities(ServerPlayer player, LivingEntity target, ServerLevel level) {
        int interval = 20 * 3;
        int duration = 20 * 7;

        String pathway = BeyonderData.getPathway(player);
        int sequence = BeyonderData.getSequence(player);

        ServerScheduler.scheduleForDuration(0, interval, duration, () -> {
            if (player.isRemoved() || !player.isAlive()) return;
            if (target.isRemoved() || !target.isAlive()) return;

            List<Ability> abilities = new ArrayList<>(
                    LOTMCraft.abilityHandler.getByPathwayAndSequence(pathway, sequence));
            if (abilities.isEmpty()) return;

            Ability chosen = abilities.get(random.nextInt(abilities.size()));
            chosen.useAbility(level, player);
        }, level);
    }


    // control individual: directly possess the target's body (walk around as them),
    // leaving the caster's own body behind at their current position.

    private void control(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (!(entity instanceof ServerPlayer player)) return;



        if (ControllingUtils.isControlling(player)) return;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 20, 2);
        if (target == null) {
            AbilityUtil.sendActionBar(entity,
                    Component.translatable("ability.lotmcraft.frenzy.no_target").withColor(0xFFff124d));
            return;
        }

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
        int targetSeq = BeyonderData.getSequence(target);
        String targetPathway = BeyonderData.getPathway(target);
        if(targetPathway.equals("visionary") && targetSeq < entitySeq){
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.dream_traversal.failed").withColor(0xFFff124d));

            if(targetSeq <= 1 && target instanceof ServerPlayer targetPlayer && entity instanceof ServerPlayer entityPlayer){
                MetaAwarenessAbility.onDivined(entityPlayer, targetPlayer);
            }

            return;
        }

        int casterSeq = AbilityUtil.getSeqWithArt(entity, this);
        if (BeyonderData.isBeyonder(target)) {
            double failChance = getControlFailChance(casterSeq, BeyonderData.getSequence(target));
            if (failChance >= 1.0 || random.nextDouble() < failChance) {
                AbilityUtil.sendActionBar(entity,
                        Component.translatable("ability.lotmcraft.manipulation.control.too_strong").withColor(0xFFff124d));
                return;
            }
        }

        if (!target.hasEffect(ModEffects.ASLEEP) && !target.isSleeping()) {
            AbilityUtil.sendActionBar(entity,
                    Component.translatable("ability.lotmcraft.dream_traversal.must_be_asleep").withColor(0xFFff124d));
            return;
        }

        VisionaryHandler.addDreamBond(player, target);

        // Capture only the target's characteristics before possess() swaps body data.
        ArrayList<Characteristic> targetChars = new ArrayList<>();
        for (Characteristic c : BeyonderData.getCharList(target)) {
            targetChars.add(new Characteristic(c.pathway(), c.stack(), c.sequence()));
        }
        float casterSpirituality = BeyonderData.getSpirituality(player);

        // Spawn a body double at the caster's current spot and take direct control of the target.
        ControllingUtils.startControlling(player, target, true, true);
        activeManipulationControls.add(player.getUUID());
        // We manage the controlled body's characteristic list ourselves, so skip
        // ControllingUtil.reset()'s legacy "-1 borrowed characteristic" cleanup on release.

        ArrayList<Characteristic> controlledChars = player.getData(ModAttachments.BEYONDER_COMPONENT).getCharacteristicList();
        controlledChars.clear();
        if (BeyonderData.isBeyonder(target)) {
            // Force-assume the target's pathway/sequence; skip characteristic bookkeeping here since we set
            // the list ourselves right below (avoids ControllingUtil's copy silently no-op'ing or double-stacking).
            BeyonderData.setBeyonder(player, targetPathway, targetSeq, true, false, false, false, false, false, false);

            // Rebuild the characteristic list from scratch: the target's own characteristics, plus a synthetic
            // entry for their assumed pathway/sequence if they didn't have one.
            controlledChars.addAll(targetChars);
            if (controlledChars.stream().noneMatch(c -> c.pathway().equals(targetPathway) && c.sequence() == targetSeq)) {
                controlledChars.add(new Characteristic(targetPathway, 1, targetSeq));
            }
        }

        // The target's asleep state carries over onto the caster; wake them up so they can actually move.
        player.removeEffect(ModEffects.ASLEEP);
        if (player.isSleeping()) player.stopSleeping();

        // Assuming the target's identity shouldn't drain/replace the caster's own spirituality pool.
        BeyonderData.setSpirituality(player, casterSpirituality);

        // Guard against malformed entries reaching the network codec (e.g. a null pathway), and log the
        // final state so a future sync-related disconnect can be diagnosed from the resulting state dump.
        controlledChars.removeIf(c -> c == null || c.pathway() == null);
        LOTMCraft.LOGGER.info("Manipulation control started: caster={} assumedPathway={} assumedSeq={} chars={}",
                player.getUUID(), targetPathway, targetSeq, controlledChars);

        // setBeyonder's own sync packet fired before the characteristic list was finished being rebuilt above;
        // resync now so the client (Introspect screen, ability wheel, etc.) sees the final merged list.
        PacketHandler.syncBeyonderDataToPlayer(player);
        syncIntrospectMenu(player);

        if (BeyonderData.isBeyonder(target)) {
            targetSnapshotByPlayer.put(player.getUUID(), new TargetSnapshot(targetPathway, targetSeq, targetChars));
        }

        int durationTicks = getControlDurationTicks(casterSeq);
        if (durationTicks == PERMANENT_CONTROL) {
            AbilityUtil.sendActionBar(entity,
                    Component.translatable("ability.lotmcraft.manipulation.control.success_permanent").withColor(0xFFe3ffff));
        } else {
            AbilityUtil.sendActionBar(entity,
                    Component.translatable("ability.lotmcraft.manipulation.control.success").withColor(0xFFe3ffff));

            ServerScheduler.scheduleDelayed(durationTicks, () -> endControl(player, serverLevel, target));
        }
    }

    // Manually ends manipulation control early (also used by the timed auto-release).
    private void stopControl(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (!(entity instanceof ServerPlayer player)) return;


        if (!ControllingUtils.isControlling(player)) {
            AbilityUtil.sendActionBar(entity,
                    Component.translatable("ability.lotmcraft.manipulation.stop_control.not_controlling").withColor(0xFFff124d));
            return;
        }

        //ControllingUtils.cancel(player, 0, true, false);
        EntityControllingComponent controllingData = player.getData(ModAttachments.ENTITY_CONTROLLING_COMPONENT);
        UUID targetUUID = controllingData.getControlledEntity().getUUID();
        LivingEntity target = targetUUID != null && serverLevel.getEntity(targetUUID) instanceof LivingEntity le ? le : null;

        endControl(player, serverLevel, target);

        AbilityUtil.sendActionBar(entity,
                Component.translatable("ability.lotmcraft.manipulation.stop_control.success").withColor(0xFFe3ffff));
    }

    private static void endControl(ServerPlayer player, ServerLevel serverLevel, LivingEntity target) {
        EntityControllingComponent controllingData = player.getData(ModAttachments.ENTITY_CONTROLLING_COMPONENT);
        if (!controllingData.isControlling()) return;
        activeManipulationControls.remove(player.getUUID());

        LivingEntity restoredTarget = controllingData.getControlledEntity();

        ControllingUtils.cancel(player, 0, true, false);

        // ControllingUtil.reset() restores a possessed target through paths built for other consumers
        // (e.g. non-players get a brand new UUID on respawn, players go through a path meant for the
        // caster's own body double); force-reapply the exact pre-control snapshot onto whatever entity
        // actually came back so the target's real data never drifts/duplicates across sessions.
        TargetSnapshot snapshot = targetSnapshotByPlayer.remove(player.getUUID());
        if (snapshot != null && restoredTarget != null && restoredTarget.isAlive()) {
            ArrayList<Characteristic> restoredChars = new ArrayList<>();
            for (Characteristic c : snapshot.characteristics()) {
                restoredChars.add(new Characteristic(c.pathway(), c.stack(), c.sequence()));
            }
            restoredTarget.getData(ModAttachments.BEYONDER_COMPONENT).setCharacteristicList(restoredChars);
            BeyonderData.setBeyonder(restoredTarget, snapshot.pathway(), snapshot.sequence(), true, false, false, true, false, false, false);
            if (restoredTarget instanceof ServerPlayer targetPlayer) {
                PacketHandler.syncBeyonderDataToPlayer(targetPlayer);
            }
        }

        // reset()'s internal setBeyonder sync fires before its own characteristic-list assignment finishes,
        // so resync now to make sure the client sees the caster's fully restored data.
        PacketHandler.syncBeyonderDataToPlayer(player);
        syncIntrospectMenu(player);
        if (target != null) {
            VisSeq2Ritual.onManipulationReleased(player, target);
        }
    }

    // The Introspect screen's displayed pathway/sequence/title is a separate client-side snapshot that only
    // updates via this packet (not the general Beyonder-data sync), so it must be sent explicitly on change.
    private static void syncIntrospectMenu(ServerPlayer player) {
        PacketHandler.sendToPlayer(player, new SyncIntrospectMenuPacket(
                BeyonderData.getSequence(player),
                BeyonderData.getPathway(player),
                player.getData(ModAttachments.SANITY_COMPONENT).getSanity(),
                player.getData(ModAttachments.CORRUPTION_COMPONENT).getCorruption()
        ));
    }

    // Runs before ControllingUtil's own logout reset so target snapshots are restored consistently.
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        EntityControllingComponent controllingData = player.getData(ModAttachments.ENTITY_CONTROLLING_COMPONENT);
        if (!controllingData.isControlling() || !activeManipulationControls.contains(player.getUUID())) return;

        UUID targetUUID = controllingData.getControlledEntity().getUUID();
        LivingEntity target = targetUUID != null && serverLevel.getEntity(targetUUID) instanceof LivingEntity le ? le : null;

        endControl(player, serverLevel, target);
    }

    // Manipulation unlocks at Visionary seq 4; that's the 10s baseline, growing at lower sequences.
    // Sequence 0 caps out at 10 minutes; Great Old One grants permanent control (no auto-release) until stopped manually.
    private static final int PERMANENT_CONTROL = -1;

    private static int getControlDurationTicks(int seq) {
        return switch (seq) {
            case 4 -> 20 * 10;
            case 3 -> 20 * 15;
            case 2 -> 20 * 20;
            case 1 -> 20 * 30;
            case 0 -> 20 * 60 * 10;
            case LOTMCraft.GREAT_OLD_ONE_SEQ -> PERMANENT_CONTROL;
            default -> 20 * 10;
        };
    }


    // 0% against weaker targets, 35% at the same sequence, +15% per sequence stronger, always 100% at seq 0 (or Great Old One).
    private static double getControlFailChance(int casterSeq, int targetSeq) {
        if (targetSeq <= 0) return 1.0;
        if (targetSeq > casterSeq) return 0.0;
        int levels = casterSeq - targetSeq;
        return Math.min(0.95, 0.35 + 0.15 * levels);
    }

}
