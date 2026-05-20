package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.rendering.*;
import de.jakob.lotm.util.ClientBeyonderCache;
import de.jakob.lotm.util.data.ClientData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record ResetClientEffectsPacket() implements CustomPacketPayload {
    public static final Type<ResetClientEffectsPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "reset_client_effects"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ResetClientEffectsPacket> STREAM_CODEC =
            StreamCodec.unit(new ResetClientEffectsPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ResetClientEffectsPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientBeyonderCache.clearCache();
            ClientData.clearCache();
            ActiveToggleAbilitiesRenderer.clearCache();
            CullOverlay.clearCache();
            DangerPremonitionOverlayRenderer.clearCache();
            DecryptionOverlayRenderer.clearCache();
            EyeOfDeathOverlayRenderer.clearCache();
            MarionetteOverlayRenderer.clearCache();
            SpectatingOverlayRenderer.clearCache();
            SpiritVisionOverlayRenderer.clearCache();
            TelepathyOverlayRenderer.clearCache();
        });
    }
}