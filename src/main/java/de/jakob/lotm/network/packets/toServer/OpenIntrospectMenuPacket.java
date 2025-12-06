package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.*;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SanityComponent;
import de.jakob.lotm.gui.custom.Introspect.IntrospectMenuProvider;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncIntrospectMenuPacket;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

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

                int sequence = packet.sequence();
                String pathway = packet.pathway();
                float digestionProgress = BeyonderData.getDigestionProgress(player);

                List<ItemStack> passiveAbilities = new ArrayList<>(PassiveAbilityHandler.ITEMS.getEntries().stream().filter(entry -> {
                    if (!(entry.get() instanceof PassiveAbilityItem abilityItem))
                        return false;
                    return abilityItem.shouldApplyTo(context.player());
                }).map(entry -> new ItemStack(entry.get())).toList());

                SanityComponent sanityComponent = player.getData(ModAttachments.SANITY_COMPONENT);
                float sanity = sanityComponent.getSanity();

                player.openMenu(new IntrospectMenuProvider(passiveAbilities, sequence, pathway, digestionProgress, sanity));



                PacketHandler.sendToPlayer(player, new SyncIntrospectMenuPacket(sequence, pathway, sanity));
            }
        });
    }

}