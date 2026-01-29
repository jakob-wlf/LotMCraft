package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.gui.custom.AbilityWheel.AbilityWheelMenu;
import de.jakob.lotm.util.helper.AbilityWheelHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenAbilityWheelPacket() implements CustomPacketPayload {
    public static final Type<OpenAbilityWheelPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "open_ability_wheel"));

    public static final StreamCodec<FriendlyByteBuf, OpenAbilityWheelPacket> STREAM_CODEC =
            StreamCodec.unit(new OpenAbilityWheelPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenAbilityWheelPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isServer()) {
                ServerPlayer player = (ServerPlayer) context.player();
                AbilityWheelHelper.removeUnusableAbilities(player);
                player.openMenu(new SimpleMenuProvider(
                        (id, inventory, p) -> new AbilityWheelMenu(id, inventory),
                        Component.translatable("lotm.ability_wheel.title")
                ));
            }
        });
    }

}