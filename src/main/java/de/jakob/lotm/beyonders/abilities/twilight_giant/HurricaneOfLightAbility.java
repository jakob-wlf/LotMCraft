package de.jakob.lotm.beyonders.abilities.twilight_giant;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.beyonders.abilities.twilight_giant.handlers.TwilightAging;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.ability_entities.twilight_giant.HurricaneOfLightEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HurricaneOfLightAbility extends ToggleAbility {

    private static final int RADIUS = 20;
    private static final int EVIL_MULTIPLIER = 2;
    private static final int TWILIGHT_RADIUS = 45;
    private static final float TWILIGHT_YEARS = 2f;
    private static final ResourceLocation ROOT_ID = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "hurricane_of_light_root");
    private final Map<UUID, Integer> visuals = new HashMap<>();

    public HurricaneOfLightAbility(String id) {
        super(id, "purification");
        interactionRadius = RADIUS;
        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(80f, 60f, 50f, 40f, 30f, 25f, 20f));
        baseDamage = 6;
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
        if (level instanceof ServerLevel serverLevel) ensureVisual(serverLevel, entity, radius(entity));
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
        ensureVisual(serverLevel, entity, (float) radius);

        float damage = baseDamage;
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
        Integer id = visuals.remove(entity.getUUID());
        if (level instanceof ServerLevel serverLevel && id != null && serverLevel.getEntity(id) instanceof HurricaneOfLightEntity hurricane) {
            hurricane.release();
        }
    }

    private void ensureVisual(ServerLevel level, LivingEntity entity, float radius) {
        Integer id = visuals.get(entity.getUUID());
        Entity existing = id == null ? null : level.getEntity(id);
        boolean twilight = BeyonderData.getSequence(entity) <= 2;
        if (existing instanceof HurricaneOfLightEntity hurricane && !hurricane.isRemoved() && !hurricane.releasing()) {
            hurricane.setRadius(radius);
            hurricane.setTwilight(twilight);
            return;
        }
        HurricaneOfLightEntity hurricane = new HurricaneOfLightEntity(ModEntities.HURRICANE_OF_LIGHT.get(), level);
        hurricane.setPos(entity.getX(), entity.getY(), entity.getZ());
        hurricane.setOwner(entity);
        hurricane.setRadius(radius);
        hurricane.setTwilight(twilight);
        level.addFreshEntity(hurricane);
        visuals.put(entity.getUUID(), hurricane.getId());
    }

    private static float radius(LivingEntity entity) {
        return BeyonderData.getSequence(entity) <= 2 ? TWILIGHT_RADIUS : RADIUS;
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
