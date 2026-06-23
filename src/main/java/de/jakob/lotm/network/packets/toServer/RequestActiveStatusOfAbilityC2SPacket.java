package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncAbilityActiveStatusS2CPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RequestActiveStatusOfAbilityC2SPacket(String abilityId) implements CustomPacketPayload {
    public static final Type<RequestActiveStatusOfAbilityC2SPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "request_ability_active_status"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestActiveStatusOfAbilityC2SPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            RequestActiveStatusOfAbilityC2SPacket::abilityId,
            RequestActiveStatusOfAbilityC2SPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RequestActiveStatusOfAbilityC2SPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            Ability ability = LOTMCraft.abilityHandler.getById(packet.abilityId());
            PacketHandler.sendToPlayer(player, new SyncAbilityActiveStatusS2CPacket(ability.canUse(player)));
        });
    }
}