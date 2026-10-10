package de.jakob.lotm.beyonders.abilities.twilight_giant.handlers;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.DisabledAbilitiesComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.MultiplierModifierComponent;
import de.jakob.lotm.beyonders.abilities.justiciar.LawAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.ability_entities.twilight_giant.TwilightDomeEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
    private static final int BLACK_FADE_TICKS = 20;
    private static final String DEATH_LOCK = "twilight_finish_death";

    private static final Set<UUID> fading = new HashSet<>();
    private static final Set<UUID> aged = new HashSet<>();
    private static final Map<UUID, UUID> domes = new HashMap<>();
    private static final Map<UUID, SunsetDeath> deaths = new HashMap<>();

    private TwilightAging() {
    }

    public static void age(ServerLevel level, LivingEntity target, LivingEntity source, float years) {
        age(level, target, source, years, false);
    }

    public static void age(ServerLevel level, LivingEntity target, LivingEntity source, float years, boolean fade) {
        if (unaging(target)) return;
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

    public static float years(LivingEntity entity) {
        return entity.getPersistentData().getFloat(YEARS);
    }

    public static boolean setYears(LivingEntity entity, float years) {
        if (years < 0f && "error".equals(BeyonderData.getPathway(entity)) && entity.isAlive() && entity.level() instanceof ServerLevel) {
            entity.getPersistentData().putFloat(YEARS, years);
            entity.getPersistentData().remove(RECOVER_AT);
            entity.getPersistentData().remove(CLEAR_AT);
            aged.add(entity.getUUID());
            syncAgingModifier(entity, 0f);
            if (!fading.contains(entity.getUUID())) {
                entity.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                entity.removeEffect(MobEffects.DIG_SLOWDOWN);
            }
            return true;
        }
        if (years <= 0f) {
            finish(entity);
            return true;
        }
        if (unaging(entity) || !entity.isAlive() || !(entity.level() instanceof ServerLevel level)) return false;
        entity.getPersistentData().putFloat(YEARS, years);
        aged.add(entity.getUUID());
        scheduleRecovery(entity, level.getGameTime());
        applyDebuffs(entity, years);
        syncAgingModifier(entity, years);
        return true;
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
        if (deaths.containsKey(target.getUUID())) return;
        if (target instanceof Player && beginSunset(level, target, source)) return;
        finishDeath(level, target, source);
    }

    private static boolean beginSunset(ServerLevel level, LivingEntity target, LivingEntity source) {
        if (!(target instanceof Player player)) return false;
        boolean fresh = !domes.containsKey(player.getUUID());
        TwilightDomeEntity dome = ensureDome(level, player, 0.0F);
        if (dome == null) return false;
        float start = fresh ? 0.0F : dome.sun();
        dome.setSun(start);
        int setTicks = Math.max(20, Math.round((1.0F - start) * TwilightDomeEntity.SUNSET_TICKS));
        long now = level.getServer().getTickCount();
        deaths.put(player.getUUID(), new SunsetDeath(source == null ? null : source.getUUID(), start, now, setTicks, now + setTicks, now + setTicks + TwilightDomeEntity.BLACK_TICKS));
        DisabledAbilitiesComponent component = player.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT);
        component.disableAbilityUsageForTime(DEATH_LOCK, setTicks + TwilightDomeEntity.BLACK_TICKS + 20, player);
        return true;
    }

    private static void finishDeath(ServerLevel level, LivingEntity target, LivingEntity source) {
        if (target.isRemoved() || target.deathTime > 0) return;
        if (target instanceof Player player && (player.isCreative() || player.isSpectator())) return;
        DisabledAbilitiesComponent component = target.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT);
        component.disableAbilityUsageForTime(DEATH_LOCK, 20, target);
        UUID id = target.getUUID();
        boolean fool = "fool".equals(BeyonderData.getPathway(target));
        if (!fool) LawAbility.SOLACE_KILLED.add(id);
        target.invulnerableTime = 0;
        float before = target.getHealth();
        DamageSource damage = ModDamageTypes.source(level, ModDamageTypes.BEYONDER_GENERIC, source);
        float strike = before + target.getAbsorptionAmount() + 1f;
        target.hurt(damage, strike);
        if (target.deathTime <= 0 && !target.isRemoved() && target.isAlive()) {
            target.invulnerableTime = 0;
            if (!(fool && target.getHealth() >= before)) {
                target.setHealth(0f);
                target.die(damage);
            }
        }
        if (!fool) ServerScheduler.scheduleDelayed(2, () -> LawAbility.SOLACE_KILLED.remove(id), level);
    }

    private static boolean unaging(LivingEntity target) {
        return "twilight_giant".equals(BeyonderData.getPathway(target)) && BeyonderData.getSequence(target) <= 0;
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
        if (unaging(target)) {
            finish(target);
            return;
        }
        if (!(target.level() instanceof ServerLevel level) || !target.isAlive()) return;
        float years = target.getPersistentData().getFloat(YEARS);
        if (years < 0f && "error".equals(BeyonderData.getPathway(target))) return;
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
        if (!target.level().isClientSide() && target.level().getServer() != null) cancelSunset(target.getUUID(), target.level().getServer());
        target.getPersistentData().remove(YEARS);
        target.getPersistentData().remove(RECOVER_AT);
        target.getPersistentData().remove(CLEAR_AT);
        aged.remove(target.getUUID());
        syncAgingModifier(target, 0f);
        if (fading.contains(target.getUUID())) return;
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
        tickSunsets(event.getServer());
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

    private static void tickSunsets(MinecraftServer server) {
        long now = server.getTickCount();
        for (UUID id : new ArrayList<>(aged)) {
            if (deaths.containsKey(id)) continue;
            LivingEntity living = findLiving(server, id);
            if (!(living instanceof Player player) || !player.isAlive() || !(player.level() instanceof ServerLevel level) || player.getPersistentData().getFloat(YEARS) <= 0.0F) {
                dropDome(server, id);
                continue;
            }
            float target = sunFor(player);
            TwilightDomeEntity dome = ensureDome(level, player, target);
            if (dome == null) continue;
            float step = TwilightDomeEntity.SUN_HORIZON / 20.0F;
            float next = dome.sun() + Mth.clamp(target - dome.sun(), -step, step);
            if (Math.abs(dome.sun() - next) > 0.0001F) dome.setSun(next);
            if (dome.black() != 0.0F) dome.setBlack(0.0F);
        }
        if (deaths.isEmpty()) return;
        for (UUID id : new ArrayList<>(deaths.keySet())) {
            SunsetDeath death = deaths.get(id);
            if (death == null) continue;
            LivingEntity living = findLiving(server, id);
            if (!(living instanceof Player player) || !player.isAlive() || !(player.level() instanceof ServerLevel level)) {
                deaths.remove(id);
                dropDome(server, id);
                continue;
            }
            float setT = death.setTicks <= 0 ? 1.0F : (now - death.started) / (float) death.setTicks;
            float sun = setT < 1.0F ? Mth.lerp(Mth.clamp(setT, 0.0F, 1.0F), death.start, 1.0F) : 1.0F;
            float black = setT < 1.0F ? 0.0F : Mth.clamp((now - death.blackAt) / (float) BLACK_FADE_TICKS, 0.0F, 1.0F);
            TwilightDomeEntity dome = ensureDome(level, player, sun);
            if (dome != null) {
                dome.setSun(sun);
                dome.setBlack(black);
            }
            if (now < death.dieAt) continue;
            deaths.remove(id);
            dropDome(server, id);
            finishDeath(level, player, findLiving(server, death.source));
        }
    }

    private static float sunFor(LivingEntity target) {
        float limit = yearLimit(target);
        if (limit <= 0.0F) return 0.0F;
        return TwilightDomeEntity.SUN_HORIZON * Mth.clamp(target.getPersistentData().getFloat(YEARS) / limit, 0.0F, 1.0F);
    }

    private static TwilightDomeEntity ensureDome(ServerLevel level, Player player, float initialSun) {
        UUID id = player.getUUID();
        UUID domeId = domes.get(id);
        if (domeId != null && level.getEntity(domeId) instanceof TwilightDomeEntity existing) return existing;
        dropDome(level.getServer(), id);
        TwilightDomeEntity dome = ModEntities.TWILIGHT_DOME.get().create(level);
        if (dome == null) return null;
        dome.setRadius(TwilightDomeEntity.LOCAL_RADIUS);
        dome.setSunset(true);
        dome.setSubject(id);
        dome.setSun(initialSun);
        dome.setBlack(0.0F);
        dome.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), 0.0F);
        level.addFreshEntity(dome);
        domes.put(id, dome.getUUID());
        return dome;
    }

    private static void dropDome(MinecraftServer server, UUID playerId) {
        discardDome(server, domes.remove(playerId));
    }

    private static void cancelSunset(UUID id, MinecraftServer server) {
        boolean cancelled = deaths.remove(id) != null;
        dropDome(server, id);
        if (!cancelled) return;
        LivingEntity living = findLiving(server, id);
        if (living != null) living.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT).enableAbilityUsage(DEATH_LOCK);
    }

    private static LivingEntity findLiving(MinecraftServer server, UUID id) {
        if (id == null) return null;
        for (ServerLevel level : server.getAllLevels()) {
            if (level.getEntity(id) instanceof LivingEntity living) return living;
        }
        return null;
    }

    private static void discardDome(MinecraftServer server, UUID domeId) {
        if (domeId == null) return;
        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(domeId);
            if (entity != null) {
                entity.discard();
                return;
            }
        }
    }

    private static final class SunsetDeath {
        private final UUID source;
        private final float start;
        private final long started;
        private final int setTicks;
        private final long blackAt;
        private final long dieAt;

        private SunsetDeath(UUID source, float start, long started, int setTicks, long blackAt, long dieAt) {
            this.source = source;
            this.start = start;
            this.started = started;
            this.setTicks = setTicks;
            this.blackAt = blackAt;
            this.dieAt = dieAt;
        }
    }
}
