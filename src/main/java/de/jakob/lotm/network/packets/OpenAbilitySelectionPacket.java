package de.jakob.lotm.network.packets;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.*;
import de.jakob.lotm.gui.custom.AbilitySelectionMenuProvider;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.pathways.PathwayInfos;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.lwjgl.glfw.GLFW;

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

            List<ItemStack> abilities = AbilityHandler.ITEMS.getEntries().stream().filter(entry -> {
                if(entry.get() instanceof AbilityItem abilityItem)
                    return abilityItem.getRequirements().containsKey(pathway) && sequence == abilityItem.getRequirements().get(pathway);
                else if(entry.get() instanceof ToggleAbilityItem abilityItem)
                    return abilityItem.getRequirements().containsKey(pathway) && sequence == abilityItem.getRequirements().get(pathway);
                else return false;
            }).map(entry -> new ItemStack(entry.get())).toList();

            passiveAbilities.addAll(abilities);

            PathwayInfos pathwayInfo = BeyonderData.pathwayInfos.get(pathway);

            Minecraft mc = Minecraft.getInstance();
            long windowHandle = mc.getWindow().getWindow();

            // Get current mouse position
            double[] xPos = new double[1];
            double[] yPos = new double[1];
            GLFW.glfwGetCursorPos(windowHandle, xPos, yPos);

            player.openMenu(new AbilitySelectionMenuProvider(passiveAbilities, pathwayInfo.name() + " Pathway Sequence " + sequence, pathwayInfo.color(), sequence, pathway));

            PacketHandler.sendToPlayer(player, new SyncAbilityMenuPacket(sequence, pathway));

            GLFW.glfwSetCursorPos(windowHandle, xPos[0], yPos[0]);
        });
    }

}