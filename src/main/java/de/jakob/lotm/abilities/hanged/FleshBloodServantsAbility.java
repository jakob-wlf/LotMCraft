package de.jakob.lotm.abilities.hanged;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.subordinates.SubordinateUtils;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Map;

public class FleshBloodServantsAbility extends Ability {
    private static final Map<String, Integer> REQUIREMENTS =
            Map.of(HangedPathwayConstants.PATHWAY_ID, HangedPathwayConstants.SEQUENCE_ROSE_BISHOP);
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            LOTMCraft.MOD_ID, "textures/abilities/golem_creation_ability.png");
    private static final float SPIRITUALITY_COST = 300.0f;
    private static final float MAX_COUNT_SCALE_SEQ1 = 3.0f;
    private static final float MAX_ATTRIBUTE_SCALE_SEQ1 = 5.0f;
    private static final float MAX_DURATION_SCALE_SEQ1 = 3.0f;
    private static final float MAX_AREA_SCALE_SEQ1 = 3.0f;
    private static final DustParticleOptions BLOOD_DUST =
            new DustParticleOptions(new Vector3f(0.72f, 0.12f, 0.17f), 1.2f);

    private static final int SERVANT_COUNT = 3;
    private static final int SERVANT_LIFETIME = 20 * 35;
    private static final String SUMMONED_MESSAGE = "ability.lotmcraft.flesh_blood_servants.summoned";

    public FleshBloodServantsAbility(String id) {
        super(id, 20f, "summon");
        canBeCopied = false;
        autoClear = false;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        float attributeScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_ROSE_BISHOP, MAX_ATTRIBUTE_SCALE_SEQ1);
        float areaScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_ROSE_BISHOP, MAX_AREA_SCALE_SEQ1);
        int servantCount = Math.min(9, HangedPathwayConstants.scaleIntForCurrentSequence(entity,
                HangedPathwayConstants.SEQUENCE_ROSE_BISHOP, SERVANT_COUNT, MAX_COUNT_SCALE_SEQ1));
        int servantLifetime = Math.min(20 * 120, HangedPathwayConstants.scaleIntForCurrentSequence(entity,
                HangedPathwayConstants.SEQUENCE_ROSE_BISHOP, SERVANT_LIFETIME, MAX_DURATION_SCALE_SEQ1));
        AbilityUtil.sendActionBar(entity,
                Component.translatable(SUMMONED_MESSAGE).withColor(HangedPathwayConstants.pathwayColor()));
        HangedEffectUtil.playFleshCast(serverLevel, entity.position());
        HangedEffectUtil.spawnFleshBurst(serverLevel, entity.position().add(0, entity.getBbHeight() * 0.45, 0), 0.9 * areaScale, 30);

        for (int i = 0; i < servantCount; i++) {
            Vec3 spawnPos = entity.position().add(level.random.nextDouble(), 0, level.random.nextDouble());
            BeyonderNPCEntity servant = new BeyonderNPCEntity(ModEntities.BEYONDER_NPC.get(), serverLevel, false, HangedPathwayConstants.PATHWAY_ID, 8);
            servant.setPos(spawnPos);
            servant.setCustomName(Component.translatable("entity.lotmcraft.flesh_blood_servant"));
            servant.setPersistenceRequired();
            serverLevel.addFreshEntity(servant);
            HangedEffectUtil.spawnFleshBurst(serverLevel, spawnPos.add(0, 0.8, 0), 0.65, 18);
            HangedEffectUtil.playFleshPulse(serverLevel, spawnPos, 0.7f + level.random.nextFloat() * 0.2f);

            SubordinateUtils.turnEntityIntoSubordinate(servant, entity, false);
            if (servant.getAttribute(Attributes.MAX_HEALTH) != null) {
                servant.getAttribute(Attributes.MAX_HEALTH).setBaseValue(52 + (14 * multiplier(entity) * attributeScale));
            }
            if (servant.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
                servant.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(10 + (5 * multiplier(entity) * attributeScale));
            }
            if (servant.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
                servant.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.36 + ((attributeScale - 1.0f) * 0.03f));
            }
            if (servant.getAttribute(Attributes.KNOCKBACK_RESISTANCE) != null) {
                servant.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0);
            }

            servant.setHealth(servant.getMaxHealth());
            servant.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, servantLifetime, 1, false, false, false));
            servant.addEffect(new MobEffectInstance(MobEffects.REGENERATION, servantLifetime, 1, false, false, false));
            servant.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, servantLifetime,
                    Math.min(3, Math.round((attributeScale - 1.0f) * 0.35f)), false, false, false));

            ServerScheduler.scheduleDelayed(servantLifetime, () -> selfDestruct(servant, entity, serverLevel));
        }
    }

    private void selfDestruct(BeyonderNPCEntity servant, LivingEntity caster, ServerLevel serverLevel) {
        if (!servant.isAlive() || servant.isRemoved()) {
            return;
        }

        float attributeScale = HangedPathwayConstants.scaleForCurrentSequence(caster, HangedPathwayConstants.SEQUENCE_ROSE_BISHOP, MAX_ATTRIBUTE_SCALE_SEQ1);
        float areaScale = HangedPathwayConstants.scaleForCurrentSequence(caster, HangedPathwayConstants.SEQUENCE_ROSE_BISHOP, MAX_AREA_SCALE_SEQ1);
        Vec3 center = servant.position().add(0, servant.getBbHeight() * 0.45, 0);
        serverLevel.playSound(null, center.x, center.y, center.z, SoundEvents.SLIME_SQUISH, SoundSource.HOSTILE, 1.4f, 0.7f);
        HangedEffectUtil.spawnFleshBurst(serverLevel, center, 0.95 * areaScale, 34);
        serverLevel.sendParticles(BLOOD_DUST, center.x, center.y, center.z, 22, 0.65, 0.65, 0.65, 0.03);
        serverLevel.sendParticles(ParticleTypes.SMOKE, center.x, center.y, center.z, 14, 0.4, 0.4, 0.4, 0.02);

        for (LivingEntity victim : AbilityUtil.getNearbyEntities(caster, serverLevel, center, 3.6 * areaScale)) {
            victim.hurt(ModDamageTypes.source(serverLevel, ModDamageTypes.BEYONDER_GENERIC, caster), 12.0f * attributeScale);
            victim.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0, false, false, true));
            victim.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 1, false, false, true));
        }

        servant.discard();
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
