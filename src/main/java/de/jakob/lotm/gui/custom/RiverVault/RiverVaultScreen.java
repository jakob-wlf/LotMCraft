package de.jakob.lotm.gui.custom.RiverVault;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.RiverVaultActionPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;

public class RiverVaultScreen extends Screen {

    // ── Layout ────────────────────────────────────────────────────────────────
    private static final int COLS     = 9;
    private static final int SLOT     = 18; // slot square size (item is 16x16 inside)

    // ── Colours ───────────────────────────────────────────────────────────────
    private static final int PANEL_BG          = 0xEE1A0A40;
    private static final int PANEL_INNER       = 0xEE130830;
    private static final int PANEL_BORDER      = 0xFF9933FF;
    private static final int VAULT_SLOT_BG     = 0xFF2A1060;
    private static final int VAULT_SLOT_BORDER = 0xFF7722CC;
    private static final int IU_SLOT_BG        = 0xFF0D2010;
    private static final int IU_SLOT_BORDER    = 0xFF22AA44;
    private static final int DIV_COLOR         = 0xFF2A0055;
    private static final int TEXT_VAULT        = 0xFFCC88FF;
    private static final int TEXT_IU           = 0xFF66FFCC;
    private static final int TEXT_DIM          = 0xFF666666;
    private static final int HIGHLIGHT         = 0x55FFFFFF;

    // ── State ─────────────────────────────────────────────────────────────────
    private final List<ItemStack> vaultItems;
    private final List<ItemStack> iuItems;
    private final int maxIU;
    private final int vaultCapacity;

    // Computed in init()
    private int panelX, panelY, panelW, panelH;
    private int gridX;
    private int vaultGridY;  // y of the first vault row
    private int iuGridY;     // y of the first IU row
    private int vaultRows, iuRows;

    public RiverVaultScreen(List<ItemStack> vaultItems, List<ItemStack> iuItems, int maxIU, int vaultCapacity) {
        super(Component.literal("River Soul Vault"));
        this.vaultItems   = new ArrayList<>(vaultItems);
        this.iuItems      = new ArrayList<>(iuItems);
        this.maxIU        = maxIU;
        this.vaultCapacity = vaultCapacity;
    }

    @Override
    protected void init() {
        vaultRows = Math.max(1, (int) Math.ceil((double) vaultCapacity / COLS));
        iuRows    = Math.max(1, (int) Math.ceil((double) maxIU / COLS));

        // Width: 9 slots + 8px padding each side
        panelW = COLS * SLOT + 16;

        // Height: title(14) + pad(6) + vaultLabel(9) + vaultGrid + divider(14) + iuLabel(9) + iuGrid + pad(8)
        panelH = 14 + 6 + 9 + vaultRows * SLOT + 14 + 9 + iuRows * SLOT + 8;

        panelX = (this.width  - panelW) / 2;
        panelY = (this.height - panelH) / 2;
        gridX  = panelX + 8;

        vaultGridY = panelY + 14 + 6 + 9;
        iuGridY    = vaultGridY + vaultRows * SLOT + 14 + 9;
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Override to skip the vanilla blur shader — draw a plain dark overlay instead.
        g.fill(0, 0, this.width, this.height, 0xB0000000);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);

        // Panel
        g.fill(panelX, panelY, panelX + panelW, panelY + panelH, PANEL_BG);
        g.fill(panelX + 2, panelY + 2, panelX + panelW - 2, panelY + panelH - 2, PANEL_INNER);
        g.renderOutline(panelX, panelY, panelW, panelH, PANEL_BORDER);

        // Title
        Component titleComp = Component.literal("River Soul Vault")
                .withStyle(s -> s.withColor(TEXT_VAULT).withBold(true));
        g.drawString(font, titleComp, panelX + (panelW - font.width(titleComp)) / 2, panelY + 5, TEXT_VAULT, true);

        // Vault label
        String vaultLabel = "\u26B1 Vault  " + vaultItems.size() + " / " + vaultCapacity;
        g.drawString(font, vaultLabel, gridX, vaultGridY - 8, TEXT_VAULT, false);

        // Vault slots
        int hoveredVaultIdx = getVaultIndexAt(mouseX, mouseY);
        for (int i = 0; i < vaultCapacity; i++) {
            int col = i % COLS;
            int row = i / COLS;
            int sx  = gridX + col * SLOT;
            int sy  = vaultGridY + row * SLOT;
            boolean hasItem = i < vaultItems.size();
            boolean hovered = (i == hoveredVaultIdx) && hasItem;

            g.fill(sx, sy, sx + SLOT, sy + SLOT, hovered ? HIGHLIGHT : VAULT_SLOT_BG);
            g.renderOutline(sx, sy, SLOT, SLOT, VAULT_SLOT_BORDER);
            if (hasItem) {
                g.renderItem(vaultItems.get(i), sx + 1, sy + 1);
            }
        }

        // Divider: two arrow labels showing transfer direction
        int divY = vaultGridY + vaultRows * SLOT + 3;
        String arrowDown = "\u25BC  to IU";
        String arrowUp   = "to Vault  \u25B2";
        g.drawString(font, arrowDown, panelX + panelW / 4 - font.width(arrowDown) / 2, divY + 1, TEXT_VAULT, false);
        g.drawString(font, arrowUp,   panelX + 3 * panelW / 4 - font.width(arrowUp) / 2, divY + 1, TEXT_IU, false);

        // IU label
        String iuLabel = "\u262F Internal Underworld  " + iuItems.size() + " / " + maxIU;
        g.drawString(font, iuLabel, gridX, iuGridY - 8, TEXT_IU, false);

        // IU slots
        int hoveredIUIdx = getIUIndexAt(mouseX, mouseY);
        for (int i = 0; i < maxIU; i++) {
            int col = i % COLS;
            int row = i / COLS;
            int sx  = gridX + col * SLOT;
            int sy  = iuGridY + row * SLOT;
            boolean hasItem = i < iuItems.size();
            boolean hovered = (i == hoveredIUIdx) && hasItem;

            g.fill(sx, sy, sx + SLOT, sy + SLOT, hovered ? HIGHLIGHT : IU_SLOT_BG);
            g.renderOutline(sx, sy, SLOT, SLOT, IU_SLOT_BORDER);
            if (hasItem) {
                g.renderItem(iuItems.get(i), sx + 1, sy + 1);
            }
        }

        // Tooltip
        ItemStack hovered = null;
        boolean isVaultHovered = false;
        if (hoveredVaultIdx >= 0 && hoveredVaultIdx < vaultItems.size()) {
            hovered = vaultItems.get(hoveredVaultIdx);
            isVaultHovered = true;
        } else if (hoveredIUIdx >= 0 && hoveredIUIdx < iuItems.size()) {
            hovered = iuItems.get(hoveredIUIdx);
        }
        if (hovered != null) {
            renderItemTooltip(g, hovered, isVaultHovered, mouseX, mouseY);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderItemTooltip(GuiGraphics g, ItemStack stack, boolean fromVault, int mx, int my) {
        List<Component> lines = new ArrayList<>();
        lines.add(stack.getHoverName());
        ItemLore lore = stack.get(DataComponents.LORE);
        if (lore != null) {
            lines.addAll(lore.lines());
        }
        Component hint = fromVault
                ? Component.literal("Click to move to Internal Underworld")
                        .withStyle(s -> s.withColor(TEXT_IU).withItalic(true))
                : Component.literal("Click to move to Vault")
                        .withStyle(s -> s.withColor(TEXT_VAULT).withItalic(true));
        lines.add(hint);
        g.renderComponentTooltip(font, lines, mx, my);
    }

    // ── Input ─────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        int mx = (int) mouseX;
        int my = (int) mouseY;

        int vi = getVaultIndexAt(mx, my);
        if (vi >= 0 && vi < vaultItems.size()) {
            String key = getSoulKey(vaultItems.get(vi));
            if (key != null) {
                PacketHandler.sendToServer(new RiverVaultActionPacket(key, true));
            }
            return true;
        }

        int ii = getIUIndexAt(mx, my);
        if (ii >= 0 && ii < iuItems.size()) {
            String key = getSoulKey(iuItems.get(ii));
            if (key != null) {
                PacketHandler.sendToServer(new RiverVaultActionPacket(key, false));
            }
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Returns the vault slot index at (mx, my), or -1 if none. */
    private int getVaultIndexAt(int mx, int my) {
        for (int i = 0; i < vaultCapacity; i++) {
            int col = i % COLS;
            int row = i / COLS;
            int sx  = gridX + col * SLOT;
            int sy  = vaultGridY + row * SLOT;
            if (mx >= sx && mx < sx + SLOT && my >= sy && my < sy + SLOT) return i;
        }
        return -1;
    }

    /** Returns the IU slot index at (mx, my), or -1 if none. */
    private int getIUIndexAt(int mx, int my) {
        for (int i = 0; i < maxIU; i++) {
            int col = i % COLS;
            int row = i / COLS;
            int sx  = gridX + col * SLOT;
            int sy  = iuGridY + row * SLOT;
            if (mx >= sx && mx < sx + SLOT && my >= sy && my < sy + SLOT) return i;
        }
        return -1;
    }

    private String getSoulKey(ItemStack stack) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd == null) return null;
        CompoundTag tag = cd.copyTag();
        if (!tag.contains("SoulData")) return null;
        CompoundTag soulData = tag.getCompound("SoulData");
        String key = soulData.getString("SoulKey");
        return key.isEmpty() ? null : key;
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
