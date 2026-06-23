package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.helper.AbilityWheelHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UpdateSelectedAbilityC2SPacket(int selectedAbility) implements CustomPacketPayload {

    public static final Type<UpdateSelectedAbilityC2SPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "update_selected_ability"));

    public static final StreamCodec<ByteBuf, UpdateSelectedAbilityC2SPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            UpdateSelectedAbilityC2SPacket::selectedAbility,
            UpdateSelectedAbilityC2SPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UpdateSelectedAbilityC2SPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                AbilityWheelHelper.setSelectedAbility(serverPlayer, packet.selectedAbility());
            }
        });
    }
}