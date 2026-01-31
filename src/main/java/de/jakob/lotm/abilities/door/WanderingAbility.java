package de.jakob.lotm.abilities.door;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.helper.ParticleUtil;
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
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.StreamSupport;

public class WanderingAbility extends Ability {
    public WanderingAbility(String id) {
        super(id, 1);

        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 3));
    }

    @Override
    public float getSpiritualityCost() {
        return 200;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide || !(level instanceof ServerLevel serverLevel))
            return;



        List<ServerLevel> dimensions = StreamSupport.stream(serverLevel.getServer().getAllLevels().spliterator(), false)
                .filter(s -> !s.dimension().equals(ModDimensions.SEFIRAH_CASTLE_DIMENSION_KEY))
                .filter(s -> !s.dimension().equals(ModDimensions.CONCEALMENT_WORLD_DIMENSION_KEY))
                .toList();

        if(dimensions.size() <= 1) {
            if(entity instanceof ServerPlayer player) {
                ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.translatable("lotmcraft.wandering_ability.no_dimension_found").withColor(0xFF68dff7));
                player.connection.send(packet);
            }
        }

        int currentIndex = dimensions.indexOf(serverLevel);
        if(currentIndex == -1) {
            if(entity instanceof ServerPlayer player) {
                ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.translatable("lotmcraft.wandering_ability.no_dimension_found").withColor(0xFF68dff7));
                player.connection.send(packet);
            }
        }

        int nextIndex = (currentIndex + 1) % dimensions.size();
        ServerLevel targetLevel = dimensions.get(nextIndex);
        entity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20 * 5, 1, false, false, false));
        double yValue = entity.position().y;
        for(int i = 0; i < 100; i++) {
            BlockPos pos = BlockPos.containing(entity.getX(), yValue, entity.getZ());
            BlockState state = targetLevel.getBlockState(pos);

            if(state.getCollisionShape(targetLevel, pos).isEmpty()) {
                break;
            }

            yValue += 1;
        }
        ParticleUtil.spawnParticles((ServerLevel) level, ModParticles.STAR.get(), entity.position().add(0, 1, 0), 100, .4, 1.1, .4, .05);
        ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.ENCHANT, entity.position().add(0, 1, 0), 100, .4, 1.1, .4, .05);
        level.playSound(null, entity.blockPosition(), SoundEvents.ENDER_CHEST_OPEN, SoundSource.BLOCKS, 1, 1);
        entity.teleportTo(targetLevel, entity.getX(), yValue, entity.getZ(), Set.of(), entity.getYRot(), entity.getXRot());
        ParticleUtil.spawnParticles(targetLevel, ModParticles.STAR.get(), entity.position().add(0, 1, 0), 100, .4, 1.1, .4, .05);
        ParticleUtil.spawnParticles(targetLevel, ParticleTypes.ENCHANT, entity.position().add(0, 1, 0), 100, .4, 1.1, .4, .05);
        targetLevel.playSound(null, entity.blockPosition(), SoundEvents.ENDER_CHEST_OPEN, SoundSource.BLOCKS, 1, 1);

    }
}
