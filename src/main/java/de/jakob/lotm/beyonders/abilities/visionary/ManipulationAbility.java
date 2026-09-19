package de.jakob.lotm.beyonders.abilities.visionary;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
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
            //TODO reimplement
            //case 1 -> control(level, entity);
            //case 2 -> stopControl(level, entity);
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
}
