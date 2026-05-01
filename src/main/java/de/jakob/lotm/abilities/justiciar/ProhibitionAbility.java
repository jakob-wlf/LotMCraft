package de.jakob.lotm.abilities.justiciar;

import de.jakob.lotm.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.AbilitySelectionPacket;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ProhibitionAbility extends SelectableAbility {

    public static final List<ProhibitionZone> ACTIVE_ZONES = new CopyOnWriteArrayList<>();
    public static final Map<UUID, Integer> FAIL_COUNT_BY_ENTITY = new ConcurrentHashMap<>();

    private static final double ZONE_RADIUS = 80.0;

    private static final DustParticleOptions DUST_GOLD = new DustParticleOptions(new Vector3f(1.0f, 0.75f, 0.0f), 1.4f);
    private static final DustParticleOptions DUST_PALE = new DustParticleOptions(new Vector3f(1.0f, 0.95f, 0.6f), 0.9f);
    private static final DustParticleOptions DUST_RED  = new DustParticleOptions(new Vector3f(0.9f, 0.15f, 0.05f), 1.1f);

    public ProhibitionAbility(String id) {
        super(id, 15f, "prohibition");
        interactionRadius = 40;
        hasOptimalDistance = false;
        postsUsedAbilityEventManually = true;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("justiciar", 6));
    }

    @Override
    protected float getSpiritualityCost() {
        return 800;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.prohibition.beyonder_abilities",
                "ability.lotmcraft.prohibition.combat",
                "ability.lotmcraft.prohibition.flying",
                "ability.lotmcraft.prohibition.item_use",
                "ability.lotmcraft.prohibition.players",
                "ability.lotmcraft.prohibition.outside_world",
                "ability.lotmcraft.prohibition.stand_ins",
                "ability.lotmcraft.prohibition.marionette_interchange",
                "ability.lotmcraft.prohibition.theft"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if (level.isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) level;

        int casterSeq = BeyonderData.getSequence(entity);
        int zoneDuration = 3600 * (int) Math.max(multiplier(entity) / 4, 1);
        int maxZonesPerType = 3 * (int) Math.max(multiplier(entity) / 4, 1);
        double failChance = casterSeq <= 4 ? 0.15 : 0.4;

        Optional<LivingEntity> resistor = AbilityUtil.getNearbyEntities(entity, serverLevel, entity.position(), (int) ZONE_RADIUS)
                .stream()
                .filter(e -> e != entity && BeyonderData.isBeyonder(e))
                .filter(target -> BeyonderData.getSequence(target) <= casterSeq && random.nextDouble() < failChance)
                .findFirst();

        if (resistor.isPresent()) {
            FAIL_COUNT_BY_ENTITY.merge(resistor.get().getUUID(), 1, Integer::sum);
            spawnResistEffect(serverLevel, entity.position());
            if (entity instanceof ServerPlayer sp) {
                sp.sendSystemMessage(Component.translatable("ability.lotmcraft.prohibition.verdict_failed")
                        .withStyle(ChatFormatting.RED));
            }
            return;
        }

        ProhibitionType type = ProhibitionType.values()[abilityIndex];
        long expiryTick = serverLevel.getGameTime() + zoneDuration;

        List<ProhibitionZone> ownZones = new ArrayList<>(ACTIVE_ZONES.stream()
                .filter(z -> z.ownerId.equals(entity.getUUID()) && z.type == type)
                .sorted(Comparator.comparingLong(z -> z.expiryTick))
                .toList());
        while (ownZones.size() >= maxZonesPerType) {
            ACTIVE_ZONES.remove(ownZones.remove(0));
        }

        long now = serverLevel.getGameTime();
        ACTIVE_ZONES.removeIf(z -> z.expiryTick < now);

        ProhibitionZone newZone = new ProhibitionZone(entity.getUUID(), type, entity.position(), serverLevel, expiryTick, AbilityUtil.getSeqWithArt(entity, this));
        ACTIVE_ZONES.add(newZone);

        spawnProhibitionCastEffect(serverLevel, entity);

        final double px = entity.getX(), py = entity.getY(), pz = entity.getZ();
        EffectManager.playEffect(EffectManager.Effect.PROHIBITION, px, py, pz, serverLevel);
        ServerScheduler.scheduleForDuration(140, 140, zoneDuration, () -> {
            if (!newZone.isActive()) return;
            EffectManager.playEffect(EffectManager.Effect.PROHIBITION, px, py, pz, serverLevel);
        }, null, serverLevel);

        String typeName = type.displayName;
        Component message = Component.literal("§6⚖ §fProhibition declared: §e" + typeName + " §6⚖")
                .withStyle(ChatFormatting.WHITE);

        serverLevel.getServer().getPlayerList().getPlayers().forEach(p -> {
            if (p.level().equals(serverLevel) && p.distanceTo(entity) <= ZONE_RADIUS) {
                p.sendSystemMessage(message);
                p.playNotifySound(SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.7f, 0.45f);
            }
        });

        NeoForge.EVENT_BUS.post(new AbilityUsedEvent(serverLevel, entity.position(), entity, this, interactionFlags, ZONE_RADIUS, 20 * 2));
    }

    @Override
    public void onHold(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        if (!(entity instanceof ServerPlayer player)) return;

        Vec3 pos = entity.position();
        double y = pos.y;

        for (ProhibitionZone zone : ACTIVE_ZONES) {
            if (!zone.ownerId.equals(entity.getUUID())) continue;
            if (!zone.level.equals(level)) continue;

            for (int deg = 0; deg < 360; deg += 4) {
                double rad = Math.toRadians(deg);
                double x = zone.center.x + ZONE_RADIUS * Math.cos(rad);
                double z = zone.center.z + ZONE_RADIUS * Math.sin(rad);

                player.connection.send(new ClientboundLevelParticlesPacket(
                        DUST_GOLD, true, x, y, z, 0, 0.2f, 0, 0, 1
                ));
                if (deg % 12 == 0) {
                    player.connection.send(new ClientboundLevelParticlesPacket(
                            DUST_PALE, true, x, y + 1.0, z, 0, 0.15f, 0, 0, 1
                    ));
                }
            }
        }
    }

    private void spawnProhibitionCastEffect(ServerLevel level, LivingEntity caster) {
        Vec3 center = caster.position();

        level.playSound(null, caster.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.5f, 0.9f);
        level.playSound(null, caster.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0f, 0.4f);
        level.playSound(null, caster.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.7f, 0.5f);

        ServerScheduler.scheduleForDuration(0, 1, 40, () -> {
            for (int deg = 0; deg < 360; deg += 8) {
                double rad = Math.toRadians(deg);
                double fraction = (double) (deg) / 360.0;
                double r = fraction * 20.0;
                double x = center.x + r * Math.cos(rad);
                double z = center.z + r * Math.sin(rad);
                level.sendParticles(DUST_GOLD, x, center.y, z, 1, 0, 0.2, 0, 0);
                if (deg % 24 == 0) {
                    level.sendParticles(DUST_PALE, x, center.y + 0.5, z, 1, 0, 0.3, 0, 0);
                    level.sendParticles(ParticleTypes.END_ROD, x, center.y, z, 1, 0, 0.4, 0, 0.02);
                }
            }
        }, level);

        ServerScheduler.scheduleForDuration(0, 3, 30, () ->
                        ParticleUtil.spawnSphereParticles(level, DUST_GOLD, center.add(0, 1, 0), 1.5, 10),
                level);
    }

    private void spawnResistEffect(ServerLevel level, Vec3 center) {
        level.playSound(null, (int) center.x, (int) center.y, (int) center.z,
                SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.2f, 0.6f);
        level.playSound(null, (int) center.x, (int) center.y, (int) center.z,
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8f, 1.5f);

        ParticleUtil.spawnSphereParticles(level, DUST_RED, center.add(0, 1, 0), 1.5, 24);
        ParticleUtil.spawnSphereParticles(level, DUST_RED, center.add(0, 1, 0), 0.8, 12);
        ParticleUtil.spawnParticles(level, ParticleTypes.SMOKE, center.add(0, 1, 0), 12, 0.6);
    }

    @Override
    public void nextAbility(LivingEntity entity) {
        if (getAbilityNames().length == 0) return;
        if (!selectedAbilities.containsKey(entity.getUUID())) selectedAbilities.put(entity.getUUID(), 0);

        int selected = selectedAbilities.get(entity.getUUID()) + 1;
        if (selected >= getAbilityNames().length) selected = 0;
        selected = clampToSequence(selected, entity);

        selectedAbilities.put(entity.getUUID(), selected);
        PacketHandler.sendToServer(new AbilitySelectionPacket(getId(), selected));
    }

    @Override
    public void previousAbility(LivingEntity entity) {
        if (getAbilityNames().length == 0) return;
        if (!selectedAbilities.containsKey(entity.getUUID())) selectedAbilities.put(entity.getUUID(), 0);

        int selected = selectedAbilities.get(entity.getUUID()) - 1;
        if (selected <= -1) selected = getAbilityNames().length - 1;
        selected = clampToSequence(selected, entity);

        selectedAbilities.put(entity.getUUID(), selected);
        PacketHandler.sendToServer(new AbilitySelectionPacket(getId(), selected));
    }

    private int clampToSequence(int selected, LivingEntity entity) {
        int seq = AbilityUtil.getSeqWithArt(entity, this);
        if (seq > 6 && selected >= 2) return 0;
        if (seq > 4 && selected >= 3) return 0;
        if (seq > 3 && selected >= 4) return 0;
        return selected;
    }

    public enum ProhibitionType {
        BEYONDER_ABILITIES("Beyonder Abilities"),
        COMBAT("Combat"),
        FLYING("Flying"),
        ITEM_USE("Item Use"),
        PLAYERS("Players"),
        OUTSIDE_WORLD("Outside World"),
        STAND_INS("Stand-ins"),
        MARIONETTE_INTERCHANGE("Marionette Interchange"),
        THEFT("Theft");

        public final String displayName;

        ProhibitionType(String name) {
            this.displayName = name;
        }
    }

    public static class ProhibitionZone {
        public final UUID ownerId;
        public final ProhibitionType type;
        public final Vec3 center;
        public final ServerLevel level;
        public final int casterSequence;
        public final long expiryTick;

        public ProhibitionZone(UUID ownerId, ProhibitionType type, Vec3 center, ServerLevel level, long expiryTick, int casterSequence) {
            this.ownerId = ownerId;
            this.type = type;
            this.center = center;
            this.level = level;
            this.expiryTick = expiryTick;
            this.casterSequence = casterSequence;
        }

        public boolean isActive() {
            return level.getGameTime() < expiryTick;
        }

        public boolean isInZone(Vec3 pos, ServerLevel lvl) {
            return lvl.equals(level) && pos.distanceTo(center) <= ZONE_RADIUS;
        }
    }
}