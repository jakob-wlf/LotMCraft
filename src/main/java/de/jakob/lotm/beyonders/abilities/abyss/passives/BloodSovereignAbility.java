package de.jakob.lotm.beyonders.abilities.abyss.passives;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.abyss.DevilWorship.DevilWorshipAbility;
import de.jakob.lotm.beyonders.abilities.core.PassiveAbility;
import de.jakob.lotm.beyonders.abilities.core.PassiveAbilityHandler;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
public class BloodSovereignAbility extends PassiveAbility {
    public static float capacityFor(int sequence) {
        return switch (sequence) {
            case 2 -> 1000f;
            case 1 -> 1500f;
            case 0 -> 2000f;
            default -> 0f;
        };
    }
    public static final float BASE_REVIVE_COST = 250f;
    public static final float EXPLOSION_MULT = 1.5f;   // area attacks
    public static final float LIGHTNING_MULT = 1.5f;
    public static final float FIRE_MULT = 1.5f;
    public static final float FALL_MULT = 0.5f;
    public static final float DROWNING_MULT = 0.5f;
    public static final Map<String, Float> DAMAGE_TYPE_MULT = new HashMap<>();
    static {
        DAMAGE_TYPE_MULT.put(ModDamageTypes.CORROSION.location().toString(), 1.5f); // placeholder
        DAMAGE_TYPE_MULT.put(ModDamageTypes.FIRE.location().toString(), 1.5f);      // placeholder
    }
    public static final float PURIFICATION_DRAIN_PER_DAMAGE = 1.0f;
    public static final float REVIVE_HEALTH_FRACTION = 0.5f;
    public static final int REVIVE_PROTECTION_TICKS = 20 * 3;
    public static final float BASE_BLOOD_GAIN_FRACTION = 0.25f;
    public static final float MOB_GAIN_FRACTION = 0.5f;
    // Mobs above this maximum health count as Sequence 4
    public static final float HIGH_HEALTH_MOB_THRESHOLD = 100.0f;
    private static final String KEY = "lotm_blood_sovereign";
    private static final DustParticleOptions BLOOD_DUST = new DustParticleOptions(new Vector3f(0.55f, 0.02f, 0.05f), 1.6f);
    private static final Map<UUID, float[]> LAST_SYNC = new ConcurrentHashMap<>();
    public BloodSovereignAbility(String id) {
        super(id);
    }
    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("abyss", 2));
    }
    @Override
    public void onPassiveAbilityGained(LivingEntity entity, ServerLevel serverLevel) {
        if (entity instanceof ServerPlayer player && hasBloodSovereign(player)) {
            setBlood(player, blood(player));
            LAST_SYNC.remove(player.getUUID());
        }
    }
    @Override
    public void onPassiveAbilityRemoved(LivingEntity entity, ServerLevel serverLevel) {
        if (entity instanceof ServerPlayer player) {
            LAST_SYNC.remove(player.getUUID());
            PacketDistributor.sendToPlayer(player, new BloodSovereignClient.SyncBlood(0f, 0f)); // hides the vial
        }
    }
    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level.isClientSide || !(entity instanceof ServerPlayer player)) return;
        if (!hasBloodSovereign(player)) return;
        setBlood(player, blood(player)); // keeps it inside the capacity, which grows with sequence
        syncIfChanged(player);
    }
    //data
    static boolean hasBloodSovereign(LivingEntity entity) {
        PassiveAbility ability = PassiveAbilityHandler.getById("curse_resistance_ability");
        return ability != null && ability.shouldApplyTo(entity);
    }
    static float capacity(ServerPlayer player) {
        return capacityFor(BeyonderData.getSequence(player));
    }
    /** Current blood. A player with no saved vial yet starts full. */
    static float blood(ServerPlayer player) {
        CompoundTag tag = player.getPersistentData().getCompound(KEY);
        return tag.contains("blood") ? tag.getFloat("blood") : capacity(player);
    }
    static void setBlood(ServerPlayer player, float value) {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("blood", Math.clamp(value, 0f, capacity(player)));
        player.getPersistentData().put(KEY, tag);
    }
    private static void syncIfChanged(ServerPlayer player) {
        float b = blood(player), c = capacity(player);
        float[] last = LAST_SYNC.get(player.getUUID());
        if (last != null && last[0] == b && last[1] == c) return;
        LAST_SYNC.put(player.getUUID(), new float[] { b, c });
        PacketDistributor.sendToPlayer(player, new BloodSovereignClient.SyncBlood(b, c));
    }
    //rules
    private static boolean isPurification(DamageSource source) {
        String id = source.getMsgId().toLowerCase();
        return id.contains("purification") || id.contains("purify");
    }
    static float reviveCost(DamageSource source) {
        String typeId = source.typeHolder().unwrapKey().map(k -> k.location().toString()).orElse("");
        Float override = DAMAGE_TYPE_MULT.get(typeId);
        if (override != null) return BASE_REVIVE_COST * override;
        if (source.is(DamageTypeTags.IS_EXPLOSION)) return BASE_REVIVE_COST * EXPLOSION_MULT;
        if (source.is(DamageTypeTags.IS_LIGHTNING)) return BASE_REVIVE_COST * LIGHTNING_MULT;
        if (source.is(DamageTypeTags.IS_FIRE)) return BASE_REVIVE_COST * FIRE_MULT;
        if (source.is(DamageTypeTags.IS_FALL)) return BASE_REVIVE_COST * FALL_MULT;
        if (source.is(DamageTypeTags.IS_DROWNING)) return BASE_REVIVE_COST * DROWNING_MULT;
        return BASE_REVIVE_COST;
    }
    // kills give blood and killing a beyonder that's equal to you in seq gives one revives worth of blood
    static float bloodGain(ServerPlayer killer, LivingEntity dead) {
        int killerSequence = BeyonderData.getSequence(killer);
        int victimSequence;
        if (BeyonderData.isBeyonder(dead)) {victimSequence = BeyonderData.getSequence(dead);}
        else {
            // Powerful mobs count as Sequence 4; ordinary mobs count as half a Sequence 9.
            if (dead.getMaxHealth() > HIGH_HEALTH_MOB_THRESHOLD) {victimSequence = 4;}
            else {return capacity(killer) * BASE_BLOOD_GAIN_FRACTION * MOB_GAIN_FRACTION * (float) Math.pow(0.5, Math.max(0, 9 - killerSequence));}
        }
        int sequenceDifference = victimSequence - killerSequence;
        float gain = capacity(killer) * BASE_BLOOD_GAIN_FRACTION;
        if (sequenceDifference > 0) {gain *= (float) Math.pow(0.5, sequenceDifference);}
        else if (sequenceDifference < 0) {gain *= (float) Math.pow(2.0, -sequenceDifference);}
        return gain;
    }
    private static int percent(ServerPlayer player, float amount) {
        float cap = capacity(player);
        return cap <= 0 ? 0 : Math.round(amount / cap * 100f);
    }
    private static void rebirth(ServerPlayer player, float cost) {
        player.setHealth(player.getMaxHealth() * REVIVE_HEALTH_FRACTION);
        player.clearFire();
        player.fallDistance = 0;
        player.setAirSupply(player.getMaxAirSupply());
        for (MobEffectInstance effect : new ArrayList<>(player.getActiveEffects())) {if (effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {player.removeEffect(effect.getEffect());}}
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, REVIVE_PROTECTION_TICKS, 4, false, false));
        if (player.level() instanceof ServerLevel level) {
            ParticleUtil.spawnSphereParticles(level, BLOOD_DUST, player.position().add(0, player.getBbHeight() / 2, 0), 2.5, 120);
            level.playSound(null, player.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, player.getSoundSource(), 2f, 0.6f);
        }
        AbilityUtil.sendActionBar(player, Component.literal("Reborn from your own blood. -" + percent(player, cost) + "% blood").withColor(0xB01818));
    }
    //events
    @EventBusSubscriber(modid = LOTMCraft.MOD_ID)
    public static class Events {
        @SubscribeEvent
        public static void onIncomingDamage(LivingIncomingDamageEvent event) {
            if (!(event.getEntity() instanceof ServerPlayer player) || !hasBloodSovereign(player)) return;
            if (!isPurification(event.getSource())) return;
            float current = blood(player);
            if (current <= 0f) return;
            float drained = Math.min(current, event.getAmount() * PURIFICATION_DRAIN_PER_DAMAGE);
            setBlood(player, current - drained);
            syncIfChanged(player);
        }
        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onDeath(LivingDeathEvent event) {
            if (!(event.getEntity() instanceof ServerPlayer player) || !hasBloodSovereign(player)) return;
            DamageSource source = event.getSource();
            if (isPurification(source)) {
                setBlood(player, 0);
                return; // disintegrated by purification
            }
            if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return; // /kill and the void are never revived
            float cost = reviveCost(source);
            float current = blood(player);
            if (current < cost) return; // not enough blood, you die
            event.setCanceled(true);
            setBlood(player, current - cost);
            rebirth(player, cost);
            syncIfChanged(player);
        }
        @SubscribeEvent(priority = EventPriority.LOW)
        public static void onKill(LivingDeathEvent event) {
            LivingEntity dead = event.getEntity();
            if (dead.level().isClientSide) return;
            if (!(event.getSource().getEntity() instanceof ServerPlayer killer) || !hasBloodSovereign(killer)) return;
            if (dead == killer) return;
            if (dead instanceof TamableAnimal pet && pet.isTame()) return;
            if (dead.getPersistentData().getBoolean(DevilWorshipAbility.DEVIL_TAG)) return; // summoned devils give nothing
            float gain = bloodGain(killer, dead);
            setBlood(killer, blood(killer) + gain);
            syncIfChanged(killer);
        }
        @SubscribeEvent
        public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {LAST_SYNC.remove(event.getEntity().getUUID());}
        @SubscribeEvent
        public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {LAST_SYNC.remove(event.getEntity().getUUID());}
    }
}