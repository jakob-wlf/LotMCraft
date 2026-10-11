package de.jakob.lotm.gui.custom.sefirah;

import com.mojang.blaze3d.systems.RenderSystem;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.AllyComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.sefirah.SefirahHandler;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.network.packets.toServer.HandleSefirotGuestPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SefirahScreen extends AbstractContainerScreen<SefirahMenu> {
    private ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/gui/sefirah/sefirah_castle/sefirah_castle_gui.png");


    Set<AllyComponent.AllyInfo> allies;
    private AllyList allyList;

    private List<String> perks = new ArrayList<>();
    private PerkList perkList;

    private static final int PERKS_PANEL_WIDTH = 150;
    private static final int PERKS_PANEL_GAP = 6;
    private static final int PERKS_ROW_HEIGHT = 24;
    private static final int PERKS_TITLE_HEIGHT = 14;

    private static final int PANEL_BORDER_OUTER = 0xFF000000;
    private static final int PANEL_TEXT_COLOR = 0xFFE8E8E8;

    private int perksPanelHeight;

    // Theme colors derived from SefirahHandler.getColorsForSefirot(...), applied to the perks panel
    private int perksAccentColor = 0xFFFFFFFF;
    private int perksBorderInnerColor = 0xFFFFFFFF;
    private int perksBgTopColor = 0xE0303030;
    private int perksBgBottomColor = 0xE0101010;
    private int perksRowAltColor = 0x1AFFFFFF;
    private int perksRowHoverColor = 0x30FFFFFF;

    private static int withAlpha(int rgb, int alpha) {
        return (alpha << 24) | (rgb & 0xFFFFFF);
    }

    private static int blend(int colorA, int colorB, float ratio) {
        int rA = (colorA >> 16) & 0xFF, gA = (colorA >> 8) & 0xFF, bA = colorA & 0xFF;
        int rB = (colorB >> 16) & 0xFF, gB = (colorB >> 8) & 0xFF, bB = colorB & 0xFF;
        int r = Math.round(rA + (rB - rA) * ratio);
        int g = Math.round(gA + (gB - gA) * ratio);
        int b = Math.round(bA + (bB - bA) * ratio);
        return (r << 16) | (g << 8) | b;
    }

    private void applySefirotTheme(String sefirot) {
        int[] colors = SefirahHandler.getColorsForSefirot(sefirot);
        int dark = colors.length > 1 ? colors[1] : 0x000000;
        int accent = colors.length > 0 ? colors[0] : 0xFFFFFF;
        int light = colors.length > 2 ? colors[2] : accent;

        perksAccentColor = withAlpha(accent, 0xFF);
        perksBorderInnerColor = withAlpha(light, 0xFF);
        perksBgTopColor = withAlpha(blend(dark, accent, 0.35f), 0xE0);
        perksBgBottomColor = withAlpha(dark, 0xE8);
        perksRowAltColor = withAlpha(light, 0x22);
        perksRowHoverColor = withAlpha(light, 0x40);
    }

    public SefirahScreen(SefirahMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        this.imageWidth = 176;
        this.imageHeight = 240;
    }

    @Override
    protected void init() {
        super.init();

        Player player = ClientHandler.getPlayer();
        allies = player.getData(ModAttachments.ALLY_COMPONENT).allies().stream().filter(AllyComponent.AllyInfo::isPlayer).collect(Collectors.toSet());

        GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, getTexturePathForPlayer(player));

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        int listX = x + 8;
        int listY = y + 20;
        int listWidth = imageWidth - 16;
        int listHeight = imageHeight - 125;

        allyList = new AllyList(listX, listY, listWidth, listHeight, this::onInviteClicked, this::onKickClicked);
        allyList.setAllies(allies);
        addRenderableWidget(allyList);

        int progress = SefirahHandler.getSefirotProgress(player);
        String sefirot = getSefirotForPlayer(player);
        perks = SefirahHandler.getPerksForProgressAndSefirot(progress, sefirot);
        applySefirotTheme(sefirot);

        int perksX = x + imageWidth + PERKS_PANEL_GAP;
        int perksListY = y + PERKS_TITLE_HEIGHT;
        int perksListHeight = Math.max(PERKS_ROW_HEIGHT, perks.size() * PERKS_ROW_HEIGHT);
        perksPanelHeight = PERKS_TITLE_HEIGHT + perksListHeight;

        perkList = new PerkList(perksX, perksListY, PERKS_PANEL_WIDTH, perksListHeight);
        perkList.setPerks(perks);
        addRenderableWidget(perkList);
    }

    private void onInviteClicked(AllyComponent.AllyInfo info) {
        PacketHandler.sendToServer(new HandleSefirotGuestPacket(info.uuid(), 0));
    }

    private void onKickClicked(AllyComponent.AllyInfo info) {
        PacketHandler.sendToServer(new HandleSefirotGuestPacket(info.uuid(), 1));
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {

    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    }

    private void renderPerksPanel(GuiGraphics guiGraphics) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        int perksX = x + imageWidth + PERKS_PANEL_GAP;
        int perksY = y;
        int perksHeight = perksPanelHeight;

        // Outer dark border, then a theme-colored inset border for a subtle bevel
        guiGraphics.fill(perksX - 1, perksY - 1, perksX + PERKS_PANEL_WIDTH + 1, perksY + perksHeight + 1, PANEL_BORDER_OUTER);
        guiGraphics.fill(perksX, perksY, perksX + PERKS_PANEL_WIDTH, perksY + perksHeight, perksBorderInnerColor);

        // Background gradient, tinted by the sefirot theme, inset from the border
        guiGraphics.fillGradient(
                perksX + 1, perksY + 1,
                perksX + PERKS_PANEL_WIDTH - 1, perksY + perksHeight - 1,
                perksBgTopColor, perksBgBottomColor
        );

        Component title = Component.literal("Perks").withStyle(ChatFormatting.BOLD);
        int titleWidth = ClientHandler.getMinecraftInstance().font.width(title);
        int titleX = perksX + (PERKS_PANEL_WIDTH - titleWidth) / 2;
        guiGraphics.drawString(ClientHandler.getMinecraftInstance().font, title, titleX, perksY + 4, perksAccentColor);

        guiGraphics.fill(
                perksX + 4, perksY + PERKS_TITLE_HEIGHT - 1,
                perksX + PERKS_PANEL_WIDTH - 4, perksY + PERKS_TITLE_HEIGHT - 2,
                perksBorderInnerColor
        );
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        renderPerksPanel(guiGraphics);

        guiGraphics.drawString(
                ClientHandler.getMinecraftInstance().font,
                Component.literal("Allies"),
                (width - imageWidth) / 2 + 8,
                (height - imageHeight) / 2 + 8,
                0xFFFFFF
        );

        guiGraphics.drawString(
                ClientHandler.getMinecraftInstance().font,
                Component.literal("Sefirot Control: " + SefirahHandler.getSefirotProgress(ClientHandler.getPlayer())).withStyle(ChatFormatting.BOLD),
                (width - imageWidth) / 2 + 8,
                (height - imageHeight) / 2 + 20 + imageHeight - 120,
                0xFFFFFF
        );
    }

    private String getSefirotForPlayer(Player player) {
        if (player.level().dimension().equals(ModDimensions.SEFIRAH_CASTLE_DIMENSION_KEY)) {
            return "sefirah_castle";
        } else if (player.level().dimension().equals(ModDimensions.BROOD_HIVE_DIMENSION_KEY)) {
            return "brood_hive";
        }

        return "sefirah_castle";
    }

    private String getTexturePathForPlayer(Player player) {
        if(player.level().dimension().equals(ModDimensions.SEFIRAH_CASTLE_DIMENSION_KEY)) {
            return "textures/gui/sefirah/sefirah_castle/sefirah_castle_gui.png";
        } else if(player.level().dimension().equals(ModDimensions.BROOD_HIVE_DIMENSION_KEY)) {
            return "textures/gui/sefirah/brood_hive/brood_hive_gui.png";
        }

        return "textures/gui/sefirah/sefirah_castle/sefirah_castle_gui.png";
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Always let Escape close the screen, regardless of what's focused
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public class AllyList extends ObjectSelectionList<AllyList.AllyEntry> {

        private final Consumer<AllyComponent.AllyInfo> onInvite;
        private final Consumer<AllyComponent.AllyInfo> onKick;

        public AllyList(int x, int y, int width, int height, Consumer<AllyComponent.AllyInfo> onInvite, Consumer<AllyComponent.AllyInfo> onKick) {
            super(ClientHandler.getMinecraftInstance(), width, height, y, 50);
            this.setX(x);
            this.onInvite = onInvite;
            this.onKick = onKick;
        }

        public void setAllies(Set<AllyComponent.AllyInfo> allies) {
            this.clearEntries();
            for (AllyComponent.AllyInfo info : allies) {
                this.addEntry(new AllyEntry(info));
            }
        }

        @Override
        public int getRowWidth() {
            return this.width - 10;
        }

        @Override
        protected int getScrollbarPosition() {
            return this.getX() + this.width - 6;
        }

        public class AllyEntry extends ObjectSelectionList.Entry<AllyEntry> {
            private final AllyComponent.AllyInfo info;
            private final Button inviteButton;
            private final Button kickButton;

            public AllyEntry(AllyComponent.AllyInfo info) {
                this.info = info;
                this.inviteButton = Button.builder(
                                Component.literal("Invite"),
                                btn -> onInvite.accept(info))
                        .bounds(0, 0, 50, 20)
                        .build();

                this.kickButton = Button.builder(
                                Component.literal("Kick"),
                                btn -> onKick.accept(info))
                        .bounds(0, 0, 50, 20)
                        .build();
            }

            @Override
            public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height,
                               int mouseX, int mouseY, boolean hovering, float partialTick) {
                String label = info.playerName() + (info.isPlayer() ? "" : " (offline)");
                guiGraphics.drawString(
                        ClientHandler.getMinecraftInstance().font,
                        label,
                        left + 4,
                        top + (height - 8) / 2,
                        0xFFFFFF
                );

                inviteButton.setX(left + width - 55);
                inviteButton.setY(top);
                inviteButton.render(guiGraphics, mouseX, mouseY, partialTick);

                kickButton.setX(left + width - 55);
                kickButton.setY(top + 22);
                kickButton.render(guiGraphics, mouseX, mouseY, partialTick);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (inviteButton.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
                if (kickButton.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
                return super.mouseClicked(mouseX, mouseY, button);
            }

            @Override
            public Component getNarration() {
                return Component.literal(info.playerName());
            }
        }
    }

    public class PerkList extends ObjectSelectionList<PerkList.PerkEntry> {

        public PerkList(int x, int y, int width, int height) {
            super(ClientHandler.getMinecraftInstance(), width, height, y, 24);
            this.setX(x);
        }

        public void setPerks(List<String> perkKeys) {
            this.clearEntries();
            for (String key : perkKeys) {
                this.addEntry(new PerkEntry(key));
            }
        }

        @Override
        public int getRowWidth() {
            return this.width - 10;
        }

        @Override
        protected int getScrollbarPosition() {
            return this.getX() + this.width - 6;
        }

        public class PerkEntry extends ObjectSelectionList.Entry<PerkEntry> {
            private final String translationKey;

            public PerkEntry(String translationKey) {
                this.translationKey = translationKey;
            }

            @Override
            public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height,
                               int mouseX, int mouseY, boolean hovering, float partialTick) {
                if (index % 2 == 0) {
                    guiGraphics.fill(left, top, left + width, top + height, perksRowAltColor);
                }
                if (hovering) {
                    guiGraphics.fill(left, top, left + width, top + height, perksRowHoverColor);
                }

                Component text = Component.translatable(translationKey);
                List<FormattedCharSequence> lines = ClientHandler.getMinecraftInstance().font.split(text, width - 14);

                int visibleLines = Math.min(lines.size(), 2);
                int lineY = top + (height - visibleLines * 9) / 2;

                guiGraphics.drawString(
                        ClientHandler.getMinecraftInstance().font,
                        "•",
                        left + 3,
                        lineY,
                        perksAccentColor
                );

                int textX = left + 13;
                for (FormattedCharSequence line : lines) {
                    if (lineY > top + height - 9) {
                        break;
                    }
                    guiGraphics.drawString(
                            ClientHandler.getMinecraftInstance().font,
                            line,
                            textX,
                            lineY,
                            PANEL_TEXT_COLOR
                    );
                    lineY += 9;
                }
            }

            @Override
            public Component getNarration() {
                return Component.translatable(translationKey);
            }
        }
    }
}