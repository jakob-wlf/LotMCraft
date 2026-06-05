package de.jakob.lotm.abilities.sun;

import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.mixin.EntityAccessor;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;
import java.util.stream.Collectors;

public class EvilDetectionAbility extends ToggleAbility {

    private final HashMap<UUID, HashSet<LivingEntity>> glowingEntities = new HashMap<>();

    public EvilDetectionAbility(String id) {
        super(id);

        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("sun", 8));
    }

    @Override
    protected float getSpiritualityCost() {
        return 2;
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if(level.isClientSide) return;
        if(!(entity instanceof ServerPlayer player)) return;

        List<LivingEntity> nearby = AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, entity.getEyePosition(), 50)
                .stream()
                .filter(AbilityUtil::isUndead)
                .toList();

        HashSet<LivingEntity> currentlyGlowing = glowingEntities.getOrDefault(player.getUUID(), new HashSet<>());
        currentlyGlowing.forEach(e -> {
            if(e.distanceTo(entity) > 50) {
                setGlowingForPlayer(e, player, false);
            }
        });
        currentlyGlowing.removeIf(e -> e.distanceTo(entity) > 50 || !nearby.contains(e));

        HashSet<LivingEntity> newGlowing = new HashSet<>(currentlyGlowing);
        newGlowing.addAll(nearby);
        newGlowing.forEach(e -> {
            if(!currentlyGlowing.contains(e)) {
                setGlowingForPlayer(e, player, true);
            }
        });
        glowingEntities.put(player.getUUID(), newGlowing);
    }

    @Override
    public void start(Level level, LivingEntity entity) {

    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if(level.isClientSide) return;
        if(!(entity instanceof ServerPlayer player)) return;

        HashSet<LivingEntity> currentlyGlowing = glowingEntities.remove(player.getUUID());
        if(currentlyGlowing == null) return;

        currentlyGlowing.forEach(e -> setGlowingForPlayer(e, player, false));
    }

    private void setGlowingForPlayer(Entity entity, ServerPlayer player, boolean glowing) {
        EntityDataAccessor<Byte> FLAGS = EntityAccessor.getSharedFlagsId();


        byte flags = entity.getEntityData().get(FLAGS);

        if (glowing) {
            flags |= 0x40; // glowing bit
        } else {
            flags &= ~0x40; // clear glowing bit
        }

        List<SynchedEntityData.DataValue<?>> values = new ArrayList<>();
        values.add(SynchedEntityData.DataValue.create(FLAGS, flags));

        ClientboundSetEntityDataPacket packet =
                new ClientboundSetEntityDataPacket(entity.getId(), values);
        player.connection.send(packet);
    }
}
