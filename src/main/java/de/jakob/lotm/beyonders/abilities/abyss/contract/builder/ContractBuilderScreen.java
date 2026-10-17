package de.jakob.lotm.beyonders.abilities.abyss.contract.builder;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.SubmitContractBuilderPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ContractBuilderScreen extends Screen {
    private static final int LEFT_WIDTH = 150;
    private static final int MARGIN = 20;
    private static final int TERM_BUTTON_HEIGHT = 30;
    private static final int TERM_BUTTON_GAP = 4;
    private static final int ACTION_HEIGHT = 20;
    private static final int ACTION_GAP = 5;

    private final List<BuilderToken> tokens = new ArrayList<>();

    //These are NOT added to the normal Minecraft widget list I am render and click them manually so they can be clipped.
    private final List<Button> termButtons = new ArrayList<>();

    private BuilderToken activeInputToken = null;
    private EditBox valueBox;
    private int signMode = 0;
    // Contract scrolling
    private int contractScrollOffset = 0;
    private int maxContractScrollOffset = 0;
    // Term list scrolling
    private int termScrollOffset = 0;
    private int maxTermScrollOffset = 0;

    public ContractBuilderScreen() {
        super(Component.literal("Contract Builder"));
    }

    @Override
    protected void init() {
         //TERM BUTTONS
        termButtons.clear();
        for (ContractTermKind kind : ContractTermKind.values()) {
            Button button = Button.builder(Component.literal(kind.label), btn -> onTermClicked(kind)).bounds(MARGIN, 0, LEFT_WIDTH, TERM_BUTTON_HEIGHT).build();
            termButtons.add(button);
        }
         // BOTTOM BUTTONS
        int bottomY = this.height - 28;
        int bw = 90;
        this.addRenderableWidget(Button.builder(Component.literal("Sign as Party A"), b -> submit(1)).bounds(MARGIN, bottomY, bw, ACTION_HEIGHT).build());
        this.addRenderableWidget(Button.builder(Component.literal("Sign as Party B"), b -> submit(2)).bounds(MARGIN + bw + ACTION_GAP, bottomY, bw, ACTION_HEIGHT).build());
        this.addRenderableWidget(Button.builder(Component.literal("Leave Blank"), b -> signMode = 0).bounds(MARGIN + (bw + ACTION_GAP) * 2, bottomY, bw, ACTION_HEIGHT).build());
        this.addRenderableWidget(Button.builder(Component.literal("Clear"), b -> clearAll()).bounds(MARGIN + (bw + ACTION_GAP) * 3, bottomY, bw, ACTION_HEIGHT).build());
         // INPUT BOX
        valueBox = new EditBox(this.font, LEFT_WIDTH + MARGIN * 2, bottomY - 24, this.width - LEFT_WIDTH - MARGIN * 3, 18, Component.literal("value"));
        valueBox.setResponder(s -> {if (activeInputToken != null) {activeInputToken.value = s;}});
        valueBox.setVisible(false);
        this.addRenderableWidget(valueBox);
        updateTermScroll();
    }
     //TERM BUTTONS
    private int getTermPanelTop() {
        return MARGIN;
    }
    private int getTermPanelBottom() {
        return this.height - 38;
    }
    private int getTermPanelHeight() {
        return getTermPanelBottom() - getTermPanelTop();
    }
    private void updateTermScroll() {
        int contentHeight = termButtons.size() * (TERM_BUTTON_HEIGHT + TERM_BUTTON_GAP);
        maxTermScrollOffset = Math.max(0, contentHeight - getTermPanelHeight());
        termScrollOffset = Math.clamp(termScrollOffset, 0, maxTermScrollOffset);
    }

    private void renderTermButtons(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

        int panelTop = getTermPanelTop();
        int panelBottom = getTermPanelBottom();
        //Dark background behind the term list.
        guiGraphics.fill(MARGIN - 2, panelTop - 2, MARGIN + LEFT_WIDTH + 2, panelBottom + 2, 0xFF202020);


        //Clip everything inside the term panel.
        guiGraphics.enableScissor(MARGIN, panelTop, MARGIN + LEFT_WIDTH, panelBottom);
        int y = panelTop - termScrollOffset;
        for (Button button : termButtons) {
            button.setX(MARGIN);
            button.setY(y);
            if (y + TERM_BUTTON_HEIGHT >= panelTop && y <= panelBottom) {
                button.render(guiGraphics, mouseX, mouseY, partialTick);
            }
            y += TERM_BUTTON_HEIGHT + TERM_BUTTON_GAP;
        }
        guiGraphics.disableScissor();
        //Scrollbar.
        if (maxTermScrollOffset > 0) {
            int scrollbarX = MARGIN + LEFT_WIDTH + 5;
            int scrollbarHeight = getTermPanelHeight();
            guiGraphics.fill(scrollbarX, panelTop, scrollbarX + 4, panelTop + scrollbarHeight, 0xFF444444);
            int totalContentHeight = termButtons.size() * (TERM_BUTTON_HEIGHT + TERM_BUTTON_GAP);
            int thumbHeight = Math.max(20, (int) ((float) scrollbarHeight * scrollbarHeight / totalContentHeight));
            int thumbTravel = scrollbarHeight - thumbHeight;
            int thumbY = panelTop + (int) ((float) termScrollOffset / maxTermScrollOffset * thumbTravel);
            guiGraphics.fill(scrollbarX, thumbY, scrollbarX + 4, thumbY + thumbHeight, 0xFFAAAAAA);
        }
    }

    //CONTRACT
    private int getPageX() {
        return LEFT_WIDTH + MARGIN * 2;
    }
    private int getPageY() {
        return MARGIN;
    }
    private int getPageW() {
        return this.width - getPageX() - MARGIN;
    }
    private int getPageH() {
        return this.height - getPageY() - 60;
    }
    //TERM CLICK
    private void onTermClicked(ContractTermKind kind) {
        commitActiveInput();
        if (kind == ContractTermKind.TIME_UNIT) {
            if (!tokens.isEmpty() && tokens.getLast().kind == ContractTermKind.TIME_UNIT) {
                BuilderToken last = tokens.getLast();
                last.value = switch (last.value) {
                    case "days" -> "minutes";
                    case "minutes" -> "seconds";
                    default -> "days";
                };
                return;
            }
            tokens.add(new BuilderToken(kind, "days")
            );
            return;
        }
        if (kind == ContractTermKind.ENTITY_TYPE) {
            if (!tokens.isEmpty() && tokens.getLast().kind == ContractTermKind.ENTITY_TYPE) {
                BuilderToken last = tokens.getLast();
                last.value = "MOB".equalsIgnoreCase(last.value) ? "PLAYER" : "MOB";
                return;
            }
            tokens.add(new BuilderToken(kind, "MOB")
            );
            return;
        }
        BuilderToken token = new BuilderToken(kind, "");
        tokens.add(token);
        if (kind.isInput) {
            activeInputToken = token;
            valueBox.setVisible(true);
            valueBox.setValue("");
            valueBox.setFocused(true);
        }
    }
    private void commitActiveInput() {
        activeInputToken = null;
        if (valueBox != null) valueBox.setVisible(false);
    }
    private void clearAll() {
        tokens.clear();
        signMode = 0;
        contractScrollOffset = 0;
        termScrollOffset = 0;
        commitActiveInput();
    }
    private void submit(int signMode) {
        commitActiveInput();
        List<SubmitContractBuilderPacket.TokenDTO> dto = new ArrayList<>();
        for (BuilderToken t : tokens) {
            dto.add(new SubmitContractBuilderPacket.TokenDTO(t.kind.ordinal(), t.value));
        }
        PacketHandler.sendToServer(new SubmitContractBuilderPacket(dto, signMode));
        this.onClose();
    }


     //CONTRACT SCROLLING
    private void updateContractScroll(int lineCount) {
        int lineHeight = 10;
        int textTop = getPageY() + 28;
        int textBottom = getPageY() + getPageH() - 8;
        int visibleHeight = textBottom - textTop;
        int totalHeight = lineCount * lineHeight;
        maxContractScrollOffset =
                Math.max(0, totalHeight - visibleHeight);
        contractScrollOffset = Math.clamp(contractScrollOffset, 0, maxContractScrollOffset);
    }

    //RENDER
    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        int pageX = getPageX();
        int pageY = getPageY();
        int pageW = getPageW();
        int pageH = getPageH();
        //Paper
        guiGraphics.fill(pageX, pageY, pageX + pageW, pageY + pageH, 0xFFF5EED8);
        guiGraphics.renderOutline(pageX, pageY, pageW, pageH, 0xFF8A806C);
        //Title
        guiGraphics.drawString(this.font, "CONTRACT", pageX + 10, pageY + 8, 0xFF333333, false);


        //Contract Text
        String sentence = renderSentence();
        if (sentence.isEmpty()) sentence = "Your contract will appear here...";
        List<FormattedCharSequence> lines = this.font.split(FormattedText.of(sentence), pageW - 20);
        updateContractScroll(lines.size());
        int lineHeight = 10;
        int textTop = pageY + 28;
        int textBottom = pageY + pageH - 8;
        guiGraphics.enableScissor(pageX + 1, textTop, pageX + pageW - 1, textBottom);
        int ly = textTop - contractScrollOffset;
        for (FormattedCharSequence line : lines) {
            guiGraphics.drawString(this.font, line, pageX + 10, ly, 0xFF111111, false);
            ly += lineHeight;
        }
        guiGraphics.disableScissor();
        //Contract Scrollbar
        if (maxContractScrollOffset > 0) {
            int scrollbarX = pageX + pageW - 6;
            int scrollbarHeight = textBottom - textTop;
            guiGraphics.fill(scrollbarX, textTop, scrollbarX + 4, textTop + scrollbarHeight, 0xFFB0A995);
            int totalHeight = lines.size() * lineHeight;
            int thumbHeight = Math.max(15, (int) ((float) scrollbarHeight * scrollbarHeight / totalHeight));
            int thumbTravel = scrollbarHeight - thumbHeight;
            int thumbY = textTop + (int) ((float) contractScrollOffset / maxContractScrollOffset * thumbTravel);
            guiGraphics.fill(scrollbarX, thumbY, scrollbarX + 4, thumbY + thumbHeight, 0xFF756F62);
        }
        //Term Buttons
        updateTermScroll();
        renderTermButtons(guiGraphics, mouseX, mouseY, partialTick);
        //Widgets
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        //Term Scrolling
        if (mouseX >= MARGIN && mouseX <= MARGIN + LEFT_WIDTH && mouseY >= getTermPanelTop() && mouseY <= getTermPanelBottom()) {
            termScrollOffset -= (int) (scrollY * 20);
            termScrollOffset = Math.clamp(termScrollOffset, 0, maxTermScrollOffset);
            return true;
        }
        //Contract Scrolling
        int pageX = getPageX();
        int pageY = getPageY();
        int pageW = getPageW();
        int pageH = getPageH();
        if (mouseX >= pageX && mouseX <= pageX + pageW && mouseY >= pageY && mouseY <= pageY + pageH) {
            contractScrollOffset -= (int) (scrollY * 10);
            contractScrollOffset = Math.clamp(contractScrollOffset, 0, maxContractScrollOffset);
            return true;
        }
        return super.mouseScrolled(
                mouseX,
                mouseY,
                scrollX,
                scrollY
        );
    }
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        //Term Buttons

        int panelTop = getTermPanelTop();
        int panelBottom = getTermPanelBottom();
        if (mouseX >= MARGIN && mouseX <= MARGIN + LEFT_WIDTH && mouseY >= panelTop && mouseY <= panelBottom) {
            for (Button termButton : termButtons) {
                int buttonY = termButton.getY();
                if (buttonY + TERM_BUTTON_HEIGHT < panelTop || buttonY > panelBottom) continue;
                if (termButton.isMouseOver(mouseX, mouseY)) return termButton.mouseClicked(mouseX, mouseY, mouseButton);
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private String renderSentence() {
        StringBuilder sb = new StringBuilder();
        String lastWord = null;
        for (BuilderToken t : tokens) {
            String word = switch (t.kind) {
                case PARTY_A -> "Party A";
                case PARTY_B -> "Party B";
                case GIVES -> "gives";
                case GETS -> "gets";
                case FOR -> "for";
                case FROM -> "from";
                case RETURNS -> "returns";
                case TO -> "to";
                case KILLS -> "kills";
                case VICTIM -> t.value.isEmpty() ? "[victim]" : t.value;
                case BIND_LIFE_DEATH -> "binds life and death with";
                case SOUL -> null;
                case RESOURCE -> t.value.isEmpty() ? "[resource]" : t.value;
                case AMOUNT -> t.value.isEmpty() ? "[amount]" : t.value;
                case TIME_UNIT -> t.value;
                case UNTIL -> "until";
                case PASSES -> "passes";
                case DIGEST -> "digest";
                case ENTITY_TYPE -> t.value.isEmpty() ? "[entity type]" : t.value;
            };

            if (t.kind == ContractTermKind.SOUL) {
                if (lastWord != null) {
                    int idx = sb.lastIndexOf(lastWord);
                    if (idx >= 0) {
                        sb.replace(idx, idx + lastWord.length(), lastWord + "'s soul");
                    }
                } else {
                    sb.append(sb.isEmpty() ? "" : " ");
                    sb.append("[invalid: bare soul]");
                }
                continue;
            }
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(word);
            lastWord = word;
        }
        return sb.toString();
    }
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Intentionally empty.
    }
}
