package de.jakob.lotm.beyonders.abilities.abyss.passives;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
public class BloodSovereignClient {

    // Layout of the vial. Change these to move or resize it.
    private static final int RADIUS = 22;
    private static final int MARGIN_RIGHT = 10;
    private static final int OFFSET_FROM_CENTER_Y = 0;
    private static volatile float blood = 0f;
    private static volatile float capacity = 0f;
    public record SyncBlood(float blood, float capacity) implements CustomPacketPayload {
        public static final Type<SyncBlood> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_blood_sovereign"));
        public static final StreamCodec<FriendlyByteBuf, SyncBlood> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.FLOAT, SyncBlood::blood,
                ByteBufCodecs.FLOAT, SyncBlood::capacity,
                SyncBlood::new);
        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }
        public static void handle(SyncBlood packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                BloodSovereignClient.blood = packet.blood();
                BloodSovereignClient.capacity = packet.capacity();
            });
        }
    }
    //drawing
    private static void renderVial(GuiGraphics g, DeltaTracker deltaTracker) {
        float cap = capacity;
        if (cap <= 0f) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        float fill = Math.clamp(blood / cap, 0f, 1f);
        int cx = g.guiWidth() - RADIUS - MARGIN_RIGHT;
        int cy = g.guiHeight() / 2 + OFFSET_FROM_CENTER_Y;
        drawDisc(g, cx, cy, RADIUS + 2, 0xFF1A0505);  // rim
        drawDisc(g, cx, cy, RADIUS, 0xFF2B0B0B);      // empty glass
        int bottom = cy + RADIUS;
        int top = bottom - Math.round(2f * RADIUS * fill);
        for (int y = top; y <= bottom; y++) {
            int dy = y - cy;
            int half = (int) Math.round(Math.sqrt((double) RADIUS * RADIUS - (double) dy * dy));
            float depth = (y - top) / (float) Math.max(1, bottom - top);
            int red = 200 - Math.round(depth * 80f);       // brighter at the surface, darker at the bottom
            int color = 0xFF000000 | (red << 16) | (14 << 8) | 16;
            if (y == top) color = 0xFFE23A3A;              // the surface line
            g.fill(cx - half, y, cx + half + 1, y + 1, color);
        }
        g.fill(cx - RADIUS / 2, cy - RADIUS / 2, cx - RADIUS / 2 + 7, cy - RADIUS / 2 + 3, 0x50FFFFFF);
        g.drawCenteredString(mc.font, Math.round(fill * 100f) + "%", cx, cy + RADIUS + 6, 0xFFFFFF);
    }
    private static void drawDisc(GuiGraphics g, int cx, int cy, int radius, int color) {
        for (int dy = -radius; dy <= radius; dy++) {
            int half = (int) Math.round(Math.sqrt((double) radius * radius - (double) dy * dy));
            g.fill(cx - half, cy + dy, cx + half + 1, cy + dy + 1, color);
        }
    }
    //registration
    @EventBusSubscriber(modid = LOTMCraft.MOD_ID)
    public static class Layers {
        @SubscribeEvent
        public static void registerLayers(RegisterGuiLayersEvent event) {event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "blood_sovereign_vial"), BloodSovereignClient::renderVial);}
    }
    @EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
            blood = 0f;
            capacity = 0f;
        }
    }
}