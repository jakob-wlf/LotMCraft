package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.AbilityWheelComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SanityComponent;
import de.jakob.lotm.gui.custom.introspect.IntrospectMenuProvider;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncAbilityWheelDataToIntrospectPacket;
import de.jakob.lotm.network.packets.toClient.SyncIntrospectMenuPacket;
import de.jakob.lotm.network.packets.toClient.SyncKillCountPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityWheelHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenIntrospectMenuPacket(int sequence, String pathway) implements CustomPacketPayload {
    public static final Type<OpenIntrospectMenuPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "open_introspect"));

    public static final StreamCodec<FriendlyByteBuf, OpenIntrospectMenuPacket> STREAM_CODEC =
            StreamCodec.composite(
                    StreamCodec.of(FriendlyByteBuf::writeInt, FriendlyByteBuf::readInt),
                    OpenIntrospectMenuPacket::sequence,
                    StreamCodec.of(FriendlyByteBuf::writeUtf, FriendlyByteBuf::readUtf),
                    OpenIntrospectMenuPacket::pathway,
                    OpenIntrospectMenuPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenIntrospectMenuPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isServer()) {
                ServerPlayer player = (ServerPlayer) context.player();
                
                if(!BeyonderData.isBeyonder(player))
                    return;

                int sequence = BeyonderData.getSequence(player);
                String pathway = BeyonderData.getPathway(player);
                float digestionProgress = BeyonderData.getDigestionProgress(player);

                SanityComponent sanityComponent = player.getData(ModAttachments.SANITY_COMPONENT);
                float sanity = sanityComponent.getSanity();

                AbilityWheelHelper.removeUnusableAbilities(player);
                AbilityWheelComponent abilityWheelComponent = player.getData(ModAttachments.ABILITY_WHEEL_COMPONENT);

                player.openMenu(new IntrospectMenuProvider(sequence, pathway, digestionProgress, sanity), buf -> {
                    buf.writeInt(sequence);
                    buf.writeUtf(pathway);
                });

                PacketHandler.sendToPlayer(player, new SyncIntrospectMenuPacket(sequence, pathway, sanity));
                PacketHandler.sendToPlayer(player, new SyncAbilityWheelDataToIntrospectPacket(abilityWheelComponent.getAbilities()));
                PacketHandler.sendToPlayer(player, new SyncKillCountPacket(player.getData(ModAttachments.KILL_COUNT_COMPONENT).getKillCount()));
                PacketHandler.syncUniquenessToPlayer(player);
            }
        });
    }

}