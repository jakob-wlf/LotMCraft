package de.jakob.lotm.abilities.fool;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class FlamingJumpAbility extends Ability {
    public FlamingJumpAbility(String id) {
        super(id, .05f);

        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("fool", 7));
    }

    @Override
    public float getSpiritualityCost() {
        return 12;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        BlockPos block = getSelectedFire(level, entity, true);

        if(block == null) {
            if(entity instanceof ServerPlayer player) {
                ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.translatable("lotmcraft.flaming_jump_ability.no_fire_found").withColor(0xFFff124d));
                player.connection.send(packet);
            }
            return;
        }

        entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 3, 1, false, false, false));
        ServerScheduler.scheduleForDuration(0, 1, 20 * 3, () -> entity.setRemainingFireTicks(0));
        entity.teleportTo(block.getCenter().x, block.getCenter().y + .75, block.getCenter().z);

        level.playSound(null, block, SoundEvents.BLAZE_SHOOT, SoundSource.BLOCKS, 1, 1);
        ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.FLAME, block.getCenter().add(0, .8, 0), 60, .3, .8, .3, .05);
    }

    @Override
    public void onHold(Level level, LivingEntity entity) {
        if(!level.isClientSide)
            return;

        BlockPos selectedFire = getSelectedFire(level, entity, false);
        if(selectedFire == null) {
            return;
        }

        ParticleUtil.spawnParticles((ClientLevel) level, ParticleTypes.FLASH, selectedFire.getCenter(), 20, .1, 0);
    }

    public BlockPos getSelectedFire(Level level, LivingEntity entity, boolean checkNearestIfNoneSelected) {
        BlockPos block = AbilityUtil.getTargetBlock(entity, 50, false, true);

        if(level.getBlockState(block).is(Blocks.FIRE))
            return block;

        BlockPos nextBestBlock = AbilityUtil.getBlocksInSphereRadius(level, block.getCenter(), 3.2, true, false, false)
                .stream()
                .filter(b -> level.getBlockState(b).getBlock() instanceof BaseFireBlock)
                .min(Comparator.comparing(b -> b.distToCenterSqr(block.getCenter()))).orElse(null);

        if(nextBestBlock != null) {
            return nextBestBlock;
        }

        if(!checkNearestIfNoneSelected) {
            return null;
        }

        return AbilityUtil.getBlocksInSphereRadius(level, entity.position(), 20, true, false, false)
                .stream()
                .filter(b -> level.getBlockState(b).is(Blocks.FIRE))
                .min(Comparator.comparing(b -> b.distToCenterSqr(entity.position()))).orElse(null);
    }
}
