package de.jakob.lotm.addons.rituals.fool;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.addons.rituals.RitualDescriptionHelper;
import de.jakob.lotm.addons.rituals.RitualEffectHandlerEvent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.checkerframework.checker.units.qual.A;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq2 {
    private static final int AMOUNT = 255;
    private static final Set<UUID> TRACKED_SNIFFERS = new HashSet<>();

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!(player.level() instanceof ServerLevel level))return;
        if(!BeyonderData.getPathway(player).equals("fool") ||
                BeyonderData.getSequence(player) != 3) return;

        var component = player.getData(ModAttachments.RITUALS.get());

        if(TRACKED_SNIFFERS.size() >= AMOUNT){
            component.setCompleted(true);
        }
        else{
            if(component.isCompleted()){
                RitualEffectHandlerEvent.removeRitualWithMessage(player);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Sniffer sniffer && event.getLevel() instanceof ServerLevel level) {
            if (level.dimension() == Level.OVERWORLD) {
                TRACKED_SNIFFERS.add(sniffer.getUUID());
            }
        }
    }

    @SubscribeEvent
    private static void onDeath(LivingDeathEvent event){
        if (event.getEntity() instanceof Sniffer sniffer) {
            TRACKED_SNIFFERS.remove(sniffer.getUUID());
        }
    }
}
