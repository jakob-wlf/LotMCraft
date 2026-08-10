package de.jakob.lotm.beyonders.abilities.red_priest;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncExplodedTrapPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ClientScheduler;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.client.multiplayer.ClientLevel;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class TrapAbility extends Ability {
    private static HashMap<UUID, Integer> amount = new HashMap<>();

    public TrapAbility(String id) {
        super(id, 8, "explosion");

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

    DustParticleOptions dustOptions = new DustParticleOptions(
            new Vector3f(237 / 255f, 50 / 255f, 50 / 255f),
            1.35f
    );

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        final int duration = 20 * 40;
        Vec3 pos = entity.position();

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
        if(amount.containsKey(entity.getUUID()) && amount.get(entity.getUUID()) >= getMaxAmount(entitySeq)){
            AbilityUtil.sendActionBar(entity,
                    Component.translatable("ability.lotmcraft.trap_ability.out_of_traps")
                            .withColor(0xFFff124d));
            return;
        }

        int current = 0;
        if(amount.containsKey(entity.getUUID()))
            current = amount.get(entity.getUUID());

        amount.put(entity.getUUID(), current + 1);

        String trapKey = entity.getUUID() + "_" + pos.x + "_" + pos.y + "_" + pos.z + "_" + amount.get(entity.getUUID());
        UUID trapId = UUID.nameUUIDFromBytes(trapKey.getBytes());

        AtomicBoolean hasExploded = new AtomicBoolean(false);

        if(!level.isClientSide()) {
            ServerScheduler.scheduleForDuration(0, 1, duration, () -> {
                if(hasExploded.get()) {
                    return;
                }

                var nearby = AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, pos, 1.35f);

                if(!nearby.isEmpty()) {
                    hasExploded.set(true);

                    // Send packet to all nearby clients to stop particles
                    if(entity instanceof ServerPlayer player) {
                        PacketHandler.sendToPlayer(player, new SyncExplodedTrapPacket(trapId));
                    }

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

                    for(var obj : nearby){
                        obj.hurt(ModDamageTypes.source(level, ModDamageTypes.IMPACT, entity), baseDamage);
                        obj.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 5, 3));
                    }
                }
            }, () -> {amount.put(entity.getUUID(), amount.get(entity.getUUID()) - 1);} ,(ServerLevel) level);
        }
        else {
            ClientScheduler.scheduleForDuration(0, 1, duration, () -> {
                if(hasExploded.get() || ClientTrapManager.hasTrapExploded(trapId))
                    return;
                ParticleUtil.spawnCircleParticles((ClientLevel) level, dustOptions, pos, 1.6f, 22);
            }, (ClientLevel) level);

            // Clean up after duration
            ClientScheduler.scheduleDelayed(duration + 10, () -> {
                ClientTrapManager.clearTrapState(trapId);
            }, (ClientLevel) level);
        }
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

    public class ClientTrapManager {
        private static final Map<UUID, Boolean> explodedTraps = new HashMap<>();

        public static void setTrapExploded(UUID entityId) {
            explodedTraps.put(entityId, true);
        }

        public static boolean hasTrapExploded(UUID entityId) {
            return explodedTraps.getOrDefault(entityId, false);
        }

        public static void clearTrapState(UUID entityId) {
            explodedTraps.remove(entityId);
        }
    }
}
