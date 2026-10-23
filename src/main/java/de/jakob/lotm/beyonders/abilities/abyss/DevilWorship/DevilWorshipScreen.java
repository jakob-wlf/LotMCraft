package de.jakob.lotm.beyonders.abilities.abyss.DevilWorship;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DevilWorshipScreen extends Screen {
    private enum Page { LIST, SEEK_TYPE, SEEK_CONFIG, FACTIONS, FACTION_OPTIONS, TARGET, DETAIL }
    private final int pleasure;
    private final String tribute;
    private final List<DevilWorshipNetwork.PlayerEntry> players;
    private final List<String> pathways;
    private Page page = Page.LIST;
    private Page detailBack = Page.FACTIONS;
    private int selectedIndex = -1;// index into DevilWorshipAbility.MISSIONS
    private DevilWorshipAbility.Faction selectedFaction;
    private DevilWorshipAbility.Reward seekReward = DevilWorshipAbility.Reward.CHARACTERISTIC;
    private int seekPathwayIndex = -1;// -1 = any pathway
    private int seekSequence = DevilWorshipAbility.SEEK_MAX_SEQ;
    public static void open(int pleasure, String tribute, List<DevilWorshipNetwork.PlayerEntry> players, List<String> pathways) {Minecraft.getInstance().setScreen(new DevilWorshipScreen(pleasure, tribute, players, pathways));}
    private DevilWorshipScreen(int pleasure, String tribute, List<DevilWorshipNetwork.PlayerEntry> players, List<String> pathways) {
        super(Component.literal("Summon Devil"));
        this.pleasure = pleasure;
        this.tribute = tribute;
        this.players = players;
        this.pathways = pathways;
    }
    private DevilWorshipAbility.MissionDef selected() {return selectedIndex < 0 ? null : DevilWorshipAbility.MISSIONS.get(selectedIndex);}
    @Override
    protected void init() {
        rebuild();
    }
    //widgets
    private void add(int x, int y, int w, String text, String tooltip, Runnable action) {
        Button.Builder builder = Button.builder(Component.literal(text), b -> action.run()).bounds(x, y, w, 20);
        if (tooltip != null && !tooltip.isEmpty()) builder.tooltip(Tooltip.create(Component.literal(tooltip)));
        addRenderableWidget(builder.build());
    }
    private void goTo(Page newPage) {
        page = newPage;
        rebuild();
    }
    private void missionButton(int cx, int y, DevilWorshipAbility.MissionDef def, Runnable action) {add(cx - 180, y, 360, def.title() + "  |  " + def.timerLabel(), def.description(), action);}
    private void rebuild() {
        clearWidgets();
        int cx = width / 2;
        int y = 92;
        switch (page) {
            case LIST -> {
                if (pleasure >= 100) {
                    add(cx - 180, y, 360, "Seek a Characteristic, Recipe or Ingredient", "The Devil searches the world for what you ask for. Stronger sequences and chosen pathways take longer.", () -> goTo(Page.SEEK_TYPE));
                    y += 22;
                    DevilWorshipAbility.MissionDef kill = DevilWorshipAbility.KILL_PLAYER_MISSION;
                    missionButton(cx, y, kill, () -> {selectedIndex = DevilWorshipAbility.MISSIONS.indexOf(kill);goTo(Page.TARGET);});
                    y += 22;
                    DevilWorshipAbility.MissionDef protection = DevilWorshipAbility.PROTECTION_MISSION;
                    missionButton(cx, y, protection, () -> {selectedIndex = DevilWorshipAbility.MISSIONS.indexOf(protection);chooseFixed("");});
                    y += 22;
                    add(cx - 180, y, 360, "Raid a Faction", "Send the Devil against a faction's beyonder team, church or cathedral.", () -> goTo(Page.FACTIONS));
                    y += 22;
                }
                add(cx - 60, Math.max(y + 8, 130), 120, "Close", null, this::onClose);
            }
            case SEEK_TYPE -> {
                add(cx - 120, y, 240, "Characteristic", null, () -> startSeek(DevilWorshipAbility.Reward.CHARACTERISTIC));
                y += 24;
                add(cx - 120, y, 240, "Recipe", null, () -> startSeek(DevilWorshipAbility.Reward.RECIPE));
                y += 24;
                add(cx - 120, y, 240, "Ingredient", "A sub-ingredient. Main ingredients are never found this way.", () -> startSeek(DevilWorshipAbility.Reward.SUB_INGREDIENT));
                y += 24;
                add(cx - 120, y + 8, 240, "Back", null, () -> goTo(Page.LIST));
            }
            case SEEK_CONFIG -> {
                // Pathway row and sequence row. The labels are drawn in render() so the arrows don't need a rebuild.
                add(cx - 150, 96, 20, "<", null, () -> cyclePathway(-1));
                add(cx + 130, 96, 20, ">", null, () -> cyclePathway(1));
                add(cx - 150, 124, 20, "<", "Weaker (takes less time)", () -> changeSequence(1));
                add(cx + 130, 124, 20, ">", "Stronger (takes longer)", () -> changeSequence(-1));
                add(cx - 120, 186, 240, "Begin", null, this::chooseSeek);
                add(cx - 120, 210, 240, "Back", null, () -> goTo(Page.SEEK_TYPE));
            }
            case FACTIONS -> {
                for (DevilWorshipAbility.Faction faction : DevilWorshipAbility.FACTIONS) {
                    add(cx - 120, y, 240, faction.name(), null, () -> {selectedFaction = faction;goTo(Page.FACTION_OPTIONS);});
                    y += 24;
                }
                add(cx - 120, y, 240, "None", "An abandoned cathedral with no faction.", () -> {
                    selectedIndex = DevilWorshipAbility.MISSIONS.indexOf(DevilWorshipAbility.ABANDONED_CATHEDRAL);
                    detailBack = Page.FACTIONS;
                    goTo(Page.DETAIL);
                });
                y += 24;
                add(cx - 120, y + 8, 240, "Back", null, () -> goTo(Page.LIST));
            }
            case FACTION_OPTIONS -> {
                if (selectedFaction != null) {
                    for (DevilWorshipAbility.MissionDef def : List.of(selectedFaction.team(), selectedFaction.church(), selectedFaction.cathedral())) {
                        missionButton(cx, y, def, () -> {
                            selectedIndex = DevilWorshipAbility.MISSIONS.indexOf(def);
                            detailBack = Page.FACTION_OPTIONS;
                            goTo(Page.DETAIL);
                        });
                        y += 24;
                    }
                }
                add(cx - 120, y + 8, 240, "Back", null, () -> goTo(Page.FACTIONS));
            }
            case TARGET -> {
                for (DevilWorshipNetwork.PlayerEntry entry : players) {
                    add(cx - 120, y, 240, entry.name(), null, () -> chooseFixed(entry.uuid()));
                    y += 24;
                }
                add(cx - 120, y + 8, 240, "Back", null, () -> goTo(Page.LIST));
            }
            case DETAIL -> {
                DevilWorshipAbility.MissionDef def = selected();
                int lootLines = def == null ? 1 : Math.max(1, def.loot().size());
                int buttonsY = 108 + lootLines * 12 + 12;
                add(cx - 120, buttonsY, 240, "Begin", null, () -> chooseFixed(""));
                add(cx - 120, buttonsY + 24, 240, "Back", null, () -> goTo(detailBack));
            }
        }
    }
    //actions
    private void startSeek(DevilWorshipAbility.Reward reward) {
        seekReward = reward;
        goTo(Page.SEEK_CONFIG);
    }
    private void cyclePathway(int direction) {
        if (pathways.isEmpty()) return;
        seekPathwayIndex += direction;
        if (seekPathwayIndex < -1) seekPathwayIndex = pathways.size() - 1;
        if (seekPathwayIndex > pathways.size() - 1) seekPathwayIndex = -1;
    }
    private void changeSequence(int direction) {seekSequence = Math.clamp(seekSequence + direction, DevilWorshipAbility.SEEK_MIN_SEQ, DevilWorshipAbility.SEEK_MAX_SEQ);}
    private void chooseFixed(String targetUuid) {
        PacketDistributor.sendToServer(new DevilWorshipNetwork.ChooseMission(selectedIndex, targetUuid, -1, "", 0));
        onClose();
    }
    private void chooseSeek() {
        String pathway = seekPathwayIndex < 0 ? "" : pathways.get(seekPathwayIndex);
        PacketDistributor.sendToServer(new DevilWorshipNetwork.ChooseMission(DevilWorshipAbility.SEEK_MISSION_INDEX, "", seekReward.ordinal(), pathway, seekSequence));
        onClose();
    }
    private String rewardName() {
        return switch (seekReward) {
            case CHARACTERISTIC -> "Characteristic";
            case RECIPE -> "Recipe";
            case SUB_INGREDIENT -> "Ingredient";
        };
    }
    //render
    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, 0xB0000000); // own dim, so we never touch the vanilla background blur
        int cx = width / 2;
        DevilWorshipAbility.MissionDef def = selected();
        switch (page) {
            case TARGET -> {
                g.drawCenteredString(font, "Choose the player the Devil will hunt", cx, 40, 0xFFD700);
                g.drawCenteredString(font, DevilWorshipAbility.KILL_PLAYER_MISSION.title(), cx, 56, 0xCCCCCC);
            }
            case SEEK_TYPE -> g.drawCenteredString(font, "What should the Devil seek?", cx, 40, 0xFFD700);
            case SEEK_CONFIG -> {
                g.drawCenteredString(font, "Seek a " + rewardName(), cx, 40, 0xFFD700);
                g.drawCenteredString(font, "Choose a pathway and a sequence", cx, 56, 0xCCCCCC);
                boolean pathwayChosen = seekPathwayIndex >= 0;
                String pathwayText = pathwayChosen ? DevilWorshipAbility.prettyPathway(pathways.get(seekPathwayIndex)) : "Any pathway";
                g.drawCenteredString(font, "Pathway: " + pathwayText, cx, 102, 0xFFFFFF);
                g.drawCenteredString(font, "Sequence: " + seekSequence, cx, 130, 0xFFFFFF);
                int ticks = DevilWorshipAbility.seekTimerTicks(seekSequence, pathwayChosen);
                g.drawCenteredString(font, "Time: " + DevilWorshipAbility.timerLabel(ticks), cx, 156, 0xCCCCCC);
                g.drawCenteredString(font, "Stronger sequences and chosen pathways take longer.", cx, 170, 0x9932CC);
            }
            case FACTIONS -> g.drawCenteredString(font, "Choose a faction to raid", cx, 40, 0xFFD700);
            case FACTION_OPTIONS -> {
                g.drawCenteredString(font, selectedFaction == null ? "" : selectedFaction.name(), cx, 40, 0xFFD700);
                g.drawCenteredString(font, "Choose what to raid", cx, 56, 0xCCCCCC);
            }
            case DETAIL -> {
                if (def != null) {
                    g.drawCenteredString(font, def.title(), cx, 30, 0xFFD700);
                    g.drawCenteredString(font, "Time: " + def.timerLabel(), cx, 50, 0xCCCCCC);
                    g.drawCenteredString(font, "Possible loot", cx, 76, 0x9932CC);
                    int y = 96;
                    if (def.loot().isEmpty()) {g.drawCenteredString(font, "No loot has been set up for this yet.", cx, y, 0x888888);}
                    for (DevilWorshipAbility.Loot loot : def.loot()) {
                        g.drawCenteredString(font, loot.label() + "  -  " + loot.percent() + "%", cx, y, 0xCCCCCC);
                        y += 12;
                    }
                }
            }
            default -> {
                g.drawCenteredString(font, "Summon Devil", cx, 20, 0xFFD700);
                g.drawCenteredString(font, tribute.isEmpty() ? "No tribute chosen" : "Tribute: " + tribute, cx, 36, 0xCCCCCC);
                int barW = 200, x = cx - barW / 2, y = 52;
                g.fill(x - 1, y - 1, x + barW + 1, y + 13, 0xFF555555);
                g.fill(x, y, x + barW, y + 12, 0xFF222222);
                g.fill(x, y, x + barW * Math.min(100, pleasure) / 100, y + 12, 0xFFB22222);
                g.drawCenteredString(font, pleasure + "%", cx, y + 2, 0xFFFFFF);
                if (pleasure < 100) {g.drawCenteredString(font, "The Devils are not pleased enough to answer yet.", cx, 76, 0xFF6666);}
                else {g.drawCenteredString(font, "Choose what the Devil will be summoned for", cx, 76, 0x9932CC);}
            }
        }
        super.render(g, mouseX, mouseY, partialTick);
    }
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    @Override
    public void renderBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Intentionally empty
    }
}