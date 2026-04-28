package de.jakob.lotm.abilities.justiciar;

import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.abilities.visionary.PsychologicalInvisibilityAbility;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class EyeOfOrderAbility extends ToggleAbility {

    public static final List<EyeZone> ACTIVE_ZONES = new CopyOnWriteArrayList<>();

    private static final int TICK_RATE = 10;
    private static final int DURATION = 20 * 60 * 5;

    private static final DustParticleOptions GOLD_DUST = new DustParticleOptions(
            new Vector3f(1.0f, 0.9f, 0.3f), 1.0f);
    private static final DustParticleOptions RED_DUST = new DustParticleOptions(
            new Vector3f(0.9f, 0.2f, 0.1f), 1.0f);
    private static final DustParticleOptions BLACK_DUST = new DustParticleOptions(
            new Vector3f(0.05f, 0.05f, 0.05f), 1.0f);

    public EyeOfOrderAbility(String id) {
        super(id, "eye_of_order");
        hasOptimalDistance = false;
        canBeUsedInArtifact = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("justiciar", 8));
    }

    @Override
    protected float getSpiritualityCost() {
        return 0;
    }

    private static int getRadiusForSequence(int seq) {
        return switch (seq) {
            case 8 -> 15;
            case 7 -> 20;
            case 6 -> 25;
            case 5 -> 30;
            case 4 -> 40;
            case 3 -> 50;
            case 2 -> 100;
            case 1 -> 150;
            case 0 -> 250;
            default -> 15;
        };
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if (level.isClientSide) return;

        int sequence = BeyonderData.getSequence(entity);
        int radius = getRadiusForSequence(sequence) *(int) Math.max(multiplier(entity)/4,1);

        BeyonderData.reduceSpirituality(entity, 36);

        EyeZone zone = new EyeZone(entity.getUUID(), entity, (ServerLevel) level, radius);
        ACTIVE_ZONES.add(zone);

        NeoForge.EVENT_BUS.post(new AbilityUsedEvent((ServerLevel) level, entity.position(), entity, this, interactionFlags, radius, 40));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level.isClientSide) return;

        Optional<EyeZone> zoneOpt = ACTIVE_ZONES.stream()
                .filter(z -> z.ownerId.equals(entity.getUUID()))
                .findFirst();

        if (zoneOpt.isEmpty()) return;
        EyeZone zone = zoneOpt.get();

        zone.ticksActive++;
        if (zone.ticksActive >= DURATION) {
            cancel((ServerLevel) level, entity);
            return;
        }

        if (entity.tickCount % TICK_RATE != 0) return;

        BeyonderData.reduceSpirituality(entity, 3);

        if (BeyonderData.getSpirituality(entity) <= 0) {
            if (entity instanceof ServerPlayer player) {
                player.connection.send(new ClientboundSetActionBarTextPacket(
                        Component.literal("Your spirituality is exhausted.").withColor(0xFF422a2a)
                ));
            }


            cancel((ServerLevel) level, entity);
            return;
        }

        applyGlow(zone);
        spawnParticles(zone);
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if (level.isClientSide) return;

        Optional<EyeZone> existing = ACTIVE_ZONES.stream()
                .filter(z -> z.ownerId.equals(entity.getUUID()))
                .findFirst();

        if (existing.isPresent()) {
            EyeZone zone = existing.get();
            cleanupGlow(zone);
            ACTIVE_ZONES.remove(zone);
        }

        NeoForge.EVENT_BUS.post(new AbilityUsedEvent((ServerLevel) level, entity.position(), entity, this, interactionFlags, 0, 0));
    }

    private static void addToTeam(ServerLevel level, LivingEntity entity, String teamName) {
        var scoreboard = level.getScoreboard();
        var team = scoreboard.getPlayerTeam(teamName);

        if (team == null) {
            team = scoreboard.addPlayerTeam(teamName);
            switch (teamName) {
                case "eye_red" -> team.setColor(ChatFormatting.RED);
                case "eye_gold" -> team.setColor(ChatFormatting.GOLD);
                case "eye_black" -> team.setColor(ChatFormatting.DARK_GRAY);
            }
        }

        scoreboard.addPlayerToTeam(entity.getStringUUID(), team);
    }

    private static void removeFromTeams(ServerLevel level, LivingEntity entity) {
        var scoreboard = level.getScoreboard();
        var currentTeam = scoreboard.getPlayersTeam(entity.getStringUUID());

        if (currentTeam != null) {
            String teamName = currentTeam.getName();
            if (teamName.equals("eye_red") || teamName.equals("eye_gold") || teamName.equals("eye_black")) {
                scoreboard.removePlayerFromTeam(entity.getStringUUID(), currentTeam);
            }
        }
    }

    private static void applyGlow(EyeZone zone) {
        LivingEntity owner = zone.owner;
        if (owner == null || owner.isRemoved()) return;

        List<LivingEntity> nearby = owner.level().getEntitiesOfClass(
                LivingEntity.class,
                owner.getBoundingBox().inflate(zone.radius),
                e -> {
                    if (e == owner) return false;

                    if (PsychologicalInvisibilityAbility.invisiblePlayersClient.containsKey(e.getUUID())) {

                        int targetSeq = PsychologicalInvisibilityAbility.invisiblePlayersClient.get(e.getUUID());
                        int selfSeq = BeyonderData.getSequence(owner);

                        if (selfSeq >= targetSeq) {
                            return false;
                        }
                    }

                    return true;
                }
        );

        Set<UUID> nearbyUuids = new HashSet<>();
        for (LivingEntity e : nearby) {
            nearbyUuids.add(e.getUUID());
        }

        for (UUID trackedId : zone.all()) {
            if (!nearbyUuids.contains(trackedId)) {
                LivingEntity e = (LivingEntity) zone.level.getEntity(trackedId);
                if (e != null) {
                    e.setGlowingTag(false);
                    removeFromTeams(zone.level, e);
                }
                zone.redGlow.remove(trackedId);
                zone.blackGlow.remove(trackedId);
                zone.goldGlow.remove(trackedId);
            }
        }

        for (LivingEntity target : nearby) {
            target.setInvisible(false);
            UUID targetId = target.getUUID();

            zone.redGlow.remove(targetId);
            zone.blackGlow.remove(targetId);
            zone.goldGlow.remove(targetId);

            if (target instanceof Mob mob) {
                if (mob.getType().getCategory() == MobCategory.MONSTER || mob.getTarget() != null) {
                    target.setGlowingTag(true);
                    addToTeam(zone.level, target, "eye_red");
                    zone.redGlow.add(targetId);
                    continue;
                }
            }

            if (isEvilDisorderMadness(target)) {
                target.setGlowingTag(true);
                addToTeam(zone.level, target, "eye_black");
                zone.blackGlow.add(targetId);
                continue;
            }

            target.setGlowingTag(true);
            addToTeam(zone.level, target, "eye_gold");
            zone.goldGlow.add(targetId);
        }
    }

    private static void cleanupGlow(EyeZone zone) {
        ServerLevel level = zone.level;
        for (UUID id : zone.all()) {
            LivingEntity e = (LivingEntity) level.getEntity(id);
            if (e != null) {
                e.setGlowingTag(false);
                removeFromTeams(level, e);
            }
        }
    }

    private static void spawnParticles(EyeZone zone) {
        ServerLevel level = zone.level;

        for (UUID id : zone.redGlow) {
            LivingEntity e = (LivingEntity) level.getEntity(id);
            if (e != null) ParticleUtil.spawnParticles(level, RED_DUST, e.position(), 3, 0.2, 0.1);
        }

        for (UUID id : zone.blackGlow) {
            LivingEntity e = (LivingEntity) level.getEntity(id);
            if (e != null) ParticleUtil.spawnParticles(level, BLACK_DUST, e.position(), 3, 0.2, 0.1);
        }

        for (UUID id : zone.goldGlow) {
            LivingEntity e = (LivingEntity) level.getEntity(id);
            if (e != null) ParticleUtil.spawnParticles(level, GOLD_DUST, e.position(), 3, 0.2, 0.1);
        }
    }

    private static boolean isEvilDisorderMadness(LivingEntity entity) {
        return false;
    }

    public static class EyeZone {
        public final UUID ownerId;
        public final LivingEntity owner;
        public final ServerLevel level;
        public final double radius;

        public int ticksActive = 0;

        public final Set<UUID> redGlow = new HashSet<>();
        public final Set<UUID> blackGlow = new HashSet<>();
        public final Set<UUID> goldGlow = new HashSet<>();

        public EyeZone(UUID ownerId, LivingEntity owner, ServerLevel level, double radius) {
            this.ownerId = ownerId;
            this.owner = owner;
            this.level = level;
            this.radius = radius;
        }

        public Set<UUID> all() {
            Set<UUID> all = new HashSet<>();
            all.addAll(redGlow);
            all.addAll(blackGlow);
            all.addAll(goldGlow);
            return all;
        }
    }
}