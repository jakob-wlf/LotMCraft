package de.jakob.lotm.util.helper;


import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.effect.ModEffects;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class ClientCorruptionCache {

    @SubscribeEvent
    public static void onMovementInputUpdate(MovementInputUpdateEvent event) {
        if (!(event.getEntity() instanceof LocalPlayer player)) return;
        if (!player.hasEffect(ModEffects.CORRUPTED)) return;

        Input input = event.getInput();

        input.forwardImpulse = 0;
        input.leftImpulse = 0;

        input.jumping = false;
        input.shiftKeyDown = false;

        input.up = false;
        input.down = false;

        input.left = false;
        input.right = false;
    }
}
