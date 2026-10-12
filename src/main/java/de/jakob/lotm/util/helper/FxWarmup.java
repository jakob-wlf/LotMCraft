package de.jakob.lotm.util.helper;

import com.lowdragmc.photon.client.fx.BlockEffectExecutor;
import com.lowdragmc.photon.client.fx.FX;
import com.lowdragmc.photon.client.fx.FXHelper;
import com.lowdragmc.photon.client.fx.FXRuntime;
import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class FxWarmup {

    private static final Queue<ResourceLocation> PENDING = new ArrayDeque<>();
    private static final List<BlockEffectExecutor> ACTIVE = new ArrayList<>();
    private static boolean done = true;

    public static void queue(ResourceLocation id) {
        PENDING.add(id);
        done = false;
    }

    private static void stopAll() {
        for (BlockEffectExecutor ex : ACTIVE) {
            try {
                FXRuntime rt = ex.getRuntime();
                if (rt != null) rt.destroy(true);
            } catch (Throwable t) {
                LOTMCraft.LOGGER.warn("Failed to stop warm-up effect", t);
            }
        }
        ACTIVE.clear();
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        if (!ACTIVE.isEmpty() && !(mc.screen instanceof WarmupScreen)) {
            stopAll();
        }

        if (done || PENDING.isEmpty()) return;
        if (mc.level == null || mc.player == null || mc.screen != null) return;

        done = true;
        mc.setScreen(new WarmupScreen());
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        stopAll();
    }

    public static class WarmupScreen extends Screen {
        private static final int PER_TICK = 3;
        private static final int SETTLE_TICKS = 20;
        private int settle = 0;
        private boolean closed = false;

        public WarmupScreen() { super(Component.literal("Loading effects")); }

        @Override
        public void tick() {
            Minecraft mc = Minecraft.getInstance();
            if (closed) return;
            if (mc.level == null || mc.player == null) { onClose(); return; }

            for (int i = 0; i < PER_TICK && !PENDING.isEmpty(); i++) {
                ResourceLocation id = PENDING.poll();
                try {
                    FX fx = FXHelper.getFX(id);
                    if (fx == null) continue;

                    Vec3 pos = mc.player.getEyePosition().add(mc.player.getLookAngle().scale(3));
                    BlockEffectExecutor ex = new BlockEffectExecutor(fx, mc.level, BlockPos.containing(pos));
                    ex.setForcedDeath(true);
                    ex.setAllowMulti(true);
                    ACTIVE.add(ex);
                    ex.start();
                } catch (Exception e) {
                    LOTMCraft.LOGGER.warn("Warm-up failed for {}", id, e);
                }
            }

            if (PENDING.isEmpty() && ++settle >= SETTLE_TICKS) onClose();
        }

        @Override
        public void removed() {
            closed = true;
            stopAll();
            super.removed();
        }

        @Override
        public void renderBackground(GuiGraphics g, int mx, int my, float pt) {
            g.fill(0, 0, width, height, 0xFF000000);
        }

        @Override
        public void render(GuiGraphics g, int mx, int my, float pt) {
            super.render(g, mx, my, pt);
            g.drawCenteredString(font, "Pre-Loading heavy effects to reduce lag when they play...", width / 2, height / 2, 0xFFFFFF);
            g.drawCenteredString(font, "Skip this screen by disabling the gamerule preLoadEffects", width / 2, height / 2 + 10, 0xFFFFFF);
        }

        @Override public boolean shouldCloseOnEsc() { return false; }
        @Override public boolean isPauseScreen() { return false; }
    }
}