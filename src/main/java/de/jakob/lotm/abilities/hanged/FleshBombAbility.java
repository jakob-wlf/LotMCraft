package de.jakob.lotm.abilities.hanged;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Map;

public class FleshBombAbility extends SelectableAbility {
    private static final Map<String, Integer> REQUIREMENTS =
            Map.of(HangedPathwayConstants.PATHWAY_ID, HangedPathwayConstants.SEQUENCE_ROSE_BISHOP);
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            LOTMCraft.MOD_ID, "textures/abilities/mutation_creation_ability.png");
    private static final float SPIRITUALITY_COST = 210.0f;
    private static final float MAX_DAMAGE_SCALE_SEQ1 = 8.0f;
    private static final float MAX_AREA_SCALE_SEQ1 = 4.0f;
    private static final float MAX_RANGE_SCALE_SEQ1 = 3.0f;
    private static final DustParticleOptions BLOOD_DUST =
            new DustParticleOptions(new Vector3f(0.73f, 0.1f, 0.16f), 1.4f);

    private static final String NO_TARGET_MESSAGE = "ability.lotmcraft.flesh_bomb.no_target";
    private static final String IMPLANTED_MESSAGE = "ability.lotmcraft.flesh_bomb.implanted";

    public FleshBombAbility(String id) {
        super(id, 7f, "corruption");
        canBeCopied = false;
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
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.flesh_bomb.throw",
                "ability.lotmcraft.flesh_bomb.implant"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        switch (selectedAbility) {
            case 0 -> throwBomb(serverLevel, entity);
            case 1 -> implantBomb(serverLevel, entity);
            default -> throwBomb(serverLevel, entity);
        }
    }

    private void throwBomb(ServerLevel serverLevel, LivingEntity entity) {
        float rangeScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_ROSE_BISHOP, MAX_RANGE_SCALE_SEQ1);
        Vec3 targetLocation = AbilityUtil.getTargetLocation(entity, Math.max(20, Math.round(20 * rangeScale)), 1.3f);
        tearAwayFlesh(entity);
        HangedEffectUtil.spawnFleshTrail(serverLevel, entity.getEyePosition(), targetLocation.add(0, 0.6, 0), 0.45);
        detonate(serverLevel, entity, targetLocation, false);
    }

    private void implantBomb(ServerLevel serverLevel, LivingEntity entity) {
        float rangeScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_ROSE_BISHOP, MAX_RANGE_SCALE_SEQ1);
        LivingEntity target = AbilityUtil.getTargetEntity(entity, Math.max(16, Math.round(16 * rangeScale)), 1.6f, true, false, false);
        if (target == null) {
            AbilityUtil.sendActionBar(entity,
                    Component.translatable(NO_TARGET_MESSAGE).withColor(HangedPathwayConstants.pathwayColor()));
            return;
        }

        tearAwayFlesh(entity);
        HangedEffectUtil.playFleshCast(serverLevel, target.position());
        AbilityUtil.sendActionBar(entity,
                Component.translatable(IMPLANTED_MESSAGE, target.getDisplayName()).withColor(HangedPathwayConstants.pathwayColor()));
        ServerScheduler.scheduleDelayed(30, () -> {
            if (target.isAlive()) {
                detonate(serverLevel, entity, target.position().add(0, target.getBbHeight() * 0.45, 0), true);
            }
        });
    }

    private void tearAwayFlesh(LivingEntity entity) {
        ModDamageTypes.trueDamage(entity, 1.5f);
        if (entity.level() instanceof ServerLevel serverLevel) {
            HangedEffectUtil.spawnFleshBurst(serverLevel, entity.position().add(0, entity.getBbHeight() * 0.45, 0), 0.55, 16);
            HangedEffectUtil.playFleshCast(serverLevel, entity.position());
        }
    }

    private void detonate(ServerLevel serverLevel, LivingEntity entity, Vec3 center, boolean implanted) {
        float damageScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_ROSE_BISHOP, MAX_DAMAGE_SCALE_SEQ1);
        float areaScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_ROSE_BISHOP, MAX_AREA_SCALE_SEQ1);
        serverLevel.playSound(null, center.x, center.y, center.z, SoundEvents.SLIME_SQUISH, SoundSource.HOSTILE, 1.9f, 0.55f);
        serverLevel.playSound(null, center.x, center.y, center.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.2f, 1.1f);
        HangedEffectUtil.spawnFleshBurst(serverLevel, center, 1.2 * areaScale, 54);
        serverLevel.sendParticles(BLOOD_DUST, center.x, center.y, center.z, 26, 0.8, 0.8, 0.8, 0.03);
        serverLevel.sendParticles(ParticleTypes.SMOKE, center.x, center.y, center.z, 18, 0.55, 0.55, 0.55, 0.03);

        float baseDamage = (float) (DamageLookup.lookupDamage(7, 1.05f * damageScale) * multiplier(entity));
        if (implanted) {
            baseDamage *= 1.45f;
        }

        double blastRadius = (implanted ? 4.3 : 3.8) * areaScale;
        for (LivingEntity victim : AbilityUtil.getNearbyEntities(entity, serverLevel, center, blastRadius)) {
            victim.hurt(ModDamageTypes.source(serverLevel, ModDamageTypes.BEYONDER_GENERIC, entity), baseDamage);
            victim.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1, false, false, true));
            victim.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0, false, false, true));
            victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 1, false, false, true));
            victim.addEffect(new MobEffectInstance(MobEffects.HUNGER, 80, 1, false, false, true));
        }
    }

    @Override
    public ResourceLocation getTextureLocation() {
        return TEXTURE;
    }
}
