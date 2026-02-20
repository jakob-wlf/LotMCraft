package de.jakob.lotm.abilities.core;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncToggleAbilityPacket;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public abstract class ToggleAbility extends Ability {

    private static final HashMap<UUID, HashSet<ToggleAbility>> activeAbilities = new HashMap<>();
    private static final HashMap<UUID, HashSet<ToggleAbility>> activeAbilitiesClientCache = new HashMap<>();

    protected ToggleAbility(String id) {
        super(id, 0);

        canBeUsedByNPC = false;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide) {
            return;
        }

        if(!activeAbilities.containsKey(entity.getUUID()) || !activeAbilities.get(entity.getUUID()).contains(this)) {
            activeAbilities.putIfAbsent(entity.getUUID(), new HashSet<>());
            activeAbilities.get(entity.getUUID()).add(this);
            start(level, entity);
            PacketHandler.sendToAllPlayersInSameLevel(new SyncToggleAbilityPacket(entity.getId(), getId(), SyncToggleAbilityPacket.Action.START.getValue()), (ServerLevel) level);
            return;
        }

        cancel((ServerLevel) level, entity);
    }

    protected void cancel(ServerLevel level, LivingEntity entity) {
        if(activeAbilities.containsKey(entity.getUUID())) {
            activeAbilities.get(entity.getUUID()).remove(this);
        }
        stop(level, entity);
        PacketHandler.sendToAllPlayersInSameLevel(new SyncToggleAbilityPacket(entity.getId(), getId(), SyncToggleAbilityPacket.Action.STOP.getValue()), level);
    }

    public static void cleanUp(ServerLevel serverLevel, LivingEntity entity) {
        if(!activeAbilities.containsKey(entity.getUUID())) {
            return;
        }

        (new HashSet<>(activeAbilities.get(entity.getUUID()))).forEach(toggleAbility -> {
            toggleAbility.cancel(serverLevel, entity);
        });
    }

    public void prepareTick(Level level, LivingEntity entity) {
        if(!level.isClientSide && shouldConsumeSpirituality(entity)) {
            if(BeyonderData.getSpirituality(entity) <= getSpiritualityCost()) {
                cancel((ServerLevel) level, entity);
                return;
            }

            BeyonderData.reduceSpirituality(entity, getSpiritualityCost());
        }

        tick(level, entity);
    }

    public abstract void tick(Level level, LivingEntity entity);
    public abstract void start(Level level, LivingEntity entity);
    public abstract void stop(Level level, LivingEntity entity);

    public boolean isActiveForEntity(LivingEntity entity) {
        if(!entity.level().isClientSide) {
            return activeAbilities.containsKey(entity.getUUID()) && activeAbilities.get(entity.getUUID()).contains(this);
        }
        else {
            return activeAbilitiesClientCache.containsKey(entity.getUUID()) && activeAbilities.get(entity.getUUID()) != null && activeAbilities.get(entity.getUUID()).contains(this);
        }
    }

    public void updateClientCache(LivingEntity entity, boolean active) {
        if(!entity.level().isClientSide) {
            return;
        }

        activeAbilitiesClientCache.putIfAbsent(entity.getUUID(), new HashSet<>());
        if(active) {
            activeAbilitiesClientCache.get(entity.getUUID()).add(this);
        }
        else {
            activeAbilitiesClientCache.get(entity.getUUID()).remove(this);
        }

    }

    public static HashSet<ToggleAbility> getActiveAbilitiesForEntity(LivingEntity entity) {
        if(!activeAbilities.containsKey(entity.getUUID())) {
            return new HashSet<>();
        }
        return new HashSet<>(activeAbilities.get(entity.getUUID()));
    }

    public static void setActiveAbilities(LivingEntity entity, HashSet<ToggleAbility> newAbilities) {
        // clear the old ones
        cleanUp((ServerLevel) entity.level(), entity);

        activeAbilities.put(entity.getUUID(), newAbilities);
    }
}
