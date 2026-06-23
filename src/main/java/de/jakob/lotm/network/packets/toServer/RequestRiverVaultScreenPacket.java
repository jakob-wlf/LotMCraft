package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.death.InternalUnderworldAbility;
import de.jakob.lotm.sefirah.SefirahHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client → Server: River owner opens the River Soul Vault GUI.
 */
public record RequestRiverVaultScreenPacket() implements CustomPacketPayload {

    public static final Type<RequestRiverVaultScreenPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "request_river_vault_screen"));

    public static final StreamCodec<ByteBuf, RequestRiverVaultScreenPacket> STREAM_CODEC =
            StreamCodec.unit(new RequestRiverVaultScreenPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(RequestRiverVaultScreenPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!context.flow().getReceptionSide().isServer()) return;
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!(player.level() instanceof ServerLevel level)) return;
            if (!"river_of_eternal_darkness".equals(SefirahHandler.getClaimedSefirot(player))) return;

            InternalUnderworldAbility.openRiverVaultGui(level, player);
        });
    }
}
