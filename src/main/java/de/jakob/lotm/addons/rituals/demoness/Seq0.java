package de.jakob.lotm.addons.rituals.demoness;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.addons.rituals.RitualEffectHandlerEvent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.potions.BeyonderCharacteristicItem;
import de.jakob.lotm.item.custom.MoonItem;
import de.jakob.lotm.item.custom.SunItem;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq0 {
    private static final int TIME_SEC = 20 * 60 * 60 * 2;

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!BeyonderData.getPathway(player).equals("demoness") ||
                BeyonderData.getSequence(player) != 1) return;
        if(!BeyonderData.hasUniqueness(player)) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if (component.isCompleted()) return;

        boolean haveSun = false;
        boolean haveVis = false;
        boolean haveTyrant = false;

        for (ItemStack stack : player.getInventory().items) {
            if(stack.getItem() instanceof BeyonderCharacteristicItem item){
                if(item.getSequence() == 1){
                    switch (item.getPathway()){
                        case "visionary" -> haveVis = true;
                        case "tyrant" -> haveTyrant = true;
                        case "sun" -> haveSun = true;
                    }
                }
            }
        }

        if (haveTyrant && haveSun && haveVis) {
            component.setStage(component.getStage() + 1);

            if (component.getStage() >= TIME_SEC) {
                component.setCompleted(true);
            }
        } else {
            component.setStage(0);
            RitualEffectHandlerEvent.removeRitual(player);
        }
    }
}
