package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.data.ClientData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Server → Client: sends the current list of available and unlocked sefirot cross-path abilities.
 * The client stores these in {@link ClientData} so the Introspect screen can include them in
 * the available-abilities panel.
 */
public record SyncSefirotAuthorityDataPacket(List<String> availableIds,
                                             List<String> unlockedIds)
        implements CustomPacketPayload {

    public static final Type<SyncSefirotAuthorityDataPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_sefirot_authority_data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncSefirotAuthorityDataPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8),
                    SyncSefirotAuthorityDataPacket::availableIds,
                    ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8),
                    SyncSefirotAuthorityDataPacket::unlockedIds,
                    SyncSefirotAuthorityDataPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncSefirotAuthorityDataPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientData.setSefirotAvailableAbilityIds(packet.availableIds());
            ClientData.setSefirotUnlockedAbilityIds(packet.unlockedIds());
        });
    }
}
