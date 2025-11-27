package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.*;
import de.jakob.lotm.gui.custom.AbilitySelectionMenuProvider;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncAbilityMenuPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.pathways.PathwayInfos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record OpenAbilitySelectionPacket(int sequence, String pathway) implements CustomPacketPayload {
    public static final Type<OpenAbilitySelectionPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "open_ability_selection"));

    public static final StreamCodec<FriendlyByteBuf, OpenAbilitySelectionPacket> STREAM_CODEC =
            StreamCodec.composite(
                    StreamCodec.of(FriendlyByteBuf::writeInt, FriendlyByteBuf::readInt),
                    OpenAbilitySelectionPacket::sequence,
                    StreamCodec.of(FriendlyByteBuf::writeUtf, FriendlyByteBuf::readUtf),
                    OpenAbilitySelectionPacket::pathway,
                    OpenAbilitySelectionPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenAbilitySelectionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isServer()) {
                ServerPlayer player = (ServerPlayer) context.player();
                if(!BeyonderData.isBeyonder(player))
                    return;

                int sequence = packet.sequence();
                String pathway = packet.pathway();

                List<ItemStack> passiveAbilities = new ArrayList<>(PassiveAbilityHandler.ITEMS.getEntries().stream().filter(entry -> {
                    if (!(entry.get() instanceof PassiveAbilityItem abilityItem))
                        return false;
                    return abilityItem.getRequirements().containsKey(pathway) && sequence == abilityItem.getRequirements().get(pathway);
                }).map(entry -> new ItemStack(entry.get())).toList());

                List<ItemStack> abilities = AbilityItemHandler.ITEMS.getEntries().stream().filter(entry -> {
                    if(entry.get() instanceof AbilityItem abilityItem)
                        return abilityItem.getRequirements().containsKey(pathway) && sequence == abilityItem.getRequirements().get(pathway);
                    else if(entry.get() instanceof ToggleAbilityItem abilityItem)
                        return abilityItem.getRequirements().containsKey(pathway) && sequence == abilityItem.getRequirements().get(pathway);
                    else return false;
                }).map(entry -> new ItemStack(entry.get())).toList();

                passiveAbilities.addAll(abilities);

                if(passiveAbilities.isEmpty())
                    passiveAbilities.add(AbilityItemHandler.ABILITY_NOT_IMPLEMENTED.get().getDefaultInstance());

                PathwayInfos pathwayInfo = BeyonderData.pathwayInfos.get(pathway);

                player.openMenu(new AbilitySelectionMenuProvider(passiveAbilities, pathwayInfo.getName() + " " + Component.translatable("lotm.pathway").append(" ").append(Component.translatable("lotm.sequence")).getString() + " " + sequence, pathwayInfo.color(), sequence, pathway));

                PacketHandler.sendToPlayer(player, new SyncAbilityMenuPacket(sequence, pathway));
            }
        });
    }

}