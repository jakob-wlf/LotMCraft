package de.jakob.lotm.gui.custom.Introspect;

import com.mojang.blaze3d.systems.RenderSystem;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.network.packets.toServer.*;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.ClientBeyonderCache;
import de.jakob.lotm.util.ClientSacrificeCache;
import de.jakob.lotm.util.ClientQuestData;
import de.jakob.lotm.util.ClientUniquenessCache;
import de.jakob.lotm.util.data.ClientData;
import de.jakob.lotm.util.helper.ClientTeamData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class IntrospectScreen extends AbstractContainerScreen<IntrospectMenu> {
    private ResourceLocation containerBackground;
    private Inventory playerInventory;

    private boolean showAbilities = false;
    private boolean showAllAbilities = false;
    private boolean showSubAbilities = false;
    private Button toggleAbilitiesButton;
    private Button toggleAllAbilitiesButton;
    private Button toggleSubAbilitiesButton;
    private Button clearWheelButton;
    private Button messageButton;
    private Button clearBarButton;

    private Button apotheosisButton = null;

    private boolean showQuests = false;
    private Button toggleQuestsButton;
    private Button discardQuestButton;

    private enum Tab {
        ABILITY_WHEEL,
        ABILITY_BAR,
        SHARED_ABILITIES
    }

    private Tab currentTab = Tab.ABILITY_WHEEL;
    private Button wheelTabButton;
    private Button barTabButton;

    private final List<Ability> availableAbilities = new ArrayList<>();
    private final List<SubAbilityEntry> subAbilityEntries = new ArrayList<>();
    private final List<Ability> abilityWheelSlots = new ArrayList<>();
    private final List<Integer> abilityWheelSubIndexes = new ArrayList<>();
    private final List<Ability> abilityBarSlots = new ArrayList<>();
    private final List<Integer> abilityBarSubIndexes = new ArrayList<>();
    private final List<String> sharedWheelSlots = new ArrayList<>();
    private int draggedFromSharedWheelIndex = -1;
    private int draggedFromSharedPoolIndex = -1;
    private Ability draggedAbility = null;
    private int draggedSubIndex = -1;
    private int draggedFromWheelIndex = -1;
    private int draggedFromBarIndex = -1;
    private boolean draggedFromAvailable = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;

    private static final int ABILITIES_PANEL_WIDTH = 120;
    private static final int ABILITIES_PANEL_HEIGHT = 115;
    private static final int ABILITY_WHEEL_HEIGHT = 100;
    private static final int ABILITY_BAR_HEIGHT = 60;
    private static final int ABILITY_ICON_SIZE = 16;
    private static final int ABILITY_WHEEL_MAX = 24;
    private static final int ABILITY_BAR_MAX = 6;
    private static final int SHARED_POOL_HEIGHT = 50;
    private static final int SHARED_WHEEL_HEIGHT = 60;

    private static final int QUESTS_PANEL_WIDTH = 140;
    private static final int COMPLETED_QUESTS_HEIGHT = 80;
    private static final int ACTIVE_QUEST_HEIGHT = 120;
    private static final int QUEST_ITEM_SIZE = 16;

    private static final String[] KEYBIND_LABELS = {"1", "2", "3", "4", "5", "6"};

    private int abilitiesScrollOffset = 0;
    private int maxAbilitiesScroll = 0;
    private int completedQuestsScrollOffset = 0;
    private int maxCompletedQuestsScroll = 0;
    private int sharedPoolScrollOffset = 0;
    private int maxSharedPoolScroll = 0;

    private record SubAbilityEntry(Ability parent, int subIndex) {}

    public IntrospectScreen(IntrospectMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.playerInventory = playerInventory;
        this.containerBackground = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/gui/introspect.png");
        this.imageHeight = 231;
        this.imageWidth = 192;
    }

    private void initializeAbilities() {
        availableAbilities.clear();
        subAbilityEntries.clear();
        abilitiesScrollOffset = 0;

        if (showAllAbilities) {
            availableAbilities.addAll(LOTMCraft.abilityHandler.getAllAbilitiesUpToSequenceOrdered(menu.getSequence()));
        } else {
            String[] pathwayHistory = ClientBeyonderCache.getPathwayHistory(minecraft.player.getUUID());
            for (int i = menu.getSequence(); i < pathwayHistory.length; i++) {
                String pathway = pathwayHistory[i];
                if (pathway != null) {
                    ArrayList<Ability> pathwayAbilities = LOTMCraft.abilityHandler.getByPathwayAndSequenceExactOrdered(pathway, i);
                    availableAbilities.addAll(pathwayAbilities);
                }
            }
        }

        List<Ability> unique = availableAbilities.stream().distinct().toList();
        availableAbilities.clear();
        availableAbilities.addAll(unique);
        availableAbilities.removeIf(Ability::getShouldBeHidden);

        if (showSubAbilities) {
            List<Ability> expanded = new ArrayList<>();
            for (Ability ability : availableAbilities) {
                expanded.add(ability);
                subAbilityEntries.add(null);
                if (ability instanceof SelectableAbility sa) {
                    String[] names = sa.getAbilityNamesCopy();
                    if (names.length > 1) {
                        for (int si = 0; si < names.length; si++) {
                            expanded.add(ability);
                            subAbilityEntries.add(new SubAbilityEntry(ability, si));
                        }
                    }
                }
            }
            availableAbilities.clear();
            availableAbilities.addAll(expanded);
        } else {
            for (int i = 0; i < availableAbilities.size(); i++) {
                subAbilityEntries.add(null);
            }
        }

        int iconsPerRow = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);
        int rows = (int) Math.ceil((double) availableAbilities.size() / iconsPerRow);
        int visibleRows = (ABILITIES_PANEL_HEIGHT - 20) / (ABILITY_ICON_SIZE + 2);
        maxAbilitiesScroll = Math.max(0, rows - visibleRows);
    }

    private void updateCompletedQuestsScroll() {
        int questsCount = ClientQuestData.getCompletedQuests().size();
        int lineHeight = this.font.lineHeight + 2;
        int visibleLines = (COMPLETED_QUESTS_HEIGHT - 20) / lineHeight;
        maxCompletedQuestsScroll = Math.max(0, questsCount - visibleLines);
    }

    public void setAbilityWheelSlots(ArrayList<String> abilityIds) {
        this.abilityWheelSlots.clear();
        this.abilityWheelSubIndexes.clear();
        for (String id : abilityIds) {
            int colonIdx = id.lastIndexOf(':');
            int subIdx = -1;
            String baseId = id;
            if (colonIdx >= 0) {
                try {
                    subIdx = Integer.parseInt(id.substring(colonIdx + 1));
                    baseId = id.substring(0, colonIdx);
                } catch (NumberFormatException ignored) {}
            }
            Ability ability = LOTMCraft.abilityHandler.getById(baseId);
            if (ability != null) {
                this.abilityWheelSlots.add(ability);
                this.abilityWheelSubIndexes.add(subIdx);
            }
        }
    }

    public void setAbilityBarSlots(ArrayList<String> abilityIds) {
        this.abilityBarSlots.clear();
        this.abilityBarSubIndexes.clear();
        for (String id : abilityIds) {
            int colonIdx = id.lastIndexOf(':');
            int subIdx = -1;
            String baseId = id;
            if (colonIdx >= 0) {
                try {
                    subIdx = Integer.parseInt(id.substring(colonIdx + 1));
                    baseId = id.substring(0, colonIdx);
                } catch (NumberFormatException ignored) {}
            }
            Ability ability = LOTMCraft.abilityHandler.getById(baseId);
            if (ability != null) {
                this.abilityBarSlots.add(ability);
                this.abilityBarSubIndexes.add(subIdx);
            }
        }
    }

    private String buildEffectiveId(Ability ability, int subIndex) {
        if (subIndex >= 0) return ability.getId() + ":" + subIndex;
        return ability.getId();
    }

    private ArrayList<String> wheelSlotsToIdList() {
        ArrayList<String> ids = new ArrayList<>();
        for (int i = 0; i < abilityWheelSlots.size(); i++) {
            ids.add(buildEffectiveId(abilityWheelSlots.get(i), abilityWheelSubIndexes.get(i)));
        }
        return ids;
    }

    private ArrayList<String> barSlotsToIdList() {
        ArrayList<String> ids = new ArrayList<>();
        for (int i = 0; i < abilityBarSlots.size(); i++) {
            ids.add(buildEffectiveId(abilityBarSlots.get(i), abilityBarSubIndexes.get(i)));
        }
        return ids;
    }

    @Override
    protected void init() {
        super.init();

        if (this.minecraft == null) return;

        this.killCount = ClientSacrificeCache.getKillCount();

        PacketHandler.sendToServer(new RequestAbilityBarPacket());
        PacketHandler.sendToServer(new RequestQuestDataPacket());
        PacketHandler.sendToServer(new RequestSharedAbilitiesPacket());

        sharedWheelSlots.clear();
        sharedWheelSlots.addAll(ClientData.getSharedWheelAbilities());

        KEYBIND_LABELS[0] = LOTMCraft.useAbilityBarAbility1.getKey().getDisplayName().getString();
        KEYBIND_LABELS[1] = LOTMCraft.useAbilityBarAbility2.getKey().getDisplayName().getString();
        KEYBIND_LABELS[2] = LOTMCraft.useAbilityBarAbility3.getKey().getDisplayName().getString();
        KEYBIND_LABELS[3] = LOTMCraft.useAbilityBarAbility4.getKey().getDisplayName().getString();
        KEYBIND_LABELS[4] = LOTMCraft.useAbilityBarAbility5.getKey().getDisplayName().getString();
        KEYBIND_LABELS[5] = LOTMCraft.useAbilityBarAbility6.getKey().getDisplayName().getString();

        updateCompletedQuestsScroll();
        updateButtonPositions();
    }

    private String abbreviateKeybind(String keybind) {
        if (keybind.startsWith("Button ")) return "B" + keybind.substring(7);
        if (keybind.equalsIgnoreCase("Not Bound") || keybind.equalsIgnoreCase("Unbound")) return "-";
        if (keybind.equalsIgnoreCase("Middle Mouse Button")) return "MMB";
        if (keybind.equalsIgnoreCase("Left Mouse Button")) return "LMB";
        if (keybind.equalsIgnoreCase("Right Mouse Button")) return "RMB";
        if (keybind.length() > 5) return keybind.substring(0, 4) + "…";
        return keybind;
    }

    private void updateButtonPositions() {
        this.clearWidgets();

        int baseLeftPos = this.leftPos;

        int abilitiesButtonX = baseLeftPos - 65;
        int abilitiesButtonY = this.topPos + 10;

        toggleAbilitiesButton = Button.builder(Component.literal(showAbilities ? "< Hide" : "Abilities >"),
                        button -> {
                            showAbilities = !showAbilities;
                            if (showAbilities) showQuests = false;
                            button.setMessage(Component.literal(showAbilities ? "< Hide" : "Abilities >"));
                            updateButtonPositions();
                        })
                .bounds(abilitiesButtonX, abilitiesButtonY, 60, 20)
                .build();
        this.addRenderableWidget(toggleAbilitiesButton);

        int questsButtonX = baseLeftPos - 65;
        int questsButtonY = this.topPos + 35;

        toggleQuestsButton = Button.builder(Component.literal(showQuests ? "< Hide" : "Quests >"),
                        button -> {
                            showQuests = !showQuests;
                            if (showQuests) showAbilities = false;
                            button.setMessage(Component.literal(showQuests ? "< Hide" : "Quests >"));
                            updateButtonPositions();
                        })
                .bounds(questsButtonX, questsButtonY, 60, 20)
                .build();
        this.addRenderableWidget(toggleQuestsButton);

        int messageButtonX = baseLeftPos - 65;
        int messageButtonY = this.topPos + 60;

        messageButton = Button.builder(Component.literal("Honorific"),
                        button -> openHonorificNamesMenu())
                .bounds(messageButtonX, messageButtonY, 60, 20)
                .build();

        if (menu.getSequence() < 4) {
            this.addRenderableWidget(messageButton);
        }

        if (ClientUniquenessCache.hasUniqueness() && menu.getSequence() == 1) {
            int apotheosisButtonX = baseLeftPos - 65;
            int apotheosisButtonY = this.topPos + 110;

            boolean canApotheosize = false;
            if (this.minecraft != null && this.minecraft.player != null) {
                int charStack = ClientBeyonderCache.getCharStack(this.minecraft.player.getUUID());
                canApotheosize = ClientUniquenessCache.getKillCount() >= RequestUniquenessApotheosisPacket.KILLS_REQUIRED_FOR_APOTHEOSIS && charStack >= 2;
            }
            final boolean finalCanApotheosize = canApotheosize;

            apotheosisButton = Button.builder(
                            Component.literal("Apotheosis").withStyle(finalCanApotheosize ? ChatFormatting.GOLD : ChatFormatting.GRAY),
                            button -> {
                                if (finalCanApotheosize) PacketHandler.sendToServer(new RequestUniquenessApotheosisPacket());
                            })
                    .bounds(apotheosisButtonX, apotheosisButtonY, 60, 20)
                    .build();
            apotheosisButton.active = finalCanApotheosize;
            this.addRenderableWidget(apotheosisButton);
        }

        if (isCreativeOp()) {
            int allAbilitiesButtonX = baseLeftPos - 65;
            int allAbilitiesButtonY = this.topPos + 85;

            toggleAllAbilitiesButton = Button.builder(
                            Component.literal(showAllAbilities ? "All: ON" : "All: OFF")
                                    .withStyle(showAllAbilities ? ChatFormatting.GREEN : ChatFormatting.RED),
                            button -> {
                                showAllAbilities = !showAllAbilities;
                                initializeAbilities();
                                button.setMessage(Component.literal(showAllAbilities ? "All: ON" : "All: OFF")
                                        .withStyle(showAllAbilities ? ChatFormatting.GREEN : ChatFormatting.RED));
                            })
                    .bounds(allAbilitiesButtonX, allAbilitiesButtonY, 60, 20)
                    .build();
            this.addRenderableWidget(toggleAllAbilitiesButton);
        }

        if (showAbilities) {
            addAbilityButtons(baseLeftPos);
        }

        if (showQuests) {
            addQuestButtons(baseLeftPos);
        }
    }

    private void addAbilityButtons(int baseLeftPos) {
        int panelX = baseLeftPos + this.imageWidth + 5;
        int tabButtonY = this.topPos;

        boolean showSharedTab = ClientTeamData.hasTeam();
        int tabCount = showSharedTab ? 3 : 2;
        int subButtonWidth = 50;
        int remainingWidth = ABILITIES_PANEL_WIDTH - subButtonWidth;
        int tabButtonWidth = remainingWidth / tabCount;

        wheelTabButton = Button.builder(Component.literal("Wheel"),
                        button -> {
                            currentTab = Tab.ABILITY_WHEEL;
                            updateButtonPositions();
                        })
                .bounds(panelX, tabButtonY, tabButtonWidth, 15)
                .build();

        barTabButton = Button.builder(Component.literal("Bar"),
                        button -> {
                            currentTab = Tab.ABILITY_BAR;
                            updateButtonPositions();
                        })
                .bounds(panelX + tabButtonWidth, tabButtonY, tabButtonWidth, 15)
                .build();

        this.addRenderableWidget(wheelTabButton);
        this.addRenderableWidget(barTabButton);

        if (showSharedTab) {
            Button sharedTabButton = Button.builder(Component.literal("Shared"),
                            button -> {
                                currentTab = Tab.SHARED_ABILITIES;
                                sharedPoolScrollOffset = 0;
                                updateButtonPositions();
                            })
                    .bounds(panelX + tabButtonWidth * 2, tabButtonY, tabButtonWidth, 15)
                    .build();
            this.addRenderableWidget(sharedTabButton);
            if (currentTab == Tab.SHARED_ABILITIES) sharedTabButton.active = false;
        } else if (currentTab == Tab.SHARED_ABILITIES) {
            currentTab = Tab.ABILITY_WHEEL;
        }

        if (currentTab == Tab.ABILITY_WHEEL) {
            wheelTabButton.active = false;
        } else if (currentTab == Tab.ABILITY_BAR) {
            barTabButton.active = false;
        }

        toggleSubAbilitiesButton = Button.builder(
                        Component.literal(showSubAbilities ? "Sub: ON" : "Sub: OFF")
                                .withStyle(showSubAbilities ? ChatFormatting.AQUA : ChatFormatting.GRAY),
                        button -> {
                            showSubAbilities = !showSubAbilities;
                            initializeAbilities();
                            button.setMessage(Component.literal(showSubAbilities ? "Sub: ON" : "Sub: OFF")
                                    .withStyle(showSubAbilities ? ChatFormatting.AQUA : ChatFormatting.GRAY));
                        })
                .bounds(panelX + ABILITIES_PANEL_WIDTH - subButtonWidth, tabButtonY, subButtonWidth, 15)
                .build();
        this.addRenderableWidget(toggleSubAbilitiesButton);

        int clearButtonX = baseLeftPos + this.imageWidth + 5;
        int clearButtonY;

        if (currentTab == Tab.ABILITY_WHEEL) {
            clearButtonY = this.topPos + 15 + ABILITIES_PANEL_HEIGHT + 5 + ABILITY_WHEEL_HEIGHT + 5;
            clearWheelButton = Button.builder(Component.literal("Clear Wheel").withStyle(ChatFormatting.RED),
                            button -> {
                                abilityWheelSlots.clear();
                                abilityWheelSubIndexes.clear();
                                PacketHandler.sendToServer(new SyncAbilityWheelAbilitiesPacket(new ArrayList<>()));
                            })
                    .bounds(clearButtonX, clearButtonY, ABILITIES_PANEL_WIDTH, 20)
                    .build();
            this.addRenderableWidget(clearWheelButton);
        } else if (currentTab == Tab.ABILITY_BAR) {
            clearButtonY = this.topPos + 15 + ABILITIES_PANEL_HEIGHT + 5 + ABILITY_BAR_HEIGHT + 5;
            clearBarButton = Button.builder(Component.literal("Clear Bar").withStyle(ChatFormatting.RED),
                            button -> {
                                abilityBarSlots.clear();
                                abilityBarSubIndexes.clear();
                                PacketHandler.sendToServer(new SyncAbilityBarAbilitiesPacket(new ArrayList<>()));
                            })
                    .bounds(clearButtonX, clearButtonY, ABILITIES_PANEL_WIDTH, 20)
                    .build();
            this.addRenderableWidget(clearBarButton);
        }
    }

    private void addQuestButtons(int baseLeftPos) {
        int panelX = baseLeftPos + this.imageWidth + 5;

        if (ClientQuestData.hasActiveQuest()) {
            int discardButtonY = this.topPos + COMPLETED_QUESTS_HEIGHT + 5 + ACTIVE_QUEST_HEIGHT + 5;

            discardQuestButton = Button.builder(Component.literal("Discard Quest").withStyle(ChatFormatting.RED),
                            button -> {
                                PacketHandler.sendToServer(new DiscardQuestPacket());
                                PacketHandler.sendToServer(new RequestQuestDataPacket());
                            })
                    .bounds(panelX, discardButtonY, QUESTS_PANEL_WIDTH, 20)
                    .build();
            this.addRenderableWidget(discardQuestButton);
        }
    }

    private void openHonorificNamesMenu() {
        if (menu.getSequence() >= 4) return;
        PacketHandler.sendToServer(new OpenHonorificNamesMenuPacket());
    }

    private boolean isCreativeOp() {
        return this.minecraft != null && this.minecraft.player != null
                && this.minecraft.player.isCreative()
                && this.minecraft.player.hasPermissions(2);
    }

    private int killCount = 0;

    public void updateKillCount(int killCount) {
        this.killCount = killCount;
    }

    public void updateMenuData(int sequence, String pathway, float digestionProgress, float sanity) {
        this.menu.updateData(sequence, pathway, digestionProgress, sanity);
        initializeAbilities();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (showQuests) {
            renderQuestPanel(guiGraphics, mouseX, mouseY);
        }

        if (showAbilities) {
            renderAbilitiesPanel(guiGraphics, mouseX, mouseY);
        }

        if (draggedAbility != null) {
            renderAbilityIcon(guiGraphics, draggedAbility, mouseX - dragOffsetX, mouseY - dragOffsetY, draggedSubIndex);
        }

        this.renderTooltip(guiGraphics, mouseX, mouseY);

        if (showAbilities && draggedAbility == null) {
            renderAbilityTooltips(guiGraphics, mouseX, mouseY);
        }

        if (showQuests) {
            renderQuestItemTooltips(guiGraphics, mouseX, mouseY);
        }
    }

    private void renderQuestPanel(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int baseLeftPos = this.leftPos;
        int panelX = baseLeftPos + this.imageWidth + 5;
        int panelY = this.topPos;

        renderCompletedQuestsSection(guiGraphics, panelX, panelY, mouseX, mouseY);

        int activeQuestY = panelY + COMPLETED_QUESTS_HEIGHT + 5;
        renderActiveQuestSection(guiGraphics, panelX, activeQuestY, mouseX, mouseY);
    }

    private void renderCompletedQuestsSection(GuiGraphics guiGraphics, int panelX, int panelY, int mouseX, int mouseY) {
        guiGraphics.fill(panelX, panelY, panelX + QUESTS_PANEL_WIDTH, panelY + COMPLETED_QUESTS_HEIGHT, 0xCC000000);
        guiGraphics.renderOutline(panelX, panelY, QUESTS_PANEL_WIDTH, COMPLETED_QUESTS_HEIGHT, 0xFFAAAAAA);

        Component label = Component.literal("Completed Quests").withStyle(ChatFormatting.BOLD);
        guiGraphics.drawString(this.font, label, panelX + 5, panelY + 5, 0xFFFFFFFF, true);

        int listY = panelY + 15;
        int listHeight = COMPLETED_QUESTS_HEIGHT - 20;

        List<String> completedQuests = new ArrayList<>(ClientQuestData.getCompletedQuests());
        int lineHeight = this.font.lineHeight + 2;

        int startIndex = completedQuestsScrollOffset;
        int endIndex = Math.min(completedQuests.size(), startIndex + (listHeight / lineHeight));

        for (int i = startIndex; i < endIndex; i++) {
            String questId = completedQuests.get(i);
            Component questName = Component.translatable("lotm.quest.impl." + questId);
            if (questName.getString().length() > 24) {
                questName = Component.literal(questName.getString().substring(0, 21).strip() + "…");
            }
            int textY = listY + (i - startIndex) * lineHeight;
            guiGraphics.drawString(this.font, "✓", panelX + 5, textY, 0xFF4CAF50, false);
            guiGraphics.drawString(this.font, questName, panelX + 15, textY, 0xFFCCCCCC, false);
        }

        if (maxCompletedQuestsScroll > 0) {
            Component scrollHint = Component.literal("(Scroll)").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
            int hintWidth = this.font.width(scrollHint);
            guiGraphics.drawString(this.font, scrollHint, panelX + QUESTS_PANEL_WIDTH - hintWidth - 5,
                    panelY + COMPLETED_QUESTS_HEIGHT - 12, 0xFF888888, false);
        }
    }

    private void renderActiveQuestSection(GuiGraphics guiGraphics, int panelX, int panelY, int mouseX, int mouseY) {
        guiGraphics.fill(panelX, panelY, panelX + QUESTS_PANEL_WIDTH, panelY + ACTIVE_QUEST_HEIGHT, 0xCC000000);
        guiGraphics.renderOutline(panelX, panelY, QUESTS_PANEL_WIDTH, ACTIVE_QUEST_HEIGHT, 0xFFAAAAAA);

        Component label = Component.literal("Active Quest").withStyle(ChatFormatting.BOLD);
        guiGraphics.drawString(this.font, label, panelX + 5, panelY + 5, 0xFFFFFFFF, true);

        if (!ClientQuestData.hasActiveQuest()) {
            Component noQuest = Component.literal("No active quest").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
            int textWidth = this.font.width(noQuest);
            guiGraphics.drawString(this.font, noQuest, panelX + (QUESTS_PANEL_WIDTH - textWidth) / 2,
                    panelY + 40, 0xFF888888, false);
            return;
        }

        int contentY = panelY + 15;

        Component questName = Component.literal(ClientQuestData.getActiveQuestName())
                .withStyle(ChatFormatting.BOLD)
                .withColor(BeyonderData.pathwayInfos.get(menu.getPathway()).color());

        if (questName.getString().length() > 24) {
            questName = Component.literal(questName.getString().substring(0, 21).strip() + "…")
                    .withStyle(ChatFormatting.BOLD)
                    .withColor(BeyonderData.pathwayInfos.get(menu.getPathway()).color());
        }

        guiGraphics.drawString(this.font, questName, panelX + 5, contentY, 0xFFFFFFFF, false);
        contentY += this.font.lineHeight + 3;

        String description = ClientQuestData.getActiveQuestDescription();
        List<String> wrappedDesc = wrapText(description, QUESTS_PANEL_WIDTH - 10);
        for (String line : wrappedDesc) {
            guiGraphics.drawString(this.font, line, panelX + 5, contentY, 0xFFCCCCCC, false);
            contentY += this.font.lineHeight;
        }
        contentY += 3;

        float progress = ClientQuestData.getActiveQuestProgress();
        int barWidth = QUESTS_PANEL_WIDTH - 10;
        int barHeight = 10;

        guiGraphics.fill(panelX + 5, contentY, panelX + 5 + barWidth, contentY + barHeight, 0xFF333333);
        int progressWidth = (int) (barWidth * progress);
        guiGraphics.fillGradient(panelX + 5, contentY, panelX + 5 + progressWidth, contentY + barHeight,
                0xFF2196F3, 0xFF1976D2);
        guiGraphics.renderOutline(panelX + 5, contentY, barWidth, barHeight, 0xFF666666);

        Component progressText = Component.literal((int) (progress * 100) + "%");
        int progressTextWidth = this.font.width(progressText);
        guiGraphics.drawString(this.font, progressText,
                panelX + 5 + (barWidth - progressTextWidth) / 2, contentY + 1, 0xFFFFFFFF, true);

        contentY += barHeight + 5;

        Component rewardsLabel = Component.literal("Rewards:").withStyle(ChatFormatting.BOLD);
        guiGraphics.drawString(this.font, rewardsLabel, panelX + 5, contentY, 0xFFFFFFFF, false);
        contentY += this.font.lineHeight + 2;

        List<ItemStack> rewards = ClientQuestData.getActiveQuestRewards();
        int rewardX = panelX + 5;
        int rewardY = contentY;

        for (int i = 0; i < rewards.size() && i < 8; i++) {
            ItemStack reward = rewards.get(i);
            int x = rewardX + (i % 4) * (QUEST_ITEM_SIZE + 6);
            int y = rewardY + (i / 4) * (QUEST_ITEM_SIZE + 6);
            guiGraphics.renderItem(reward, x, y);
            guiGraphics.renderItemDecorations(this.font, reward, x, y);
        }

        contentY += (((rewards.size() - 1) / 4) + 1) * (QUEST_ITEM_SIZE + 6) + 2;

        int digestion = ClientQuestData.getActiveQuestDigestionReward();
        if (digestion > 0) {
            Component digestionText = Component.literal("+" + digestion + " Digestion").withStyle(ChatFormatting.GOLD);
            guiGraphics.drawString(this.font, digestionText, panelX + 5, contentY, 0xFFFFAA00, false);
        }
    }

    private void renderQuestItemTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!ClientQuestData.hasActiveQuest()) return;

        int baseLeftPos = this.leftPos;
        int panelX = baseLeftPos + this.imageWidth + 5;
        int panelY = this.topPos + COMPLETED_QUESTS_HEIGHT + 5;

        List<ItemStack> rewards = ClientQuestData.getActiveQuestRewards();
        int rewardX = panelX + 5;
        int rewardY = panelY + 80;

        for (int i = 0; i < rewards.size() && i < 8; i++) {
            ItemStack reward = rewards.get(i);
            int x = rewardX + (i % 4) * (QUEST_ITEM_SIZE + 6);
            int y = rewardY + (i / 4) * (QUEST_ITEM_SIZE + 6);

            if (mouseX >= x && mouseX < x + QUEST_ITEM_SIZE && mouseY >= y && mouseY < y + QUEST_ITEM_SIZE) {
                guiGraphics.renderTooltip(this.font, reward, mouseX, mouseY);
                return;
            }
        }
    }

    private void renderAbilityTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int baseLeftPos = this.leftPos;
        int panelX = baseLeftPos + this.imageWidth + 5;
        int panelY = this.topPos + 15;
        int slotY = panelY + ABILITIES_PANEL_HEIGHT + 5;

        Ability hoveredAbility = null;
        int hoveredSubIndex = -1;

        int availIdx = getAbilityIndexAt(mouseX, mouseY, panelX, panelY, true);
        if (availIdx >= 0) {
            hoveredAbility = availableAbilities.get(availIdx);
            hoveredSubIndex = (subAbilityEntries.size() > availIdx && subAbilityEntries.get(availIdx) != null)
                    ? subAbilityEntries.get(availIdx).subIndex() : -1;
        }

        if (hoveredAbility == null) {
            if (currentTab == Tab.ABILITY_WHEEL) {
                int wheelSlot = getAbilityWheelSlot(mouseX, mouseY, panelX, slotY);
                if (wheelSlot >= 0 && wheelSlot < abilityWheelSlots.size()) {
                    hoveredAbility = abilityWheelSlots.get(wheelSlot);
                    hoveredSubIndex = abilityWheelSubIndexes.get(wheelSlot);
                }
            } else if (currentTab == Tab.SHARED_ABILITIES && ClientTeamData.hasTeam()) {
                int iconsPerRow2 = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);
                List<String> allPooledTooltip = getAllPooledAbilities();
                for (int s = 0; s < allPooledTooltip.size(); s++) {
                    int sRow = s / iconsPerRow2 - sharedPoolScrollOffset;
                    int sx = panelX + 5 + (s % iconsPerRow2) * (ABILITY_ICON_SIZE + 2);
                    int sy = slotY + 14 + sRow * (ABILITY_ICON_SIZE + 2);
                    if (sy < slotY + 14 || sy + ABILITY_ICON_SIZE > slotY + SHARED_POOL_HEIGHT) continue;
                    if (mouseX >= sx && mouseX <= sx + ABILITY_ICON_SIZE && mouseY >= sy && mouseY <= sy + ABILITY_ICON_SIZE) {
                        hoveredAbility = LOTMCraft.abilityHandler.getById(allPooledTooltip.get(s));
                        break;
                    }
                }
                if (hoveredAbility == null) {
                    int wheelY2 = slotY + SHARED_POOL_HEIGHT + 5;
                    int iconsPerRowW2 = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);
                    for (int i = 0; i < sharedWheelSlots.size(); i++) {
                        int wx = panelX + 5 + (i % iconsPerRowW2) * (ABILITY_ICON_SIZE + 2);
                        int wy = wheelY2 + 14 + (i / iconsPerRowW2) * (ABILITY_ICON_SIZE + 2);
                        if (mouseX >= wx && mouseX <= wx + ABILITY_ICON_SIZE && mouseY >= wy && mouseY <= wy + ABILITY_ICON_SIZE) {
                            hoveredAbility = LOTMCraft.abilityHandler.getById(sharedWheelSlots.get(i));
                            break;
                        }
                    }
                }
            } else if (currentTab == Tab.ABILITY_BAR) {
                int barSlot = getAbilityBarSlot(mouseX, mouseY, panelX, slotY);
                if (barSlot >= 0 && barSlot < abilityBarSlots.size()) {
                    hoveredAbility = abilityBarSlots.get(barSlot);
                    hoveredSubIndex = abilityBarSubIndexes.get(barSlot);
                }
            }
        }

        if (hoveredAbility != null) {
            List<Component> tooltipLines = new ArrayList<>();

            if (hoveredSubIndex >= 0 && hoveredAbility instanceof SelectableAbility sa) {
                String[] names = sa.getAbilityNamesCopy();
                if (hoveredSubIndex < names.length) {
                    int color = showAllAbilities ? 0xFFFFFF : BeyonderData.pathwayInfos.get(menu.getPathway()).color();
                    tooltipLines.add(Component.translatable(names[hoveredSubIndex]).withStyle(ChatFormatting.BOLD).withColor(color));
                    tooltipLines.add(Component.literal("(" + hoveredAbility.getNameFormatted(ClientHandler.getPlayer()).getString() + ")").withStyle(ChatFormatting.DARK_GRAY));
                } else {
                    tooltipLines.add(hoveredAbility.getNameFormatted(ClientHandler.getPlayer()));
                }
            } else if (showAllAbilities) {
                tooltipLines.add(hoveredAbility.getNameFormatted(ClientHandler.getPlayer()));
            } else {
                int color = BeyonderData.pathwayInfos.get(menu.getPathway()).color();
                tooltipLines.add(hoveredAbility.getName().withStyle(ChatFormatting.BOLD).withColor(color));
            }

            Component description = hoveredAbility.getDescription();
            if (description != null) {
                String descText = description.getString();
                List<String> wrappedLines = wrapText(descText, 100);
                for (String line : wrappedLines) {
                    tooltipLines.add(Component.literal(line).withStyle(ChatFormatting.DARK_GRAY));
                }
                tooltipLines.add(Component.literal(""));
            }

            int cooldown = hoveredAbility.getCooldown();
            if (cooldown > 0) {
                tooltipLines.add(Component.literal("Cooldown: ").withStyle(ChatFormatting.DARK_GRAY)
                        .append(Component.literal(cooldown / 20 + "s").withStyle(ChatFormatting.BLUE)));
            }

            float spiritualityCost = hoveredAbility.spiritualityCost();
            if (spiritualityCost > 0) {
                tooltipLines.add(Component.literal("Spirituality Cost: ").withStyle(ChatFormatting.DARK_GRAY)
                        .append(Component.literal(spiritualityCost + "").withStyle(ChatFormatting.DARK_PURPLE)));
            }

            guiGraphics.renderTooltip(this.font, tooltipLines, java.util.Optional.empty(), mouseX, mouseY);
        }
    }

    private List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            int lineWidth = this.font.width(testLine);

            if (lineWidth > maxWidth && currentLine.length() > 0) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            } else {
                if (currentLine.length() > 0) currentLine.append(" ");
                currentLine.append(word);
            }
        }

        if (currentLine.length() > 0) lines.add(currentLine.toString());
        return lines;
    }

    private void renderAbilitiesPanel(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int baseLeftPos = this.leftPos;
        int panelX = baseLeftPos + this.imageWidth + 5;
        int panelY = this.topPos + 15;

        guiGraphics.fill(panelX, panelY, panelX + ABILITIES_PANEL_WIDTH, panelY + ABILITIES_PANEL_HEIGHT, 0xCC000000);
        guiGraphics.renderOutline(panelX, panelY, ABILITIES_PANEL_WIDTH, ABILITIES_PANEL_HEIGHT, 0xFFAAAAAA);

        Component abilitiesLabel = Component.literal("Abilities").withStyle(ChatFormatting.BOLD);
        guiGraphics.drawString(this.font, abilitiesLabel, panelX + 5, panelY + 5, 0xFFFFFFFF, true);

        renderAvailableAbilities(guiGraphics, panelX, panelY + 15, mouseX, mouseY);

        int slotY = panelY + ABILITIES_PANEL_HEIGHT + 5;

        if (currentTab == Tab.ABILITY_WHEEL) {
            renderAbilityWheelSection(guiGraphics, panelX, slotY, mouseX, mouseY);
        } else if (currentTab == Tab.ABILITY_BAR) {
            renderAbilityBarSection(guiGraphics, panelX, slotY, mouseX, mouseY);
        } else if (currentTab == Tab.SHARED_ABILITIES) {
            renderSharedAbilitiesTab(guiGraphics, panelX, slotY, mouseX, mouseY);
        }
    }

    private void renderAvailableAbilities(GuiGraphics guiGraphics, int panelX, int panelY, int mouseX, int mouseY) {
        int startX = panelX + 5;
        int startY = panelY - abilitiesScrollOffset * (ABILITY_ICON_SIZE + 2);

        List<Ability> displayedAbilities = currentTab == Tab.SHARED_ABILITIES
                ? buildDisplayedSharedAbilities()
                : availableAbilities;

        List<SubAbilityEntry> displayedEntries = currentTab == Tab.SHARED_ABILITIES
                ? buildDisplayedSharedEntries()
                : subAbilityEntries;

        int iconsPerRow = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);

        for (int i = 0; i < displayedAbilities.size(); i++) {
            int row = i / iconsPerRow;
            int col = i % iconsPerRow;

            int x = startX + col * (ABILITY_ICON_SIZE + 2);
            int y = startY + row * (ABILITY_ICON_SIZE + 2);

            if (y >= panelY && y + ABILITY_ICON_SIZE <= panelY + ABILITIES_PANEL_HEIGHT - 15) {
                Ability ability = displayedAbilities.get(i);
                int si = (displayedEntries.size() > i && displayedEntries.get(i) != null)
                        ? displayedEntries.get(i).subIndex() : -1;
                renderAbilityIcon(guiGraphics, ability, x, y, si);
            }
        }
    }

    private List<Ability> buildDisplayedSharedAbilities() {
        List<Ability> result = new ArrayList<>();
        for (int i = 0; i < availableAbilities.size(); i++) {
            Ability a = availableAbilities.get(i);
            SubAbilityEntry e = subAbilityEntries.size() > i ? subAbilityEntries.get(i) : null;
            if (e != null || a.canBeShared) result.add(a);
        }
        return result;
    }

    private List<SubAbilityEntry> buildDisplayedSharedEntries() {
        List<SubAbilityEntry> result = new ArrayList<>();
        for (int i = 0; i < availableAbilities.size(); i++) {
            Ability a = availableAbilities.get(i);
            SubAbilityEntry e = subAbilityEntries.size() > i ? subAbilityEntries.get(i) : null;
            if (e != null || a.canBeShared) result.add(e);
        }
        return result;
    }

    private void renderAbilityWheelSection(GuiGraphics guiGraphics, int panelX, int wheelY, int mouseX, int mouseY) {
        guiGraphics.fill(panelX, wheelY, panelX + ABILITIES_PANEL_WIDTH, wheelY + ABILITY_WHEEL_HEIGHT, 0xCC000000);
        guiGraphics.renderOutline(panelX, wheelY, ABILITIES_PANEL_WIDTH, ABILITY_WHEEL_HEIGHT, 0xFFAAAAAA);

        Component wheelLabel = Component.literal("Ability Wheel").withStyle(ChatFormatting.BOLD);
        guiGraphics.drawString(this.font, wheelLabel, panelX + 5, wheelY + 5, 0xFFFFFFFF, true);

        renderAbilityWheel(guiGraphics, panelX, wheelY + 15, mouseX, mouseY);
    }

    private void renderAbilityBarSection(GuiGraphics guiGraphics, int panelX, int barY, int mouseX, int mouseY) {
        guiGraphics.fill(panelX, barY, panelX + ABILITIES_PANEL_WIDTH, barY + ABILITY_BAR_HEIGHT, 0xCC000000);
        guiGraphics.renderOutline(panelX, barY, ABILITIES_PANEL_WIDTH, ABILITY_BAR_HEIGHT, 0xFFAAAAAA);

        Component barLabel = Component.literal("Ability Bar").withStyle(ChatFormatting.BOLD);
        guiGraphics.drawString(this.font, barLabel, panelX + 5, barY + 5, 0xFFFFFFFF, true);

        renderAbilityBar(guiGraphics, panelX, barY + 15, mouseX, mouseY);
    }

    private void renderAbilityWheel(GuiGraphics guiGraphics, int panelX, int panelY, int mouseX, int mouseY) {
        int startX = panelX + 5;
        int startY = panelY;
        int iconsPerRow = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);

        for (int i = 0; i < ABILITY_WHEEL_MAX; i++) {
            int row = i / iconsPerRow;
            int col = i % iconsPerRow;
            int x = startX + col * (ABILITY_ICON_SIZE + 2);
            int y = startY + row * (ABILITY_ICON_SIZE + 2);

            guiGraphics.fill(x, y, x + ABILITY_ICON_SIZE, y + ABILITY_ICON_SIZE, 0xFF333333);
            guiGraphics.renderOutline(x, y, ABILITY_ICON_SIZE, ABILITY_ICON_SIZE, 0xFF666666);

            if (i < abilityWheelSlots.size() && draggedFromWheelIndex != i) {
                int si = abilityWheelSubIndexes.get(i);
                renderAbilityIcon(guiGraphics, abilityWheelSlots.get(i), x, y, si);
            }
        }
    }

    private void renderAbilityBar(GuiGraphics guiGraphics, int panelX, int panelY, int mouseX, int mouseY) {
        int startX = panelX + 5;
        int startY = panelY;
        int slotWidth = (ABILITIES_PANEL_WIDTH - 10) / ABILITY_BAR_MAX;
        int iconX = (slotWidth - ABILITY_ICON_SIZE) / 2;

        for (int i = 0; i < ABILITY_BAR_MAX; i++) {
            int x = startX + i * slotWidth + iconX;
            int y = startY;

            guiGraphics.fill(x, y, x + ABILITY_ICON_SIZE, y + ABILITY_ICON_SIZE, 0xFF333333);
            guiGraphics.renderOutline(x, y, ABILITY_ICON_SIZE, ABILITY_ICON_SIZE, 0xFF666666);

            if (i < abilityBarSlots.size() && draggedFromBarIndex != i) {
                int si = abilityBarSubIndexes.get(i);
                renderAbilityIcon(guiGraphics, abilityBarSlots.get(i), x, y, si);
            }

            String keybind = abbreviateKeybind(KEYBIND_LABELS[i]);
            Component keybindText = Component.literal(keybind).withStyle(ChatFormatting.GRAY);
            int textWidth = this.font.width(keybindText);
            int textX = startX + i * slotWidth + (slotWidth - textWidth) / 2;
            int textY = y + ABILITY_ICON_SIZE + 2;
            guiGraphics.drawString(this.font, keybindText, textX, textY, 0xFFAAAAAA, false);
        }
    }

    private void renderSharedAbilitiesTab(GuiGraphics guiGraphics, int panelX, int sectionY, int mouseX, int mouseY) {
        String myUUID = this.minecraft.player.getStringUUID();
        List<String> myContributions = new ArrayList<>(ClientTeamData.getContributionsFor(myUUID));
        int maxSlots = ClientTeamData.getSlotsPerMember();
        List<String> allPooled = getAllPooledAbilities();

        guiGraphics.fill(panelX, sectionY, panelX + ABILITIES_PANEL_WIDTH, sectionY + SHARED_POOL_HEIGHT, 0xCC000000);
        guiGraphics.renderOutline(panelX, sectionY, ABILITIES_PANEL_WIDTH, SHARED_POOL_HEIGHT, 0xFFAAAAAA);
        guiGraphics.drawString(this.font, Component.literal("Sharing").withStyle(ChatFormatting.BOLD), panelX + 5, sectionY + 3, 0xFFFFFFFF, true);

        int iconsPerRow = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);

        int slotsToShow = allPooled.size();
        int myFilledCount = myContributions.size();
        int emptyOwnSlots = Math.max(0, maxSlots - myFilledCount);
        int totalSlots = slotsToShow + emptyOwnSlots;
        int totalRows = (int) Math.ceil((double) totalSlots / iconsPerRow);
        int visibleRows = (SHARED_POOL_HEIGHT - 14) / (ABILITY_ICON_SIZE + 2);
        maxSharedPoolScroll = Math.max(0, totalRows - visibleRows);

        for (int i = 0; i < slotsToShow; i++) {
            String abilityId = allPooled.get(i);
            int row = i / iconsPerRow - sharedPoolScrollOffset;
            int col = i % iconsPerRow;
            int ix = panelX + 5 + col * (ABILITY_ICON_SIZE + 2);
            int iy = sectionY + 14 + row * (ABILITY_ICON_SIZE + 2);
            if (iy < sectionY + 14 || iy + ABILITY_ICON_SIZE > sectionY + SHARED_POOL_HEIGHT) continue;

            boolean isMine = myContributions.contains(abilityId);
            int bgColor = isMine ? 0xFF333333 : 0xFF222233;
            int borderColor = isMine ? 0xFF555555 : 0xFF4444AA;
            guiGraphics.fill(ix, iy, ix + ABILITY_ICON_SIZE, iy + ABILITY_ICON_SIZE, bgColor);
            guiGraphics.renderOutline(ix, iy, ABILITY_ICON_SIZE, ABILITY_ICON_SIZE, borderColor);

            Ability ability = LOTMCraft.abilityHandler.getById(abilityId);
            if (ability != null && draggedFromSharedPoolIndex != i) {
                renderAbilityIcon(guiGraphics, ability, ix, iy, -1);
            }
        }

        for (int e = 0; e < emptyOwnSlots; e++) {
            int i = slotsToShow + e;
            int row = i / iconsPerRow - sharedPoolScrollOffset;
            int col = i % iconsPerRow;
            int ix = panelX + 5 + col * (ABILITY_ICON_SIZE + 2);
            int iy = sectionY + 14 + row * (ABILITY_ICON_SIZE + 2);
            if (iy < sectionY + 14 || iy + ABILITY_ICON_SIZE > sectionY + SHARED_POOL_HEIGHT) continue;
            guiGraphics.fill(ix, iy, ix + ABILITY_ICON_SIZE, iy + ABILITY_ICON_SIZE, 0xFF1A1A1A);
            guiGraphics.renderOutline(ix, iy, ABILITY_ICON_SIZE, ABILITY_ICON_SIZE, 0xFF444444);
        }

        int wheelY = sectionY + SHARED_POOL_HEIGHT + 5;
        guiGraphics.fill(panelX, wheelY, panelX + ABILITIES_PANEL_WIDTH, wheelY + SHARED_WHEEL_HEIGHT, 0xCC000000);
        guiGraphics.renderOutline(panelX, wheelY, ABILITIES_PANEL_WIDTH, SHARED_WHEEL_HEIGHT, 0xFFAAAAAA);
        guiGraphics.drawString(this.font, Component.literal("Shared Wheel").withStyle(ChatFormatting.BOLD), panelX + 5, wheelY + 3, 0xFFFFFFFF, true);

        int startX = panelX + 5;
        int startY = wheelY + 14;
        int iconsPerRowW = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);
        int maxWheelSlots = iconsPerRowW * 2;

        if (allPooled.isEmpty()) {
            Component empty = Component.literal("No shared abilities").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
            int w = this.font.width(empty);
            guiGraphics.drawString(this.font, empty, panelX + (ABILITIES_PANEL_WIDTH - w) / 2, wheelY + 28, 0xFF888888, false);
        } else {
            for (int i = 0; i < maxWheelSlots; i++) {
                int wx = startX + (i % iconsPerRowW) * (ABILITY_ICON_SIZE + 2);
                int wy = startY + (i / iconsPerRowW) * (ABILITY_ICON_SIZE + 2);
                guiGraphics.fill(wx, wy, wx + ABILITY_ICON_SIZE, wy + ABILITY_ICON_SIZE, 0xFF333333);
                guiGraphics.renderOutline(wx, wy, ABILITY_ICON_SIZE, ABILITY_ICON_SIZE, 0xFF666666);
                if (i < sharedWheelSlots.size() && draggedFromSharedWheelIndex != i) {
                    Ability ability = LOTMCraft.abilityHandler.getById(sharedWheelSlots.get(i));
                    if (ability != null) {
                        renderAbilityIcon(guiGraphics, ability, wx, wy, -1);
                    }
                }
            }
        }
    }

    private List<String> getAllPooledAbilities() {
        List<String> result = new ArrayList<>();
        String leaderUUID = ClientTeamData.getLeaderUUID();
        if (!leaderUUID.isEmpty()) {
            for (String id : ClientTeamData.getContributionsFor(leaderUUID)) {
                if (!result.contains(id)) result.add(id);
            }
        }
        for (String memberUUID : ClientTeamData.getMemberUUIDs()) {
            for (String id : ClientTeamData.getContributionsFor(memberUUID)) {
                if (!result.contains(id)) result.add(id);
            }
        }
        String myUUID = this.minecraft.player.getStringUUID();
        for (String id : ClientTeamData.getContributionsFor(myUUID)) {
            if (!result.contains(id)) result.add(id);
        }
        return result;
    }

    private void renderAbilityIcon(GuiGraphics guiGraphics, Ability ability, int x, int y) {
        renderAbilityIcon(guiGraphics, ability, x, y, -1);
    }

    private void renderAbilityIcon(GuiGraphics guiGraphics, Ability ability, int x, int y, int subIndex) {
        if (ability.getTextureLocation() != null) {
            guiGraphics.blit(ability.getTextureLocation(), x, y, 0, 0, ABILITY_ICON_SIZE, ABILITY_ICON_SIZE, ABILITY_ICON_SIZE, ABILITY_ICON_SIZE);
        } else {
            guiGraphics.fill(x, y, x + ABILITY_ICON_SIZE, y + ABILITY_ICON_SIZE, 0xFFFFFFFF);
            guiGraphics.renderOutline(x, y, ABILITY_ICON_SIZE, ABILITY_ICON_SIZE, 0xFF000000);
        }
        if (subIndex >= 0) {
            String badge = String.valueOf(subIndex);
            int bx = x + ABILITY_ICON_SIZE - font.width(badge) - 1;
            int by = y + ABILITY_ICON_SIZE - font.lineHeight + 1;
            guiGraphics.fill(bx - 1, by - 1, x + ABILITY_ICON_SIZE, y + ABILITY_ICON_SIZE, 0x99000000);
            guiGraphics.drawString(font, badge, bx, by, 0xFFFFFF, false);
        }
    }

    private boolean handleSharedTabClick(int mouseX, int mouseY, int panelX, int sectionY) {
        if (!ClientTeamData.hasTeam()) return false;

        String myUUID = this.minecraft.player.getStringUUID();
        List<String> myContributions = new ArrayList<>(ClientTeamData.getContributionsFor(myUUID));
        int iconsPerRow = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);

        List<String> allPooled = getAllPooledAbilities();
        for (int i = 0; i < allPooled.size(); i++) {
            int row = i / iconsPerRow - sharedPoolScrollOffset;
            int ix = panelX + 5 + (i % iconsPerRow) * (ABILITY_ICON_SIZE + 2);
            int iy = sectionY + 14 + row * (ABILITY_ICON_SIZE + 2);
            if (iy < sectionY + 14 || iy + ABILITY_ICON_SIZE > sectionY + SHARED_POOL_HEIGHT) continue;
            if (mouseX >= ix && mouseX <= ix + ABILITY_ICON_SIZE && mouseY >= iy && mouseY <= iy + ABILITY_ICON_SIZE) {
                Ability ability = LOTMCraft.abilityHandler.getById(allPooled.get(i));
                if (ability == null) return false;
                draggedAbility = ability;
                draggedSubIndex = -1;
                draggedFromSharedPoolIndex = myContributions.contains(allPooled.get(i)) ? i : -1;
                draggedFromSharedWheelIndex = -1;
                draggedFromWheelIndex = -1;
                draggedFromBarIndex = -1;
                draggedFromAvailable = false;
                dragOffsetX = mouseX - ix;
                dragOffsetY = mouseY - iy;
                return true;
            }
        }

        int wheelY = sectionY + SHARED_POOL_HEIGHT + 5;
        int startX = panelX + 5;
        int startY = wheelY + 14;
        int iconsPerRowW = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);
        int maxWheelSlots = iconsPerRowW * 2;

        for (int i = 0; i < maxWheelSlots; i++) {
            int wx = startX + (i % iconsPerRowW) * (ABILITY_ICON_SIZE + 2);
            int wy = startY + (i / iconsPerRowW) * (ABILITY_ICON_SIZE + 2);
            if (mouseX >= wx && mouseX <= wx + ABILITY_ICON_SIZE && mouseY >= wy && mouseY <= wy + ABILITY_ICON_SIZE) {
                if (i < sharedWheelSlots.size()) {
                    draggedAbility = LOTMCraft.abilityHandler.getById(sharedWheelSlots.get(i));
                    draggedSubIndex = -1;
                    draggedFromSharedWheelIndex = i;
                    draggedFromSharedPoolIndex = -1;
                    draggedFromWheelIndex = -1;
                    draggedFromBarIndex = -1;
                    draggedFromAvailable = false;
                    dragOffsetX = mouseX - wx;
                    dragOffsetY = mouseY - wy;
                    return true;
                }
                return false;
            }
        }

        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && showAbilities) {
            int baseLeftPos = this.leftPos;
            int panelX = baseLeftPos + this.imageWidth + 5;
            int panelY = this.topPos + 15;
            int slotY = panelY + ABILITIES_PANEL_HEIGHT + 5;

            if (currentTab == Tab.SHARED_ABILITIES) {
                if (handleSharedTabClick((int) mouseX, (int) mouseY, panelX, slotY)) return true;
            }

            if (currentTab == Tab.ABILITY_WHEEL) {
                int wheelSlot = getAbilityWheelSlot((int) mouseX, (int) mouseY, panelX, slotY);
                if (wheelSlot >= 0 && wheelSlot < abilityWheelSlots.size()) {
                    draggedAbility = abilityWheelSlots.get(wheelSlot);
                    draggedSubIndex = abilityWheelSubIndexes.get(wheelSlot);
                    draggedFromWheelIndex = wheelSlot;
                    draggedFromBarIndex = -1;
                    draggedFromAvailable = false;
                    dragOffsetX = (int) mouseX - getWheelSlotX(wheelSlot, panelX, slotY);
                    dragOffsetY = (int) mouseY - getWheelSlotY(wheelSlot, panelX, slotY);
                    return true;
                }
            } else if (currentTab == Tab.ABILITY_BAR) {
                int barSlot = getAbilityBarSlot((int) mouseX, (int) mouseY, panelX, slotY);
                if (barSlot >= 0 && barSlot < abilityBarSlots.size()) {
                    draggedAbility = abilityBarSlots.get(barSlot);
                    draggedSubIndex = abilityBarSubIndexes.get(barSlot);
                    draggedFromBarIndex = barSlot;
                    draggedFromWheelIndex = -1;
                    draggedFromAvailable = false;
                    dragOffsetX = (int) mouseX - getBarSlotX(barSlot, panelX, slotY);
                    dragOffsetY = (int) mouseY - getBarSlotY(barSlot, panelX, slotY);
                    return true;
                }
            }

            int availIdx = getAbilityIndexAt((int) mouseX, (int) mouseY, panelX, panelY, true);
            if (availIdx >= 0) {
                List<Ability> clickableAbilities = currentTab == Tab.SHARED_ABILITIES
                        ? buildDisplayedSharedAbilities() : availableAbilities;
                List<SubAbilityEntry> clickableEntries = currentTab == Tab.SHARED_ABILITIES
                        ? buildDisplayedSharedEntries() : subAbilityEntries;

                draggedAbility = clickableAbilities.get(availIdx);
                draggedSubIndex = (clickableEntries.size() > availIdx && clickableEntries.get(availIdx) != null)
                        ? clickableEntries.get(availIdx).subIndex() : -1;
                draggedFromWheelIndex = -1;
                draggedFromBarIndex = -1;
                draggedFromAvailable = true;
                dragOffsetX = (int) mouseX - getAbilityXByIndex(availIdx, panelX, panelY, clickableAbilities);
                dragOffsetY = (int) mouseY - getAbilityYByIndex(availIdx, panelX, panelY);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && draggedAbility != null) {
            int baseLeftPos = this.leftPos;
            int panelX = baseLeftPos + this.imageWidth + 5;
            int panelY = this.topPos + 15;
            int slotY = panelY + ABILITIES_PANEL_HEIGHT + 5;

            if (currentTab == Tab.ABILITY_WHEEL) {
                if (isInAbilityWheelArea((int) mouseX, (int) mouseY, panelX, slotY)) {
                    int targetSlot = getAbilityWheelSlot((int) mouseX, (int) mouseY, panelX, slotY);

                    if (targetSlot >= 0 && targetSlot < ABILITY_WHEEL_MAX) {
                        if (draggedFromAvailable) {
                            if (targetSlot < abilityWheelSlots.size()) {
                                abilityWheelSlots.set(targetSlot, draggedAbility);
                                abilityWheelSubIndexes.set(targetSlot, draggedSubIndex);
                            } else {
                                while (abilityWheelSlots.size() < targetSlot) {
                                    abilityWheelSlots.add(draggedAbility);
                                    abilityWheelSubIndexes.add(-1);
                                }
                                abilityWheelSlots.add(draggedAbility);
                                abilityWheelSubIndexes.add(draggedSubIndex);
                            }
                        } else if (draggedFromWheelIndex >= 0) {
                            if (targetSlot < abilityWheelSlots.size() && targetSlot != draggedFromWheelIndex) {
                                Ability temp = abilityWheelSlots.get(targetSlot);
                                int tempSi = abilityWheelSubIndexes.get(targetSlot);
                                abilityWheelSlots.set(targetSlot, draggedAbility);
                                abilityWheelSubIndexes.set(targetSlot, draggedSubIndex);
                                abilityWheelSlots.set(draggedFromWheelIndex, temp);
                                abilityWheelSubIndexes.set(draggedFromWheelIndex, tempSi);
                            } else if (targetSlot >= abilityWheelSlots.size()) {
                                abilityWheelSlots.remove(draggedFromWheelIndex);
                                abilityWheelSubIndexes.remove(draggedFromWheelIndex);
                                abilityWheelSlots.add(draggedAbility);
                                abilityWheelSubIndexes.add(draggedSubIndex);
                            }
                        }
                    }
                } else {
                    if (!draggedFromAvailable && draggedFromWheelIndex >= 0) {
                        abilityWheelSlots.remove(draggedFromWheelIndex);
                        abilityWheelSubIndexes.remove(draggedFromWheelIndex);
                    }
                }

                PacketHandler.sendToServer(new SyncAbilityWheelAbilitiesPacket(wheelSlotsToIdList()));

            } else if (currentTab == Tab.ABILITY_BAR) {
                if (isInAbilityBarArea((int) mouseX, (int) mouseY, panelX, slotY)) {
                    int targetSlot = getAbilityBarSlot((int) mouseX, (int) mouseY, panelX, slotY);

                    if (targetSlot >= 0 && targetSlot < ABILITY_BAR_MAX) {
                        if (draggedFromAvailable) {
                            if (targetSlot < abilityBarSlots.size()) {
                                abilityBarSlots.set(targetSlot, draggedAbility);
                                abilityBarSubIndexes.set(targetSlot, draggedSubIndex);
                            } else {
                                abilityBarSlots.add(draggedAbility);
                                abilityBarSubIndexes.add(draggedSubIndex);
                            }
                        } else if (draggedFromBarIndex >= 0) {
                            if (targetSlot < abilityBarSlots.size() && targetSlot != draggedFromBarIndex) {
                                Ability temp = abilityBarSlots.get(targetSlot);
                                int tempSi = abilityBarSubIndexes.get(targetSlot);
                                abilityBarSlots.set(targetSlot, draggedAbility);
                                abilityBarSubIndexes.set(targetSlot, draggedSubIndex);
                                abilityBarSlots.set(draggedFromBarIndex, temp);
                                abilityBarSubIndexes.set(draggedFromBarIndex, tempSi);
                            } else if (targetSlot >= abilityBarSlots.size()) {
                                abilityBarSlots.remove(draggedFromBarIndex);
                                abilityBarSubIndexes.remove(draggedFromBarIndex);
                                abilityBarSlots.add(draggedAbility);
                                abilityBarSubIndexes.add(draggedSubIndex);
                            }
                        }
                    }
                } else {
                    if (!draggedFromAvailable && draggedFromBarIndex >= 0) {
                        abilityBarSlots.remove(draggedFromBarIndex);
                        abilityBarSubIndexes.remove(draggedFromBarIndex);
                    }
                }

                PacketHandler.sendToServer(new SyncAbilityBarAbilitiesPacket(barSlotsToIdList()));

            } else if (currentTab == Tab.SHARED_ABILITIES && draggedAbility != null) {
                int sectionY = panelY + ABILITIES_PANEL_HEIGHT + 5;
                String myUUID = this.minecraft.player.getStringUUID();
                List<String> myContributions = new ArrayList<>(ClientTeamData.getContributionsFor(myUUID));
                String draggedId = draggedAbility.getId();

                boolean inPoolArea = (int) mouseX >= panelX && (int) mouseX <= panelX + ABILITIES_PANEL_WIDTH
                        && (int) mouseY >= sectionY && (int) mouseY <= sectionY + SHARED_POOL_HEIGHT;

                int wheelY = sectionY + SHARED_POOL_HEIGHT + 5;
                int startX = panelX + 5;
                int startY = wheelY + 14;
                int iconsPerRowW = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);
                int maxWheelSlots = iconsPerRowW * 2;

                int targetWheelSlot = -1;
                for (int i = 0; i < maxWheelSlots; i++) {
                    int wx = startX + (i % iconsPerRowW) * (ABILITY_ICON_SIZE + 2);
                    int wy = startY + (i / iconsPerRowW) * (ABILITY_ICON_SIZE + 2);
                    if ((int) mouseX >= wx && (int) mouseX <= wx + ABILITY_ICON_SIZE
                            && (int) mouseY >= wy && (int) mouseY <= wy + ABILITY_ICON_SIZE) {
                        targetWheelSlot = i;
                        break;
                    }
                }

                if (draggedFromAvailable) {
                    if (inPoolArea && !myContributions.contains(draggedId)) {
                        myContributions.add(draggedId);
                        PacketHandler.sendToServer(new SyncSharedAbilitiesPacket(new ArrayList<>(myContributions)));
                    }
                } else if (draggedFromSharedWheelIndex >= 0) {
                    if (targetWheelSlot >= 0 && targetWheelSlot != draggedFromSharedWheelIndex) {
                        sharedWheelSlots.remove(draggedFromSharedWheelIndex);
                        sharedWheelSlots.add(Math.min(targetWheelSlot, sharedWheelSlots.size()), draggedId);
                    } else if (targetWheelSlot < 0 && !inPoolArea) {
                        sharedWheelSlots.remove(draggedFromSharedWheelIndex);
                    }
                } else if (draggedFromSharedPoolIndex >= 0) {
                    if (targetWheelSlot >= 0) {
                        List<String> allPooled = getAllPooledAbilities();
                        if (allPooled.contains(draggedId) && !sharedWheelSlots.contains(draggedId)) {
                            if (targetWheelSlot < sharedWheelSlots.size()) {
                                sharedWheelSlots.add(targetWheelSlot, draggedId);
                            } else {
                                sharedWheelSlots.add(draggedId);
                            }
                        }
                    } else if (!inPoolArea) {
                        myContributions.remove(draggedId);
                        sharedWheelSlots.remove(draggedId);
                        ClientData.setSharedWheelAbilities(sharedWheelSlots);
                        PacketHandler.sendToServer(new SyncSharedAbilitiesPacket(new ArrayList<>(myContributions)));
                    }
                } else {
                    if (targetWheelSlot >= 0) {
                        List<String> allPooled = getAllPooledAbilities();
                        if (allPooled.contains(draggedId) && !sharedWheelSlots.contains(draggedId)) {
                            int mySeq = BeyonderData.getSequence(this.minecraft.player);
                            int reqSeq = draggedAbility.lowestSequenceUsable();
                            if (reqSeq >= 0 && mySeq > reqSeq) {
                                this.minecraft.player.displayClientMessage(
                                        Component.literal("Your sequence is too low to use this ability.").withStyle(ChatFormatting.RED), true);
                            } else {
                                if (targetWheelSlot < sharedWheelSlots.size()) {
                                    sharedWheelSlots.add(targetWheelSlot, draggedId);
                                } else {
                                    sharedWheelSlots.add(draggedId);
                                }
                            }
                        }
                    }
                }
            }

            ClientData.setSharedWheelAbilities(sharedWheelSlots);

            draggedAbility = null;
            draggedSubIndex = -1;
            draggedFromWheelIndex = -1;
            draggedFromBarIndex = -1;
            draggedFromSharedWheelIndex = -1;
            draggedFromSharedPoolIndex = -1;
            draggedFromAvailable = false;
            return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (showAbilities) {
            int baseLeftPos = this.leftPos;
            int panelX = baseLeftPos + this.imageWidth + 5;
            int panelY = this.topPos + 15;

            if (mouseX >= panelX && mouseX <= panelX + ABILITIES_PANEL_WIDTH &&
                    mouseY >= panelY && mouseY <= panelY + ABILITIES_PANEL_HEIGHT - 15) {
                abilitiesScrollOffset = Math.max(0, Math.min(maxAbilitiesScroll, abilitiesScrollOffset - (int) scrollY));
                return true;
            }

            if (currentTab == Tab.SHARED_ABILITIES) {
                int slotY = this.topPos + 15 + ABILITIES_PANEL_HEIGHT + 5;
                if (mouseX >= panelX && mouseX <= panelX + ABILITIES_PANEL_WIDTH &&
                        mouseY >= slotY && mouseY <= slotY + SHARED_POOL_HEIGHT) {
                    sharedPoolScrollOffset = Math.max(0, Math.min(maxSharedPoolScroll, sharedPoolScrollOffset - (int) scrollY));
                    return true;
                }
            }
        }

        if (showQuests) {
            int baseLeftPos = this.leftPos;
            int panelX = baseLeftPos + this.imageWidth + 5;
            int panelY = this.topPos;

            if (mouseX >= panelX && mouseX <= panelX + QUESTS_PANEL_WIDTH &&
                    mouseY >= panelY && mouseY <= panelY + COMPLETED_QUESTS_HEIGHT) {
                completedQuestsScrollOffset = Math.max(0, Math.min(maxCompletedQuestsScroll, completedQuestsScrollOffset - (int) scrollY));
                updateCompletedQuestsScroll();
                return true;
            }
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private int getAbilityIndexAt(int mouseX, int mouseY, int panelX, int panelY, boolean useScroll) {
        int adjustedMouseY = mouseY - 15;
        int startX = panelX + 5;
        int startY = panelY - (useScroll ? abilitiesScrollOffset * (ABILITY_ICON_SIZE + 2) : 0);
        int iconsPerRow = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);

        int clipTop = panelY;
        int clipBottom = panelY + ABILITIES_PANEL_HEIGHT - 15;

        List<Ability> displayedAbilities = currentTab == Tab.SHARED_ABILITIES
                ? buildDisplayedSharedAbilities() : availableAbilities;

        for (int i = 0; i < displayedAbilities.size(); i++) {
            int row = i / iconsPerRow;
            int col = i % iconsPerRow;
            int x = startX + col * (ABILITY_ICON_SIZE + 2);
            int y = startY + row * (ABILITY_ICON_SIZE + 2);

            if (y < clipTop || y + ABILITY_ICON_SIZE > clipBottom) continue;

            if (mouseX >= x && mouseX <= x + ABILITY_ICON_SIZE &&
                    adjustedMouseY >= y && adjustedMouseY <= y + ABILITY_ICON_SIZE) {
                return i;
            }
        }

        return -1;
    }

    private int getAbilityXByIndex(int index, int panelX, int panelY, List<Ability> abilities) {
        int iconsPerRow = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);
        int col = index % iconsPerRow;
        return panelX + 5 + col * (ABILITY_ICON_SIZE + 2);
    }

    private int getAbilityYByIndex(int index, int panelX, int panelY) {
        int iconsPerRow = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);
        int row = index / iconsPerRow;
        return panelY - abilitiesScrollOffset * (ABILITY_ICON_SIZE + 2) + row * (ABILITY_ICON_SIZE + 2) + 15;
    }

    private boolean isInAbilityWheelArea(int mouseX, int mouseY, int panelX, int wheelY) {
        return mouseX >= panelX && mouseX <= panelX + ABILITIES_PANEL_WIDTH &&
                mouseY >= wheelY + 15 && mouseY <= wheelY + ABILITY_WHEEL_HEIGHT;
    }

    private boolean isInAbilityBarArea(int mouseX, int mouseY, int panelX, int barY) {
        return mouseX >= panelX && mouseX <= panelX + ABILITIES_PANEL_WIDTH &&
                mouseY >= barY + 15 && mouseY <= barY + ABILITY_BAR_HEIGHT;
    }

    private int getAbilityWheelSlot(int mouseX, int mouseY, int panelX, int wheelY) {
        int startX = panelX + 5;
        int startY = wheelY + 15;
        int iconsPerRow = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);

        for (int i = 0; i < ABILITY_WHEEL_MAX; i++) {
            int row = i / iconsPerRow;
            int col = i % iconsPerRow;
            int x = startX + col * (ABILITY_ICON_SIZE + 2);
            int y = startY + row * (ABILITY_ICON_SIZE + 2);

            if (mouseX >= x && mouseX <= x + ABILITY_ICON_SIZE &&
                    mouseY >= y && mouseY <= y + ABILITY_ICON_SIZE) {
                return i;
            }
        }
        return -1;
    }

    private int getAbilityBarSlot(int mouseX, int mouseY, int panelX, int barY) {
        int startX = panelX + 5;
        int startY = barY + 15;
        int slotWidth = (ABILITIES_PANEL_WIDTH - 10) / ABILITY_BAR_MAX;
        int iconX = (slotWidth - ABILITY_ICON_SIZE) / 2;

        for (int i = 0; i < ABILITY_BAR_MAX; i++) {
            int x = startX + i * slotWidth + iconX;
            int y = startY;

            if (mouseX >= x && mouseX <= x + ABILITY_ICON_SIZE &&
                    mouseY >= y && mouseY <= y + ABILITY_ICON_SIZE) {
                return i;
            }
        }
        return -1;
    }

    private int getWheelSlotX(int slot, int panelX, int wheelY) {
        int iconsPerRow = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);
        int col = slot % iconsPerRow;
        return panelX + 5 + col * (ABILITY_ICON_SIZE + 2);
    }

    private int getWheelSlotY(int slot, int panelX, int wheelY) {
        int iconsPerRow = (ABILITIES_PANEL_WIDTH - 10) / (ABILITY_ICON_SIZE + 2);
        int row = slot / iconsPerRow;
        return wheelY + 15 + row * (ABILITY_ICON_SIZE + 2);
    }

    private int getBarSlotX(int slot, int panelX, int barY) {
        int slotWidth = (ABILITIES_PANEL_WIDTH - 10) / ABILITY_BAR_MAX;
        int iconX = (slotWidth - ABILITY_ICON_SIZE) / 2;
        return panelX + 5 + slot * slotWidth + iconX;
    }

    private int getBarSlotY(int slot, int panelX, int barY) {
        return barY + 15;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        guiGraphics.blit(containerBackground, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        renderPathwaySymbol(guiGraphics, x, y);
        renderSequenceNumber(guiGraphics, x, y);
        renderSequenceName(guiGraphics, x, y);
        renderDigestionLabel(guiGraphics, x, y);
        renderDigestionProgress(guiGraphics, x, y);
        renderSanityLabel(guiGraphics, x, y);
        renderSanityProgress(guiGraphics, x, y);
        renderPassiveAbilitiesText(guiGraphics, x, y);
        renderKillCount(guiGraphics, x, y);
        renderUniquenessIcon(guiGraphics, x, y);
        RenderSystem.disableBlend();
    }

    private void renderKillCount(GuiGraphics guiGraphics, int x, int y) {
        if (!menu.getPathway().equals("red_priest") || menu.getSequence() > 3) return;
        Component text = Component.literal("Kills: " + killCount).withStyle(ChatFormatting.BOLD);
        guiGraphics.drawString(this.font, text, x + 7, y + 154, 0xDDDDDD, true);
    }

    private void renderUniquenessIcon(GuiGraphics guiGraphics, int x, int y) {
        if (!ClientUniquenessCache.hasUniqueness()) return;
        String pathway = ClientUniquenessCache.getPathway();
        if (pathway.isEmpty()) return;

        ResourceLocation textureLocation = ResourceLocation.fromNamespaceAndPath(
                LOTMCraft.MOD_ID, "textures/item/" + pathway + "_uniqueness.png");

        int iconSize = 16;
        int iconX = x + 7;
        int iconY = y + 50;

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(textureLocation, iconX, iconY, 0, 0, iconSize, iconSize, iconSize, iconSize);

        int kills = ClientUniquenessCache.getKillCount();
        Component killText = Component.literal(kills + "/" + RequestUniquenessApotheosisPacket.KILLS_REQUIRED_FOR_APOTHEOSIS + " kills").withStyle(ChatFormatting.GOLD);
        guiGraphics.drawString(this.font, killText, iconX + iconSize + 3, iconY + 4, 0xFFAA00, true);
    }

    private void renderPassiveAbilitiesText(GuiGraphics guiGraphics, int x, int y) {
        Component passiveAbilitiesText = Component.translatable("lotm.passive_abilities").withStyle(ChatFormatting.BOLD);
        int color = 0xDDDDDD;
        boolean showKillCount = menu.getPathway().equals("red_priest") && menu.getSequence() <= 3;
        int textY = showKillCount ? 171 : 162;
        guiGraphics.drawString(this.font, passiveAbilitiesText, x + 7, y + textY, color, true);
    }

    private void renderSanityLabel(GuiGraphics guiGraphics, int x, int y) {
        Component digestionText = Component.translatable("lotm.sanity").withStyle(ChatFormatting.BOLD);
        guiGraphics.drawString(this.font, digestionText, x + 7, y + 115, 0xDDDDDD, true);
    }

    private void renderSanityProgress(GuiGraphics guiGraphics, int x, int y) {
        int barEndX = (int) (115 * menu.getSanity()) + 3;
        guiGraphics.fillGradient(3 + x, 132 + y, barEndX + x, 143 + y, 0xFFe8bb68, 0xFFF5ad2a);
    }

    private void renderDigestionLabel(GuiGraphics guiGraphics, int x, int y) {
        Component digestionText = Component.translatable("lotm.digestion").withStyle(ChatFormatting.BOLD);
        guiGraphics.drawString(this.font, digestionText, x + 7, y + 76, 0xDDDDDD, true);
    }

    private void renderDigestionProgress(GuiGraphics guiGraphics, int x, int y) {
        int barEndX = (int) (115 * menu.getDigestionProgress()) + 3;
        guiGraphics.fillGradient(3 + x, 93 + y, barEndX + x, 104 + y, 0xFFe36c54, 0xFFa8422d);
    }

    private void renderSequenceNumber(GuiGraphics guiGraphics, int x, int y) {
        Player player = playerInventory.player;
        int charStackCount = 0;
        if (player.level().isClientSide) {
            charStackCount = ClientBeyonderCache.getCharStack(player.getUUID());
        }
        Component sequenceText = Component.translatable("lotm.sequence")
                .append(": ")
                .append(Component.literal(menu.getSequence() + ""))
                .append(charStackCount > 0 ? " +" + charStackCount : "")
                .withStyle(ChatFormatting.BOLD);
        guiGraphics.drawString(this.font, sequenceText, x + 7, y + 7, 0xDDDDDD, true);
    }

    private void renderSequenceName(GuiGraphics guiGraphics, int x, int y) {
        int color = BeyonderData.pathwayInfos.get(menu.getPathway()).color();
        Component sequenceNameText = Component.literal(BeyonderData.getSequenceName(menu.getPathway(), menu.getSequence()));
        guiGraphics.drawString(this.font, sequenceNameText, x + 7, y + 28, color, true);
    }

    private void renderPathwaySymbol(GuiGraphics guiGraphics, int x, int y) {
        ResourceLocation iconTexture = ResourceLocation.fromNamespaceAndPath(
                LOTMCraft.MOD_ID, "textures/gui/icons/" + menu.getPathway() + "_icon.png");
        guiGraphics.blit(iconTexture, x + 126, y + 3, 0, 0, 62, 62, 62, 62);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title.getString(), this.titleLabelX, this.titleLabelY, 0xCCCCCC, true);
    }
}