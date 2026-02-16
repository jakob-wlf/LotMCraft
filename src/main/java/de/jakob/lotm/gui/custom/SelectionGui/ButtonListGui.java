package de.jakob.lotm.gui.custom.SelectionGui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public abstract class ButtonListGui<T> extends Screen {
    protected int panelWidth = 220;
    protected int panelHeight = 200;
    protected int rowHeight = 24;

    protected final List<T> items;
    private ButtonList list;
    private EditBox search;

    public ButtonListGui(Component title, List<T> items) {
        super(title);
        this.items = items;
    }

    @Override
    protected void init() {
        int x = (width - panelWidth) / 2;
        int y = (height - panelHeight) / 2;

        // search box
        this.search = new EditBox(font, x, y, panelWidth, 20, Component.empty());
        this.search.setResponder(s -> updateList());
        this.search.setHint(Component.literal("Search..."));
        addRenderableWidget(search);
        setInitialFocus(search);

        // button list
        this.list = new ButtonList(minecraft, panelWidth, panelHeight, y + 25, rowHeight);
        this.list.setX(x);
        updateList();
        addRenderableWidget(list);

    }

    protected void updateList() {
        if (search.getValue().isEmpty()) {
            list.update(items);
        } else {
            String query = search.getValue().toLowerCase();
            List<T> filtered = items.stream()
                    .filter(item -> getItemName(item).getString().toLowerCase().contains(query))
                    .toList();
            list.update(filtered);
        }
    }

    protected abstract Component getItemName(T item);

    protected abstract void onItemSelected(T item);

    private class ButtonList extends AbstractSelectionList<ButtonList.Entry> {
        public ButtonList(Minecraft mc, int w, int h, int top, int rowHeight) {
            super(mc, w, h, top, rowHeight);
        }

        @Override
        public int getRowWidth() {
            return width - 12;
        }

        @Override
        public void updateWidgetNarration(NarrationElementOutput output) {
            output.add(NarratedElementType.TITLE, Component.literal("Selection List"));
        }

        public void update(List<T> items) {
            clearEntries();
            items.forEach(item -> addEntry(new Entry(item)));
        }

        private class Entry extends AbstractSelectionList.Entry<Entry> {
            private final Button btn;

            Entry(T item) {
                this.btn = Button.builder(getItemName(item).copy().withStyle(ChatFormatting.AQUA), b -> onItemSelected(item))
                        .size(panelWidth - 30, rowHeight - 4)
                        .build();
            }

            @Override
            public void render(GuiGraphics g, int index, int top, int left, int width, int height,
                               int mouseX, int mouseY, boolean hover, float delta) {
                btn.setX(left + (width - btn.getWidth()) / 2);
                btn.setY(top + (height - btn.getHeight()) / 2);
                btn.render(g, mouseX, mouseY, delta);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                return btn.mouseClicked(mouseX, mouseY, button);
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}