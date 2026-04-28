package de.jakob.lotm.abilities.visionary;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.AbilityUseTracker;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.abilities.door.ReplicatingAbility;
import de.jakob.lotm.attachments.CopiedAbilityComponent;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.entity.custom.ability_entities.door_pathway.ApprenticeBookEntity;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.CopiedAbilityHelper;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class DiscernmentAbility extends SelectableAbility {
    public DiscernmentAbility(String id) {
        super(id, 3f);

        canBeCopied = false;
        canBeUsedByNPC = false;
        cannotBeStolen = true;
        canBeReplicated = false;
        canBeUsedInArtifact = false;
        canBeShared = false;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.discernment_ability.believe_self",
                "ability.lotmcraft.discernment_ability.spectate_ability_usage",
                "ability.lotmcraft.discernment_ability.envision_skill"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        switch (selectedAbility){
            case 0 -> buff(level, entity);
            case 1 -> performSpectate(level, entity);
            case 2 -> openCopiedAbilityWheel(level, entity);
        }
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 2));
    }

    @Override
    protected float getSpiritualityCost() {
        return 200;
    }

    private void openCopiedAbilityWheel(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel) || !(entity instanceof ServerPlayer player)) return;
        CopiedAbilityHelper.openCopiedAbilityWheel(player);
    }

    private void performSpectate(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel))
            return;

        AtomicBoolean hasReplicatedAbility = new AtomicBoolean(false);

        level.playSound(null, BlockPos.containing(entity.position()), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1, 1);

        HashSet<Ability> entityAbilities = LOTMCraft.abilityHandler.getByPathwayAndSequence(
                BeyonderData.getPathway(entity), BeyonderData.getSequence(entity));

        int entitySeq = BeyonderData.getSequence(entity);

        ServerScheduler.scheduleForDuration(0, 1, 20 * 10, () -> {
            if (hasReplicatedAbility.get())
                return;

            AbilityUseTracker.AbilityUseRecord record = AbilityUseTracker.getRecentUseInArea(
                    entity.getEyePosition(), level, 40, entity);
            if (record == null || entityAbilities.contains(record.ability()))
                return;

            Ability usedAbility = record.ability();
            hasReplicatedAbility.set(true);

            if (!usedAbility.canBeReplicated) {
                AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.discernment_ability.cannot_copy"));
                level.playSound(null, BlockPos.containing(entity.position()), SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 1, 1);
                return;
            }

            if (usedAbility.lowestSequenceUsable() + 2 < entitySeq) {
                entity.hurt(entity.damageSources().source(ModDamageTypes.LOOSING_CONTROL), entity.getHealth() - .5f);
                AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.discernment_ability.too_high_sequence"));
                level.playSound(null, BlockPos.containing(entity.position()), SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 1, 1);
                return;
            }

            // 1 in 8 chance to succeed (harder than recording and replication)
            if (random.nextInt(8) != 0) {
                AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.discernment_ability.failed"));
                level.playSound(null, BlockPos.containing(entity.position()), SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 1, 1);
                return;
            }

            level.playSound(null, BlockPos.containing(entity.position()), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1, 1);

            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.discernment_ability.start_meditating"));

            ServerScheduler.scheduleDelayed(20 * 120, () -> {
                if (entity instanceof ServerPlayer player) {
                    CopiedAbilityHelper.addAbility(player,
                            new CopiedAbilityComponent.CopiedAbilityData(
                                    usedAbility.getId(),
                                    "envisioned",
                                    -1,
                                    null
                            ));
                    AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.discernment_ability.success"));
                }
            });

        }, () -> {
            if (hasReplicatedAbility.get())
                return;
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.discernment_ability.no_ability"));
            level.playSound(null, BlockPos.containing(entity.position()), SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 1, 1);
        }, serverLevel);
    }

    private void buff(Level level, LivingEntity entity){
        if(level.isClientSide) return;

        EffectManager.playEffect(EffectManager.Effect.DISCERNMENT, entity.getX(), entity.getEyePosition().y, entity.getZ(), (ServerLevel) level);

        switch (random.nextInt(4)){
            case 0 -> entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 60, 3, false, false, false));
            case 1 -> entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 60, 4, false, false, false));
            case 2 -> entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 60, 2, false, false, false));
            case 3 -> entity.addEffect(new MobEffectInstance(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 60, 3, false, false, false)));
        }
    }
}
