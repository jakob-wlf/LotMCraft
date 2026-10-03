package de.jakob.lotm.beyonders.abilities.twilight_giant;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.beyonders.abilities.twilight_giant.handlers.TwilightAging;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class HurricaneOfLightAbility extends ToggleAbility {

    private static final int RADIUS = 20;
    private static final double DAMAGE_SCALE = 0.5D;
    private static final float EVIL_MULTIPLIER = 2f;
    private static final int RINGS = 5;
    private static final int BLADES_PER_RING = 12;
    private static final int TWILIGHT_RADIUS = 45;
    private static final float TWILIGHT_YEARS = 2f;
    private static final ResourceLocation ROOT_ID = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "hurricane_of_light_root");
    private static final DustParticleOptions DAWN_DUST = new DustParticleOptions(new Vector3f(1f, 0.97f, 0.9f), 1.8f);

    public HurricaneOfLightAbility(String id) {
        super(id, "purification");
        interactionRadius = RADIUS;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("twilight_giant", 6));
    }

    @Override
    protected float getSpiritualityCost() {
        return 20;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (!level.isClientSide() && !isActiveForEntity(entity) && !holdsSword(entity)) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.twiligh_giant_dawn.no_sword").withColor(0xFFFFF3D6));
            return;
        }
        super.onAbilityUse(level, entity);
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        setRooted(entity, true);
        level.playSound(null, entity.blockPosition(), SoundEvents.TRIDENT_RIPTIDE_3.value(), entity.getSoundSource(), 2f, 1.2f);
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (!holdsSword(entity)) {
            cancel(serverLevel, entity);
            return;
        }

        entity.setDeltaMovement(0, Math.min(entity.getDeltaMovement().y, 0), 0);
        entity.hurtMarked = true;
        boolean twilight = BeyonderData.getSequence(entity) <= 2;
        double radius = twilight ? TWILIGHT_RADIUS : RADIUS;
        spawnBlades(serverLevel, entity, radius, twilight ? TwilightAging.TWILIGHT_DUST : DAWN_DUST);

        float damage = (float) (DamageLookup.lookupDamage(6, DAMAGE_SCALE) * multiplier(entity) * 10 / 20f);
        DamageSource source = ModDamageTypes.source(serverLevel, ModDamageTypes.PURIFICATION, entity);
        for (LivingEntity target : AbilityUtil.getNearbyEntities(entity, serverLevel, entity.position(), radius)) {
            if (!AbilityUtil.mayDamage(entity, target)) continue;
            float amount = ArsenalOfDawnAbility.isEvil(target) ? damage * EVIL_MULTIPLIER : damage;
            if (!twilight) {
                target.hurt(source, amount);
                continue;
            }
            if (target.getHealth() <= amount) {
                TwilightAging.fade(serverLevel, target, entity);
                continue;
            }
            target.hurt(source, amount);
            TwilightAging.age(serverLevel, target, entity, TWILIGHT_YEARS, true);
        }
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        setRooted(entity, false);
        level.playSound(null, entity.blockPosition(), SoundEvents.BEACON_DEACTIVATE, entity.getSoundSource(), 1.5f, 1.4f);
    }

    private void spawnBlades(ServerLevel level, LivingEntity entity, double maxRadius, DustParticleOptions dust) {
        double phase = entity.tickCount * 0.35;
        Vec3 center = entity.position().add(0, 1, 0);
        for (int ring = 1; ring <= RINGS; ring++) {
            double radius = maxRadius * ring / (double) RINGS;
            for (int blade = 0; blade < BLADES_PER_RING; blade++) {
                double angle = phase * (RINGS + 1 - ring) / RINGS + blade * Math.PI * 2 / BLADES_PER_RING;
                Vec3 pos = center.add(Math.cos(angle) * radius, (ring % 2) * 0.8, Math.sin(angle) * radius);
                level.sendParticles(ParticleTypes.SWEEP_ATTACK, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
                level.sendParticles(dust, pos.x, pos.y, pos.z, 2, 0.3, 0.3, 0.3, 0);
            }
        }
        ParticleUtil.spawnParticles(level, dust == DAWN_DUST ? ParticleTypes.END_ROD : ParticleTypes.FLAME, center, 20, maxRadius / 2.0, 1.5, maxRadius / 2.0, 0.05);
    }

    private static boolean holdsSword(LivingEntity entity) {
        return ArsenalOfDawnAbility.is(entity.getMainHandItem(), ArsenalOfDawnAbility.SWORDS);
    }

    private static void setRooted(LivingEntity entity, boolean rooted) {
        setModifier(entity, Attributes.MOVEMENT_SPEED, rooted);
        setModifier(entity, Attributes.JUMP_STRENGTH, rooted);
    }

    private static void setModifier(LivingEntity entity, Holder<Attribute> attribute, boolean rooted) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) return;
        instance.removeModifier(ROOT_ID);
        if (rooted) instance.addTransientModifier(new AttributeModifier(ROOT_ID, -1.0D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }
}
