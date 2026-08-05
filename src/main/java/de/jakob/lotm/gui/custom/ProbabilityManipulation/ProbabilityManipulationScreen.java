package de.jakob.lotm.gui.custom.ProbabilityManipulation;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.sefirah.ProbabilityManipulationManager;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.OpenProbabilityManipulationPacket;
import de.jakob.lotm.network.packets.toServer.UpdateProbabilityManipulationPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class ProbabilityManipulationScreen extends Screen {
    private static final int panelWidth = 420;
    private static final int panelHeight = 286;
    private static final int rowHeight = 24;
    private static final int visibleRows = 8;

    private final Map<String, Integer> rules;
    private final int maximumAbilities;
    private final int minimumChance;
    private final int maximumChance;
    private final List<Ability> allAbilities;
    private List<Ability> filteredAbilities;
    private EditBox searchBox;
    private int scrollOffset;

    public ProbabilityManipulationScreen(OpenProbabilityManipulationPacket packet) {
        super(Component.literal("Probability Manipulation"));
        rules = new HashMap<>(packet.rules());
        maximumAbilities = packet.maximumAbilities();
        minimumChance = packet.minimumChance();
        maximumChance = packet.maximumChance();
        allAbilities = LOTMCraft.abilityHandler.getAbilities().stream()
            .filter(ability -> ProbabilityManipulationManager.isEligibleAbility(
                ability, packet.ownerSequence()))
                .sorted(Comparator.comparing(ability -> ability.getName().getString()))
                .toList();
        filteredAbilities = allAbilities;
    }

    @Override
    protected void init() {
        int left = (width - panelWidth) / 2;
        int top = (height - panelHeight) / 2;
        searchBox = new EditBox(font, left + 14, top + 48, panelWidth - 28, 18,
                Component.literal("Search abilities"));
        searchBox.setHint(Component.literal("Search abilities...").withStyle(ChatFormatting.DARK_GRAY));
        searchBox.setResponder(this::filterAbilities);
        addRenderableWidget(searchBox);
    }

    private void filterAbilities(String query) {
        String normalized = query.toLowerCase(Locale.ROOT).trim();
        filteredAbilities = normalized.isEmpty() ? allAbilities : allAbilities.stream()
                .filter(ability -> ability.getName().getString().toLowerCase(Locale.ROOT).contains(normalized)
                        || ability.getId().toLowerCase(Locale.ROOT).contains(normalized))
                .toList();
        scrollOffset = 0;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xB0000000);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        int left = (width - panelWidth) / 2;
        int top = (height - panelHeight) / 2;
        graphics.fill(left - 1, top - 1, left + panelWidth + 1, top + panelHeight + 1, 0xFFD0AD45);
        graphics.fill(left, top, left + panelWidth, top + panelHeight, 0xF0121117);
        graphics.drawCenteredString(font, title.copy().withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                left + panelWidth / 2, top + 10, 0xFFFFFFFF);
        String limits = rules.size() + "/" + maximumAbilities + " abilities | "
                + minimumChance + "-" + maximumChance + "% failure chance";
        graphics.drawCenteredString(font, limits, left + panelWidth / 2, top + 27, 0xFFBDB7C5);

        int listTop = top + 74;
        int end = Math.min(filteredAbilities.size(), scrollOffset + visibleRows);
        for (int index = scrollOffset; index < end; index++) {
            int rowTop = listTop + (index - scrollOffset) * rowHeight;
            Ability ability = filteredAbilities.get(index);
            boolean selected = rules.containsKey(ability.getId());
            boolean hovered = inside(mouseX, mouseY, left + 12, rowTop, panelWidth - 24, rowHeight - 3);
            graphics.fill(left + 12, rowTop, left + panelWidth - 12, rowTop + rowHeight - 3,
                    hovered ? 0xFF393340 : selected ? 0xFF3D3217 : 0xFF211E27);
            graphics.renderOutline(left + 18, rowTop + 5, 11, 11, selected ? 0xFFFFD45A : 0xFF77727D);
            if (selected) graphics.fill(left + 21, rowTop + 8, left + 27, rowTop + 14, 0xFFFFD45A);
            graphics.drawString(font, trim(ability.getName().getString(), 245), left + 36, rowTop + 7,
                    selected ? 0xFFFFE9A8 : 0xFFE0DCE5, false);
            if (selected) {
                int chance = rules.get(ability.getId());
                graphics.drawCenteredString(font, "-", left + 322, rowTop + 7, 0xFFFFFFFF);
                graphics.drawCenteredString(font, chance + "%", left + 354, rowTop + 7, 0xFFFFD45A);
                graphics.drawCenteredString(font, "+", left + 389, rowTop + 7, 0xFFFFFFFF);
            }
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int left = (width - panelWidth) / 2;
        int listTop = (height - panelHeight) / 2 + 74;
        if (button == 0 && inside(mouseX, mouseY, left + 12, listTop,
                panelWidth - 24, visibleRows * rowHeight)) {
            int visibleIndex = (int) ((mouseY - listTop) / rowHeight);
            int index = scrollOffset + visibleIndex;
            if (index < filteredAbilities.size()
                    && mouseY < listTop + visibleIndex * rowHeight + rowHeight - 3) {
                Ability ability = filteredAbilities.get(index);
                String abilityId = ability.getId();
                if (rules.containsKey(abilityId) && mouseX >= left + 306) {
                    int change = mouseX < left + 338 ? -1 : mouseX >= left + 372 ? 1 : 0;
                    if (change != 0) setChance(abilityId, rules.get(abilityId) + change);
                } else if (rules.containsKey(abilityId)) {
                    rules.remove(abilityId);
                    sendUpdate(abilityId, 0);
                } else if (rules.size() < maximumAbilities) {
                    rules.put(abilityId, minimumChance);
                    sendUpdate(abilityId, minimumChance);
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void setChance(String abilityId, int chance) {
        int clamped = Math.clamp(chance, minimumChance, maximumChance);
        rules.put(abilityId, clamped);
        sendUpdate(abilityId, clamped);
    }

    private void sendUpdate(String abilityId, int chance) {
        PacketHandler.sendToServer(new UpdateProbabilityManipulationPacket(abilityId, chance));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scrollOffset = Math.max(0, Math.min(Math.max(0, filteredAbilities.size() - visibleRows),
                scrollOffset - (int) Math.signum(scrollY)));
        return true;
    }

    private boolean inside(double mouseX, double mouseY, int x, int y, int areaWidth, int areaHeight) {
        return mouseX >= x && mouseX < x + areaWidth && mouseY >= y && mouseY < y + areaHeight;
    }

    private String trim(String value, int maximumWidth) {
        return font.width(value) <= maximumWidth ? value
                : font.plainSubstrByWidth(value, maximumWidth - 8) + "...";
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}