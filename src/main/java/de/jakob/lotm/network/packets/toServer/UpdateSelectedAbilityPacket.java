package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.AbilityWheelComponent;
import de.jakob.lotm.attachments.ModAttachments;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UpdateSelectedAbilityPacket(int selectedAbility) implements CustomPacketPayload {

    public static final Type<UpdateSelectedAbilityPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "update_selected_ability"));

    public static final StreamCodec<ByteBuf, UpdateSelectedAbilityPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            UpdateSelectedAbilityPacket::selectedAbility,
            UpdateSelectedAbilityPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UpdateSelectedAbilityPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                AbilityWheelComponent component = serverPlayer.getData(ModAttachments.ABILITY_WHEEL_COMPONENT);
                component.setSelectedAbility(packet.selectedAbility());
            }
        });
    }
}