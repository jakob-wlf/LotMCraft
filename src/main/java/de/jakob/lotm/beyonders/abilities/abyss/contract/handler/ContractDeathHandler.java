package de.jakob.lotm.beyonders.abilities.abyss.contract.handler;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.abyss.contract.ContractData;
import de.jakob.lotm.beyonders.abilities.abyss.contract.ContractItem;
import de.jakob.lotm.beyonders.abilities.abyss.contract.registry.SoulOwnershipRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ContractDeathHandler {
    private static final Set<UUID> CONTRACT_DEATHS = ConcurrentHashMap.newKeySet();
    // Marks the next death of this player as caused by a contract.
    public static void markContractDeath(UUID playerUUID) {
        CONTRACT_DEATHS.add(playerUUID);
    }
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer dead)) return;
        // A death caused by a contract is consumed here and never triggers further contract handling or cleanup.
        if (CONTRACT_DEATHS.remove(dead.getUUID())) return;
        MinecraftServer server = dead.server;
        UUID deadUUID = dead.getUUID();
        List<UUID> toKill = new ArrayList<>();
        for (ServerPlayer holder : server.getPlayerList().getPlayers()) {
            Container inventory = holder.getInventory();
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                ItemStack stack = inventory.getItem(i);
                if (!(stack.getItem() instanceof ContractItem)) continue;
                ContractData data = ContractItem.readData(stack);
                if (!data.isActive() || !data.hasBothParties()) continue;
                boolean deadIsA = deadUUID.equals(data.getPartyA());
                boolean deadIsB = deadUUID.equals(data.getPartyB());
                if (!deadIsA && !deadIsB) continue;
                //LIFE / DEATH CONTRACT
                if (data.isLifeDeathContract()) {
                    if (data.isComplete() || data.lifeDeathExpired() || data.lifeDeathStartGameTime() < 0 || !data.areAllTermsFulfilled()) {continue;}
                    UUID otherUUID = deadIsA ? data.getPartyB() : data.getPartyA();
                    // End the link first: item contract removed,
                    // soul contract kept and marked complete.
                    LifeDeathHandler.endLifeLink(server, stack, data);
                    if (data.lifeDeathExpired()) {toKill.add(otherUUID);}
                    // Souls stay bound until the owner sacrifices them.
                    continue;
                }
                //ALL OTHER CONTRACTS
                cleanupSouls(data);
                inventory.setItem(i, ItemStack.EMPTY);
            }
        }
        for (UUID uuid : toKill) {
            ServerPlayer other = server.getPlayerList().getPlayer(uuid);
            if (other == null || !other.isAlive()) continue;
            markContractDeath(uuid);
            other.hurt(other.damageSources().magic(), Float.MAX_VALUE);
            // If they survived (totem, resistance, ability), clear the mark so their next real death isn't wrongly ignored.
            if (other.isAlive()) {
                CONTRACT_DEATHS.remove(uuid);
            }
        }
    }
    private static void cleanupSouls(ContractData data) {
        ContractSoulHandler.cancelDrain(data.getPartyA());
        ContractSoulHandler.cancelDrain(data.getPartyB());
        SoulOwnershipRegistry.release(data.getPartyA());
        SoulOwnershipRegistry.release(data.getPartyB());
    }
}