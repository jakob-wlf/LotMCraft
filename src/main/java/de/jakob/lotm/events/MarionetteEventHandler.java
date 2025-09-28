package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.entity.custom.goals.MarionetteFollowGoal;
import de.jakob.lotm.entity.custom.goals.MarionetteStayGoal;
import de.jakob.lotm.entity.custom.goals.MarionetteTargetGoal;
import de.jakob.lotm.util.helper.marionettes.MarionetteComponent;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.DefendVillageTargetGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
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
                mob.targetSelector.removeAllGoals(goal ->
                        goal instanceof StrollThroughVillageGoal ||
                        goal instanceof BreedGoal ||
                        goal instanceof MoveToBlockGoal ||
                        goal instanceof PanicGoal ||
                        goal instanceof RandomStrollGoal ||
                        goal instanceof TargetGoal
                );

                mob.goalSelector.addGoal(0, new MarionetteFollowGoal(mob));    // Highest priority for following
                mob.goalSelector.addGoal(1, new MarionetteStayGoal(mob));    // Highest priority for following
                mob.targetSelector.addGoal(0, new MarionetteTargetGoal(mob));  // Highest priority for targeting
                mob.setTarget(null);
            }
        }
    }
}