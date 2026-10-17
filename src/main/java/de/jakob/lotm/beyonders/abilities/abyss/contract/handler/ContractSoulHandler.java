package de.jakob.lotm.beyonders.abilities.abyss.contract.handler;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.abyss.contract.registry.SoulOwnershipRegistry;
import de.jakob.lotm.beyonders.abilities.core.PhysicalEnhancementsAbility;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ContractSoulHandler {
    private static final float SANITY_DAMAGE = 0.2f;
    private static final Map<UUID, UUID> activeDrains = new ConcurrentHashMap<>(); // soulOwner -> soulHolder
    private static final int DRAIN_INTERVAL_TICKS = 20;
    private static int tickCounter = 0;
    public static void startDrain(UUID soulOwner, UUID soulHolder) {
        activeDrains.put(soulOwner, soulHolder);
    }
    public static void consumeSoul(ServerPlayer holder, UUID ownerUUID) {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        ServerPlayer owner = server.getPlayerList().getPlayer(ownerUUID);
        if (owner == null) return;
        // Keep the soul drain active until the owner dies.
        activeDrains.put(ownerUUID, holder.getUUID());
        int holderSeq = BeyonderData.getSequence(holder);
        int ownerResistance = PhysicalEnhancementsAbility.getResistanceLevel(ownerUUID);
        if (ownerResistance > 0) {PhysicalEnhancementsAbility.addEnhancementBoost(holder, PhysicalEnhancementsAbility.EnhancementType.RESISTANCE, "contract_soul_" + ownerUUID, Math.max(1, ownerResistance / 2), 20_000L);}
        owner.getData(ModAttachments.SANITY_COMPONENT).decreaseSanityWithSequenceDifference(SANITY_DAMAGE, owner, holderSeq, BeyonderData.getSequence(owner));
    }
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (++tickCounter < DRAIN_INTERVAL_TICKS) return;
        tickCounter = 0;
        if (activeDrains.isEmpty()) return;
        var server = event.getServer();
        for (Map.Entry<UUID, UUID> entry : activeDrains.entrySet()) {
            ServerPlayer owner = server.getPlayerList().getPlayer(entry.getKey());
            if (owner == null) continue;
            if (owner.isDeadOrDying()) {
                activeDrains.remove(entry.getKey());
                SoulOwnershipRegistry.release(entry.getKey());
                continue;
            }
            ServerPlayer holder = server.getPlayerList().getPlayer(entry.getValue());
            if (holder == null) {
                activeDrains.remove(entry.getKey());
                continue;
            }
            int holderSeq = BeyonderData.getSequence(holder);
            owner.getData(ModAttachments.SANITY_COMPONENT).decreaseSanityWithSequenceDifference(SANITY_DAMAGE, owner, holderSeq, BeyonderData.getSequence(owner));
            ContractDeathHandler.markContractDeath(owner.getUUID());
        }
    }
    public static void cancelDrain(UUID soulOwnerUUID) {if (soulOwnerUUID != null) activeDrains.remove(soulOwnerUUID);}
}