package de.jakob.lotm.abilities.red_priest;

import de.jakob.lotm.abilities.ToggleAbilityItem;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.mixin.EntityAccessor;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.*;

public class CullAbility extends ToggleAbilityItem {
    private final HashMap<UUID, Set<Entity>> glowingEntities = new HashMap<>();


    public CullAbility(Properties properties) {
        super(properties);
    }

    @Override
    protected float getSpiritualityCost() {
        return 3;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("red_priest", 5));
    }

    @Override
    protected void start(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;
        BeyonderData.addModifier(entity, "cull", 1.7);
    }

    @Override
    protected void tick(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        List<LivingEntity> nearbyEntities = AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, entity.getEyePosition(), 30)
                .stream()
                .toList();

        for (LivingEntity nearbyEntity : nearbyEntities) {
            setGlowingForPlayer(nearbyEntity, (ServerPlayer) entity, true);
        }

        glowingEntities.putIfAbsent(entity.getUUID(), new HashSet<>(Set.of()));
        glowingEntities.get(entity.getUUID()).addAll(nearbyEntities);
    }

    @Override
    protected void stop(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;
        BeyonderData.removeModifier(entity, "cull");
        if(!(entity instanceof ServerPlayer player))
            return;
        if(glowingEntities.containsKey(entity.getUUID()))
            glowingEntities.get(entity.getUUID()).forEach(e -> setGlowingForPlayer(e, player, false));
        glowingEntities.remove(entity.getUUID());
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
}
