package de.jakob.lotm.beyonders.abilities.wheel_of_fortune;

import de.jakob.lotm.gui.custom.TextDisplay.AbilityMenuBuilder;
import de.jakob.lotm.gui.custom.TextDisplay.ColoredTextDisplayScreen;
import de.jakob.lotm.gui.custom.TextDisplay.TextColorHelper;
import net.minecraft.client.gui.screens.Screen;

public final class WheelOfFortuneAbilityMenus {
    private WheelOfFortuneAbilityMenus() {
    }

    public static ColoredTextDisplayScreen createProphecyAbilityMenu(Screen previousScreen) {
        return new AbilityMenuBuilder("Prophecy", previousScreen)
                .header("Overview", TextColorHelper.GOLD)
                .line("Write prophecies into existence by sacrificing luck.", TextColorHelper.WHITE)
                .line("The paid luck directly selects a power tier and scales its effect.", TextColorHelper.WHITE)

                .spacing()
                .header("How to Use", TextColorHelper.GOLD)
                .line("1. Use Prophecy while not sneaking to enter writing mode.", TextColorHelper.WHITE)
                .line("2. Write the prophecy in chat using the exact format below.", TextColorHelper.WHITE)
                .line("3. Send the message. Valid prophecy text is not broadcast.", TextColorHelper.WHITE)
                .line("4. Use Prophecy again to leave writing mode.", TextColorHelper.WHITE)
                .ability("Format", "<target> will be affected by <modifier> at the price of <amount> luck [in <minutes> mins]",
                        TextColorHelper.GOLD, TextColorHelper.CYAN)
                .line("The timer is optional and may be 1–60 minutes.", TextColorHelper.WHITE)
                .line("Only one pending or active prophecy can occupy a target at once.", TextColorHelper.WHITE)
                .line("Cooldown begins when the prophecy takes hold, not when it is written.", TextColorHelper.WHITE)
                .line("Only the caster learns the selected outcome and duration.", TextColorHelper.WHITE)

                .spacing()
                .header("Targets", TextColorHelper.GOLD)
                .ability("Player name", "Targets one online player", TextColorHelper.GOLD, TextColorHelper.WHITE)
                .ability("Nearby", "Targets players within 64 blocks", TextColorHelper.GOLD, TextColorHelper.WHITE)
                .ability("All", "Targets all online players; requires Sequence 0 and Key of Light",
                        TextColorHelper.GOLD, TextColorHelper.WHITE)

                .spacing()
                .header("Modifiers", TextColorHelper.GOLD)
                .ability("fortune", "Creates a beneficial fate", TextColorHelper.GOLD, TextColorHelper.WHITE)
                .ability("misfortune", "Creates a harmful fate", TextColorHelper.GOLD, TextColorHelper.WHITE)
                .ability("disaster", "Immediately creates a tornado, earthquake, or meteor",
                        TextColorHelper.GOLD, TextColorHelper.WHITE)
                .ability("convergence", "Forces an eligible Sefirot convergence",
                        TextColorHelper.GOLD, TextColorHelper.WHITE)

                .spacing()
                .header("Luck Tiers", TextColorHelper.GOLD)
                .line("Tiers use the luck actually consumed. Each tier has an exclusive outcome pool.",
                        TextColorHelper.WHITE)
                .ability("Low Fortune", "5% to below 35% of adjusted full price",
                        TextColorHelper.GOLD, TextColorHelper.WHITE)
                .ability("Low Misfortune", "10% to below 35% of adjusted full price",
                        TextColorHelper.GOLD, TextColorHelper.WHITE)
                .ability("Medium", "35% to below 70% of adjusted full price",
                        TextColorHelper.GOLD, TextColorHelper.WHITE)
                .ability("High", "70% or more of adjusted full price", TextColorHelper.GOLD, TextColorHelper.WHITE)
                .line("Stronger targets raise the price through Sequence resistance.", TextColorHelper.WHITE)
                .line("A higher-Sequence target requires at least 80% maximum luck.", TextColorHelper.WHITE)
                .line("Its success chance is 15%, rising to 30% at full luck.", TextColorHelper.WHITE)
                .line("A Sefirot owner requires 100% maximum luck and has a 10% chance.", TextColorHelper.WHITE)
                .line("If also higher-Sequence, that chance falls to 5%.", TextColorHelper.WHITE)
                .line("Concealment and protected realms make a target unreachable.", TextColorHelper.WHITE)

                .spacing()
                .header("Examples", TextColorHelper.GOLD)
                .line("Steve will be affected by fortune at the price of 5000 luck", TextColorHelper.CYAN)
                .line("Nearby will be affected by misfortune at the price of 10000 luck", TextColorHelper.CYAN)
                .line("Alex will be affected by disaster at the price of 12500 luck", TextColorHelper.CYAN)
                .line("Steve will be affected by misfortune at the price of 10000 luck in 5 mins", TextColorHelper.CYAN)

                .spacing()
                .warning("The luck price is spent once even when some selected targets resist.")
                .line("Press ESC to close this menu", TextColorHelper.DARK_GRAY)
                .build();
    }
}