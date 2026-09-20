package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.sefirah.SefirotAuthorityManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client → Server: player clicked enable/disable on a cross-path ability in the Sefirot Authority GUI.
 */
public record ToggleSefirotAuthorityAbilityPacket(String abilityId, boolean unlock)
        implements CustomPacketPayload {

    public static final Type<ToggleSefirotAuthorityAbilityPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "toggle_sefirot_authority_ability"));

    public static final StreamCodec<ByteBuf, ToggleSefirotAuthorityAbilityPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, ToggleSefirotAuthorityAbilityPacket::abilityId,
                    ByteBufCodecs.BOOL,        ToggleSefirotAuthorityAbilityPacket::unlock,
                    ToggleSefirotAuthorityAbilityPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ToggleSefirotAuthorityAbilityPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isServer()) {
                ServerPlayer player = (ServerPlayer) context.player();
                if (packet.unlock()) {
                    SefirotAuthorityManager.unlockAbility(player, packet.abilityId());
                } else {
                    SefirotAuthorityManager.lockAbility(player, packet.abilityId());
                }
            }
        });
    }
}
