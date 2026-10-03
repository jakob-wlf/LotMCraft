package de.jakob.lotm.beyonders.abilities.sun;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.PlayPhotonBlockEffectPacket;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnshadowedDomainAbility extends Ability {
    public UnshadowedDomainAbility(String id) {
        super(id, 50, "purification", "purification_holy", "light_source", "light_strong", "light_weak");
        interactionRadius = 40;
        interactionCacheTicks = 20 * 30;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("sun", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 800;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 startPos = entity.position();

        List<BlockPos> blocks = AbilityUtil.getBlocksInSphereRadius((ServerLevel) level, startPos, 40, true, false, false).stream().filter(
                b -> {
                    BlockState state = level.getBlockState(b);
                    return state.isAir() &&
                            (!level.getBlockState(b.below()).isAir() ||
                                    !level.getBlockState(b.above()).isAir() ||
                                    !level.getBlockState(b.north()).isAir() ||
                                    !level.getBlockState(b.south()).isAir() ||
                                    !level.getBlockState(b.east()).isAir() ||
                                    !level.getBlockState(b.west()).isAir());
                }
        ).toList();

        blocks.forEach(b -> level.setBlockAndUpdate(b, Blocks.LIGHT.defaultBlockState()));

        ServerScheduler.scheduleDelayed(2, () -> {
            PacketHandler.sendToNearbyPlayers(
                    new PlayPhotonBlockEffectPacket("unshadowed_domain", BlockPos.containing(startPos), 0, 0, 0, 1, null, -1, true, false),
                    (ServerLevel) level,
                    startPos,
                    512
            );
        });

        ServerScheduler.scheduleForDuration(0, 10, 20 * 30, () -> {
            AbilityUtil.addPotionEffectToNearbyEntities((ServerLevel) level, entity, 40, startPos, new MobEffectInstance(MobEffects.GLOWING, 20 * 2, 1, false, false, false));
            AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, startPos, 40)
                    .stream()
                    .filter(e -> (AbilityUtil.isUndead(e)) && (e instanceof Mob || e instanceof Player))
                    .forEach(e -> e.hurt(ModDamageTypes.source(level, ModDamageTypes.PURIFICATION_INDIRECT, entity), (float) (DamageLookup.lookupDps(4, .4, 10, 20) * multiplier(entity))));
        }, () -> blocks.forEach(b -> {
            BlockState state = level.getBlockState(b);
            if(state.is(Blocks.LIGHT))
                level.setBlockAndUpdate(b, Blocks.AIR.defaultBlockState());
        }), (ServerLevel) level, () -> AbilityUtil.getTimeInArea(entity, new de.jakob.lotm.util.data.Location(entity.position(), level)));
    }
}
