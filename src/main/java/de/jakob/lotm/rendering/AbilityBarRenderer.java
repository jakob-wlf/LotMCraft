package de.jakob.lotm.rendering;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.ClientBeyonderCache;
import de.jakob.lotm.util.data.AbilityWheelClientData;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.Arrays;
import java.util.UUID;


@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class AbilityBarRenderer {

    private static final int HOTBAR_WIDTH = 182;
    private static final int HOTBAR_HEIGHT = 22;

    
    private static final int PLAQUE_H = 28;
    private static final int PLAQUE_GAP = 6;
    private static final int PLAQUE_PAD = 9;
    private static final int ICON_SIZE = 16;
    private static final float LABEL_SCALE = 0.65f;

    
    private static final int JAR_W = 26;
    private static final int JAR_H = 32;
    private static final int WORM_SIZE = 14;
    private static final int SURVIVAL_JAR_OFFSET = 17;

    private static final String[] COWARDLY_PATHWAYS = new String[]{"fool", "door", "error"};

    private static final ResourceLocation WORM_FOOL = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/misc/worm_of_spirit.png");
    private static final ResourceLocation WORM_DOOR = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/misc/worm_of_star.png");
    private static final ResourceLocation WORM_ERROR = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/misc/worm_of_time.png");

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR,
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "ability_bar"),
                AbilityBarRenderer::render);
    }

    private static void render(GuiGraphics g, DeltaTracker dt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui) return;

        UUID id = mc.player.getUUID();
        if (!ClientBeyonderCache.isBeyonder(id)) return;

        String pathway = ClientBeyonderCache.getPathway(id);
        if (!BeyonderData.pathwayInfos.containsKey(pathway)) return;

        int color = HudTheme.pathwayColor(pathway);
        float time = HudTheme.time(mc, dt);

        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();
        int hotbarRight = (screenW + HOTBAR_WIDTH) / 2;

        renderPlaque(g, mc, color, hotbarRight, screenH, time);
        renderJar(g, mc, id, pathway, hotbarRight, screenH, time);
    }

    

    private static void renderPlaque(GuiGraphics g, Minecraft mc, int color, int hotbarRight, int screenH, float time) {
        int selectedIndex = AbilityWheelClientData.getSelectedAbility();
        var abilities = AbilityWheelClientData.getAbilityWheelAbilities();
        if (selectedIndex < 0 || selectedIndex >= abilities.size()) return;

        String abilityId = abilities.get(selectedIndex);
        String baseId = abilityId.split(":")[0];
        if (getIndex(abilityId) >= 0) return;

        Ability selected = LOTMCraft.abilityHandler.getById(baseId);
        if (!(selected instanceof SelectableAbility ability)) return;

        Component label = Component.translatable("lotm.selected");
        Component name = Component.translatable(ability.getSelectedAbility(mc.player));

        int labelW = (int) Math.ceil(mc.font.width(label) * LABEL_SCALE);
        int textW = Math.max(labelW, mc.font.width(name));

        int w = PLAQUE_PAD + ICON_SIZE + 6 + textW + PLAQUE_PAD;
        int h = PLAQUE_H;
        int x = hotbarRight + PLAQUE_GAP;
        int y = screenH - h - 1;

        
        int[] s = HudTheme.size(HudTheme.PLAQUE);
        int srcBorder = Math.max(1, Math.min(s[0], s[1]) / 4);
        HudWidgets.drawNineSlice(g, HudTheme.PLAQUE, x, y, w, h, srcBorder, Math.max(3, Math.min(srcBorder, 6)));

        
        ResourceLocation icon = ability.getTextureLocation();
        int iconX = x + PLAQUE_PAD;
        int iconY = y + (h - ICON_SIZE) / 2;
        HudWidgets.drawIcon(g, icon, iconX, iconY, ICON_SIZE);

        
        int textX = iconX + ICON_SIZE + 6;
        int labelY = y + 5;
        g.pose().pushPose();
        g.pose().translate(textX, labelY, 0);
        g.pose().scale(LABEL_SCALE, LABEL_SCALE, 1f);
        g.drawString(mc.font, label, 0, 0, HudTheme.PARCHMENT_DIM, false);
        g.pose().popPose();

        int nameY = labelY + (int) Math.ceil(mc.font.lineHeight * LABEL_SCALE) + 2;
        g.drawString(mc.font, name, textX, nameY, color, true);

        
        float glow = 0.35f + 0.35f * HudTheme.pulse(time, 0.08f);
        int gx0 = x + PLAQUE_PAD - 2;
        int gx1 = x + w - PLAQUE_PAD + 2;
        g.fill(gx0, y + h - 5, gx1, y + h - 4, HudTheme.withAlpha(color, glow));
        g.fill(gx0 + 2, y + h - 4, gx1 - 2, y + h - 3, HudTheme.withAlpha(color, glow * 0.4f));
    }

    private static int getIndex(String s) {
        String[] parts = s.split(":");
        if (parts.length < 2) return -1;
        try {
            return Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    

    private static void renderJar(GuiGraphics g, Minecraft mc, UUID id, String pathway, int hotbarRight, int screenH, float time) {
        if (!Arrays.asList(COWARDLY_PATHWAYS).contains(pathway)) return;
        if (ClientBeyonderCache.getSequence(id) > 4) return;

        ResourceLocation wormTexture = switch (pathway) {
            case "door" -> WORM_DOOR;
            case "error" -> WORM_ERROR;
            default -> WORM_FOOL;
        };

        int x = hotbarRight - JAR_W;
        int y = screenH - HOTBAR_HEIGHT - JAR_H - 4;

        if (mc.gameMode != null && mc.gameMode.canHurtPlayer()) {
            y -= SURVIVAL_JAR_OFFSET;
        }

        HudWidgets.blitFull(g, HudTheme.JAR, x, y, JAR_W, JAR_H);

        int bob = Math.round((float) Math.sin(time * 0.12f) * 1.5f);
        int wx = x + (JAR_W - WORM_SIZE) / 2;
        int wy = y + (JAR_H - WORM_SIZE) / 2 + 2 + bob;
        HudWidgets.drawIcon(g, wormTexture, wx, wy, WORM_SIZE);

        
        HudWidgets.blitFull(g, HudTheme.JAR_GLASS, x, y, JAR_W, JAR_H);

        
        String count = String.valueOf(ClientBeyonderCache.getCowardWormAmount(id));
        int textW = mc.font.width(count);
        int plateW = Math.max(16, textW + 8);
        int plateH = 11;
        int px = x + (JAR_W - plateW) / 2;
        int py = y + JAR_H - plateH + 3;
        HudWidgets.blitFull(g, HudTheme.COUNTER_PLATE, px, py, plateW, plateH);
        g.drawString(mc.font, count, px + (plateW - textW) / 2, py + (plateH - mc.font.lineHeight) / 2 + 1,
                HudTheme.PARCHMENT, true);
    }
}