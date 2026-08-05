package de.jakob.lotm.beyonders.abilities.wheel_of_fortune;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.OpenConnectionManagerPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ConnectionAbility extends SelectableAbility {
    private static final int maximumConnections = 3;
    private static final String connectionCreatorKey = "lotm_connection_creator";
    private static final String connectionIdKey = "lotm_connection_id";
    private static final String connectionsKey = "lotm_connections";
    private static final String registryIdKey = "id";
    private static final String registryItemKey = "item";
    private static final String registryNameKey = "name";
    private static final String nextAbilityKey = "lotm_use_next_ability_on_connections";

    public ConnectionAbility(String id) {
        super(id, 1);
        canBeCopied = false;
        canBeReplicated = false;
        canBeUsedInArtifact = false;
        canBeShared = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return Map.of("wheel_of_fortune", 4);
    }

    @Override
    protected float getSpiritualityCost() {
        return 50;
    }

    @Override
    public ResourceLocation getTextureLocation() {
        return ResourceLocation.fromNamespaceAndPath(
            "lotmcraft", "textures/abilities/spirtual_connection.png");
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.connection.create_item",
            "ability.lotmcraft.connection.use_next",
            "ability.lotmcraft.connection.manage"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        if (level.isClientSide() || !(entity instanceof ServerPlayer player)) return;

        if (selectedAbility == 0) {
            createConnectedItem(player);
            return;
        }

        if (selectedAbility == 2) {
            openConnectionManager(player);
            return;
        }

        player.getPersistentData().putBoolean(nextAbilityKey, true);
        int targets = findConnectedTargets(player).size();
        AbilityUtil.sendActionBar(player, Component.literal(
                "Connection armed for the next targeted ability (" + targets + " holder(s))."));
    }

    private static void createConnectedItem(ServerPlayer player) {
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.isEmpty()) {
            AbilityUtil.sendActionBar(player, Component.literal("Hold an item in your main hand."));
            return;
        }
        if (heldItem.getCount() != 1) {
            AbilityUtil.sendActionBar(player, Component.literal(
                "Connections can only be created from a single, unstacked item."));
            return;
        }

        ListTag connections = discoverConnections(player);
        if (connections.size() >= maximumConnections) {
            AbilityUtil.sendActionBar(player, Component.literal(
                "You already have the maximum of 3 connected items."));
            return;
        }

        CompoundTag tag = heldItem.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.hasUUID(connectionIdKey)) {
            UUID existingId = getConnectionId(heldItem, player.getUUID());
            if (existingId != null && hasRegisteredConnection(player, existingId)) {
                AbilityUtil.sendActionBar(player, Component.literal("That item is already connected."));
                return;
            }
            tag.remove(connectionCreatorKey);
            tag.remove(connectionIdKey);
        }
        UUID connectionId = UUID.randomUUID();
        tag.putUUID(connectionCreatorKey, player.getUUID());
        tag.putUUID(connectionIdKey, connectionId);
        heldItem.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        heldItem.set(DataComponents.CUSTOM_NAME,
            heldItem.getHoverName().copy().append(Component.literal(".")).withStyle(style -> style.withItalic(false)));

        CompoundTag entry = new CompoundTag();
        entry.putUUID(registryIdKey, connectionId);
        entry.putString(registryItemKey, BuiltInRegistries.ITEM.getKey(heldItem.getItem()).toString());
        entry.putString(registryNameKey, heldItem.getHoverName().getString());
        connections.add(entry);
        player.getPersistentData().put(connectionsKey, connections);
        AbilityUtil.sendActionBar(player, Component.literal(
            "Connected item created (" + connections.size() + "/" + maximumConnections + ")."));
    }

    public static List<ServerPlayer> consumeConnectedTargets(LivingEntity caster, Ability ability) {
        if (!(caster instanceof ServerPlayer player)
                || ability instanceof ConnectionAbility
                || ability instanceof ToggleAbility
                || !player.getPersistentData().getBoolean(nextAbilityKey)) {
            return List.of();
        }

        player.getPersistentData().remove(nextAbilityKey);
        List<ServerPlayer> targets = findConnectedTargets(player);
        if (targets.isEmpty()) {
            AbilityUtil.sendActionBar(player, Component.literal("No online player carries one of your connections."));
        }
        return targets;
    }

    private static List<ServerPlayer> findConnectedTargets(ServerPlayer creator) {
        discoverConnections(creator);
        return creator.server.getPlayerList().getPlayers().stream()
                .filter(player -> player != creator)
                .filter(player -> carriesConnection(player, creator))
                .toList();
    }

    private static boolean carriesConnection(ServerPlayer player, ServerPlayer creator) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            UUID connectionId = getConnectionId(stack, creator.getUUID());
            if (connectionId != null && hasRegisteredConnection(creator, connectionId)) return true;
        }
        return false;
    }

    public static void openConnectionManager(ServerPlayer creator) {
        List<OpenConnectionManagerPacket.ConnectionInfo> infos = new java.util.ArrayList<>();
        ListTag connections = discoverConnections(creator);
        for (int index = 0; index < connections.size(); index++) {
            CompoundTag entry = connections.getCompound(index);
            if (!entry.hasUUID(registryIdKey)) continue;
            UUID connectionId = entry.getUUID(registryIdKey);
            ServerPlayer holder = findHolder(creator, connectionId);
            String holderName = holder == null ? "Not held by an online player" : holder.getName().getString();
            String pathway = holder == null || !BeyonderData.isBeyonder(holder)
                ? "None" : BeyonderData.getPathway(holder);
            int sequence = holder == null || !BeyonderData.isBeyonder(holder)
                ? -1 : BeyonderData.getSequence(holder);
            infos.add(new OpenConnectionManagerPacket.ConnectionInfo(
                connectionId.toString(), entry.getString(registryItemKey), entry.getString(registryNameKey),
                holderName, pathway, sequence));
        }
        PacketHandler.sendToPlayer(creator, new OpenConnectionManagerPacket(infos));
    }

    public static boolean clearConnection(ServerPlayer creator, UUID connectionId) {
        ListTag connections = getConnections(creator);
        boolean removed = connections.removeIf(tag -> tag instanceof CompoundTag entry
            && entry.hasUUID(registryIdKey) && connectionId.equals(entry.getUUID(registryIdKey)));
        if (!removed) return false;
        creator.getPersistentData().put(connectionsKey, connections);

        for (ServerPlayer player : creator.server.getPlayerList().getPlayers()) {
            for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                ItemStack stack = player.getInventory().getItem(slot);
                if (connectionId.equals(getConnectionId(stack, creator.getUUID()))) clearConnectionTags(stack);
            }
        }
        return true;
    }

    public static boolean cutConnectedItem(ServerPlayer cutter, ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return false;
        CompoundTag tag = customData.copyTag();
        if (!tag.hasUUID(connectionCreatorKey) || !tag.hasUUID(connectionIdKey)) return false;

        UUID creatorId = tag.getUUID(connectionCreatorKey);
        UUID connectionId = tag.getUUID(connectionIdKey);
        ServerPlayer creator = cutter.server.getPlayerList().getPlayer(creatorId);
        if (creator != null) clearConnection(creator, connectionId);
        clearConnectionTags(stack);
        return true;
    }

    private static ListTag getConnections(ServerPlayer creator) {
        return creator.getPersistentData().getList(connectionsKey, Tag.TAG_COMPOUND).copy();
    }

    private static ListTag discoverConnections(ServerPlayer creator) {
        ListTag connections = getConnections(creator);
        if (connections.size() >= maximumConnections) return connections;

        for (ServerPlayer player : creator.server.getPlayerList().getPlayers()) {
            for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                ItemStack stack = player.getInventory().getItem(slot);
                UUID connectionId = getConnectionId(stack, creator.getUUID());
                if (connectionId == null || containsConnection(connections, connectionId)) continue;
                CompoundTag entry = new CompoundTag();
                entry.putUUID(registryIdKey, connectionId);
                entry.putString(registryItemKey, BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
                entry.putString(registryNameKey, stack.getHoverName().getString());
                connections.add(entry);
                if (connections.size() >= maximumConnections) break;
            }
            if (connections.size() >= maximumConnections) break;
        }
        creator.getPersistentData().put(connectionsKey, connections);
        return connections;
    }

    private static boolean containsConnection(ListTag connections, UUID connectionId) {
        return connections.stream().anyMatch(tag -> tag instanceof CompoundTag entry
            && entry.hasUUID(registryIdKey) && connectionId.equals(entry.getUUID(registryIdKey)));
    }

    private static boolean hasRegisteredConnection(ServerPlayer creator, UUID connectionId) {
        return containsConnection(getConnections(creator), connectionId);
    }

    private static ServerPlayer findHolder(ServerPlayer creator, UUID connectionId) {
        for (ServerPlayer player : creator.server.getPlayerList().getPlayers()) {
            for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                if (connectionId.equals(getConnectionId(player.getInventory().getItem(slot), creator.getUUID()))) {
                    return player;
                }
            }
        }
        return null;
    }

    private static UUID getConnectionId(ItemStack stack, UUID creatorUUID) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return null;
        CompoundTag tag = customData.copyTag();
        return tag.hasUUID(connectionCreatorKey) && creatorUUID.equals(tag.getUUID(connectionCreatorKey))
            && tag.hasUUID(connectionIdKey) ? tag.getUUID(connectionIdKey) : null;
    }

    private static void clearConnectionTags(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return;
        CompoundTag tag = customData.copyTag();
        tag.remove(connectionCreatorKey);
        tag.remove(connectionIdKey);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}