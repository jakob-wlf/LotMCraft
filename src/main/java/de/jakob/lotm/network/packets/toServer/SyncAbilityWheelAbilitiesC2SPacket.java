package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.helper.AbilityWheelHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;

public record SyncAbilityWheelAbilitiesC2SPacket(ArrayList<String> abilities) implements CustomPacketPayload {
    public static final Type<SyncAbilityWheelAbilitiesC2SPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_ability_wheel_abilities"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncAbilityWheelAbilitiesC2SPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8),
            SyncAbilityWheelAbilitiesC2SPacket::abilities,
            SyncAbilityWheelAbilitiesC2SPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncAbilityWheelAbilitiesC2SPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            AbilityWheelHelper.setAbilities(player, packet.abilities);
        });
    }
}