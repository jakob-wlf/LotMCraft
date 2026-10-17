package de.jakob.lotm.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.util.data.AbilityHotbarClientState;
import de.jakob.lotm.util.data.ClientAbilityCooldowns;
import de.jakob.lotm.util.helper.AbilityBarHelper;
import de.jakob.lotm.util.helper.AbilityId;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.List;
import java.util.Set;

/**
 * Replaces the vanilla item hotbar with two rows of ability bar slots while ability hotbar mode is on.
 * Bottom row = slots 1-9, top row = slots 10-18. The vanilla status bars are pushed up by one row.
 */
@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class AbilityHotbarRenderer {

    // Same brass sprites the Introspect menu uses
    private static final ResourceLocation DRAWER_SPRITE = introspectSprite("drawer");
    private static final ResourceLocation SLOT_SPRITE = introspectSprite("slot_idle");
    private static final ResourceLocation SLOT_EMPTY_SPRITE = introspectSprite("slot_empty");
    private static final ResourceLocation SLOT_COPIED_SPRITE = introspectSprite("slot_copied");
    private static final ResourceLocation COG_SPRITE = introspectSprite("cog_large");
    private static final int SLOT_SIZE = 18;
    private static final int COG_SIZE = 48;

    private static final int HOTBAR_WIDTH = 182;
    private static final int HOTBAR_HEIGHT = 22;
    private static final int SLOTS_PER_ROW = 9;
    private static final int ICON_SIZE = 16;
    private static final float KEY_LABEL_SCALE = 0.5f;

    /** Extra height taken up by the second row; anything stacked above the hotbar moves up by this much. */
    public static final int EXTRA_HEIGHT = HOTBAR_HEIGHT;
    /** How far each cog sticks out past the ends of the bar (a quarter of it); neighbouring widgets shift by this much. */
    public static final int COG_OVERHANG = COG_SIZE / 4;

    private static final Set<ResourceLocation> SHIFTED_LAYERS = Set.of(
            VanillaGuiLayers.PLAYER_HEALTH,
            VanillaGuiLayers.ARMOR_LEVEL,
            VanillaGuiLayers.FOOD_LEVEL,
            VanillaGuiLayers.VEHICLE_HEALTH,
            VanillaGuiLayers.AIR_LEVEL,
            VanillaGuiLayers.EXPERIENCE_BAR,
            VanillaGuiLayers.EXPERIENCE_LEVEL,
            VanillaGuiLayers.JUMP_METER,
            VanillaGuiLayers.SELECTED_ITEM_NAME
    );

    private static boolean posePushed = false;

    private static ResourceLocation introspectSprite(String name) {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "introspect/" + name);
    }

    public static boolean isShown() {
        Minecraft mc = Minecraft.getInstance();
        return AbilityHotbarClientState.isActive() && !mc.player.isSpectator();
    }

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR,
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "ability_hotbar"),
                AbilityHotbarRenderer::render);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderLayerPre(RenderGuiLayerEvent.Pre event) {
        if (!isShown()) return;

        ResourceLocation name = event.getName();
        if (name.equals(VanillaGuiLayers.HOTBAR)) {
            event.setCanceled(true);
        } else if (SHIFTED_LAYERS.contains(name)) {
            GuiGraphics g = event.getGuiGraphics();
            g.pose().pushPose();
            g.pose().translate(0, -EXTRA_HEIGHT, 0);
            posePushed = true;
        }
    }

    @SubscribeEvent
    public static void onRenderLayerPost(RenderGuiLayerEvent.Post event) {
        if (posePushed && SHIFTED_LAYERS.contains(event.getName())) {
            event.getGuiGraphics().pose().popPose();
            posePushed = false;
        }
    }

    private static void render(GuiGraphics g, DeltaTracker dt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || !isShown()) return;

        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();
        int left = (screenW - HOTBAR_WIDTH) / 2;

        List<String> abilities = AbilityBarHelper.getAbilities(mc.player);
        float partialTick = dt.getGameTimeDeltaPartialTick(false);

        // Drawn first so the panel covers most of them, leaving a quarter of a cog peeking out of each end.
        // They turn in opposite directions, like the meshing cogs in the Introspect menu.
        float cogAngle = HudTheme.time(mc, dt) * 0.5f;
        int cogInset = COG_SIZE / 2 - COG_OVERHANG;
        renderCog(g, left + cogInset, screenH - HOTBAR_HEIGHT, -cogAngle);
        renderCog(g, left + HOTBAR_WIDTH - cogInset, screenH - HOTBAR_HEIGHT, cogAngle);

        int rows = AbilityBarHelper.MAX_SLOTS / SLOTS_PER_ROW;
        RenderSystem.enableBlend();
        g.blitSprite(DRAWER_SPRITE, left, screenH - HOTBAR_HEIGHT * rows, HOTBAR_WIDTH, HOTBAR_HEIGHT * rows);

        for (int row = 0; row < rows; row++) {
            int top = screenH - HOTBAR_HEIGHT * (row + 1);

            for (int col = 0; col < SLOTS_PER_ROW; col++) {
                int slot = row * SLOTS_PER_ROW + col;
                int x = left + 3 + col * 20;
                int y = top + 3;

                String abilityId = slot < abilities.size() ? abilities.get(slot) : null;
                ResourceLocation slotSprite = abilityId == null ? SLOT_EMPTY_SPRITE
                        : AbilityId.parse(abilityId).copied() ? SLOT_COPIED_SPRITE : SLOT_SPRITE;
                g.blitSprite(slotSprite, x - 1, y - 1, SLOT_SIZE, SLOT_SIZE);

                if (abilityId != null) {
                    renderAbility(g, mc, abilityId, x, y, partialTick);
                }
                renderKeyLabel(g, mc, slot, x, y);
            }
        }
    }

    private static void renderCog(GuiGraphics g, float cx, float cy, float degrees) {
        g.pose().pushPose();
        g.pose().translate(cx, cy, 0);
        g.pose().mulPose(Axis.ZP.rotationDegrees(degrees));
        g.blitSprite(COG_SPRITE, -COG_SIZE / 2, -COG_SIZE / 2, COG_SIZE, COG_SIZE);
        g.pose().popPose();
    }

    private static void renderAbility(GuiGraphics g, Minecraft mc, String abilityId, int x, int y, float partialTick) {
        Ability ability = LOTMCraft.abilityHandler.getById(abilityId.split(":")[0]);
        if (ability == null) return;

        g.blit(ability.getTextureLocation(), x, y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);

        int subIndex = getSubIndex(abilityId);
        if (subIndex != -1) {
            String badge = String.valueOf(subIndex);
            int bx = x + ICON_SIZE - mc.font.width(badge);
            int by = y + ICON_SIZE - mc.font.lineHeight + 1;
            g.fill(bx, by, bx + mc.font.width(badge), by + mc.font.lineHeight - 1, 0xAA000000);
            g.drawString(mc.font, badge, bx, by, 0xFFFFFF, false);
        }

        renderCooldown(g, mc, ability.getId(), x, y, partialTick);
    }

    // Same look as the vanilla item cooldown: a dark overlay that shrinks downwards, plus seconds left
    private static void renderCooldown(GuiGraphics g, Minecraft mc, String abilityId, int x, int y, float partialTick) {
        int remainingTicks = ClientAbilityCooldowns.getRemainingTicks(abilityId);
        if (remainingTicks <= 0) return;

        float fraction = ClientAbilityCooldowns.getRemainingFraction(abilityId, partialTick);
        int top = y + Math.round(ICON_SIZE * (1f - fraction));
        g.pose().pushPose();
        g.pose().translate(0, 0, 150);
        g.fill(x, top, x + ICON_SIZE, y + ICON_SIZE, 0x99000000);

        int seconds = (remainingTicks + 19) / 20;
        String text = seconds >= 60 ? (seconds / 60) + "m" : String.valueOf(seconds);
        int tx = x + (ICON_SIZE - mc.font.width(text)) / 2;
        int ty = y + (ICON_SIZE - mc.font.lineHeight) / 2 + 1;
        g.drawString(mc.font, text, tx, ty, 0xFFFFFFFF, true);
        g.pose().popPose();
    }

    private static void renderKeyLabel(GuiGraphics g, Minecraft mc, int slot, int x, int y) {
        String label = AbilityHotbarClientState.shortKeyLabel(LOTMCraft.useAbilityBarKeys[slot]);
        g.pose().pushPose();
        g.pose().translate(x, y, 200);
        g.pose().scale(KEY_LABEL_SCALE, KEY_LABEL_SCALE, 1f);
        g.drawString(mc.font, label, 0, 0, 0xFFDDDDDD, true);
        g.pose().popPose();
    }

    private static int getSubIndex(String s) {
        String[] parts = s.split(":");
        if (parts.length < 2) return -1;
        try {
            return Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
