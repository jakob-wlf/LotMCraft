package de.jakob.lotm.network.packets.toClient;


import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.door.PlayerTeleportationAbility;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClearPlayerListInTeleportationPacket() implements CustomPacketPayload {
    public static final Type<ClearPlayerListInTeleportationPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "clear_player_list_in_teleportation_ability"));

    public static final StreamCodec<FriendlyByteBuf, ClearPlayerListInTeleportationPacket> STREAM_CODEC =
            StreamCodec.unit(new ClearPlayerListInTeleportationPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ClearPlayerListInTeleportationPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            PlayerTeleportationAbility.allPlayers.clear();
        });
    }
}