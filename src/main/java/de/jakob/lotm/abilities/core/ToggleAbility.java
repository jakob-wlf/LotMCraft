package de.jakob.lotm.abilities.core;

import de.jakob.lotm.abilities.black_emperor.EntropySubAbility;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncToggleAbilityPacket;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public abstract class ToggleAbility extends Ability {

    private static final HashMap<UUID, HashSet<ToggleAbility>> activeAbilities = new HashMap<>();
    private static final HashMap<UUID, HashSet<ToggleAbility>> activeAbilitiesClientCache = new HashMap<>();


    public int tickRate = 5;

    protected ToggleAbility(String id, String... interactionFlags) {
        super(id, 0, interactionFlags);

        canBeUsedByNPC = false;
        doesNotIncreaseDigestion = true;
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
            if(entity instanceof ServerPlayer player)
                PacketHandler.sendToPlayer(player, new SyncToggleAbilityPacket(entity.getId(), getId(), SyncToggleAbilityPacket.Action.START.getValue()));
            return;
        }

        cancel((ServerLevel) level, entity);
    }

    protected void cancel(ServerLevel level, LivingEntity entity) {
        if(activeAbilities.containsKey(entity.getUUID())) {
            activeAbilities.get(entity.getUUID()).remove(this);
        }
        stop(level, entity);
        if(entity instanceof ServerPlayer player)
            PacketHandler.sendToPlayer(player, new SyncToggleAbilityPacket(entity.getId(), getId(), SyncToggleAbilityPacket.Action.STOP.getValue()));
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
            float cost = getSpiritualityCost();
            if (level instanceof ServerLevel sl) {
                var pdata = entity.getPersistentData();
                if (pdata.contains(EntropySubAbility.ENTROPY_DRAIN_SPIRIT_MULT_KEY)) {
                    if (pdata.getLong(EntropySubAbility.ENTROPY_DRAIN_SPIRIT_UNTIL_KEY) > sl.getGameTime()) {
                        cost *= pdata.getFloat(EntropySubAbility.ENTROPY_DRAIN_SPIRIT_MULT_KEY);
                    } else {
                        pdata.remove(EntropySubAbility.ENTROPY_DRAIN_SPIRIT_MULT_KEY);
                        pdata.remove(EntropySubAbility.ENTROPY_DRAIN_SPIRIT_UNTIL_KEY);
                    }
                }
            }
            if(BeyonderData.getSpirituality(entity) <= cost) {
                cancel((ServerLevel) level, entity);
                return;
            }

            BeyonderData.reduceSpirituality(entity, cost);
        }

        tick(level, entity);
        if(level instanceof ServerLevel serverLevel) NeoForge.EVENT_BUS.post(new AbilityUsedEvent(serverLevel, entity.position(), entity, this, interactionFlags, interactionRadius, interactionCacheTicks));
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

    public static Set<ToggleAbility> getActiveAbilitiesForEntity(LivingEntity entity) {
        if(!activeAbilities.containsKey(entity.getUUID())) {
            return Collections.emptySet();
        }
        return new HashSet<>(activeAbilities.get(entity.getUUID()));
    }

    public static void setActiveAbilities(LivingEntity entity, HashSet<ToggleAbility> newAbilities) {
        // clear the old ones
        cleanUp((ServerLevel) entity.level(), entity);

        activeAbilities.put(entity.getUUID(), newAbilities);
    }
}
