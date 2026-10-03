package de.jakob.lotm.util.helper;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.effect.ModEffects;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;

/**
 * Client-only handler for the Fooling effect's input inversion and suppression.
 * Kept separate from FoolingEffect so the server never loads client-only classes.
 *
 * Uses MovementInputUpdateEvent to invert movement smoothly after Minecraft has
 * already computed the input vector from key states.
 */
@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class ClientFoolingCache {

    @SubscribeEvent
    public static void onMovementInputUpdate(MovementInputUpdateEvent event) {
        if (!(event.getEntity() instanceof LocalPlayer player)) return;
        if (!player.hasEffect(ModEffects.FOOLING)) return;

        Input input = event.getInput();

        // Invert: forward↔back, left↔right, jump↔sneak
        input.forwardImpulse = -input.forwardImpulse;
        input.leftImpulse = -input.leftImpulse;

        boolean wasJumping = input.jumping;
        input.jumping = input.shiftKeyDown;
        input.shiftKeyDown = wasJumping;

        boolean wasUp = input.up;
        input.up = input.down;
        input.down = wasUp;

        boolean wasLeft = input.left;
        input.left = input.right;
        input.right = wasLeft;
    }
}
