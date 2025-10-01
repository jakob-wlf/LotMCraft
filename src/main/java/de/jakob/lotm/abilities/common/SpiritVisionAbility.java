package de.jakob.lotm.abilities.common;

import de.jakob.lotm.abilities.ToggleAbilityItem;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.SyncSpiritVisionAbilityPacket;
import de.jakob.lotm.util.mixin.EntityAccessor;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.*;

public class SpiritVisionAbility extends ToggleAbilityItem {


    public SpiritVisionAbility(Properties properties) {
        super(properties);

        canBeCopied = false;
        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "fool", 9,
                "door", 7,
                "hermit", 9,
                "demoness", 7,
                "mother", 8,
                "wheel_of_fortune", 9
        ));
    }

    @Override
    protected void start(Level level, LivingEntity entity) {
        if(!level.isClientSide) {
            if(entity instanceof ServerPlayer player) {
                PacketHandler.sendToPlayer(player,  new SyncSpiritVisionAbilityPacket(true, -1));
            }
            return;
        }

        entity.playSound(SoundEvents.ENCHANTMENT_TABLE_USE, 1, 1);
    }

    private final DustParticleOptions dust = new DustParticleOptions(new Vector3f(255, 255, 255), 2f);

    private final HashMap<UUID, Set<Entity>> glowingEntities = new HashMap<>();

    @Override
    protected void tick(Level level, LivingEntity entity) {
        if(level.isClientSide()) {
            List<LivingEntity> nearbyEntities = AbilityUtil.getNearbyEntities(entity, (ClientLevel) level, entity.getEyePosition(), 30)
                    .stream()
                    .toList();

            for (LivingEntity nearbyEntity : nearbyEntities) {
                ParticleUtil.spawnParticles((ClientLevel) level, dust, nearbyEntity.getEyePosition().subtract(0, nearbyEntity.getEyeHeight() / 2, 0), 3, .6, .95, .6, 0);
            }
        }
        else {
            if(!(entity instanceof ServerPlayer player))
                return;

            LivingEntity lookedAt = AbilityUtil.getTargetEntity(entity, 40, 1.2f);
            PacketHandler.sendToPlayer(player,  new SyncSpiritVisionAbilityPacket(true, lookedAt == null ? -1 : lookedAt.getId()));

            entity.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 10, 1, false, false, false));

            List<LivingEntity> nearbyEntities = AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, entity.getEyePosition(), 30)
                    .stream()
                    .toList();

            for (LivingEntity nearbyEntity : nearbyEntities) {
                setGlowingForPlayer(nearbyEntity, (ServerPlayer) entity, true);
            }

            glowingEntities.putIfAbsent(entity.getUUID(), new HashSet<>(Set.of()));
            glowingEntities.get(entity.getUUID()).addAll(nearbyEntities);
        }
    }

    public static void setGlowingForPlayer(Entity entity, ServerPlayer player, boolean glowing) {
        EntityDataAccessor<Byte> FLAGS = EntityAccessor.getSharedFlagsId();

        // Current flags from the entity
        byte flags = entity.getEntityData().get(FLAGS);

        if (glowing) {
            flags |= 0x40; // glowing bit
        } else {
            flags &= ~0x40; // clear glowing bit
        }

        // Build a list of data values (only the one we care about)
        List<SynchedEntityData.DataValue<?>> values = new ArrayList<>();
        values.add(SynchedEntityData.DataValue.create(FLAGS, flags));

        // Send metadata update ONLY to that player
        ClientboundSetEntityDataPacket packet =
                new ClientboundSetEntityDataPacket(entity.getId(), values);
        player.connection.send(packet);
    }

    @Override
    protected void stop(Level level, LivingEntity entity) {
        if(level.isClientSide) {
            entity.playSound(SoundEvents.ENCHANTMENT_TABLE_USE, 1, 1);
        }
        else {
            if(!(entity instanceof ServerPlayer player))
                return;
            if(glowingEntities.containsKey(entity.getUUID()))
                glowingEntities.get(entity.getUUID()).forEach(e -> setGlowingForPlayer(e, player, false));
            glowingEntities.remove(entity.getUUID());

            PacketHandler.sendToPlayer(player,  new SyncSpiritVisionAbilityPacket(false, -1));
        }
    }

    private int getRandomColor() {
        Random random = new Random();
        int alpha = 0xFF; // Full opacity
        int red = random.nextInt(256);   // 0 to 255
        int green = random.nextInt(256); // 0 to 255
        int blue = random.nextInt(256);  // 0 to 255

        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    @Override
    protected float getSpiritualityCost() {
        return 0;
    }

}
