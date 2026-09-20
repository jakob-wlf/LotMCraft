package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.sefirah.RiverBlessingManager;
import de.jakob.lotm.beyonders.sefirah.SefirahHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client → Server: River owner summons or dismisses their blessed audience.
 *
 * Actions:
 *   SUMMON  (0) — teleport all online blessed players to the river dimension audience spot
 *   DISMISS (1) — return all current audience members to where they came from
 */
public record RiverAudienceActionPacket(int action) implements CustomPacketPayload {

    public static final int SUMMON  = 0;
    public static final int DISMISS = 1;

    public static final Type<RiverAudienceActionPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "river_audience_action"));

    public static final StreamCodec<ByteBuf, RiverAudienceActionPacket> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(RiverAudienceActionPacket::new, RiverAudienceActionPacket::action);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(RiverAudienceActionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!context.flow().getReceptionSide().isServer()) return;
            if (!(context.player() instanceof ServerPlayer owner)) return;
            // Security: only the river owner may call this
            if (!"river_of_eternal_darkness".equals(SefirahHandler.getClaimedSefirot(owner))) return;

            switch (packet.action()) {
                case SUMMON  -> RiverBlessingManager.summonBlessedToAudience(owner, owner.server);
                case DISMISS -> RiverBlessingManager.dismissAudience(owner.server);
            }
        });
    }
}
