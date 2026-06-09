package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.death.InternalUnderworldAbility;
import de.jakob.lotm.sefirah.SefirahHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client → Server: River vault soul transfer action.
 * fromVault=true  → move soul from vault to Internal Underworld
 * fromVault=false → move soul from Internal Underworld to vault
 */
public record RiverVaultActionPacket(String soulKey, boolean fromVault) implements CustomPacketPayload {

    public static final Type<RiverVaultActionPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "river_vault_action"));

    public static final StreamCodec<FriendlyByteBuf, RiverVaultActionPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(FriendlyByteBuf buf, RiverVaultActionPacket packet) {
            buf.writeUtf(packet.soulKey());
            buf.writeBoolean(packet.fromVault());
        }

        @Override
        public RiverVaultActionPacket decode(FriendlyByteBuf buf) {
            String soulKey = buf.readUtf();
            boolean fromVault = buf.readBoolean();
            return new RiverVaultActionPacket(soulKey, fromVault);
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RiverVaultActionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!context.flow().getReceptionSide().isServer()) return;
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!(player.level() instanceof ServerLevel level)) return;
            if (!"river_of_eternal_darkness".equals(SefirahHandler.getClaimedSefirot(player))) return;

            InternalUnderworldAbility.handleVaultAction(level, player, packet.soulKey(), packet.fromVault());
        });
    }
}
