package de.jakob.lotm.abilities;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.ClientBeyonderCache;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ToggleAbilityItem extends Item {
    private static final Map<UUID, Map<ToggleAbilityItem, ItemStack>> activeAbilities = new ConcurrentHashMap<>();

    private final Map<UUID, Boolean> activeStates = new ConcurrentHashMap<>();

    public ToggleAbilityItem(Properties properties) {
        super(properties);
    }

    protected abstract float getSpiritualityCost();
    protected boolean canBeUsedByNPC = true;
    protected boolean canBeCopied = true;

    public abstract Map<String, Integer> getRequirements();

    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        if (!canUse(player)) return InteractionResultHolder.fail(player.getItemInHand(hand));

        UUID playerUUID = player.getUUID();
        boolean isActive = activeStates.getOrDefault(playerUUID, false);

        if(isActive)
            stop(level, player);
        else
            start(level, player);

        // Only update state on server side to prevent desync
        if (!level.isClientSide) {
            // Update active state
            activeStates.put(playerUUID, !isActive);

            // Update global tracking
            if (!isActive) {
                // Starting ability
                activeAbilities.computeIfAbsent(playerUUID, k -> new ConcurrentHashMap<>())
                        .put(this, player.getItemInHand(hand));
            } else {
                // Stopping ability
                Map<ToggleAbilityItem, ItemStack> playerAbilities = activeAbilities.get(playerUUID);
                if (playerAbilities != null) {
                    playerAbilities.remove(this);
                    if (playerAbilities.isEmpty()) {
                        activeAbilities.remove(playerUUID);
                    }
                }
            }
        }

        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    public boolean canUse(Player player) {
        return canUse(player, false);
    }

    public boolean canUse(Player player, boolean ignoreCreative) {
        return AbilityHandler.canUse(player, ignoreCreative, getRequirements(), getSpiritualityCost());
    }

    public boolean isActive(LivingEntity entity) {
        return activeStates.getOrDefault(entity.getUUID(), false);
    }

    public static void cleanupEntity(UUID entity) {
        activeAbilities.remove(entity);
    }

    protected double multiplier(LivingEntity entity) {
        return BeyonderData.getMultiplier(entity);
    }

    protected abstract void start(Level level, LivingEntity entity);
    protected abstract void tick(Level level, LivingEntity entity);
    protected abstract void stop(Level level, LivingEntity entity);

    @EventBusSubscriber(modid = LOTMCraft.MOD_ID)
    public static class ToggleAbilityTickHandler {

        @SubscribeEvent
        public static void onPlayerTick(PlayerTickEvent.Post event) {
            Player player = event.getEntity();

            // Only run on server side to prevent double processing and desync
            if (player.level().isClientSide) {
                UUID playerUUID = player.getUUID();

                Map<ToggleAbilityItem, ItemStack> playerAbilities = activeAbilities.get(playerUUID);
                if (playerAbilities == null || playerAbilities.isEmpty()) return;

                // Create a copy to avoid concurrent modification
                Map<ToggleAbilityItem, ItemStack> abilitiesToTick = new HashMap<>(playerAbilities);

                for (Map.Entry<ToggleAbilityItem, ItemStack> entry : abilitiesToTick.entrySet()) {
                    ToggleAbilityItem ability = entry.getKey();

                    // Verify player still has the item and it's still active
                    if (ability.isActive(player) && ability.getSpiritualityCost() <= BeyonderData.getSpirituality(player)) {
                        ability.tick(player.level(), player);
                    }
                }
                return;
            }

            UUID playerUUID = player.getUUID();

            Map<ToggleAbilityItem, ItemStack> playerAbilities = activeAbilities.get(playerUUID);
            if (playerAbilities == null || playerAbilities.isEmpty()) return;

            // Create a copy to avoid concurrent modification
            Map<ToggleAbilityItem, ItemStack> abilitiesToTick = new HashMap<>(playerAbilities);

            for (Map.Entry<ToggleAbilityItem, ItemStack> entry : abilitiesToTick.entrySet()) {
                ToggleAbilityItem ability = entry.getKey();

                // Verify player still has the item and it's still active
                if (ability.isActive(player) && ability.getSpiritualityCost() <= BeyonderData.getSpirituality(player)) {
                    ability.tick(player.level(), player);
                    if(!player.isCreative())
                        BeyonderData.reduceSpirituality(player, ability.getSpiritualityCost());
                } else {
                    // Clean up inactive abilities
                    ability.activeStates.remove(playerUUID);
                    playerAbilities.remove(ability);
                }
            }

            // Clean up empty player entries
            if (playerAbilities.isEmpty()) {
                activeAbilities.remove(playerUUID);
            }
        }
    }
}