package de.jakob.lotm.beyonders.abilities.twilight_giant.handlers;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.MultiplierModifierComponent;
import de.jakob.lotm.beyonders.abilities.justiciar.LawAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public final class TwilightAging {

    public static final int TWILIGHT_TEXT = 0xFFFF6A2A;
    public static final DustParticleOptions TWILIGHT_DUST = new DustParticleOptions(new Vector3f(1f, 0.35f, 0.1f), 1.8f);
    public static final DustParticleOptions ASH_DUST = new DustParticleOptions(new Vector3f(0.55f, 0.5f, 0.45f), 1.2f);
    private static final String YEARS = "lotmcraft_twilight_years";
    private static final String AGING_MODIFIER = "twilight_aging";
    private static final String RECOVER_AT = "lotmcraft_twilight_recover_at";
    private static final String CLEAR_AT = "lotmcraft_twilight_clear_at";
    private static final String FADING = "lotmcraft_twilight_fading";
    private static final float[] YEAR_LIMITS = {200f, 150f, 120f, 100f, 80f, 50f, 40f, 30f, 20f, 10f};
    private static final float MASS_CLEAR_YEARS = 100f;
    private static final int RECOVER_TICKS = 20 * 60;
    private static final int CLEAR_TICKS = 20 * 300;
    private static final int DEBUFF_TICKS = 40;
    private static final float YEARS_PER_AMPLIFIER = 4f;
    private static final int MAX_AMPLIFIER = 4;
    private static final float WEAKER_BONUS_PER_SEQUENCE = 0.25f;
    private static final float STRONGER_PENALTY_PER_SEQUENCE = 0.4f;
    private static final float MIN_RESISTANCE = 0.1f;
    private static final int MAX_WEAKER_DIFFERENCE = 4;
    private static final int FADE_TICKS = 40;

    private static final Set<UUID> fading = new HashSet<>();
    private static final Set<UUID> aged = new HashSet<>();

    private TwilightAging() {
    }

    public static void age(ServerLevel level, LivingEntity target, LivingEntity source, float years) {
        age(level, target, source, years, false);
    }

    public static void age(ServerLevel level, LivingEntity target, LivingEntity source, float years, boolean fade) {
        if (!target.isAlive() || fading.contains(target.getUUID())) return;
        if (target instanceof Player player && (player.isCreative() || player.isSpectator())) return;
        if (immune(source, target)) return;
        float amount = years * resistance(source, target);
        if (amount <= 0) return;
        float total = target.getPersistentData().getFloat(YEARS) + amount;
        target.getPersistentData().putFloat(YEARS, total);
        aged.add(target.getUUID());
        scheduleRecovery(target, level.getGameTime());
        applyDebuffs(target, total);
        syncAgingModifier(target, total);
        double width = target.getBbWidth() * 0.5;
        level.sendParticles(ASH_DUST, target.getX(), target.getY(0.5), target.getZ(), 6, width, target.getBbHeight() * 0.4, width, 0.01);
        if (total >= yearLimit(target) && BeyonderData.getSequence(target) > 0) {
            if (fade) fade(level, target, source);
            else dieOfOldAge(level, target, source);
        }
    }

    public static boolean isAged(LivingEntity entity) {
        return entity.getPersistentData().getFloat(YEARS) > 0f;
    }

    public static void clear(LivingEntity entity) {
        finish(entity);
    }

    public static void fade(ServerLevel level, LivingEntity target, LivingEntity source) {
        UUID id = target.getUUID();
        if (!fading.add(id)) return;
        Vec3 spot = target.position();
        if (target instanceof Mob mob) {
            mob.setNoAi(true);
            mob.setDeltaMovement(Vec3.ZERO);
            mob.getPersistentData().putBoolean(FADING, true);
        } else {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, FADE_TICKS + 10, 9, false, false));
        }
        ServerScheduler.scheduleForDuration(0, 2, FADE_TICKS, () -> {
            if (target instanceof Mob && target.isAlive()) target.teleportTo(spot.x, spot.y, spot.z);
            double width = target.getBbWidth() * 0.6;
            level.sendParticles(TWILIGHT_DUST, spot.x, spot.y + target.getBbHeight() * 0.5, spot.z, 8, width, target.getBbHeight() * 0.5, width, 0.02);
            level.sendParticles(ASH_DUST, spot.x, spot.y + target.getBbHeight() * 0.7, spot.z, 6, width, target.getBbHeight() * 0.3, width, 0.04);
        }, () -> {
            fading.remove(id);
            if (target instanceof Mob mob) {
                mob.getPersistentData().remove(FADING);
                mob.setNoAi(false);
            }
            if (!target.isRemoved() && target.deathTime <= 0) dieOfOldAge(level, target, source);
        }, level);
    }

    public static void dieOfOldAge(ServerLevel level, LivingEntity target, LivingEntity source) {
        if (target.isRemoved() || target.deathTime > 0) return;
        if (target instanceof Player player && (player.isCreative() || player.isSpectator())) return;
        UUID id = target.getUUID();
        boolean fool = "fool".equals(BeyonderData.getPathway(target));
        if (!fool) LawAbility.SOLACE_KILLED.add(id);
        target.invulnerableTime = 0;
        float before = target.getHealth();
        DamageSource damage = ModDamageTypes.source(level, ModDamageTypes.BEYONDER_GENERIC, source);
        if (!Float.isFinite(before)) {
            target.setHealth(0f);
            target.die(damage);
        } else {
            float strike = before + target.getAbsorptionAmount() + 1f;
            target.hurt(damage, Float.isFinite(strike) ? strike : before + 1f);
            if (target.isAlive() && target.deathTime <= 0) {
                target.invulnerableTime = 0;
                if (!(fool && Float.isFinite(target.getHealth()) && target.getHealth() >= before)) {
                    target.setHealth(0f);
                    target.die(damage);
                }
            }
        }
        if (!fool) ServerScheduler.scheduleDelayed(2, () -> LawAbility.SOLACE_KILLED.remove(id), level);
    }

    private static boolean immune(LivingEntity source, LivingEntity target) {
        if (source == null || !"twilight_giant".equals(BeyonderData.getPathway(source)) || !"twilight_giant".equals(BeyonderData.getPathway(target))) return false;
        return BeyonderData.getSequence(target) < BeyonderData.getSequence(source);
    }

    private static float resistance(LivingEntity source, LivingEntity target) {
        int difference = BeyonderData.getSequence(target) - BeyonderData.getSequence(source);
        if (difference >= 0) return 1 + WEAKER_BONUS_PER_SEQUENCE * Math.min(difference, MAX_WEAKER_DIFFERENCE);
        return Math.max(MIN_RESISTANCE, 1 + STRONGER_PENALTY_PER_SEQUENCE * difference);
    }

    private static float yearLimit(LivingEntity target) {
        int sequence = BeyonderData.getSequence(target);
        if (sequence <= 0) return YEAR_LIMITS[0];
        if (sequence >= YEAR_LIMITS.length) return YEAR_LIMITS[YEAR_LIMITS.length - 1];
        return YEAR_LIMITS[sequence];
    }

    private static float recoverableHealth(LivingEntity target) {
        float blocked = Math.min(1f, target.getPersistentData().getFloat(YEARS) / yearLimit(target));
        return target.getMaxHealth() * (1f - blocked);
    }

    private static void applyDebuffs(LivingEntity target, float years) {
        if (fading.contains(target.getUUID())) return;
        int amplifier = Math.min(MAX_AMPLIFIER, (int) (years / YEARS_PER_AMPLIFIER));
        target.forceAddEffect(new MobEffectInstance(MobEffects.WEAKNESS, DEBUFF_TICKS, amplifier, false, false), null);
        target.forceAddEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, DEBUFF_TICKS, amplifier, false, false), null);
        target.forceAddEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, DEBUFF_TICKS, amplifier, false, false), null);
    }

    private static void scheduleRecovery(LivingEntity target, long now) {
        if (target.getPersistentData().getFloat(YEARS) > MASS_CLEAR_YEARS) {
            if (target.getPersistentData().getLong(CLEAR_AT) == 0L) target.getPersistentData().putLong(CLEAR_AT, now + CLEAR_TICKS);
            return;
        }
        target.getPersistentData().remove(CLEAR_AT);
        if (target.getPersistentData().getLong(RECOVER_AT) == 0L) target.getPersistentData().putLong(RECOVER_AT, now + RECOVER_TICKS);
    }

    private static void process(LivingEntity target) {
        if (!(target.level() instanceof ServerLevel level) || !target.isAlive()) return;
        float years = target.getPersistentData().getFloat(YEARS);
        if (years <= 0f) {
            finish(target);
            return;
        }
        long now = level.getGameTime();
        if (years > MASS_CLEAR_YEARS) {
            long clearAt = target.getPersistentData().getLong(CLEAR_AT);
            if (clearAt == 0L) target.getPersistentData().putLong(CLEAR_AT, now + CLEAR_TICKS);
            else if (now >= clearAt) {
                finish(target);
                return;
            }
        } else {
            target.getPersistentData().remove(CLEAR_AT);
            long recoverAt = target.getPersistentData().getLong(RECOVER_AT);
            if (recoverAt == 0L) {
                target.getPersistentData().putLong(RECOVER_AT, now + RECOVER_TICKS);
            } else {
                while (years > 0f && now >= recoverAt) {
                    years -= 1f;
                    recoverAt += RECOVER_TICKS;
                }
                if (years <= 0f) {
                    finish(target);
                    return;
                }
                target.getPersistentData().putFloat(YEARS, years);
                target.getPersistentData().putLong(RECOVER_AT, recoverAt);
            }
        }
        float current = target.getPersistentData().getFloat(YEARS);
        applyDebuffs(target, current);
        syncAgingModifier(target, current);
    }

    private static void syncAgingModifier(LivingEntity target, float years) {
        MultiplierModifierComponent component = target.getData(ModAttachments.MULTIPLIER_MODIFIER_COMPONENT);
        while (component.modifiers.containsKey(AGING_MODIFIER)) BeyonderData.removeModifier(target, AGING_MODIFIER);
        if (years <= 0f) return;
        float fraction = Math.min(1f, years / yearLimit(target));
        BeyonderData.addModifier(target, AGING_MODIFIER, Math.max(0.1f, 1f - fraction));
    }

    private static void finish(LivingEntity target) {
        target.getPersistentData().remove(YEARS);
        target.getPersistentData().remove(RECOVER_AT);
        target.getPersistentData().remove(CLEAR_AT);
        aged.remove(target.getUUID());
        syncAgingModifier(target, 0f);
        if (fading.contains(target.getUUID())) return;
        target.removeEffect(MobEffects.WEAKNESS);
        target.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        target.removeEffect(MobEffects.DIG_SLOWDOWN);
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!event.getEntity().level().isClientSide()) finish(event.getEntity());
    }

    @SubscribeEvent
    public static void onHeal(LivingHealEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide() || !isAged(entity)) return;
        float room = recoverableHealth(entity) - entity.getHealth();
        if (room <= 0f) event.setCanceled(true);
        else if (event.getAmount() > room) event.setAmount(room);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (aged.isEmpty() || event.getServer().getTickCount() % 20 != 0) return;
        for (UUID id : new ArrayList<>(aged)) {
            LivingEntity living = null;
            for (ServerLevel level : event.getServer().getAllLevels()) {
                if (level.getEntity(id) instanceof LivingEntity found) {
                    living = found;
                    break;
                }
            }
            if (living == null) aged.remove(id);
            else process(living);
        }
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof LivingEntity living)) return;
        if (living instanceof Mob mob && mob.getPersistentData().getBoolean(FADING) && !fading.contains(mob.getUUID())) {
            mob.getPersistentData().remove(FADING);
            mob.setNoAi(false);
        }
        if (living.getPersistentData().getFloat(YEARS) <= 0f) return;
        process(living);
        if (living.getPersistentData().getFloat(YEARS) > 0f) aged.add(living.getUUID());
    }
}
