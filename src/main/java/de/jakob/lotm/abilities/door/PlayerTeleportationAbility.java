package de.jakob.lotm.abilities.door;

import de.jakob.lotm.abilities.SelectableAbilityItem;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.EffectManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.*;

public class PlayerTeleportationAbility extends SelectableAbilityItem {

    public PlayerTeleportationAbility(Properties properties, float cooldown) {
        super(properties, cooldown);

        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 1));
    }

    @Override
    protected float getSpiritualityCost() {
        return 1900.0f;
    }

    @Override
    protected String[] getAbilityNames() {
        // Not used for this implementation
        return new String[0];
    }

    /**
     * Gets the list of available players from ALL dimensions (excluding the entity itself)
     */
    private List<ServerPlayer> getAvailablePlayers(Level level, LivingEntity excludeEntity) {
        List<ServerPlayer> players = new ArrayList<>();

        if (level.isClientSide())
            return players;

        // Get all players from all dimensions
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (!player.getUUID().equals(excludeEntity.getUUID())) {
                players.add(player);
            }
        }

        return players;
    }

    /**
     * Updates the selected player index to ensure it's valid
     */
    private void validateSelectedIndex(UUID entityUUID, int playerCount) {
        if (playerCount == 0) {
            selectedAbilities.remove(entityUUID);
            return;
        }

        if (!selectedAbilities.containsKey(entityUUID)) {
            selectedAbilities.put(entityUUID, 0);
            return;
        }

        int currentIndex = selectedAbilities.get(entityUUID);
        if (currentIndex >= playerCount) {
            selectedAbilities.put(entityUUID, playerCount - 1);
        } else if (currentIndex < 0) {
            selectedAbilities.put(entityUUID, 0);
        }
    }

    @Override
    public void nextAbility(LivingEntity entity) {
        List<ServerPlayer> players = getAvailablePlayers(entity.level(), entity);

        if (players.isEmpty())
            return;

        if (!selectedAbilities.containsKey(entity.getUUID())) {
            selectedAbilities.put(entity.getUUID(), 0);
            return;
        }

        int selectedIndex = selectedAbilities.get(entity.getUUID());
        selectedIndex++;
        if (selectedIndex >= players.size()) {
            selectedIndex = 0;
        }
        selectedAbilities.put(entity.getUUID(), selectedIndex);
    }

    @Override
    public void previousAbility(LivingEntity entity) {
        List<ServerPlayer> players = getAvailablePlayers(entity.level(), entity);

        if (players.isEmpty())
            return;

        if (!selectedAbilities.containsKey(entity.getUUID())) {
            selectedAbilities.put(entity.getUUID(), 0);
            return;
        }

        int selectedIndex = selectedAbilities.get(entity.getUUID());
        selectedIndex--;
        if (selectedIndex < 0) {
            selectedIndex = players.size() - 1;
        }
        selectedAbilities.put(entity.getUUID(), selectedIndex);
    }

    @Override
    public String getSelectedAbility(LivingEntity entity) {
        List<ServerPlayer> players = getAvailablePlayers(entity.level(), entity);

        if (players.isEmpty())
            return "No Players Available";

        validateSelectedIndex(entity.getUUID(), players.size());

        if (!selectedAbilities.containsKey(entity.getUUID())) {
            return players.get(0).getName().getString();
        }

        int selectedIndex = selectedAbilities.get(entity.getUUID());
        if (selectedIndex >= 0 && selectedIndex < players.size()) {
            return players.get(selectedIndex).getName().getString();
        }

        return "No Players Available";
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if (level.isClientSide())
            return;

        List<ServerPlayer> players = getAvailablePlayers(level, entity);

        if (players.isEmpty())
            return;

        validateSelectedIndex(entity.getUUID(), players.size());

        if (!selectedAbilities.containsKey(entity.getUUID())) {
            selectedAbilities.put(entity.getUUID(), 0);
        }

        int selectedIndex = selectedAbilities.get(entity.getUUID());

        if (selectedIndex >= 0 && selectedIndex < players.size()) {
            ServerPlayer targetPlayer = players.get(selectedIndex);
            ServerLevel targetLevel = targetPlayer.serverLevel();

            // Store origin position and level
            double originX = entity.getX();
            double originY = entity.getY();
            double originZ = entity.getZ();
            ServerLevel originLevel = (ServerLevel) entity.level();

            // Play sound and effect at origin location
            originLevel.playSound(null, originX, originY, originZ,
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
            EffectManager.playEffect(EffectManager.Effect.WAYPOINT, originX, originY, originZ, originLevel);

            // Teleport entity to the selected player (handles cross-dimension teleportation)
            if (entity instanceof ServerPlayer serverPlayer) {
                // For players, use changeDimension or teleportTo depending on dimension
                if (originLevel != targetLevel) {
                    serverPlayer.teleportTo(targetLevel,
                            targetPlayer.getX(),
                            targetPlayer.getY(),
                            targetPlayer.getZ(),
                            targetPlayer.getYRot(),
                            targetPlayer.getXRot());
                } else {
                    serverPlayer.teleportTo(
                            targetPlayer.getX(),
                            targetPlayer.getY(),
                            targetPlayer.getZ()
                    );
                    serverPlayer.setYRot(targetPlayer.getYRot());
                    serverPlayer.setXRot(targetPlayer.getXRot());
                }
            }

            // Play sound and effect at destination location
            targetLevel.playSound(null, targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
            EffectManager.playEffect(EffectManager.Effect.WAYPOINT,
                    targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ(), targetLevel);
        }
    }

    @Override
    protected void useAbility(Level level, LivingEntity entity, int abilityIndex) {
        // Not used for this implementation
    }
}