// FILE 2: LOTMJeiPlugin.java
// Create this in your mod's jei integration package
package de.jakob.lotm.jei;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.gui.custom.AbilityWheel.AbilityWheelScreen;
import de.jakob.lotm.gui.custom.Introspect.IntrospectScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.List;

@JeiPlugin
public class LOTMJeiPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        // Register handler for IntrospectScreen to hide JEI
        registration.addGuiContainerHandler(IntrospectScreen.class, new IGuiContainerHandler<IntrospectScreen>() {
            @Override
            public List<Rect2i> getGuiExtraAreas(IntrospectScreen screen) {
                // Return a rectangle covering the entire screen to hide JEI completely
                return Collections.singletonList(new Rect2i(0, 0, screen.width, screen.height));
            }
        });

        registration.addGuiContainerHandler(AbilityWheelScreen.class, new IGuiContainerHandler<AbilityWheelScreen>() {
            @Override
            public List<Rect2i> getGuiExtraAreas(AbilityWheelScreen screen) {
                // Return a rectangle covering the entire screen to hide JEI completely
                return Collections.singletonList(new Rect2i(0, 0, screen.width, screen.height));
            }
        });
    }
}