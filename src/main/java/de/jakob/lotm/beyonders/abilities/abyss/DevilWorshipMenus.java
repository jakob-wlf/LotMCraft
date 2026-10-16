package de.jakob.lotm.beyonders.abilities.abyss;

import de.jakob.lotm.gui.custom.text_display.AbilityMenuBuilder;
import de.jakob.lotm.gui.custom.text_display.ColoredTextDisplayScreen;
import de.jakob.lotm.gui.custom.text_display.TextColorHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class DevilWorshipMenus {
    public static void open() {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(createRitualDetailsMenu(mc.screen));
    }
    public static ColoredTextDisplayScreen createRitualDetailsMenu(Screen previousScreen) {
        return new AbilityMenuBuilder("Devil Worship", previousScreen)
                .header("Overview", TextColorHelper.DARK_RED)
                .line("Choose a mob to hunt as tribute. The more of them you kill,", TextColorHelper.DARK_GREEN)
                .line("the more the Devils are pleased. When they are fully pleased,", TextColorHelper.DARK_GREEN)
                .line("perform the ritual to call a projection of a Devil.", TextColorHelper.DARK_GREEN)
                .spacing()
                .header("The Ritual (a riddle)", TextColorHelper.DARK_RED)
                .line("A flame wrapped in black wax lights the way down.", TextColorHelper.DARK_GREEN)
                .line("On the Right 4 pinches of the Earths Blood.", TextColorHelper.DARK_GREEN)
                .line("On the Left 5 bones of those who no longer run ", TextColorHelper.DARK_GREEN)
                .line("In the center a stone that cries.", TextColorHelper.DARK_GREEN)
                .line("And then Call Them", TextColorHelper.DARK_GREEN)
                .line("Demons of filth who feast upon soul", TextColorHelper.DARK_GREEN)
                .line("Who Temp the foolish into the Abyss", TextColorHelper.DARK_GREEN)
                .line("Lend me a shadow of yourselves and answer my call", TextColorHelper.DARK_GREEN)
                .spacing()
                .spacing()
                .spacing()
                .line("Press ESC to close this menu", TextColorHelper.DARK_GRAY)
                .build();
    }
}