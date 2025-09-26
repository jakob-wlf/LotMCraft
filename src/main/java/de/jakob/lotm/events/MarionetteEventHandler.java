package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.entity.custom.goals.MarionetteGoal;
import de.jakob.lotm.util.helper.marionettes.MarionetteComponent;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class MarionetteEventHandler {
    
    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Mob mob && !event.getLevel().isClientSide) {
            MarionetteComponent component = mob.getData(ModAttachments.MARIONETTE_COMPONENT.get());
            
            // Re-add marionette goals if this entity is a marionette
            if (component.isMarionette()) {
                mob.goalSelector.addGoal(0, new MarionetteGoal(mob));
            }
        }
    }
}