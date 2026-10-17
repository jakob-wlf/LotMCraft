package de.jakob.lotm.beyonders.abilities.red_priest;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.CancelPhotonBlockEffectPacket;
import de.jakob.lotm.network.packets.toClient.CancelPhotonEntityEffectPacket;
import de.jakob.lotm.network.packets.toClient.PlayCancellablePhotonBlockEffectPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.PerformantExplosion;
import de.jakob.lotm.util.scheduling.ClientScheduler;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class TrapAbility extends Ability {

    private static HashMap<UUID, Integer> amount = new HashMap<>();

    public TrapAbility(String id) {
        super(id, 6, "explosion");

        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(1, 1, 1, 2, 3, 4, 5, 6, 7, 8));

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(1200f, 455f, 260f, 160f, 150f, 84f, 64f, 62.5f, 32f, 48f));

        baseDamage = 5f;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "red_priest", 9
        ));
    }

    @Override
    protected float getSpiritualityCost() {
        return 10;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide) return;

        final int duration = 20 * 30;
        Vec3 pos = entity.position();

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
        if(amount.containsKey(entity.getUUID()) && amount.get(entity.getUUID()) >= getMaxAmount(entitySeq)){
            AbilityUtil.sendActionBar(entity,
                    Component.translatable("ability.lotmcraft.trap_ability.out_of_traps")
                            .withColor(0xFFff124d));
            return;
        }

        AtomicBoolean hasExploded = new AtomicBoolean(false);

        UUID effectUUID = UUID.randomUUID();

        BlockPos blockPos = BlockPos.containing(pos);

        double offsetX = pos.x - (blockPos.getX() + 0.5);
        double offsetY = pos.y - (blockPos.getY() + 0.5);
        double offsetZ = pos.z - (blockPos.getZ() + 0.5);


        PacketHandler.sendToNearbyPlayers(
                new PlayCancellablePhotonBlockEffectPacket(
                        "trap", blockPos, offsetX, offsetY, offsetZ, 2.5, null, -1, true, true, null, effectUUID
                ),
                (ServerLevel) level, pos, 128
        );

        ServerScheduler.scheduleForDuration(0, 1, duration, () -> {
            if(hasExploded.get()) {
                return;
            }

            var nearby = AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, pos, 1.35f);

            if(!nearby.isEmpty()) {
                hasExploded.set(true);
                PacketHandler.sendToNearbyPlayers(new CancelPhotonBlockEffectPacket(effectUUID), (ServerLevel) level, pos, 128);

                ((ServerLevel)level).sendParticles(
                        ParticleTypes.EXPLOSION_EMITTER,
                        pos.x,
                        pos.y,
                        pos.z,
                        1,
                        0,
                        0,
                        0,
                        0
                );

                ((ServerLevel)level).playSound(
                        null,
                        pos.x,
                        pos.y,
                        pos.z,
                        SoundEvents.GENERIC_EXPLODE,
                        SoundSource.BLOCKS,
                        4.0F,
                        1.0F
                );

                if(BeyonderData.isGriefingEnabled(entity)) {
                    PerformantExplosion.blocksOnly(level, entity, pos, 4, true, Explosion.BlockInteraction.DESTROY_WITH_DECAY);
                }

                for(var obj : nearby){
                    obj.hurt(ModDamageTypes.source(level, ModDamageTypes.IMPACT, entity), baseDamage);
                    obj.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 5, 3));
                }
            }
        }, () -> amount.put(entity.getUUID(), amount.get(entity.getUUID()) - 1), (ServerLevel) level);
    }

    private static int getMaxAmount(int seq){
        return switch (seq){
            case 9 -> 3;
            case 8 -> 4;
            case 7 -> 5;
            case 6 -> 7;
            case 5 -> 8;
            case 4 -> 10;
            case 3 -> 11;
            case 2 -> 13;
            case 1 -> 14;
            case 0 -> 15;
            default -> 0;
        };
    }
}
