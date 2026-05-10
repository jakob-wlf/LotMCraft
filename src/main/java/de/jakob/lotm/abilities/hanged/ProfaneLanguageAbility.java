package de.jakob.lotm.abilities.hanged;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SanityComponent;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class ProfaneLanguageAbility extends SelectableAbility {
    private static final Map<String, Integer> REQUIREMENTS =
            Map.of(HangedPathwayConstants.PATHWAY_ID, HangedPathwayConstants.SEQUENCE_PROFANE_PRESBYTER);
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            LOTMCraft.MOD_ID, "textures/abilities/concealed_domain_ability.png");
    private static final float SPIRITUALITY_COST = 780.0f;
    private static final float MAX_DAMAGE_SCALE_SEQ1 = 6.8f;
    private static final float MAX_DURATION_SCALE_SEQ1 = 4.2f;

    private static final String NO_TARGET_MESSAGE = "ability.lotmcraft.profane_language.no_target";
    private static final String REVEAL_MESSAGE = "ability.lotmcraft.profane_language.reveal";

    public ProfaneLanguageAbility(String id) {
        super(id, 16.0f, "corruption", "curse", "imprison");
        canBeCopied = false;
        autoClear = false;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.profane_language.imprison",
                "ability.lotmcraft.profane_language.corrupt",
                "ability.lotmcraft.profane_language.separate",
                "ability.lotmcraft.profane_language.reveal_name"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        switch (selectedAbility) {
            case 0 -> castProfaneImprisonment(serverLevel, entity);
            case 1 -> castProfaneCorruption(serverLevel, entity);
            case 2 -> castProfaneSeparation(serverLevel, entity);
            case 3 -> castRevealName(serverLevel, entity);
            default -> castProfaneImprisonment(serverLevel, entity);
        }
    }

    private void castProfaneImprisonment(ServerLevel level, LivingEntity entity) {
        LivingEntity target = AbilityUtil.getTargetEntity(entity, 28, 1.8f, true, false, false);
        if (target == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable(NO_TARGET_MESSAGE).withColor(HangedPathwayConstants.pathwayColor()));
            return;
        }

        float durationScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_PROFANE_PRESBYTER, MAX_DURATION_SCALE_SEQ1);
        int duration = Math.round(120 * durationScale);
        HangedEffectUtil.spawnDepravityBurst(level, target.position().add(0, target.getBbHeight() * 0.45, 0), 1.1, 30);
        HangedEffectUtil.playDepravityCast(level, target.position());
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 6, false, false, true));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 2, false, false, true));
        target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, duration, 0, false, false, true));

        ServerScheduler.scheduleForDuration(0, 2, duration, () -> {
            if (!target.isAlive()) {
                return;
            }
            target.setDeltaMovement(Vec3.ZERO);
            target.hurtMarked = true;
            HangedEffectUtil.spawnShadowBurst(level, target.position().add(0, target.getBbHeight() * 0.45, 0), 0.65, 14);
        }, () -> clearArtifactScaling(entity), level, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), level)));
    }

    private void castProfaneCorruption(ServerLevel level, LivingEntity entity) {
        LivingEntity target = AbilityUtil.getTargetEntity(entity, 26, 1.7f, true, false, false);
        if (target == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable(NO_TARGET_MESSAGE).withColor(HangedPathwayConstants.pathwayColor()));
            return;
        }

        float durationScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_PROFANE_PRESBYTER, MAX_DURATION_SCALE_SEQ1);
        int duration = Math.round(140 * durationScale);
        HangedEffectUtil.spawnDepravityBurst(level, target.position().add(0, target.getBbHeight() * 0.45, 0), 1.0, 26);
        target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, duration, 1, false, false, true));
        target.addEffect(new MobEffectInstance(MobEffects.WITHER, duration, 1, false, false, true));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 2, false, false, true));
        if (target instanceof Player playerTarget) {
            playerTarget.getData(ModAttachments.SANITY_COMPONENT).decreaseSanityAndSync(0.06f, playerTarget);
        }
        if (target.getData(ModAttachments.SANITY_COMPONENT).getSanity() < 0.65f) {
            target.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, Math.max(80, duration / 2), 1, false, false, true));
        }
    }

    private void castProfaneSeparation(ServerLevel level, LivingEntity entity) {
        LivingEntity target = AbilityUtil.getTargetEntity(entity, 24, 1.7f, true, false, false);
        if (target == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable(NO_TARGET_MESSAGE).withColor(HangedPathwayConstants.pathwayColor()));
            return;
        }

        float damageScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_PROFANE_PRESBYTER, MAX_DAMAGE_SCALE_SEQ1);
        SanityComponent sanity = target.getData(ModAttachments.SANITY_COMPONENT);
        boolean degenerateMind = sanity.getSanity() < 0.72f
                || target.hasEffect(MobEffects.CONFUSION)
                || target.hasEffect(MobEffects.DARKNESS)
                || target.hasEffect(MobEffects.WITHER)
                || target.hasEffect(MobEffects.WEAKNESS);

        HangedEffectUtil.spawnDepravityBurst(level, target.position().add(0, target.getBbHeight() * 0.45, 0), 1.05, 28);
        HangedEffectUtil.playDepravityPulse(level, target.position(), 0.58f);
        sanity.setVirtualPersonaStacks(0);
        if (target instanceof Player playerTarget) {
            playerTarget.getData(ModAttachments.SANITY_COMPONENT).decreaseSanityAndSync(degenerateMind ? 0.08f : 0.04f, playerTarget);
        }

        float damage = (float) (DamageLookup.lookupDamage(7, 1.25f * damageScale) * multiplier(entity));
        target.hurt(ModDamageTypes.source(level, ModDamageTypes.DARKNESS_GENERIC, entity), damage);
        if (degenerateMind) {
            ModDamageTypes.trueDamage(target, Math.max(4.0f, damage * 0.4f), level, entity);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 90, 3, false, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 90, 2, false, false, true));
        }
    }

    private void castRevealName(ServerLevel level, LivingEntity entity) {
        float durationScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_PROFANE_PRESBYTER, MAX_DURATION_SCALE_SEQ1);
        int duration = Math.round(180 * durationScale);

        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, duration, 2, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, duration, 2, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, 1, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, duration, 1, false, false, false));
        if (entity instanceof Player player) {
            player.getData(ModAttachments.SANITY_COMPONENT).addVirtualPersonaStack();
        }

        HangedEffectUtil.spawnDepravityBurst(level, entity.position().add(0, entity.getBbHeight() * 0.55, 0), 1.1, 34);
        HangedEffectUtil.playDepravityCast(level, entity.position());
        AbilityUtil.sendActionBar(entity, Component.translatable(REVEAL_MESSAGE).withColor(HangedPathwayConstants.pathwayColor()));
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
