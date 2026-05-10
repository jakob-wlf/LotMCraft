package de.jakob.lotm.abilities.hanged;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class BlackArmorAbility extends ToggleAbility {
    private static final Map<String, Integer> REQUIREMENTS =
            Map.of(HangedPathwayConstants.PATHWAY_ID, HangedPathwayConstants.SEQUENCE_BLACK_KNIGHT);
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            LOTMCraft.MOD_ID, "textures/abilities/concealed_domain_ability.png");
    private static final float SPIRITUALITY_COST = 28.0f;
    private static final float MAX_BUFF_SCALE_SEQ1 = 4.5f;
    private static final float MAX_FRACTURE_SCALE_SEQ1 = 4.0f;
    private static final ResourceLocation SCALE_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "black_knight_armor_scale");
    private static final ResourceLocation ARMOR_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "black_knight_armor");
    private static final ResourceLocation TOUGHNESS_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "black_knight_toughness");
    private static final ResourceLocation KNOCKBACK_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "black_knight_knockback");
    private static final Set<UUID> ACTIVE_ARMORED = new HashSet<>();
    private static final Map<UUID, Long> FRACTURE_COOLDOWNS = new HashMap<>();

    private static final String ENABLED_MESSAGE = "ability.lotmcraft.black_armor.enabled";
    private static final String DISABLED_MESSAGE = "ability.lotmcraft.black_armor.disabled";

    public BlackArmorAbility(String id) {
        super(id, "corruption", "darkness");
        canBeCopied = false;
        autoClear = false;
        tickRate = 6;
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        float buffScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_BLACK_KNIGHT, MAX_BUFF_SCALE_SEQ1);
        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, Math.min(5, 2 + Math.round((buffScale - 1.0f) * 0.6f)), false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 40, Math.min(5, 2 + Math.round((buffScale - 1.0f) * 0.5f)), false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, Math.min(4, 1 + Math.round((buffScale - 1.0f) * 0.45f)), false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, Math.min(3, 1 + Math.round((buffScale - 1.0f) * 0.3f)), false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 0, false, false, false));

        if (entity.tickCount % 18 == 0) {
            HangedRenderEffectUtil.playMovable(de.jakob.lotm.rendering.effectRendering.MovableEffectManager.MovableEffect.DEPRAVITY_ARMOR, serverLevel, entity, 22, false);
        }
        HangedEffectUtil.spawnDepravityAura(serverLevel, entity);
        if (entity.tickCount % 18 == 0) {
            HangedEffectUtil.playDepravityPulse(serverLevel, entity.position().add(0, entity.getBbHeight() * 0.55, 0), 0.68f);
        }
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        ACTIVE_ARMORED.add(entity.getUUID());
        addModifier(entity.getAttribute(Attributes.SCALE), SCALE_MODIFIER_ID, 0.42);
        addModifier(entity.getAttribute(Attributes.ARMOR), ARMOR_MODIFIER_ID, 10.0);
        addModifier(entity.getAttribute(Attributes.ARMOR_TOUGHNESS), TOUGHNESS_MODIFIER_ID, 5.0);
        addModifier(entity.getAttribute(Attributes.KNOCKBACK_RESISTANCE), KNOCKBACK_MODIFIER_ID, 0.65);
        if (level instanceof ServerLevel serverLevel) {
            HangedRenderEffectUtil.playMovable(de.jakob.lotm.rendering.effectRendering.MovableEffectManager.MovableEffect.DEPRAVITY_ARMOR, serverLevel, entity, 22, false);
            HangedEffectUtil.spawnDepravityBurst(serverLevel, entity.position().add(0, entity.getBbHeight() * 0.55, 0), 1.0, 36);
            HangedEffectUtil.playDepravityCast(serverLevel, entity.position());
        }
        AbilityUtil.sendActionBar(entity, Component.translatable(ENABLED_MESSAGE).withColor(HangedPathwayConstants.pathwayColor()));
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        ACTIVE_ARMORED.remove(entity.getUUID());
        removeModifier(entity.getAttribute(Attributes.SCALE), SCALE_MODIFIER_ID);
        removeModifier(entity.getAttribute(Attributes.ARMOR), ARMOR_MODIFIER_ID);
        removeModifier(entity.getAttribute(Attributes.ARMOR_TOUGHNESS), TOUGHNESS_MODIFIER_ID);
        removeModifier(entity.getAttribute(Attributes.KNOCKBACK_RESISTANCE), KNOCKBACK_MODIFIER_ID);
        entity.removeEffect(MobEffects.DAMAGE_RESISTANCE);
        entity.removeEffect(MobEffects.ABSORPTION);
        entity.removeEffect(MobEffects.DAMAGE_BOOST);
        entity.removeEffect(MobEffects.MOVEMENT_SPEED);
        entity.removeEffect(MobEffects.FIRE_RESISTANCE);
        if (level instanceof ServerLevel serverLevel) {
            HangedEffectUtil.spawnDepravityBurst(serverLevel, entity.position().add(0, entity.getBbHeight() * 0.55, 0), 1.0, 28);
            HangedEffectUtil.playShadowPulse(serverLevel, entity.position(), 1.05f);
        }
        AbilityUtil.sendActionBar(entity, Component.translatable(DISABLED_MESSAGE).withColor(HangedPathwayConstants.pathwayColor()));
        clearArtifactScaling(entity);
    }

    private static void addModifier(AttributeInstance attribute, ResourceLocation id, double amount) {
        if (attribute != null) {
            attribute.addTransientModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    private static void removeModifier(AttributeInstance attribute, ResourceLocation id) {
        if (attribute != null) {
            attribute.removeModifier(id);
        }
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (!ACTIVE_ARMORED.contains(target.getUUID()) || event.getAmount() <= 0 || !(target.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        float fractureScale = HangedPathwayConstants.scaleForCurrentSequence(target, HangedPathwayConstants.SEQUENCE_BLACK_KNIGHT, MAX_FRACTURE_SCALE_SEQ1);
        float reduced = Math.max(1.0f, event.getAmount() * (0.45f / fractureScale));
        event.setAmount(reduced);

        long now = System.currentTimeMillis();
        long lastFracture = FRACTURE_COOLDOWNS.getOrDefault(target.getUUID(), 0L);
        if (reduced < 6.0f || now - lastFracture < 1400L) {
            return;
        }

        FRACTURE_COOLDOWNS.put(target.getUUID(), now);
        event.setAmount(Math.max(0.8f, reduced * 0.55f));
        HangedEffectUtil.spawnDepravityBurst(serverLevel, target.position().add(0, target.getBbHeight() * 0.5, 0), 1.1 * fractureScale, 32);
        HangedEffectUtil.playDepravityPulse(serverLevel, target.position(), 0.62f);

        for (LivingEntity nearby : serverLevel.getEntitiesOfClass(LivingEntity.class,
                target.getBoundingBox().inflate(2.6 * fractureScale),
                nearby -> nearby != target && nearby.isAlive())) {
            nearby.hurt(ModDamageTypes.source(serverLevel, ModDamageTypes.DARKNESS_GENERIC, target), Math.max(4.0f, reduced * 0.3f));
            nearby.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 1, false, false, true));
            nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 2, false, false, true));
        }
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return REQUIREMENTS;
    }

    @Override
    protected float getSpiritualityCost() {
        return SPIRITUALITY_COST;
    }

    @Override
    public ResourceLocation getTextureLocation() {
        return TEXTURE;
    }
}
