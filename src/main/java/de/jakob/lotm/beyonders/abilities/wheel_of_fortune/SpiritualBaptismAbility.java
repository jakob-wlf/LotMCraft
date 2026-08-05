package de.jakob.lotm.beyonders.abilities.wheel_of_fortune;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.TransformationComponent;
import de.jakob.lotm.beyonders.abilities.common.passives.FateResistanceAbility;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SanityComponent;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.beyonders.abilities.error.ParasitationAbility;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.LuckManager;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.marionettes.MarionetteUtils;
import de.jakob.lotm.util.shapeShifting.ShapeShiftingUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpiritualBaptismAbility extends SelectableAbility {
    private static final String corruptionCleanseCooldownKey = "lotm_spiritual_baptism_corruption_cleanse";
    private static final long corruptionCleanseCooldownTicks = 20L * 60 * 30;

    public SpiritualBaptismAbility(String id) {
        super(id, 5, "cleansing");
        canBeShared = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("wheel_of_fortune", 3));
    }

    @Override
    public float getSpiritualityCost() {
        return 900;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.spiritual_baptism.on_self",
                "ability.lotmcraft.spiritual_baptism.on_target"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        switch(selectedAbility){
            case 0 -> onSelf(level, entity);
            case 1 -> onTarget(level, entity);
        }
    }

    private  void onSelf(Level level, LivingEntity entity){
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        performBaptism(entity, entity, serverLevel);
    }

    private void onTarget(Level level, LivingEntity entity){
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        LivingEntity target = AbilityUtil.getTargetEntity(
            entity, (int) (15 * (multiplier(entity) * multiplier(entity))), 2, false, true, true);

        if(target == null) {
            target = entity;
        }

        performBaptism(entity, target, serverLevel);
    }

    private void performBaptism(LivingEntity caster, LivingEntity target, ServerLevel serverLevel){
        EffectManager.playEffect(EffectManager.Effect.SPIRITUAL_BAPTISM, target.getX(), target.getY(), target.getZ(), serverLevel);
        target.addEffect(new MobEffectInstance(MobEffects.HEAL, 5, 40, false, false, false));

        ParasitationAbility.cleanseParasite(serverLevel, target);
        boolean wasMarionette = MarionetteUtils.isMarionette(target);
        boolean wasPuppetNpc = target instanceof BeyonderNPCEntity npc && npc.isPuppetWarrior();
        if (wasMarionette) {
            MarionetteUtils.releaseMarionetteControl(target);
        }
        if ((wasMarionette || wasPuppetNpc) && target instanceof BeyonderNPCEntity npc) {
            npc.restoreNormalNpcState();
        }
        cleanseTransformations(target);

        target.setRemainingFireTicks(0);

        List<MobEffectInstance> harmfulEffects = target.getActiveEffects()
                .stream()
                .filter(effectInstance -> effectInstance.getEffect().value().getCategory() == MobEffectCategory.HARMFUL)
                .toList();

        LivingEntity finalTarget = target;
        harmfulEffects.forEach(effectInstance -> finalTarget.removeEffect(effectInstance.getEffect()));

        if(target instanceof Player player) {
            player.getFoodData().setSaturation(20);
            player.getFoodData().setFoodLevel(20);
        }

        if(LuckManager.getLuck(target) < 0) {
            // Fate Resistance: only seq 1 can change this target's luck
            int casterSeq = AbilityUtil.getSeqWithArt(caster, this);
            if (!FateResistanceAbility.blocksLuckChange(target.getUUID(), casterSeq)) {
                LuckManager.resetLuck(target);
            }
        }

        SanityComponent sanityComponent = target.getData(ModAttachments.SANITY_COMPONENT);
        sanityComponent.increaseSanityWithSequenceDifference(.15f, target, AbilityUtil.getSeqWithArt(caster, this), BeyonderData.getSequence(target));

        if (target instanceof ServerPlayer player) {
            long gameTime = serverLevel.getGameTime();
            long availableAt = player.getPersistentData().getLong(corruptionCleanseCooldownKey);
            if (gameTime >= availableAt) {
                de.jakob.lotm.events.CorruptionEventHandler.revertFullCorruption(player);
                BeyonderData.setCorruption(player, 0);
                player.getPersistentData().putLong(
                        corruptionCleanseCooldownKey, gameTime + corruptionCleanseCooldownTicks);
            } else if (caster instanceof ServerPlayer casterPlayer) {
                long remainingSeconds = (availableAt - gameTime + 19) / 20;
                casterPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "\u00A7eThis target's corruption can be cleansed again in "
                                + (remainingSeconds / 60) + "m " + (remainingSeconds % 60) + "s."));
            }
        }
    }

    private static void cleanseTransformations(LivingEntity target) {
        if (target instanceof ServerPlayer player) {
            if (LOTMCraft.abilityHandler.getById("zombie_disguise_ability") instanceof ToggleAbility zombieDisguise
                    && zombieDisguise.isActiveForEntity(player)) {
                zombieDisguise.onAbilityUse(player.serverLevel(), player);
            } else {
                ShapeShiftingUtil.resetShape(player);
            }
        }

        TransformationComponent transformation = target.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        if (transformation.isTransformed()) {
            transformation.setTransformedAndSync(false, target);
        }
    }
}