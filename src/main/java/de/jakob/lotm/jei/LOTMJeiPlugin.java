// FILE 2: LOTMJeiPlugin.java
// Create this in your mod's jei integration package
package de.jakob.lotm.jei;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.gui.custom.ability_wheel.AbilityWheelScreen;
import de.jakob.lotm.gui.custom.artifact_wheel.ArtifactWheelScreen;
import de.jakob.lotm.beyonders.potions.PotionRecipeItemHandler;
import de.jakob.lotm.gui.custom.flaming_jump.FlamingJumpScreen;
import de.jakob.lotm.gui.custom.introspect.IntrospectScreen;
import de.jakob.lotm.gui.custom.marionettes.MarionetteControlScreen;
import de.jakob.lotm.gui.custom.mass_puppeteering.MassPuppeteeringScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

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

        registration.addGuiContainerHandler(ArtifactWheelScreen.class, new IGuiContainerHandler<ArtifactWheelScreen>() {
            @Override
            public List<Rect2i> getGuiExtraAreas(ArtifactWheelScreen screen) {
                // Return a rectangle covering the entire screen to hide JEI completely
                return Collections.singletonList(new Rect2i(0, 0, screen.width, screen.height));
            }
        });

        registration.addGuiContainerHandler(MarionetteControlScreen.class, new IGuiContainerHandler<MarionetteControlScreen>() {
            @Override
            public List<Rect2i> getGuiExtraAreas(MarionetteControlScreen screen) {
                // Return a rectangle covering the entire screen to hide JEI completely
                return Collections.singletonList(new Rect2i(0, 0, screen.width, screen.height));
            }
        });

        registration.addGuiContainerHandler(MassPuppeteeringScreen.class, new IGuiContainerHandler<MassPuppeteeringScreen>() {
            @Override
            public List<Rect2i> getGuiExtraAreas(MassPuppeteeringScreen screen) {
                // Return a rectangle covering the entire screen to hide JEI completely
                return Collections.singletonList(new Rect2i(0, 0, screen.width, screen.height));
            }
        });

        registration.addGuiContainerHandler(FlamingJumpScreen.class, new IGuiContainerHandler<FlamingJumpScreen>() {
            @Override
            public List<Rect2i> getGuiExtraAreas(FlamingJumpScreen screen) {
                // Return a rectangle covering the entire screen to hide JEI completely
                return Collections.singletonList(new Rect2i(0, 0, screen.width, screen.height));
            }
        });
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        List<ItemStack> recipeItemsToHide = PotionRecipeItemHandler.ITEMS.getEntries().stream()
                .map(holder -> new ItemStack(holder.get()))
                .toList();

        jeiRuntime.getIngredientManager().removeIngredientsAtRuntime(
                VanillaTypes.ITEM_STACK,
                recipeItemsToHide
        );
    }
}