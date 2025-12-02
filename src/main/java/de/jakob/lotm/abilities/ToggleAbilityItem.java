package de.jakob.lotm.abilities;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.toClient.ToggleAbilityPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ToggleAbilityItem extends AbilityItem {

    // Server-side tracking
    private static final ConcurrentHashMap<UUID, ArrayList<ToggleAbilityItem>> activeAbilitiesServer = new ConcurrentHashMap<>();

    // Client-side tracking
    @OnlyIn(Dist.CLIENT)
    private static ConcurrentHashMap<UUID, ArrayList<ToggleAbilityItem>> activeAbilitiesClient;

    public ToggleAbilityItem(Properties properties) {
        super(properties, 0.01f);

        canBeCopied = false;
        canBeUsedByNPC = false;
    }

    public static void cleanupEntity(Level level, ServerPlayer player) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if(!activeAbilitiesServer.containsKey(player.getUUID())) {
            return;
        }

        List<ToggleAbilityItem> serverAbilities = activeAbilitiesServer.get(player.getUUID());

        if(serverAbilities == null) {
            return;
        }

        for (ToggleAbilityItem a : new ArrayList<>(serverAbilities)) {
            a.cancel(serverLevel, player);
        }

        activeAbilitiesServer.remove(player.getUUID());
    }

    @OnlyIn(Dist.CLIENT)
    public static void clearClientState(UUID playerId) {
        getClientAbilities().remove(playerId);
    }

    // Subclasses implement these - they can check level.isClientSide() if needed
    protected abstract void start(Level level, LivingEntity entity);
    protected abstract void tick(Level level, LivingEntity entity);
    protected abstract void stop(Level level, LivingEntity entity);

    public void cancel(ServerLevel level, LivingEntity entity) {
        List<ToggleAbilityItem> serverAbilities = activeAbilitiesServer.get(entity.getUUID());
        if (serverAbilities != null) {
            serverAbilities.remove(this);
            stop(level, entity);

            // Sync to client
            if (entity instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new ToggleAbilityPacket(
                        entity.getId(),
                        getAbilityId(),
                        false
                ));
            }
        }
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(level instanceof ServerLevel serverLevel && entity instanceof ServerPlayer serverPlayer) {
            // Server-side logic
            List<ToggleAbilityItem> activeAbilities = activeAbilitiesServer.computeIfAbsent(
                    serverPlayer.getUUID(),
                    k -> new ArrayList<>()
            );

            boolean isNowActive;
            if(!activeAbilities.contains(this)) {
                activeAbilities.add(this);
                start(serverLevel, entity);
                isNowActive = true;
            } else {
                activeAbilities.remove(this);
                stop(serverLevel, entity);
                isNowActive = false;
            }

            // Sync to client
            PacketDistributor.sendToPlayer(serverPlayer, new ToggleAbilityPacket(
                    serverPlayer.getId(),
                    getAbilityId(),
                    isNowActive
            ));
        }
        // Client-side activation is handled by packet response
    }

    /**
     * Called when receiving sync packet from server
     * This updates the client-side state
     */
    @OnlyIn(Dist.CLIENT)
    public void handleClientSync(Level level, LivingEntity entity, boolean active) {
        List<ToggleAbilityItem> activeAbilities = getClientAbilities().computeIfAbsent(
                entity.getUUID(),
                k -> new ArrayList<>()
        );

        boolean wasActive = activeAbilities.contains(this);

        if(active && !wasActive) {
            activeAbilities.add(this);
            start(level, entity);
        } else if(!active && wasActive) {
            activeAbilities.remove(this);
            stop(level, entity);
        }
    }

    @Override
    public @NotNull Component getName(ItemStack stack) {
        return Component.translatable(this.getDescriptionId(stack))
                .append(Component.literal(" ("))
                .append(Component.translatable("lotm.toggleable"))
                .append(Component.literal(")"));
    }

    public boolean isActive(LivingEntity entity) {
        Level level = entity.level();
        if(level instanceof ServerLevel) {
            List<ToggleAbilityItem> abilities = activeAbilitiesServer.get(entity.getUUID());
            return abilities != null && abilities.contains(this);
        } else {
            List<ToggleAbilityItem> abilities = getClientAbilities().get(entity.getUUID());
            return abilities != null && abilities.contains(this);
        }
    }

    /**
     * Gets the ability ID from the item's registry name
     * Used for packet synchronization
     */
    protected String getAbilityId() {
        return this.builtInRegistryHolder().key().location().toString();
    }

    @OnlyIn(Dist.CLIENT)
    private static ConcurrentHashMap<UUID, ArrayList<ToggleAbilityItem>> getClientAbilities() {
        if(activeAbilitiesClient == null) {
            activeAbilitiesClient = new ConcurrentHashMap<>();
        }
        return activeAbilitiesClient;
    }

    @EventBusSubscriber(modid = LOTMCraft.MOD_ID)
    public static class ToggleAbilityTickListener {

        @SubscribeEvent
        public static void onPlayerTick(PlayerTickEvent.Post event) {
            Player player = event.getEntity();
            Level level = player.level();

            if(level instanceof ServerLevel) {
                List<ToggleAbilityItem> abilities = activeAbilitiesServer.get(player.getUUID());
                if(abilities == null || abilities.isEmpty()) {
                    return;
                }

                // Create a copy to avoid concurrent modification
                List<ToggleAbilityItem> abilitiesCopy = new ArrayList<>(abilities);
                for(ToggleAbilityItem ability : abilitiesCopy) {
                    if(abilities.contains(ability)) { // Double-check it's still active
                        ability.tick(level, player);
                    }
                }
            } else if(level.isClientSide()) {
                List<ToggleAbilityItem> abilities = getClientAbilities().get(player.getUUID());
                if(abilities == null || abilities.isEmpty()) {
                    return;
                }

                // Create a copy to avoid concurrent modification
                List<ToggleAbilityItem> abilitiesCopy = new ArrayList<>(abilities);
                for(ToggleAbilityItem ability : abilitiesCopy) {
                    if(abilities.contains(ability)) { // Double-check it's still active
                        ability.tick(level, player);
                    }
                }
            }
        }
    }

    @EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
    public static class ClientAbilityEventHandler {

        /**
         * Called when the client player logs into a server/world
         * Clears all client-side ability state to prevent stale data
         */
        @SubscribeEvent
        public static void onClientPlayerLogin(ClientPlayerNetworkEvent.LoggingIn event) {
            ToggleAbilityItem.clearClientState(event.getPlayer().getUUID());
        }

        /**
         * Called when the client player logs out
         * Also clear state for safety
         */
        @SubscribeEvent
        public static void onClientPlayerLogout(ClientPlayerNetworkEvent.LoggingOut event) {
            if(event.getPlayer() == null) {
                return;
            }
            ToggleAbilityItem.clearClientState(event.getPlayer().getUUID());
        }
    }
}