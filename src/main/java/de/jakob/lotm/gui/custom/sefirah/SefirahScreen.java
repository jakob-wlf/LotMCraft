package de.jakob.lotm.gui.custom.sefirah;

import com.mojang.blaze3d.systems.RenderSystem;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.AllyComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.network.packets.toServer.HandleSefirotGuestPacket;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

import java.util.Set;
import java.util.function.Consumer;

public class SefirahScreen extends AbstractContainerScreen<SefirahMenu> {
    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/gui/sefirah/sefirah_castle/sefirah_castle_gui.png");

    Set<AllyComponent.AllyInfo> allies;
    private AllyList allyList;

    public SefirahScreen(SefirahMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        this.imageWidth = 176;
        this.imageHeight = 240;
    }

    @Override
    protected void init() {
        super.init();

        Player player = ClientHandler.getPlayer();
        allies = player.getData(ModAttachments.ALLY_COMPONENT).allies();

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        int listX = x + 8;
        int listY = y + 20;
        int listWidth = imageWidth - 16;
        int listHeight = imageHeight - 110;

        allyList = new AllyList(listX, listY, listWidth, listHeight, this::onInviteClicked, this::onKickClicked);
        allyList.setAllies(allies);
        addRenderableWidget(allyList);
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

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        guiGraphics.drawString(
                ClientHandler.getMinecraftInstance().font,
                Component.literal("Allies"),
                (width - imageWidth) / 2 + 8,
                (height - imageHeight) / 2 + 8,
                0xFFFFFF
        );
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
}