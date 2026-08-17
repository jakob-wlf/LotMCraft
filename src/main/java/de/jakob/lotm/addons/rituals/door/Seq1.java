package de.jakob.lotm.addons.rituals.door;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.addons.rituals.RitualEffectHandlerEvent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.potions.BeyonderCharacteristicItem;
import de.jakob.lotm.item.custom.MoonItem;
import de.jakob.lotm.item.custom.SunItem;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq1 {

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!BeyonderData.getPathway(player).equals("door") ||
                BeyonderData.getSequence(player) != 2) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        var component = player.getData(ModAttachments.RITUALS.get());

        boolean haveSun = false;
        boolean haveMoon = false;
        for (ItemStack stack : player.getInventory().items) {

            if (stack.getItem() instanceof SunItem sunItem) {
                haveSun = true;
            }
            if (stack.getItem() instanceof MoonItem moon) {
                haveMoon = true;
            }
        }


        if (haveMoon && haveSun) {
            component.setCompleted(true);
        } else {
            RitualEffectHandlerEvent.removeRitual(player);
        }

    }

}
