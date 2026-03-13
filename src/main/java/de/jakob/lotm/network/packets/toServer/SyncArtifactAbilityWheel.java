package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.data.ModDataComponents;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncArtifactAbilityWheel (int index) implements CustomPacketPayload {

    public static final Type<SyncArtifactAbilityWheel> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_artifact_ability_wheel"));

    public static final StreamCodec<ByteBuf, SyncArtifactAbilityWheel> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            SyncArtifactAbilityWheel::index,
            SyncArtifactAbilityWheel::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncArtifactAbilityWheel packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (!stack.has(ModDataComponents.SEALED_ARTIFACT_DATA)) {
                stack = player.getItemInHand(InteractionHand.OFF_HAND);
            }

            if (stack.has(ModDataComponents.SEALED_ARTIFACT_DATA)) {
                stack.set(ModDataComponents.SEALED_ARTIFACT_SELECTED, packet.index());
            }
        });
    }
}
