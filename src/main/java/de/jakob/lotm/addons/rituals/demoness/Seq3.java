package de.jakob.lotm.addons.rituals.demoness;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.*;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq3 {
    private static int NEEDED = 20 * 5; //20 * 15 * 60;
    private static Map<UUID, Integer> timer = new HashMap<>();
    private static Map<UUID, UUID> self = new HashMap<>();

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!BeyonderData.getPathway(player).equals("demoness") ||
        BeyonderData.getSequence(player) != 4) return;
        if(!(player.level() instanceof ServerLevel level)) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;

        if(component.getStage() == 1){
            component.setCompleted(true);
            self.remove(player.getUUID());
        }

        if(self.containsKey(player.getUUID())) return;

        if(!timer.containsKey(player.getUUID())){
            timer.put(player.getUUID(), 0);
        }

        if(player.level().dimension().equals(ModDimensions.SPIRIT_WORLD_DIMENSION_KEY)) {
                timer.put(player.getUUID(), timer.get(player.getUUID()) + 1);
        }

        if(timer.get(player.getUUID()) >= NEEDED){
            Vec3 behind = player.position().subtract(player.getLookAngle().normalize().scale(3));

            var entity = new BeyonderNPCEntity(ModEntities.BEYONDER_NPC.get(), level,
                    true, "demoness", 4);

            entity.setPos(behind.x, behind.y, behind.z);
            entity.setTarget(player);
            entity.setCustomName(Component.literal(player.getName().getString() + "'s mirror self"));
            entity.setShouldDrop(false);

            player.level().addFreshEntity(entity);
            timer.remove(player.getUUID());
            self.put(player.getUUID(), entity.getUUID());
        }
    }

    @SubscribeEvent
    private static void onDeath(LivingDeathEvent event){
        if(!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if(!BeyonderData.getPathway(player).equals("demoness") ||
                BeyonderData.getSequence(player) != 4) return;
        if(!(event.getEntity() instanceof BeyonderNPCEntity npc)) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;

        if(!self.containsKey(player.getUUID())) return;
        if(npc.getUUID().equals(self.get(player.getUUID()))){
            component.setStage(1);
        }
    }
}
