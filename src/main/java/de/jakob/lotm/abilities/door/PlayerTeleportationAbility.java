package de.jakob.lotm.abilities.door;

import de.jakob.lotm.abilities.SelectableAbilityItem;
import de.jakob.lotm.abilities.core.AbilityUseEvent;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.AddPlayerToTeleportationListPacket;
import de.jakob.lotm.network.packets.toClient.ClearPlayerListInTeleportationPacket;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.*;

public class PlayerTeleportationAbility extends SelectableAbility {

    public record PlayerInfo(int id, String name, UUID uuid) implements Comparable<PlayerInfo> {
        @Override
        public int compareTo(PlayerInfo other) {
            return Integer.compare(this.id, other.id);
        }
    }

    public static HashSet<PlayerInfo> allPlayers = new HashSet<>();

    public PlayerTeleportationAbility(String id) {
        super(id, 1);

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

//    /**
//     * Gets the list of available players from ALL dimensions (excluding the entity itself)
//     */
//    private List<Player> getAvailablePlayers(Level level, LivingEntity excludeEntity) {
//        List<Player> players = new ArrayList<>();
//
//        if (level.isClientSide()) {
//            return players;
//        }
//
//        // Get all players from all dimensions
//        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
//            if (excludeEntity == null || !player.getUUID().equals(excludeEntity.getUUID())) {
//                players.add(player);
//            }
//        }
//
//        return players.stream().sorted(Comparator.comparing(Entity::getId)).toList();
//    }
//
//    /**
//     * Updates the selected player index to ensure it's valid
//     */
//    private void validateSelectedIndex(UUID entityUUID, int playerCount) {
//        if (playerCount == 0) {
//            selectedAbilities.remove(entityUUID);
//            return;
//        }
//
//        if (!selectedAbilities.containsKey(entityUUID)) {
//            selectedAbilities.put(entityUUID, 0);
//            return;
//        }
//
//        int currentIndex = selectedAbilities.get(entityUUID);
//        if (currentIndex >= playerCount) {
//            selectedAbilities.put(entityUUID, playerCount - 1);
//        } else if (currentIndex < 0) {
//            selectedAbilities.put(entityUUID, 0);
//        }
//    }
//
//    @Override
//    public void nextAbility(LivingEntity entity) {
//        List<Player> players = getAvailablePlayers(entity.level(), entity);
//
//        if (players.isEmpty())
//            return;
//
//        if (!selectedAbilities.containsKey(entity.getUUID())) {
//            selectedAbilities.put(entity.getUUID(), 0);
//            return;
//        }
//
//        int selectedIndex = selectedAbilities.get(entity.getUUID());
//        selectedIndex++;
//        if (selectedIndex >= players.size()) {
//            selectedIndex = 0;
//        }
//        selectedAbilities.put(entity.getUUID(), selectedIndex);
//    }
//
//    @Override
//    public void previousAbility(LivingEntity entity) {
//        List<Player> players = getAvailablePlayers(entity.level(), entity);
//
//        if (players.isEmpty())
//            return;
//
//        if (!selectedAbilities.containsKey(entity.getUUID())) {
//            selectedAbilities.put(entity.getUUID(), 0);
//            return;
//        }
//
//        int selectedIndex = selectedAbilities.get(entity.getUUID());
//        selectedIndex--;
//        if (selectedIndex < 0) {
//            selectedIndex = players.size() - 1;
//        }
//        selectedAbilities.put(entity.getUUID(), selectedIndex);
//    }
//
//    @Override
//    public String getSelectedAbility(LivingEntity entity) {
//        if (allPlayers.isEmpty())
//            return "No Players Available";
//
//        List<PlayerInfo> sortedPlayers = allPlayers.stream()
//                .sorted()
//                .toList();
//
//        validateSelectedIndex(entity.getUUID(), sortedPlayers.size());
//
//        int selectedIndex = selectedAbilities.getOrDefault(entity.getUUID(), 0);
//        if (selectedIndex >= 0 && selectedIndex < sortedPlayers.size()) {
//            return sortedPlayers.get(selectedIndex).name();
//        }
//
//        return "No Players Available";
//    }
//
//    @Override
//    protected void onAbilityUse(Level level, LivingEntity entity) {
//        if (level.isClientSide())
//            return;
//
//        List<Player> players = getAvailablePlayers(level, entity);
//
//        if (players.isEmpty())
//            return;
//
//        validateSelectedIndex(entity.getUUID(), players.size());
//
//        if (!selectedAbilities.containsKey(entity.getUUID())) {
//            selectedAbilities.put(entity.getUUID(), 0);
//        }
//
//        int selectedIndex = selectedAbilities.get(entity.getUUID());
//
//        if (selectedIndex >= 0 && selectedIndex < players.size()) {
//            Player targetPlayer = players.get(selectedIndex);
//            if(!(targetPlayer instanceof ServerPlayer)) {
//                return;
//            }
//            ServerLevel targetLevel = ((ServerPlayer) targetPlayer).serverLevel();
//
//            // Store origin position and level
//            double originX = entity.getX();
//            double originY = entity.getY();
//            double originZ = entity.getZ();
//            ServerLevel originLevel = (ServerLevel) entity.level();
//
//            // Play sound and effect at origin location
//            originLevel.playSound(null, originX, originY, originZ,
//                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
//            EffectManager.playEffect(EffectManager.Effect.WAYPOINT, originX, originY, originZ, originLevel);
//
//            // Teleport entity to the selected player (handles cross-dimension teleportation)
//            if (entity instanceof ServerPlayer serverPlayer) {
//                // For players, use changeDimension or teleportTo depending on dimension
//                if (originLevel != targetLevel) {
//                    serverPlayer.teleportTo(targetLevel,
//                            targetPlayer.getX(),
//                            targetPlayer.getY(),
//                            targetPlayer.getZ(),
//                            targetPlayer.getYRot(),
//                            targetPlayer.getXRot());
//                } else {
//                    serverPlayer.teleportTo(
//                            targetPlayer.getX(),
//                            targetPlayer.getY(),
//                            targetPlayer.getZ()
//                    );
//                    serverPlayer.setYRot(targetPlayer.getYRot());
//                    serverPlayer.setXRot(targetPlayer.getXRot());
//                }
//            }
//
//            // Play sound and effect at destination location
//            targetLevel.playSound(null, targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ(),
//                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
//            EffectManager.playEffect(EffectManager.Effect.WAYPOINT,
//                    targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ(), targetLevel);
//        }
//    }

//    @Override
//    public void onHold(Level level, LivingEntity entity) {
//        if(!level.isClientSide && entity instanceof ServerPlayer serverPlayer) {
//            PacketHandler.sendToPlayer(serverPlayer, new ClearPlayerListInTeleportationPacket());
//            for(Player player : getAvailablePlayers(level, null)) {
//                PacketHandler.sendToPlayer(serverPlayer, new AddPlayerToTeleportationListPacket(player.getId(), player.getDisplayName().getString(), player.getUUID()));
//            }
//        }
//    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        AbilityUtil.sendActionBar(entity, Component.translatable("lotm.temporarily_disabled").withColor(0xFFFF0000));
    }
}