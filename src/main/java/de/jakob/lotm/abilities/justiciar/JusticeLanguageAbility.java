package de.jakob.lotm.abilities.justiciar;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class JusticeLanguageAbility extends SelectableAbility {

    public static final Set<UUID> FLOG_ACTIVE = ConcurrentHashMap.newKeySet();

    private static final DustParticleOptions DUST_GOLD   = new DustParticleOptions(new Vector3f(1.0f, 0.75f, 0.0f), 1.3f);
    private static final DustParticleOptions DUST_PALE   = new DustParticleOptions(new Vector3f(1.0f, 0.95f, 0.6f), 0.8f);
    private static final DustParticleOptions DUST_RED    = new DustParticleOptions(new Vector3f(0.9f, 0.10f, 0.05f), 1.2f);
    private static final DustParticleOptions DUST_DARK   = new DustParticleOptions(new Vector3f(0.15f, 0.05f, 0.3f), 1.0f);

    public JusticeLanguageAbility(String id) {
        super(id, 4f, "justice_language");
        interactionRadius = 20;
        hasOptimalDistance = false;
        postsUsedAbilityEventManually = true;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("justiciar", 6));
    }

    @Override
    protected float getSpiritualityCost() {
        return 200;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.justice_language.maintain_secrecy",
                "ability.lotmcraft.justice_language.death",
                "ability.lotmcraft.justice_language.flog"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if (level.isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) level;

        switch (abilityIndex) {
            case 0 -> maintainSecrecy(serverLevel, entity);
            case 1 -> death(serverLevel, entity);
            case 2 -> flog(serverLevel, entity);
        }
    }

    private void maintainSecrecy(ServerLevel serverLevel, LivingEntity caster) {
        LivingEntity target = AbilityUtil.getTargetEntity(caster, 20 * (int) Math.max(multiplier(caster) / 4, 1), 1.4f);
        if (target == null) return;

        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 600, 2, false, false));
        target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 600, 1, false, false));

        spawnWordBeam(serverLevel, caster, target, DUST_DARK);
        serverLevel.playSound(null, caster.blockPosition(), SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.PLAYERS, 1.2f, 0.7f);
        serverLevel.playSound(null, caster.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8f, 0.5f);

        ServerScheduler.scheduleForDuration(0, 4, 30, () -> {
            ParticleUtil.spawnSphereParticles(serverLevel, DUST_DARK, target.getEyePosition(), 0.6, 8);
            ParticleUtil.spawnParticles(serverLevel, ParticleTypes.SMOKE, target.getEyePosition(), 4, 0.3);
        }, serverLevel);

        if (caster instanceof ServerPlayer sp) {
            sp.sendSystemMessage(Component.translatable("ability.lotmcraft.justice_language.secrecy_established")
                    .withStyle(ChatFormatting.GOLD));
        }

        NeoForge.EVENT_BUS.post(new AbilityUsedEvent(serverLevel, caster.position(), caster, this, interactionFlags, 20, 20 * 2));
    }

    private void death(ServerLevel serverLevel, LivingEntity caster) {
        LivingEntity target = AbilityUtil.getTargetEntity(caster, 8, 1.4f);
        if (target == null) {
            AbilityUtil.sendActionBar(caster, Component.translatable("lotmcraft.no_target").withStyle(ChatFormatting.GOLD));
            return;
        }

        int targetSeq = BeyonderData.getSequence(target);
        int seq       = BeyonderData.getSequence(caster);
        double failChance = targetSeq < seq ? 0.7 : targetSeq == seq ? 0.3 : 0.05;

        if (random.nextDouble() < failChance) {
            spawnResistEffect(serverLevel, caster, target);
            if (caster instanceof ServerPlayer sp) {
                sp.sendSystemMessage(Component.translatable("ability.lotmcraft.justice_language.verdict_failed")
                        .withStyle(ChatFormatting.RED));
            }
            return;
        }

        serverLevel.playSound(null, caster.blockPosition(), SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.PLAYERS, 1.5f, 1.2f);
        serverLevel.playSound(null, caster.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8f, 1.8f);

        ParticleUtil.spawnSphereParticles(serverLevel, DUST_GOLD, caster.position().add(0, 1, 0), 0.6, 16);
        ParticleUtil.spawnCircleParticles(serverLevel, DUST_RED, caster.position().add(0, 0.05, 0), 0.5, 14);
        if (seq<=5) {
            Vec3 startPos = caster.getEyePosition().subtract(0, .2, 0).add(caster.getLookAngle().normalize());
            serverLevel.playSound(null, startPos.x, startPos.y, startPos.z, SoundEvents.BLAZE_SHOOT, caster.getSoundSource(), 2.0f, .5f);
            target.hurt(ModDamageTypes.source(serverLevel, ModDamageTypes.BEYONDER_GENERIC, caster),
                    (float) DamageLookup.lookupDamage(5, 1.2) * (int )Math.max (multiplier(caster)/4,1));
            target.hurtMarked = true;
            String targetName = target.getDisplayName().getString();
            spawnWordBeam(serverLevel, caster, target, DUST_DARK);
            ServerScheduler.scheduleForDuration(0, 4, 30, () -> {
                ParticleUtil.spawnSphereParticles(serverLevel, DUST_GOLD, target.getEyePosition(), 0.6, 8);
                ParticleUtil.spawnParticles(serverLevel, ParticleTypes.SMOKE, target.getEyePosition(), 4, 0.3);
            }, serverLevel);

            serverLevel.playSound(null, target.blockPosition(), SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 1.0f, 0.6f);

            if (caster instanceof ServerPlayer sp) {
                sp.sendSystemMessage(Component.translatable("ability.lotmcraft.justice_language.death_declared",
                                Component.literal(targetName).withStyle(ChatFormatting.WHITE))
                        .withStyle(ChatFormatting.DARK_RED));
            }
        }else {
            AtomicBoolean done = new AtomicBoolean(false);
            AtomicInteger ticks = new AtomicInteger(0);

            ServerScheduler.scheduleUntil(serverLevel, () -> {
                if (ticks.incrementAndGet() >= 30) {
                    done.set(true);
                    return;
                }

                Vec3 casterCenter = caster.position().add(0, 0.5, 0);
                Vec3 targetCenter = target.position().add(0, 0.5, 0);
                Vec3 dir = targetCenter.subtract(casterCenter).normalize();
                double dist = casterCenter.distanceTo(targetCenter);

                Vec3 trail = caster.position().add(0, 1, 0);
                serverLevel.sendParticles(DUST_GOLD, trail.x, trail.y, trail.z, 3, 0.08, 0.12, 0.08, 0);
                serverLevel.sendParticles(DUST_PALE, trail.x, trail.y, trail.z, 2, 0.05, 0.08, 0.05, 0);
                serverLevel.sendParticles(ParticleTypes.ENCHANT, trail.x, trail.y, trail.z, 2, 0.1, 0.2, 0.1, 0.06);

                Vec3 behind = caster.position().add(0, 1, 0).subtract(dir.scale(0.8));
                serverLevel.sendParticles(DUST_GOLD, behind.x, behind.y, behind.z, 2, 0.06, 0.06, 0.06, 0);

                if (dist <= 1.8) {
                    done.set(true);
                    spawnDeathImpact(serverLevel, caster, target, dir);
                    return;
                }

                caster.setDeltaMovement(dir.scale(1.5));
                caster.hurtMarked = true;
                caster.hasImpulse = true;

            }, 1, null, done, () -> AbilityUtil.getTimeInArea(caster, new de.jakob.lotm.util.data.Location(caster.position(), serverLevel)));
        };
        NeoForge.EVENT_BUS.post(new AbilityUsedEvent(serverLevel, caster.position(), caster, this, interactionFlags, 20, 20 * 2));
    }

    private void spawnDeathImpact(ServerLevel serverLevel, LivingEntity caster, LivingEntity target, Vec3 dir) {
        target.hurt(ModDamageTypes.source(serverLevel, ModDamageTypes.BEYONDER_GENERIC, caster),
                (float) DamageLookup.lookupDamage(6, 0.9) * (int )Math.max (multiplier(caster)/4,1));
        target.setDeltaMovement(dir.x * 2.2, 0.45, dir.z * 2.2);
        target.hurtMarked = true;

        serverLevel.playSound(null, target.blockPosition(), SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.PLAYERS, 1.2f, 0.6f);
        serverLevel.playSound(null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.9f, 0.5f);
        serverLevel.playSound(null, target.blockPosition(), SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 1.0f, 0.6f);

        Vec3 impact = target.position().add(0, 1, 0);
        ParticleUtil.spawnSphereParticles(serverLevel, DUST_RED,  impact, 1.1, 28);
        ParticleUtil.spawnSphereParticles(serverLevel, DUST_GOLD, impact, 0.8, 20);
        ParticleUtil.spawnSphereParticles(serverLevel, DUST_PALE, impact, 1.4, 14);
        serverLevel.sendParticles(ParticleTypes.CRIT,    impact.x, impact.y, impact.z, 18, 0.4, 0.4, 0.4, 0.15);
        serverLevel.sendParticles(ParticleTypes.ENCHANT, impact.x, impact.y, impact.z, 20, 0.5, 0.5, 0.5, 0.12);

        ParticleUtil.spawnCircleParticles(serverLevel, DUST_GOLD, target.position().add(0, 0.05, 0), 1.2, 22);
        ParticleUtil.spawnCircleParticles(serverLevel, DUST_RED,  target.position().add(0, 0.05, 0), 0.8, 16);
    }

    private void flog(ServerLevel serverLevel, LivingEntity caster) {
        FLOG_ACTIVE.add(caster.getUUID());

        serverLevel.playSound(null, caster.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 1.6f);
        serverLevel.playSound(null, caster.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.5f, 1.4f);

        ParticleUtil.spawnCircleParticles(serverLevel, DUST_GOLD, caster.position().add(0, 0.1, 0), 0.7, 16);
        ParticleUtil.spawnSphereParticles(serverLevel, DUST_PALE, caster.position().add(0, 1, 0), 0.8, 12);

        AbilityUtil.sendActionBar(caster, Component.literal("§6⚖ §eNext strike §fwill §eFlog §6⚖"));

        if (caster instanceof ServerPlayer sp) {
            sp.sendSystemMessage(Component.translatable("ability.lotmcraft.justice_language.flog_prepared")
                    .withStyle(ChatFormatting.GOLD));
        }

        NeoForge.EVENT_BUS.post(new AbilityUsedEvent(serverLevel, caster.position(), caster, this, interactionFlags, 5, 20 * 2));
    }

    private void spawnWordBeam(ServerLevel level, LivingEntity caster, LivingEntity target, DustParticleOptions dust) {
        Vec3 from = caster.getEyePosition();
        Vec3 to = target.getEyePosition();
        ParticleUtil.drawParticleLine(level, dust, from, to, 0.2, 2);
        ParticleUtil.drawParticleLine(level, DUST_GOLD, from, to, 0.4, 1);
        ParticleUtil.spawnSphereParticles(level, DUST_GOLD, caster.getEyePosition(), 0.4, 8);
    }

    private void spawnResistEffect(ServerLevel level, LivingEntity caster, LivingEntity target) {
        level.playSound(null, caster.blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.2f, 0.7f);
        level.playSound(null, caster.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.6f, 1.6f);
        ParticleUtil.spawnSphereParticles(level, DUST_RED, target.position().add(0, 1, 0), 1.2, 20);
        ParticleUtil.spawnParticles(level, ParticleTypes.SMOKE, target.position().add(0, 1, 0), 10, 0.5);
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;
        if (!FLOG_ACTIVE.remove(attacker.getUUID())) return;
        if (!(attacker.level() instanceof ServerLevel serverLevel)) return;

        LivingEntity target = event.getEntity();

        serverLevel.playSound(null, target.blockPosition(), SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 1.0f, 0.5f);
        serverLevel.playSound(null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.7f, 1.2f);
        ParticleUtil.spawnSphereParticles(serverLevel, DUST_RED, target.position().add(0, 1, 0), 1.0, 16);
        ParticleUtil.spawnCircleParticles(serverLevel, DUST_GOLD, target.position().add(0, 0.1, 0), 1.2, 20);

        ServerScheduler.scheduleRepeating(0, 20, 5, () -> {
            if (!target.isAlive()) return;
            float dmg = target.getMaxHealth() * 0.12f;
            target.setHealth(Math.max(0, target.getHealth() - dmg));
            target.hurtMarked = true;
            serverLevel.playSound(null, target.blockPosition(), SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 0.6f, 0.7f);
            ParticleUtil.spawnParticles(serverLevel, DUST_RED, target.position().add(0, 1, 0), 6, 0.4);
        }, serverLevel, () -> target.isAlive());

        AbilityUtil.sendActionBar(attacker, Component.literal("§6⚖ §eFlog §fapplied §6⚖"));
    }
}