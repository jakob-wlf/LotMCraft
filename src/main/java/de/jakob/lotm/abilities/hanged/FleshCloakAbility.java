package de.jakob.lotm.abilities.hanged;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class FleshCloakAbility extends ToggleAbility {
    private static final Map<String, Integer> REQUIREMENTS =
            Map.of(HangedPathwayConstants.PATHWAY_ID, HangedPathwayConstants.SEQUENCE_ROSE_BISHOP);
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            LOTMCraft.MOD_ID, "textures/abilities/mutation_creation_ability.png");
    private static final float SPIRITUALITY_COST = 10.0f;
    private static final float MAX_BUFF_SCALE_SEQ1 = 4.0f;
    private static final float MAX_DEFENSE_SCALE_SEQ1 = 5.0f;
    private static final Set<UUID> CLOAKED_ENTITIES = new HashSet<>();

    private static final String ENABLED_MESSAGE = "ability.lotmcraft.flesh_cloak.enabled";
    private static final String DISABLED_MESSAGE = "ability.lotmcraft.flesh_cloak.disabled";

    public FleshCloakAbility(String id) {
        super(id, "corruption");
        canBeCopied = false;
        autoClear = false;
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        float buffScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_ROSE_BISHOP, MAX_BUFF_SCALE_SEQ1);
        int resistanceAmplifier = Math.min(5, 2 + Math.round((buffScale - 1.0f) * 0.5f));
        int absorptionAmplifier = Math.min(4, 1 + Math.round((buffScale - 1.0f) * 0.35f));
        int damageAmplifier = Math.min(4, Math.round((buffScale - 1.0f) * 0.45f));
        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, resistanceAmplifier, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 40, absorptionAmplifier, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, damageAmplifier, false, false, false));

        if (entity.tickCount % 6 == 0) {
            HangedEffectUtil.spawnFleshAura(serverLevel, entity);
        }
        if (entity.tickCount % 24 == 0) {
            HangedEffectUtil.playFleshPulse(serverLevel, entity.position().add(0, entity.getBbHeight() * 0.5, 0), 0.75f);
        }
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        CLOAKED_ENTITIES.add(entity.getUUID());
        if (level instanceof ServerLevel serverLevel) {
            HangedEffectUtil.spawnFleshBurst(serverLevel, entity.position().add(0, entity.getBbHeight() * 0.5, 0), 0.7, 24);
            HangedEffectUtil.playFleshCast(serverLevel, entity.position());
        }
        AbilityUtil.sendActionBar(entity,
                Component.translatable(ENABLED_MESSAGE).withColor(HangedPathwayConstants.pathwayColor()));
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        CLOAKED_ENTITIES.remove(entity.getUUID());
        if (level instanceof ServerLevel serverLevel) {
            HangedEffectUtil.spawnFleshBurst(serverLevel, entity.position().add(0, entity.getBbHeight() * 0.5, 0), 0.8, 20);
            HangedEffectUtil.playFleshPulse(serverLevel, entity.position(), 1.15f);
        }
        entity.removeEffect(MobEffects.DAMAGE_RESISTANCE);
        entity.removeEffect(MobEffects.ABSORPTION);
        entity.removeEffect(MobEffects.DAMAGE_BOOST);
        AbilityUtil.sendActionBar(entity,
                Component.translatable(DISABLED_MESSAGE).withColor(HangedPathwayConstants.pathwayColor()));
        clearArtifactScaling(entity);
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

    public static boolean isCloaked(LivingEntity entity) {
        return CLOAKED_ENTITIES.contains(entity.getUUID());
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (!CLOAKED_ENTITIES.contains(target.getUUID()) || event.getAmount() <= 0) {
            return;
        }

        float defenseScale = HangedPathwayConstants.scaleForCurrentSequence(target, HangedPathwayConstants.SEQUENCE_ROSE_BISHOP, MAX_DEFENSE_SCALE_SEQ1);
        float multiplier = Math.max(0.12f, 0.72f / defenseScale);
        if (event.getSource().is(DamageTypeTags.IS_FIRE)) {
            multiplier = Math.max(0.08f, 0.55f / defenseScale);
        } else if (event.getSource().getEntity() instanceof LivingEntity attacker && BeyonderData.isBeyonder(attacker)) {
            multiplier = Math.max(0.06f, 0.5f / defenseScale);
        }

        event.setAmount(Math.max(0.5f, event.getAmount() * multiplier));
    }
}
