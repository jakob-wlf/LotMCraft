package de.jakob.lotm.abilities.door;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.AbilityUseTracker;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.attachments.CopiedAbilityComponent;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.entity.custom.ability_entities.door_pathway.ApprenticeBookEntity;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class RecordingAbility extends SelectableAbility {
    public RecordingAbility(String id) {
        super(id, 8f);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 6));
    }

    @Override
    public float getSpiritualityCost() {
        return 0;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.recording.record",
                "ability.lotmcraft.recording.use_copied"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        if (selectedAbility == 0) {
            performRecording(level, entity);
        } else if (selectedAbility == 1) {
            openCopiedAbilityWheel(level, entity);
        }
    }

    private void openCopiedAbilityWheel(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel) || !(entity instanceof ServerPlayer player)) return;
        CopiedAbilityHelper.openCopiedAbilityWheel(player);
    }

    private void performRecording(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel))
            return;

        Vec3 playerDir = (new Vec3(entity.getLookAngle().x, 0, entity.getLookAngle().z)).normalize();
        Vec3 pos = VectorUtil.getRelativePosition(entity.getEyePosition().add(0, -.4, 0), playerDir, 1.2, 0, -.4);
        Vec3 dir = entity.getEyePosition().subtract(pos).normalize();

        ApprenticeBookEntity book = new ApprenticeBookEntity(level, pos, dir);
        level.addFreshEntity(book);

        AtomicBoolean hasRecordedAbility = new AtomicBoolean(false);

        level.playSound(null, BlockPos.containing(entity.position()), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1, 1);

        ServerScheduler.scheduleForDuration(0, 1, 20 * 5, () -> {
            if (hasRecordedAbility.get())
                return;

            Vec3 currentPlayerDir = (new Vec3(entity.getLookAngle().x, 0, entity.getLookAngle().z)).normalize();
            Vec3 currentPos = VectorUtil.getRelativePosition(entity.getEyePosition().add(0, -.8, 0), currentPlayerDir, 1.1, 0, -.2);
            Vec3 currentDir = entity.getEyePosition().subtract(currentPos).normalize();
            book.setPos(currentPos);
            book.setFacingDirection(currentDir);

            AbilityUseTracker.AbilityUseRecord record = AbilityUseTracker.getRecentUseInArea(
                    entity.getEyePosition(), level, 15, entity);
            if (record == null || record.ability() instanceof RecordingAbility)
                return;

            Ability usedAbility = record.ability();
            hasRecordedAbility.set(true);
            book.discard();

            if (!usedAbility.canBeCopied) {
                AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.recording.cannot_copy").withColor(0xFF8ff4ff));
                level.playSound(null, BlockPos.containing(entity.position()), SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 1, 1);
                ParticleUtil.spawnParticles(serverLevel, ParticleTypes.PORTAL, pos, 45, .3, .02);
                return;
            }

            if (usedAbility.lowestSequenceUsable() + 2 < BeyonderData.getSequence(entity)) {
                entity.hurt(entity.damageSources().source(ModDamageTypes.LOOSING_CONTROL), entity.getHealth() - .5f);
                AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.recording.too_high_sequence").withColor(0xFF8ff4ff));
                level.playSound(null, BlockPos.containing(entity.position()), SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 1, 1);
                ParticleUtil.spawnParticles(serverLevel, ParticleTypes.PORTAL, pos, 45, .3, .02);
                return;
            }

            if (random.nextBoolean()) {
                AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.recording.failed").withColor(0xFF8ff4ff));
                level.playSound(null, BlockPos.containing(entity.position()), SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 1, 1);
                ParticleUtil.spawnParticles(serverLevel, ParticleTypes.PORTAL, pos, 45, .3, .02);
                return;
            }

            level.playSound(null, BlockPos.containing(entity.position()), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1, 1);

            if (entity instanceof ServerPlayer player) {
                CopiedAbilityHelper.addAbility(player,
                        new CopiedAbilityComponent.CopiedAbilityData(
                                usedAbility.getId(),
                                "recorded",
                                1,
                                null
                        ));
                AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.recording.success").withColor(0xFF8ff4ff));
            }

            ParticleUtil.spawnParticles(serverLevel, ParticleTypes.END_ROD, book.position(), 60, .3, .08);
            ParticleUtil.spawnParticles(serverLevel, ParticleTypes.ENCHANT, book.position(), 60, .3, .08);
        }, () -> {
            if (hasRecordedAbility.get())
                return;
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.recording.no_ability").withColor(0xFF8ff4ff));
            level.playSound(null, BlockPos.containing(entity.position()), SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 1, 1);
            ParticleUtil.spawnParticles(serverLevel, ParticleTypes.PORTAL, book.position(), 90, .3, .1);
            book.discard();
        }, serverLevel);
    }
}
