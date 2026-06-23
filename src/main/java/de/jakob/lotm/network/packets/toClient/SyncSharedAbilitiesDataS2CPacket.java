package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.data.ClientData;
import de.jakob.lotm.util.helper.ClientTeamData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sent from server to a player's client to populate the Shared Abilities tab in IntrospectScreen.
 * The contributions map (memberUUID -> contributed ability IDs) is encoded as parallel lists
 * of keys and lists-of-values to keep StreamCodec simple.
 */
public record SyncSharedAbilitiesDataS2CPacket(
        String leaderUUID,
        List<String> teamMemberUUIDs,
        List<String> teamMemberNames,
        Map<String, List<String>> contributions,
        int maxTeamSize,
        int slotsPerMember
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncSharedAbilitiesDataS2CPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_shared_abilities_data"));

    // StreamCodec for List<String>
    private static final StreamCodec<ByteBuf, List<String>> STRING_LIST_CODEC =
            ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8);

    // StreamCodec for List<List<String>>
    private static final StreamCodec<ByteBuf, List<List<String>>> STRING_LIST_LIST_CODEC =
            ByteBufCodecs.collection(ArrayList::new, STRING_LIST_CODEC);

    // StreamCodec for Map<String, List<String>> encoded as (keyList, valueListList)
    // We snapshot entries into a list first to guarantee key/value order alignment
    private static final StreamCodec<ByteBuf, Map<String, List<String>>> CONTRIBUTIONS_CODEC =
            StreamCodec.composite(
                    STRING_LIST_CODEC,
                    map -> new ArrayList<>(map.keySet()),
                    STRING_LIST_LIST_CODEC,
                    map -> {
                        List<List<String>> values = new ArrayList<>();
                        for (String key : new ArrayList<>(map.keySet())) {
                            values.add(new ArrayList<>(map.getOrDefault(key, List.of())));
                        }
                        return values;
                    },
                    (keys, values) -> {
                        Map<String, List<String>> result = new HashMap<>();
                        for (int i = 0; i < keys.size(); i++) {
                            result.put(keys.get(i), i < values.size() ? values.get(i) : new ArrayList<>());
                        }
                        return result;
                    }
            );

    public static final StreamCodec<ByteBuf, SyncSharedAbilitiesDataS2CPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            SyncSharedAbilitiesDataS2CPacket::leaderUUID,
            STRING_LIST_CODEC,
            SyncSharedAbilitiesDataS2CPacket::teamMemberUUIDs,
            STRING_LIST_CODEC,
            SyncSharedAbilitiesDataS2CPacket::teamMemberNames,
            CONTRIBUTIONS_CODEC,
            SyncSharedAbilitiesDataS2CPacket::contributions,
            ByteBufCodecs.INT,
            SyncSharedAbilitiesDataS2CPacket::maxTeamSize,
            ByteBufCodecs.INT,
            SyncSharedAbilitiesDataS2CPacket::slotsPerMember,
            SyncSharedAbilitiesDataS2CPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncSharedAbilitiesDataS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientTeamData.update(packet);
            if (packet.leaderUUID().isEmpty()) {
                // No team — clear the shared wheel entirely
                ClientData.setSharedWheelAbilities(new java.util.ArrayList<>());
                ClientData.setSelectedSharedAbility(0);
            } else {
                // Team updated — prune wheel entries that are no longer in the pool
                java.util.Set<String> pooled = new java.util.HashSet<>();
                for (java.util.List<String> contributions : packet.contributions().values()) {
                    pooled.addAll(contributions);
                }
                java.util.List<String> pruned = new java.util.ArrayList<>(ClientData.getSharedWheelAbilities());
                pruned.removeIf(id -> !pooled.contains(id));
                ClientData.setSharedWheelAbilities(pruned);
                // Clamp selected index so the HUD doesn't go out of bounds
                int sel = ClientData.getSelectedSharedAbility();
                if (sel >= pruned.size()) {
                    ClientData.setSelectedSharedAbility(Math.max(0, pruned.size() - 1));
                }
            }
        });
    }
}
