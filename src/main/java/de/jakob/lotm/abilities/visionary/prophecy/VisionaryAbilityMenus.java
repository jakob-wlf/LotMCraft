package de.jakob.lotm.abilities.visionary.prophecy;

import de.jakob.lotm.gui.custom.TextDisplay.AbilityMenuBuilder;
import de.jakob.lotm.gui.custom.TextDisplay.ColoredTextDisplayScreen;
import de.jakob.lotm.gui.custom.TextDisplay.TextColorHelper;
import net.minecraft.client.gui.screens.Screen;

public class VisionaryAbilityMenus {

    public static ColoredTextDisplayScreen createStoryWritingAbilityMenu(Screen previousScreen) {
        return new AbilityMenuBuilder("Story Writing", previousScreen)
                .header("Overview", TextColorHelper.GOLD)
                .line("Write prophecies into existence. Type natural language commands", TextColorHelper.WHITE)
                .line("to create permanent story-based effects on targets.", TextColorHelper.WHITE)

                .spacing()
                .header("How to Use", TextColorHelper.GOLD)
                .line("1. Activate the ability", TextColorHelper.WHITE)
                .line("2. Type in chat: if <player_name> <trigger> then <action>", TextColorHelper.WHITE)
                .line("3. The prophecy is recorded and stored on the target (you can use yourself as the target)", TextColorHelper.WHITE)

                .spacing()
                .header("Available Triggers", TextColorHelper.GOLD)
                .ability("instant", "Activates immediately when created", TextColorHelper.GOLD, TextColorHelper.WHITE)
                .ability("on <x y z>", "Triggers when player reaches coordinates", TextColorHelper.GOLD, TextColorHelper.WHITE)
                .ability("has <item>", "Triggers when player picks up item", TextColorHelper.GOLD, TextColorHelper.WHITE)
                .ability("health <amount>", "Triggers when player health drops below amount", TextColorHelper.GOLD, TextColorHelper.WHITE)
                .ability("sanity <amount>", "Triggers when sanity drops below amount", TextColorHelper.GOLD, TextColorHelper.WHITE)
                .ability("player <name>", "Triggers when another player is nearby", TextColorHelper.GOLD, TextColorHelper.WHITE)
                .ability("sealed", "Triggers when sealed artifact is used", TextColorHelper.GOLD, TextColorHelper.WHITE)

                .spacing()
                .header("Available Actions", TextColorHelper.GOLD)
                .ability("health <number>", "Damage (negative) or heal (positive) (only for Seq 0)", TextColorHelper.GOLD, TextColorHelper.WHITE)
                .ability("sanity <number>", "Drain or restore sanity (only for Seq 0)", TextColorHelper.GOLD, TextColorHelper.WHITE)
                .ability("teleport <x y z>", "Teleport target to coordinates (only for Seq 0)", TextColorHelper.GOLD, TextColorHelper.WHITE)
                .ability("drop <item>", "Force target to drop item", TextColorHelper.GOLD, TextColorHelper.WHITE)
                .ability("digest <number>", "Modify digestion", TextColorHelper.GOLD, TextColorHelper.WHITE)
                .ability("health <number>", "Apply or remove health", TextColorHelper.GOLD, TextColorHelper.WHITE)
                .ability("sanity <number>", "Drain or restore sanity (only for Seq 0)", TextColorHelper.GOLD, TextColorHelper.WHITE)
                .ability("stun", "Stun target", TextColorHelper.GOLD, TextColorHelper.WHITE)
                .ability("skill <name>", "Trigger skill effect", TextColorHelper.GOLD, TextColorHelper.WHITE)
                .ability("confusion", "Confuse target", TextColorHelper.GOLD, TextColorHelper.WHITE)
                .ability("seal", "Seal target", TextColorHelper.GOLD, TextColorHelper.WHITE)
                .ability("unseal", "Unseal target", TextColorHelper.GOLD, TextColorHelper.WHITE)
                .ability("calamity <type>", "Trigger calamity", TextColorHelper.GOLD, TextColorHelper.WHITE)
                .ability("spawn <entity>", "Spawn entity at target (only for Seq 0)", TextColorHelper.GOLD, TextColorHelper.WHITE)
                .ability("say <message>", "Target says message in chat", TextColorHelper.GOLD, TextColorHelper.WHITE)
                .ability("weather <type>", "Change weather (rain/thunder/clear) (only for Seq 0)", TextColorHelper.GOLD, TextColorHelper.WHITE)
                .ability("time <value>", "Change world time (Seq 1+) (only for Seq 0)", TextColorHelper.GOLD, TextColorHelper.WHITE)

                .spacing()
                .header("Examples", TextColorHelper.GOLD)
                .line("if Steve instant then health -15", TextColorHelper.CYAN)
                .line("if Alex on 100 64 200 then teleport 0 100 0", TextColorHelper.CYAN)
                .line("if Diana health 3 then sanity -5", TextColorHelper.CYAN)
                .line("if Bob has minecraft:golden_apple then say You picked up forbidden fruit", TextColorHelper.CYAN)
                .line("if Eve player Frank then confusion", TextColorHelper.CYAN)
                .line("if Charlie sealed then spawn zombie", TextColorHelper.CYAN)

                .spacing()
                .separator()
                .spacing()
                .line("Press ESC to close this menu", TextColorHelper.DARK_GRAY)

                .build();
    }

    public static ColoredTextDisplayScreen createPsychologicalCueAbilityMenu(Screen previousScreen) {
        return new AbilityMenuBuilder("Psychological Cue", previousScreen)
                .header("Overview", TextColorHelper.GOLD)
                .line("Plant psychological cues in nearby players' minds.", TextColorHelper.WHITE)
                .line("Create immediate mental effects on targets within your range. (or yourself)", TextColorHelper.WHITE)

                .spacing()
                .header("How to Use", TextColorHelper.GOLD)
                .line("1. Activate the ability", TextColorHelper.WHITE)
                .line("2. Type in chat: if <player_name> <trigger> then <action>", TextColorHelper.WHITE)
                .line("3. Target must be within range or cue fails", TextColorHelper.WHITE)
                .line("4. Cannot cue significantly stronger targets", TextColorHelper.WHITE)

                .spacing()
                .header("Available Triggers", TextColorHelper.GOLD)
                .ability("instant", "Activates immediately when created (Only at Sequence 4 unlocked)", TextColorHelper.GOLD, TextColorHelper.WHITE)
                .ability("on <x y z>", "Triggers when player reaches coordinates", TextColorHelper.GOLD, TextColorHelper.WHITE)
                .ability("has <item>", "Triggers when player picks up item", TextColorHelper.GOLD, TextColorHelper.WHITE)
                .ability("health <amount>", "Triggers when player health drops below amount (Only at Sequence 4 unlocked)", TextColorHelper.GOLD, TextColorHelper.WHITE)
                .ability("sanity <amount>", "Triggers when sanity drops below amount (Only at Sequence 4 unlocked)", TextColorHelper.GOLD, TextColorHelper.WHITE)
                .ability("player <name>", "Triggers when another player is nearby (Only at Sequence 6 unlocked)", TextColorHelper.GOLD, TextColorHelper.WHITE)

                .spacing()
                .header("Available Actions", TextColorHelper.GOLD)
                .ability("drop", "Target drops item", TextColorHelper.GOLD, TextColorHelper.WHITE)
                .ability("stun", "Stuns target (Only at Sequence 4 unlocked)", TextColorHelper.GOLD, TextColorHelper.WHITE)
                .ability("confusion", "Confuses target (Only at Sequence 4 unlocked)", TextColorHelper.GOLD, TextColorHelper.WHITE)
                .ability("skill <skill>", "Makes target use skill (Only at Sequence 4 unlocked)", TextColorHelper.GOLD, TextColorHelper.WHITE)
                .ability("say <message>", "Makes target say something in chat (Only at Sequence 4 unlocked)", TextColorHelper.GOLD, TextColorHelper.WHITE)

                .spacing()
                .header("Examples", TextColorHelper.GOLD)
                .line("if Steve on 0 64 0 then drop", TextColorHelper.CYAN)

                .spacing()
                .warning("Target must be within distance limit or cue fails!")
                .warning("Attempting to cue stronger targets triggers 'Losing Control' debuff (25 seconds)")

                .spacing()
                .separator()
                .spacing()
                .line("Press ESC to close this menu", TextColorHelper.DARK_GRAY)

                .build();
    }
}