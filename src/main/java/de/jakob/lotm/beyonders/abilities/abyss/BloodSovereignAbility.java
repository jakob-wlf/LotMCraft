package de.jakob.lotm.beyonders.abilities.abyss;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class BloodSovereignAbility extends SelectableAbility {

    private static final DustParticleOptions BLOOD_RED =
            new DustParticleOptions(new Vector3f(0.72f, 0.00f, 0.05f), 3.0f);
    private static final DustParticleOptions BLOOD_MID =
            new DustParticleOptions(new Vector3f(0.50f, 0.00f, 0.03f), 2.0f);
    private static final DustParticleOptions BLOOD_DARK =
            new DustParticleOptions(new Vector3f(0.28f, 0.00f, 0.02f), 1.5f);
    private static final DustParticleOptions CORRODE_YELLOW =
            new DustParticleOptions(new Vector3f(0.88f, 0.72f, 0.0f), 1.8f);
    private static final DustParticleOptions CORRODE_DARK =
            new DustParticleOptions(new Vector3f(0.50f, 0.35f, 0.0f), 1.2f);

    private static final Set<UUID> corrodedEntities = new HashSet<>();

    public BloodSovereignAbility(String id) {
        super(id, 12f, "blood", "corruption");
        canBeUsedInArtifact = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("abyss", 2));
    }

    @Override
    protected float getSpiritualityCost() {
        return 500;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.blood_sovereign.surge",
                "ability.lotmcraft.blood_sovereign.corrosion"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        switch (abilityIndex) {
            case 0 -> castBloodSurge(serverLevel, entity);
            case 1 -> castBloodCorrosion(serverLevel, entity);
        }
    }

    private void castBloodSurge(ServerLevel level, LivingEntity entity) {
        Vec3 center = entity.position().add(0, 1, 0);
        double range = 25 * multiplier(entity);
        double damage = DamageLookup.lookupDamage(2, 0.9) * multiplier(entity);
        int seq = AbilityUtil.getSeqWithArt(entity, this);

        level.playSound(null, BlockPos.containing(center),
                SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 3.0f, 0.50f);
        level.playSound(null, BlockPos.containing(center),
                SoundEvents.WITHER_AMBIENT, SoundSource.PLAYERS, 2.5f, 0.40f);
        ServerScheduler.scheduleDelayed(4, () ->
                level.playSound(null, BlockPos.containing(center),
                        SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 3.0f, 0.55f), level);

        ParticleUtil.spawnSphereParticles(level, BLOOD_RED,  center, 4,  300);
        ParticleUtil.spawnSphereParticles(level, BLOOD_DARK, center, 6,  200);
        ParticleUtil.spawnParticles(level, ParticleTypes.DAMAGE_INDICATOR, center, 120, 5, 0.12);

        for (int y = 0; y < 12; y++) {
            final int yFinal = y;
            ServerScheduler.scheduleDelayed(y, () -> {
                Vec3 top = center.add(0, yFinal, 0);
                ParticleUtil.spawnParticles(level, BLOOD_RED, top, 12, 0.8, 0.12);
                ParticleUtil.spawnParticles(level, BLOOD_MID, top, 8,  0.6, 0.10);
                level.sendParticles(ParticleTypes.LAVA, top.x, top.y, top.z, 6, 0.5, 0.1, 0.5, 0.0);
            }, level);
        }

        for (int ring = 0; ring < 10; ring++) {
            final int r = ring;
            ServerScheduler.scheduleDelayed(r, () -> {
                double ringRadius = (r + 1) * (range / 10.0);
                int ringPoints = (int) (ringRadius * 7);
                for (int i = 0; i < ringPoints; i++) {
                    double angle = (2.0 * Math.PI * i) / ringPoints;
                    double px = center.x + Math.cos(angle) * ringRadius;
                    double pz = center.z + Math.sin(angle) * ringRadius;
                    level.sendParticles(BLOOD_RED, px, center.y, pz, 1, 0, 0.35, 0, 0.0);
                    if (i % 3 == 0)
                        level.sendParticles(BLOOD_DARK, px, center.y + 0.3, pz, 1, 0, 0.2, 0, 0.0);
                }
                level.sendParticles(ParticleTypes.DAMAGE_INDICATOR,
                        center.x, center.y, center.z,
                        ringPoints / 5, ringRadius * 0.5, 0.2, ringRadius * 0.5, 0.02);
            }, level);
        }

        EffectManager.playEffect(EffectManager.Effect.BLOOD_SURGE, center.x, center.y, center.z, level);

        ServerScheduler.scheduleDelayed(12, () -> {
            AbilityUtil.getNearbyEntities(entity, level, entity.position(), range)
                    .stream()
                    .filter(t -> AbilityUtil.mayDamage(entity, t))
                    .forEach(t -> {
                        if (AbilityUtil.isTargetSignificantlyStronger(seq, BeyonderData.getSequence(t)))
                            return;

                        t.hurt(ModDamageTypes.source(level, ModDamageTypes.BEYONDER_GENERIC, entity),
                                (float) damage);

                        Vec3 tp = t.position().add(0, 1, 0);
                        for (int yy = 0; yy < 7; yy++) {
                            Vec3 col = tp.add(0, yy * 0.8, 0);
                            level.sendParticles(BLOOD_RED,  col.x, col.y, col.z, 5, 0.3, 0.1, 0.3, 0.0);
                            level.sendParticles(BLOOD_DARK, col.x, col.y, col.z, 3, 0.2, 0.08, 0.2, 0.0);
                        }
                        level.sendParticles(ParticleTypes.DAMAGE_INDICATOR,
                                tp.x, tp.y + 5, tp.z, 20, 1.0, 0.4, 1.0, 0.06);
                        level.playSound(null, t.getX(), t.getY(), t.getZ(),
                                SoundEvents.ARMOR_EQUIP_GENERIC, t.getSoundSource(), 1.2f, 0.4f);

                        t.addEffect(new MobEffectInstance(MobEffects.POISON,    20 * 8, 4, false, false));
                        t.addEffect(new MobEffectInstance(MobEffects.WITHER,    20 * 6, 2, false, false));
                        t.addEffect(new MobEffectInstance(MobEffects.DARKNESS,  20 * 5, 3, false, false));
                        t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 4, 3, false, false));

                        if (t.hasData(de.jakob.lotm.attachments.ModAttachments.SANITY_COMPONENT)) {
                            t.getData(de.jakob.lotm.attachments.ModAttachments.SANITY_COMPONENT)
                                    .decreaseSanityWithSequenceDifference(
                                            0.30f * multiplier(entity), t,
                                            seq, BeyonderData.getSequence(t));
                        }
                    });
        }, level);
    }

    private void castBloodCorrosion(ServerLevel level, LivingEntity entity) {
        LivingEntity target = AbilityUtil.getTargetEntity(entity, 30, 2.0f);
        if (target == null) {
            AbilityUtil.sendActionBar(entity,
                    Component.translatable("ability.lotmcraft.blood_sovereign.no_target")
                            .withColor(0x8B0000));
            return;
        }
        if (corrodedEntities.contains(target.getUUID())) return;
        corrodedEntities.add(target.getUUID());

        int seq = AbilityUtil.getSeqWithArt(entity, this);

        level.playSound(null, BlockPos.containing(target.position()),
                SoundEvents.SLIME_SQUISH, SoundSource.PLAYERS, 2.0f, 0.6f);
        level.playSound(null, BlockPos.containing(target.position()),
                SoundEvents.WITHER_AMBIENT, SoundSource.PLAYERS, 1.5f, 1.2f);

        ParticleUtil.spawnSphereParticles(level, BLOOD_RED,      target.position().add(0, 1, 0), 2.5, 120);
        ParticleUtil.spawnSphereParticles(level, CORRODE_YELLOW, target.position().add(0, 1, 0), 2.0,  80);
        ParticleUtil.spawnParticles(level, ParticleTypes.DAMAGE_INDICATOR,
                target.position().add(0, 1, 0), 30, 0.6, 0.10);

        AtomicInteger pulseCount = new AtomicInteger(0);
        final double[] helixAngleOffset = {0.0};

        ServerScheduler.scheduleForDuration(0, 4, 20 * 20, () -> {
                    if (!target.isAlive()) {
                        corrodedEntities.remove(target.getUUID());
                        return;
                    }

                    double elapsed = pulseCount.get() * 4.0 / 20.0;
                    double radius = Math.max(0.5, 2.5 - elapsed * 0.2);
                    helixAngleOffset[0] += 0.35;

                    Vec3 base = target.position();
                    for (int i = 0; i < 20; i++) {
                        double t = (double) i / 20;
                        double angle = helixAngleOffset[0] + t * 4 * Math.PI;
                        double y = base.y + t * 2.2;
                        double x = base.x + radius * Math.cos(angle);
                        double z = base.z + radius * Math.sin(angle);
                        level.sendParticles(BLOOD_RED, x, y, z, 1, 0.03, 0.03, 0.03, 0.0);
                        if (i % 3 == 0)
                            level.sendParticles(CORRODE_YELLOW, x, y, z, 1, 0.02, 0.02, 0.02, 0.0);
                    }
                    for (int i = 0; i < 12; i++) {
                        double t = (double) i / 12;
                        double angle = helixAngleOffset[0] + Math.PI + t * 4 * Math.PI;
                        double y = base.y + t * 2.2;
                        double x = base.x + radius * Math.cos(angle);
                        double z = base.z + radius * Math.sin(angle);
                        level.sendParticles(CORRODE_DARK, x, y, z, 1, 0.02, 0.02, 0.02, 0.0);
                    }

                    if (pulseCount.get() % 10 == 0) {
                        int stage = pulseCount.get() / 10;
                        double escalating = DamageLookup.lookupDps(2, 0.9, 10, 20 * 6)
                                * multiplier(entity) * (1 + stage * 0.3);

                        if (!AbilityUtil.isTargetSignificantlyStronger(seq, BeyonderData.getSequence(target))) {
                            target.hurt(ModDamageTypes.source(level, ModDamageTypes.BEYONDER_GENERIC, entity),
                                    (float) escalating);
                        }
                        target.addEffect(new MobEffectInstance(MobEffects.POISON,  20 * 3, 2 + stage / 2, false, false));
                        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20 * 3, 2 + stage / 2, false, false));

                        double burstR = 1.5 + stage * 0.5;
                        ParticleUtil.spawnSphereParticles(level, BLOOD_RED,      target.position().add(0, 1, 0), burstR, 60 + stage * 30);
                        ParticleUtil.spawnSphereParticles(level, CORRODE_YELLOW, target.position().add(0, 1, 0), burstR * 0.7, 40 + stage * 20);

                        for (int sh = 0; sh < 8 + stage * 6; sh++) {
                            Vec3 shDir = new Vec3(
                                    random.nextDouble() - 0.5,
                                    random.nextDouble() * 0.8,
                                    random.nextDouble() - 0.5).normalize().scale(0.35 + stage * 0.08);
                            Vec3 shPos = target.position().add(0, 1, 0);
                            level.sendParticles(BLOOD_DARK, shPos.x, shPos.y, shPos.z,
                                    1, shDir.x, shDir.y, shDir.z, 0.0);
                        }
                        level.playSound(null, BlockPos.containing(target.position()),
                                SoundEvents.SLIME_BLOCK_BREAK, SoundSource.PLAYERS,
                                1.0f + stage * 0.2f, 0.6f - stage * 0.07f);
                        level.sendParticles(ParticleTypes.DAMAGE_INDICATOR,
                                target.getX(), target.getY() + 1, target.getZ(),
                                12 + stage * 8, 0.8, 0.5, 0.8, 0.06);
                    }

                    pulseCount.incrementAndGet();

                }, () -> corrodedEntities.remove(target.getUUID()),
                level, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), level)));
    }
    @SubscribeEvent
    public static void onTargetDeath(LivingDeathEvent event) {
        corrodedEntities.remove(event.getEntity().getUUID());
    }
}