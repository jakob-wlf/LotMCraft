package de.jakob.lotm.beyonders.abilities.visionary.prophecy;

import de.jakob.lotm.gui.custom.TextDisplay.AbilityMenuBuilder;
import de.jakob.lotm.gui.custom.TextDisplay.ColoredTextDisplayScreen;
import de.jakob.lotm.gui.custom.TextDisplay.TextColorHelper;
import net.minecraft.client.gui.screens.Screen;

public class VisionaryAbilityMenus {

    public static ColoredTextDisplayScreen createStoryWritingAbilityMenu(Screen previousScreen, int sequence) {
        AbilityMenuBuilder builder = new AbilityMenuBuilder("Story Writing", previousScreen)
                .header("Overview", TextColorHelper.GOLD)
                .line("Write prophecies into existence. Type natural language commands", TextColorHelper.WHITE)
                .line("to create persistent conditional effects on online or offline players.", TextColorHelper.WHITE)

                .spacing()
                .header("How to Use", TextColorHelper.GOLD)
                .line("1. Activate the ability", TextColorHelper.WHITE)
                .line("2. Type in chat: if <player_name> <trigger> then <action>", TextColorHelper.WHITE)
                .line("3. Use self instead of a player name to target yourself", TextColorHelper.WHITE)
                .line("4. A success or detailed failure message appears in chat", TextColorHelper.WHITE);

        addAvailableSyntax(builder, sequence);
        return finish(builder);
    }

    public static ColoredTextDisplayScreen createPsychologicalCueAbilityMenu(Screen previousScreen, int sequence) {
        AbilityMenuBuilder builder = new AbilityMenuBuilder("Psychological Cue", previousScreen)
                .header("Overview", TextColorHelper.GOLD)
                .line("Plant psychological cues in nearby players' minds.", TextColorHelper.WHITE)
                .line("Create conditional mental effects on targets within your range, including yourself.", TextColorHelper.WHITE)

                .spacing()
                .header("How to Use", TextColorHelper.GOLD)
                .line("1. Activate the ability", TextColorHelper.WHITE)
                .line("2. Type in chat: if <player_name> <trigger> then <action>", TextColorHelper.WHITE)
                .line("3. Use self instead of a player name to target yourself", TextColorHelper.WHITE)
                .line("4. Target must be online and within range", TextColorHelper.WHITE)
                .line("5. Cannot cue significantly stronger targets", TextColorHelper.WHITE)
                .line("6. A success or detailed failure message appears in chat", TextColorHelper.WHITE);

        addAvailableSyntax(builder, sequence);
        builder.spacing()
                .warning("Target must be within distance limit or cue fails!")
                .warning("Cueing stronger targets causes Losing Control for 25 seconds");

        return finish(builder);
    }

    private static void addAvailableSyntax(AbilityMenuBuilder builder, int sequence) {
        builder.spacing().header("Available Triggers", TextColorHelper.GOLD);
        trigger(builder, sequence, 7, "on <x y z> [range]", "Target reaches the position");
        trigger(builder, sequence, 7, "has <item>", "Target has the item");
        trigger(builder, sequence, 6, "health <amount> [operation]", "Health matches the threshold");
        trigger(builder, sequence, 6, "player <name...>", "Named player is nearby");
        trigger(builder, sequence, 6, "hunger <amount> [operation]", "Hunger matches the threshold");
        trigger(builder, sequence, 6, "riding", "Target starts riding something");
        trigger(builder, sequence, 6, "light <level> [operation]", "Light level matches the threshold");
        trigger(builder, sequence, 6, "asleep", "Target falls asleep");
        trigger(builder, sequence, 4, "sanity <amount> [operation]", "Sanity matches the threshold");
        trigger(builder, sequence, 4, "spirituality <amount> [operation]", "Spirituality matches the threshold");
        trigger(builder, sequence, 1, "sealed", "Target becomes sealed");
        trigger(builder, sequence, 1, "sequence <number> [operation]", "Sequence matches the value");
        trigger(builder, sequence, 1, "pathway <pathway>", "Target follows the pathway");
        trigger(builder, sequence, 1, "become <pathway> <sequence>", "Target becomes the pathway and sequence");
        trigger(builder, sequence, -1, "instant", "Activates immediately");

        builder.spacing().header("Available Actions", TextColorHelper.GOLD);
        action(builder, sequence, 7, "drop <item|all>", "Drops matching inventory items");
        action(builder, sequence, 7, "whisper <message>", "Sends an unsettling whisper");
        action(builder, sequence, 6, "skill <ability>", "Makes the target use an ability");
        action(builder, sequence, 6, "say <message>", "Makes the target speak");
        action(builder, sequence, 6, "joy", "Applies Joy for 15 minutes");
        action(builder, sequence, 6, "anger", "Applies Anger for 15 minutes");
        action(builder, sequence, 6, "sleep", "Applies Sleep for 10 seconds");
        action(builder, sequence, 4, "stun", "Stuns the target");
        action(builder, sequence, 4, "confusion", "Confuses the target");
        action(builder, sequence, 4, "plague <type>", "Applies a plague effect");
        action(builder, sequence, 4, "double <action> and <action>", "Runs two actions");
        action(builder, sequence, 4, "player <name>", "Redirects the action to another player");
        action(builder, sequence, 4, "suicide", "Forces the target to kill itself");
        action(builder, sequence, 1, "calamity <type>", "Creates meteor, tornado, earthquake, or plague");
        action(builder, sequence, 1, "seal", "Seals the target");
        action(builder, sequence, 1, "unseal", "Removes a seal");
        action(builder, sequence, 1, "countforritual", "Counts a fulfilled prediction for the ritual");
        action(builder, sequence, 0, "teleport <x y z>", "Teleports the target");
        action(builder, sequence, 0, "digest <amount>", "Changes digestion progress");
        action(builder, sequence, 0, "sanity <amount>", "Changes sanity");
        action(builder, sequence, 0, "spawn <entity>", "Spawns an entity at the target");
        action(builder, sequence, 0, "weather <clear|rain|thunder>", "Changes weather");
        action(builder, sequence, 0, "time <day|night|noon|midnight>", "Changes time");
        action(builder, sequence, 0, "spirituality <amount>", "Changes spirituality");
        action(builder, sequence, -1, "health <amount>", "Damages or heals the target");

        builder.spacing().header("Examples", TextColorHelper.GOLD);
        example(builder, sequence, 7, "if Steve has minecraft:apple then drop minecraft:apple");
        example(builder, sequence, 6, "if self health 20 then joy");
        example(builder, sequence, 6, "if Alex asleep then anger");
        example(builder, sequence, 6, "if Alex health 10 then sleep");
        example(builder, sequence, 4, "if Alex sanity 0.5 then stun");
        example(builder, sequence, 1, "if Alex become fool 3 then countforritual");
        example(builder, sequence, 1, "if Alex sealed then calamity meteor");
        example(builder, sequence, 0, "if self health 10 then teleport 0 100 0");
        example(builder, sequence, -1, "if self instant then health 20");
        builder.line("Use operation -2, -1, 0, 1, or 2 for <, <=, =, >=, or >.", TextColorHelper.DARK_GRAY)
                .line("Replace names with exact player names; use self for yourself.", TextColorHelper.DARK_GRAY);
    }

    private static void trigger(AbilityMenuBuilder builder, int sequence, int requiredSequence,
                                String syntax, String description) {
        if (sequence <= requiredSequence) {
            builder.ability(syntax, description, TextColorHelper.GOLD, TextColorHelper.WHITE);
        }
    }

    private static void action(AbilityMenuBuilder builder, int sequence, int requiredSequence,
                               String syntax, String description) {
        if (sequence <= requiredSequence) {
            builder.ability(syntax, description, TextColorHelper.GOLD, TextColorHelper.WHITE);
        }
    }

    private static void example(AbilityMenuBuilder builder, int sequence, int requiredSequence, String text) {
        if (sequence <= requiredSequence) {
            builder.line(text, TextColorHelper.CYAN);
        }
    }

    private static ColoredTextDisplayScreen finish(AbilityMenuBuilder builder) {
        return builder.spacing()
                .separator()
                .spacing()
                .line("Press ESC to close this menu", TextColorHelper.DARK_GRAY)
                .build();
    }
}