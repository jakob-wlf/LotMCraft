package de.jakob.lotm.beyonders.abilities.twilight_giant;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.beyonders.abilities.twilight_giant.handlers.TwilightAging;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.rendering.effectRendering.EffectIds;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.rendering.effectRendering.EffectParams;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class HolinessAuthorityAbility extends SelectableAbility {

    private static final String[] PURIFICATION_FLAGS = {"purification", "light_source"};
    private static final int RADIANCE_TICKS = 20 * 30;
    private static final int RADIANCE_INTERVAL = 10;
    private static final double RADIANCE_RADIUS = 30.0D;
    private static final int RADIANCE_DAMAGE = 44;
    private static final int RADIANCE_RINGS = 3;
    private static final int RADIANCE_RING_POINTS = 36;
    private static final int BEAM_RANGE = 50;
    private static final int BEAM_DURATION = 20 * 4;
    private static final int BEAM_AGE_INTERVAL = 5;
    private static final float BEAM_YEARS = 2f;
    private static final int BEAM_DAMAGE = 3;
    private static final float BEAM_R = ((TwilightAging.TWILIGHT_TEXT >> 16) & 0xFF) / 255f;
    private static final float BEAM_G = ((TwilightAging.TWILIGHT_TEXT >> 8) & 0xFF) / 255f;
    private static final float BEAM_B = (TwilightAging.TWILIGHT_TEXT & 0xFF) / 255f;
    private static final int EVIL_MULTIPLIER = 2;
    private static final int PURIFY_DAMAGE = 41;
    private static final int[] SPIRITUALITY = {6000, 4000, 2500};

    private static final Map<UUID, Integer> radiant = new HashMap<>();
    private static final Set<UUID> holyCages = new HashSet<>();

    public HolinessAuthorityAbility(String id) {
        super(id, 18, "purification", "light_source");
        canBeUsedByNPC = false;
        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(6000f, 4000f, 2500f));
        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(6, 12, 18));
        baseDamage = RADIANCE_DAMAGE;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("twilight_giant", 2));
    }

    @Override
    protected float getSpiritualityCost() {
        return 2500;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.holiness_authority.radiance",
                "ability.lotmcraft.holiness_authority.beam",
                "ability.lotmcraft.holiness_authority.holy_cage",
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        if (!(level instanceof ServerLevel serverLevel) || !(entity instanceof ServerPlayer player)) return;
        switch (selectedAbility) {
            case 0 -> radiate(serverLevel, player);
            case 1 -> beam(serverLevel, player);
            default -> toggleHolyCage(player);
        }
    }

    public static boolean hasHolyCage(LivingEntity owner) {
        return holyCages.contains(owner.getUUID());
    }

    public static void purifyEvil(ServerLevel level, LivingEntity owner, Vec3 center, double radius) {
        int damage = PURIFY_DAMAGE;
        DamageSource source = ModDamageTypes.source(level, ModDamageTypes.PURIFICATION, owner);
        for (LivingEntity target : AbilityUtil.getNearbyEntities(owner, level, center, radius, false, true)) {
            if (!ArsenalOfDawnAbility.isEvil(target)) continue;
            target.hurt(source, damage);
            level.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY(0.5), target.getZ(), 6, 0.3, 0.5, 0.3, 0.05);
        }
        NeoForge.EVENT_BUS.post(new AbilityUsedEvent(level, center, owner, null, PURIFICATION_FLAGS, radius, 20));
    }

    private static void radiate(ServerLevel level, ServerPlayer player) {
        if (radiant.remove(player.getUUID()) != null) {
            BeyonderData.incrementSpirituality(player, costOf(player));
            player.removeEffect(MobEffects.GLOWING);
            level.playSound(null, player.blockPosition(), SoundEvents.BEACON_DEACTIVATE, player.getSoundSource(), 3f, 0.8f);
            bar(player, "ability.lotmcraft.holiness_authority.radiance_ended");
            return;
        }
        radiant.put(player.getUUID(), RADIANCE_TICKS);
        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, player.getSoundSource(), 3f, 0.7f);
        bar(player, "ability.lotmcraft.holiness_authority.radiance_started");
    }

    private void beam(ServerLevel level, ServerPlayer player) {
        int damage = BEAM_DAMAGE;
        DamageSource source = ModDamageTypes.source(level, ModDamageTypes.PURIFICATION, player);
        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, player.getSoundSource(), 3f, 0.7f);
        ServerScheduler.scheduleForDuration(0, 1, BEAM_DURATION, () -> {
            if (!player.isAlive() || player.isRemoved() || player.level() != level) return;
            LivingEntity target = AbilityUtil.getTargetEntity(player, BEAM_RANGE, 2);
            Vec3 targetPos = target != null ? target.getEyePosition() : AbilityUtil.getTargetLocation(player, BEAM_RANGE, 2);
            Vec3 startPos = player.getEyePosition().add(player.getLookAngle().normalize().scale(1.5));
            EffectManager.playEffect(EffectIds.HOLY_BEAM, startPos.x, startPos.y, startPos.z, level, player, false,
                    EffectParams.directionWithParams(2, startPos.x, startPos.y, startPos.z, targetPos.x, targetPos.y, targetPos.z, BEAM_R, BEAM_G, BEAM_B));
            EffectManager.playEffect(EffectIds.HOLY_IMPACT, targetPos.x, targetPos.y, targetPos.z, level, player,
                    EffectParams.ofParams(BEAM_R, BEAM_G, BEAM_B));
            if (target != null && AbilityUtil.mayDamage(player, target)) {
                target.hurt(source, ArsenalOfDawnAbility.isEvil(target) ? damage * EVIL_MULTIPLIER : damage);
                if (player.tickCount % BEAM_AGE_INTERVAL == 0) TwilightAging.age(level, target, player, BEAM_YEARS);
            }
        }, () -> NeoForge.EVENT_BUS.post(new AbilityUsedEvent(level, player.position(), player, null, PURIFICATION_FLAGS, BEAM_RANGE, 20)), level);
    }

    private static void toggleHolyCage(ServerPlayer player) {
        if (holyCages.remove(player.getUUID())) {
            BeyonderData.incrementSpirituality(player, costOf(player));
            bar(player, "ability.lotmcraft.holiness_authority.holy_cage_off");
            return;
        }
        holyCages.add(player.getUUID());
        bar(player, "ability.lotmcraft.holiness_authority.holy_cage_on");
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Integer left = radiant.get(player.getUUID());
        if (left == null) return;
        if (!player.isAlive() || left <= 0) {
            radiant.remove(player.getUUID());
            bar(player, "ability.lotmcraft.holiness_authority.radiance_ended");
            return;
        }
        radiant.put(player.getUUID(), left - 1);
        if (left % RADIANCE_INTERVAL != 0) return;
        ServerLevel level = player.serverLevel();
        player.addEffect(new MobEffectInstance(MobEffects.GLOWING, RADIANCE_INTERVAL * 2, 0, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 12, 0, false, false));
        double phase = player.tickCount * 0.1;
        for (int ring = 1; ring <= RADIANCE_RINGS; ring++) {
            double radius = RADIANCE_RADIUS * ring / RADIANCE_RINGS;
            for (int i = 0; i < RADIANCE_RING_POINTS; i++) {
                double angle = phase * ring + i * Math.PI * 2 / RADIANCE_RING_POINTS;
                level.sendParticles(TwilightAging.TWILIGHT_DUST, player.getX() + Math.cos(angle) * radius, player.getY() + 0.5, player.getZ() + Math.sin(angle) * radius, 1, 0.1, 0.3, 0.1, 0);
            }
        }
        level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY(0.6), player.getZ(), 20, 0.6, 1.0, 0.6, 0.08);
        int damage = RADIANCE_DAMAGE;
        DamageSource source = ModDamageTypes.source(level, ModDamageTypes.PURIFICATION, player);
        for (LivingEntity target : AbilityUtil.getNearbyEntities(player, level, player.position(), RADIANCE_RADIUS, false, true)) {
            if (ArsenalOfDawnAbility.isEvil(target)) target.hurt(source, damage);
        }
        NeoForge.EVENT_BUS.post(new AbilityUsedEvent(level, player.position(), player, null, PURIFICATION_FLAGS, RADIANCE_RADIUS, RADIANCE_INTERVAL * 2));
    }

    private static float costOf(LivingEntity entity) {
        int sequence = BeyonderData.getSequence(entity);
        if (sequence < 0 || sequence >= SPIRITUALITY.length) return SPIRITUALITY[SPIRITUALITY.length - 1];
        return SPIRITUALITY[sequence];
    }

    private static void bar(Player player, String key) {
        AbilityUtil.sendActionBar(player, Component.translatable(key).withColor(TwilightAging.TWILIGHT_TEXT));
    }
}
