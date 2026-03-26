package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.artifacts.SealedArtifactData;
import de.jakob.lotm.artifacts.SealedArtifactItem;
import de.jakob.lotm.data.ModDataComponents;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record NextArtifactAbilityPacket() implements CustomPacketPayload {
    public static final Type<NextArtifactAbilityPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "next-artifact_ability"));

    public static final StreamCodec<FriendlyByteBuf, NextArtifactAbilityPacket> STREAM_CODEC =
            StreamCodec.unit(new NextArtifactAbilityPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    public static void handle(NextArtifactAbilityPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if(context.player() instanceof ServerPlayer serverPlayer) {
                // Get Item in hand
                ItemStack stack = serverPlayer.getItemInHand(InteractionHand.MAIN_HAND);
                SealedArtifactData data = stack.get(ModDataComponents.SEALED_ARTIFACT_DATA);
                if (data == null || data.negativeEffect() == null) {
                    return;
                }

                SealedArtifactItem.switchAbility(stack);
            }
        });
    }
}