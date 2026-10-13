package de.jakob.lotm.beyonders.abilities.twilight_giant.handlers;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.DisabledAbilitiesComponent;
import de.jakob.lotm.attachments.HistoricalVoidComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.MultiplierModifierComponent;
import de.jakob.lotm.beyonders.abilities.fool.GraftingAbility;
import de.jakob.lotm.beyonders.abilities.fool.HistoricalVoidSummoningAbility;
import de.jakob.lotm.beyonders.abilities.justiciar.LawAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.gamerule.ModGameRules;
import de.jakob.lotm.entity.custom.ability_entities.twilight_giant.TwilightDomeEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public final class TwilightAging {

    public static final int TWILIGHT_TEXT = 0xFFFF6A2A;
    public static final DustParticleOptions TWILIGHT_DUST = new DustParticleOptions(new Vector3f(1f, 0.35f, 0.1f), 1.8f);
    public static final DustParticleOptions ASH_DUST = new DustParticleOptions(new Vector3f(0.55f, 0.5f, 0.45f), 1.2f);
    private static final String YEARS = "lotmcraft_twilight_years";
    private static final String PASSIVE_YEARS = "lotmcraft_twilight_passive_years";
    private static final String PASSIVE_TICKS = "lotmcraft_twilight_passive_ticks";
    private static final String AGING_MODIFIER = "twilight_aging";
    private static final String RECOVER_AT = "lotmcraft_twilight_recover_at";
    private static final String CLEAR_AT = "lotmcraft_twilight_clear_at";
    private static final String FADING = "lotmcraft_twilight_fading";
    private static final float[] YEAR_LIMITS = {6000f, 3000f, 1500f, 500f, 400f, 100f, 80f, 70f, 70f, 70f};
    private static final float MASS_CLEAR_YEARS = 100f;
    private static final int RECOVER_TICKS = 20 * 60;
    private static final int CLEAR_TICKS = 20 * 300;
    private static final int DEBUFF_TICKS = 40;
    private static final int DEBUFF_LINGER = 100;
    private static final float DEBUFF_START = 0.6f;
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
        breakGraft(source, target);
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
        applyDebuffs(target, total, true);
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
        float before = entity.getPersistentData().getFloat(YEARS);
        entity.getPersistentData().putFloat(YEARS, years);
        aged.add(entity.getUUID());
        scheduleRecovery(entity, level.getGameTime());
        applyDebuffs(entity, years, years > before);
        syncAgingModifier(entity, years);
        if (years >= yearLimit(entity) && BeyonderData.getSequence(entity) > 0) dieOfOldAge(level, entity, null);
        return true;
    }

    public static void clear(LivingEntity entity) {
        finish(entity);
    }

    public static void fade(ServerLevel level, LivingEntity target, LivingEntity source) {
        breakGraft(source, target);
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
        if (target instanceof Mob mob) mob.skipDropExperience();
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

    private static void breakGraft(LivingEntity source, LivingEntity target) {
        if (source == null || target == null || !"twilight_giant".equals(BeyonderData.getPathway(source))) return;
        if (BeyonderData.getSequence(target) < BeyonderData.getSequence(source)) return;
        GraftingAbility.endOwned(target);
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

    private static void applyDebuffs(LivingEntity target, float years, boolean receiving) {
        if (fading.contains(target.getUUID())) return;
        if (years < yearLimit(target) * DEBUFF_START) {
            target.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
            target.removeEffect(MobEffects.DIG_SLOWDOWN);
            return;
        }
        if (BeyonderData.getSequence(target) <= 0 && !receiving) return;
        int amplifier = Math.min(MAX_AMPLIFIER, (int) (years / YEARS_PER_AMPLIFIER));
        int duration = BeyonderData.getSequence(target) <= 0 ? DEBUFF_LINGER : DEBUFF_TICKS;
        target.forceAddEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, amplifier, false, false), null);
        target.forceAddEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, duration, amplifier, false, false), null);
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
            float kept = passiveYears(target);
            if (target.getPersistentData().getFloat(YEARS) > kept) {
                target.getPersistentData().putFloat(YEARS, kept);
                target.getPersistentData().remove(RECOVER_AT);
                target.getPersistentData().remove(CLEAR_AT);
                syncAgingModifier(target, kept);
            }
            return;
        }
        if (!(target.level() instanceof ServerLevel level) || !target.isAlive()) return;
        float years = target.getPersistentData().getFloat(YEARS);
        float floor = passiveYears(target);
        if (years < floor) {
            years = floor;
            target.getPersistentData().putFloat(YEARS, years);
        }
        if (years < 0f && "error".equals(BeyonderData.getPathway(target))) return;
        if (years <= 0f) {
            finish(target);
            return;
        }
        long now = level.getGameTime();
        if (years - floor > MASS_CLEAR_YEARS) {
            long clearAt = target.getPersistentData().getLong(CLEAR_AT);
            if (clearAt == 0L) target.getPersistentData().putLong(CLEAR_AT, now + CLEAR_TICKS);
            else if (now >= clearAt) {
                finish(target);
                return;
            }
        } else if (years > floor) {
            target.getPersistentData().remove(CLEAR_AT);
            long recoverAt = target.getPersistentData().getLong(RECOVER_AT);
            if (recoverAt == 0L) {
                target.getPersistentData().putLong(RECOVER_AT, now + RECOVER_TICKS);
            } else {
                while (years > floor && now >= recoverAt) {
                    years -= 1f;
                    recoverAt += RECOVER_TICKS;
                }
                if (years < floor) years = floor;
                target.getPersistentData().putFloat(YEARS, years);
                if (years <= floor) target.getPersistentData().remove(RECOVER_AT);
                else target.getPersistentData().putLong(RECOVER_AT, recoverAt);
                if (years <= 0f) {
                    finish(target);
                    return;
                }
            }
        } else {
            target.getPersistentData().remove(CLEAR_AT);
            target.getPersistentData().remove(RECOVER_AT);
        }
        float current = target.getPersistentData().getFloat(YEARS);
        if (target instanceof ServerPlayer player) hastenBorrows(player, now);
        applyDebuffs(target, current, false);
        syncAgingModifier(target, current);
    }

    private static void hastenBorrows(ServerPlayer player, long now) {
        HistoricalVoidComponent data = player.getData(ModAttachments.HISTORICAL_VOID_COMPONENT.get());
        if (data.activeSummonTimes.isEmpty()) return;
        List<HistoricalVoidComponent.SummonInfo> borrows = new ArrayList<>();
        for (HistoricalVoidComponent.SummonInfo info : data.activeSummonTimes.values()) {
            if (info.summonTime() <= now || !borrowed(info.type())) continue;
            borrows.add(info);
        }
        long past = 0;
        for (HistoricalVoidComponent.SummonInfo info : borrows) {
            data.activeSummonTimes.remove(info.summonTime());
            long next = info.summonTime() - 18;
            if (next <= now) next = now - 1 - past++;
            while (data.activeSummonTimes.containsKey(next)) next--;
            data.activeSummonTimes.put(next, new HistoricalVoidComponent.SummonInfo(next, info.type(), info.entityUUID(), info.originalBeforeBorrowing()));
        }
    }

    private static boolean borrowed(HistoricalVoidSummoningAbility.SummonType type) {
        return type == HistoricalVoidSummoningAbility.SummonType.HEALTH
                || type == HistoricalVoidSummoningAbility.SummonType.SPIRITUALITY
                || type == HistoricalVoidSummoningAbility.SummonType.CLEANSED_STATE
                || type == HistoricalVoidSummoningAbility.SummonType.SEQUENCE
                || type == HistoricalVoidSummoningAbility.SummonType.EFFECT;
    }

    private static void syncAgingModifier(LivingEntity target, float years) {
        MultiplierModifierComponent component = target.getData(ModAttachments.MULTIPLIER_MODIFIER_COMPONENT);
        while (component.modifiers.containsKey(AGING_MODIFIER)) BeyonderData.removeModifier(target, AGING_MODIFIER);
        if (years <= 0f) return;
        float fraction = Math.min(1f, years / yearLimit(target));
        BeyonderData.addModifier(target, AGING_MODIFIER, Math.max(0.2f, 1f - fraction));
    }

    private static void finish(LivingEntity target) {
        if (!target.level().isClientSide() && target.level().getServer() != null) cancelSunset(target.getUUID(), target.level().getServer());
        float passive = passiveYears(target);
        target.getPersistentData().remove(RECOVER_AT);
        target.getPersistentData().remove(CLEAR_AT);
        if (passive <= 0f) {
            target.getPersistentData().remove(YEARS);
            aged.remove(target.getUUID());
            syncAgingModifier(target, 0f);
            applyDebuffs(target, 0f, false);
            return;
        }
        target.getPersistentData().putFloat(YEARS, passive);
        aged.add(target.getUUID());
        syncAgingModifier(target, passive);
        applyDebuffs(target, passive, false);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide() || !player.isAlive() || player.isSpectator() || unaging(player)) return;
        if (!player.serverLevel().getGameRules().getBoolean(ModGameRules.PASSIVE_TWILIGHT_AGING)) return;
        if (player.tickCount % 20 != 0) return;
        CompoundTag tag = player.getPersistentData();
        long played = tag.getLong(PASSIVE_TICKS) + 20;
        float passive = tag.getFloat(PASSIVE_YEARS);
        boolean gained = false;
        boolean earned = false;
        long needed = player.serverLevel().getGameRules().getInt(ModGameRules.PASSIVE_TWILIGHT_AGING_MINUTES) * 60L * 20L;
        if (played >= needed) {
            played -= needed;
            passive += 1f;
            tag.putFloat(PASSIVE_YEARS, passive);
            gained = true;
            earned = true;
        }
        tag.putLong(PASSIVE_TICKS, played);
        if (tag.getFloat(YEARS) < passive) {
            tag.putFloat(YEARS, passive);
            aged.add(player.getUUID());
            syncAgingModifier(player, passive);
            gained = true;
        }
        if (earned && BeyonderData.getSequence(player) > 0) applyDebuffs(player, tag.getFloat(YEARS), true);
        if (!gained || !(player.level() instanceof ServerLevel level)) return;
        if (passive >= yearLimit(player) && BeyonderData.getSequence(player) > 0) dieOfOldAge(level, player, null);
    }

    private static float passiveYears(LivingEntity entity) {
        return Math.max(0f, entity.getPersistentData().getFloat(PASSIVE_YEARS));
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        CompoundTag from = event.getOriginal().getPersistentData();
        CompoundTag to = event.getEntity().getPersistentData();
        if (from.contains(PASSIVE_YEARS)) to.putFloat(PASSIVE_YEARS, from.getFloat(PASSIVE_YEARS));
        if (from.contains(PASSIVE_TICKS)) to.putLong(PASSIVE_TICKS, from.getLong(PASSIVE_TICKS));
        float passive = Math.max(0f, from.getFloat(PASSIVE_YEARS));
        if (passive > 0f && to.getFloat(YEARS) < passive) to.putFloat(YEARS, passive);
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
        for (UUID id : new ArrayList<>(domes.keySet())) {
            if (!deaths.containsKey(id)) dropDome(server, id);
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
