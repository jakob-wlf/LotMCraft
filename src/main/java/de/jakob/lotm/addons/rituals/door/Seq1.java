package de.jakob.lotm.addons.rituals.door;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.addons.rituals.RitualEffectHandlerEvent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.potions.BeyonderCharacteristicItem;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.item.custom.MoonItem;
import de.jakob.lotm.item.custom.SunItem;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerLevel;
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
public class Seq1 {
    private static Map<UUID, Integer> timer = new HashMap<>();
    private static final int TIME_SEC = 20 * 60 * 35;

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!BeyonderData.getPathway(player).equals("door") ||
                BeyonderData.getSequence(player) != 2) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        var component = player.getData(ModAttachments.RITUALS.get());

        if(!timer.containsKey(player.getUUID())){
            timer.put(player.getUUID(), 0);
        }

        boolean haveSun = false;
        boolean haveMoon = false;
        for (ItemStack stack : player.getInventory().items) {

            if (stack.getItem() instanceof SunItem) {
                haveSun = true;
            }
            if (stack.getItem() instanceof MoonItem) {
                haveMoon = true;
            }
        }

        if (haveMoon && haveSun) {
            timer.put(player.getUUID(), timer.get(player.getUUID()) + 1);

            if(timer.get(player.getUUID()) >= TIME_SEC) {
                if(level.dimension() == ModDimensions.SPACE_DIMENSION_KEY) {
                    component.setCompleted(true);
                    timer.remove(player.getUUID());
                }
            }
        } else {
            RitualEffectHandlerEvent.removeRitual(player);
            timer.remove(player.getUUID());
        }

    }

}
