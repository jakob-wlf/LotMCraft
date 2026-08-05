package de.jakob.lotm.util.helper;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.AbilityBarComponent;
import de.jakob.lotm.attachments.AbilityWheelComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.visionary.handlers.VisionaryHandler;
import de.jakob.lotm.attachments.*;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class PureIdealismUtil {

    public static HashMap<UUID, String> died = new HashMap<>();

    public static void startDiscernment(LivingEntity entity, String path, int sequence){
        var component = entity.getData(ModAttachments.DISCERNMENT_DATA.get());

        if(!(entity instanceof ServerPlayer player)) return;
        if(component.isDiscerning()) return;
        if(!component.hasSaved(path, sequence)) return;

        boolean shouldDie = BeyonderData.getSequence(entity) > 0 && sequence <= 0;

        AbilityWheelComponent wheelData = entity.getData(ModAttachments.ABILITY_WHEEL_COMPONENT);
        AbilityBarComponent barData = entity.getData(ModAttachments.ABILITY_BAR_COMPONENT);

        component.setPreviousWheel(wheelData.copy());
        component.setPreviousBar(barData.copy());

        var pair = component.getAbilitiesInBars(path, sequence);
        AbilityWheelComponent savedWheelData = pair.getFirst();
        AbilityBarComponent savedBarData = pair.getSecond();

        component.setPreviosSeq(BeyonderData.getSequence(entity));
        component.setSeq(sequence);
        component.setPathway(path);

        // Temporary discernment form, not a real advancement — must not trigger the acting cap
        de.jakob.lotm.beyonders.acting.ActingCapHelper.skipNextCapApplication = true;
        try {
            BeyonderData.setBeyonder(entity, path, sequence,true, false, false, false, true, false);
        } finally {
            de.jakob.lotm.beyonders.acting.ActingCapHelper.skipNextCapApplication = false;
        }
        component.setDiscerning(true);

        wheelData.setAbilities(savedWheelData.getAbilities());
        AbilityWheelHelper.syncToClient(player);

        barData.setAbilities(savedBarData.getAbilities());

        component.syncData(player);

        if(shouldDie){
            ServerScheduler.scheduleDelayed(20 * getDurationPerSeq(BeyonderData.getSequence(entity)), () -> {stopDiscernment(entity);});
        }

    }

    public static void stopDiscernment(LivingEntity entity){
        var component = entity.getData(ModAttachments.DISCERNMENT_DATA.get());

        if(!(entity instanceof ServerPlayer player)) return;
        if(!component.isDiscerning()) return;

        AbilityWheelComponent wheelData = entity.getData(ModAttachments.ABILITY_WHEEL_COMPONENT);
        AbilityBarComponent barData = entity.getData(ModAttachments.ABILITY_BAR_COMPONENT);

        component.updateAbilitiesForCurrent(wheelData, barData);

        boolean shouldDie = (component.getSeq() <= 0 && component.getPreviosSeq() > 0);

        String path = component.getPathway();

        component.setSeq(LOTMCraft.NON_BEYONDER_SEQ);
        component.setPathway("none");

        // Restoring the real form after discernment — must not trigger the acting cap
        de.jakob.lotm.beyonders.acting.ActingCapHelper.skipNextCapApplication = true;
        try {
            BeyonderData.setBeyonder(entity, "visionary", component.getPreviosSeq(),true, false, false, false, true, false);
        } finally {
            de.jakob.lotm.beyonders.acting.ActingCapHelper.skipNextCapApplication = false;
        }
        component.setDiscerning(false);

        wheelData.setAbilities(component.getPreviousWheel().getAbilities());
        AbilityWheelHelper.syncToClient(player);
        barData.setAbilities(component.getPreviousBar().getAbilities());

        component.syncData(player);

        if(shouldDie) {
            entity.kill();
            died.put(entity.getUUID(), path);
        }
    }

    private static int getDurationPerSeq(int seq){
        return switch (seq){
            case 2 -> 240;
            case 1 -> 560;
            default -> 1;
        };
    }

    @SubscribeEvent
    public static void onPlayerLogout (PlayerEvent.PlayerLoggedOutEvent event){
        Player player = event.getEntity();
        if (player.level() instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            var component = serverPlayer.getData(ModAttachments.DISCERNMENT_DATA.get());

            if(component.isDiscerning()){
                stopDiscernment(serverPlayer);
            }
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event){
        var entity = event.getEntity();
        if (entity.level() instanceof ServerLevel serverLevel && entity instanceof ServerPlayer serverPlayer) {
            var component = serverPlayer.getData(ModAttachments.DISCERNMENT_DATA.get());

            if(component.isDiscerning()){
                stopDiscernment(serverPlayer);
            }

            if(BeyonderData.getPathway(serverPlayer).equals("visionary") && BeyonderData.getSequence(serverPlayer) >= 3){
                component.clearAll();
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        var entity = event.getEntity();

        if(!(entity instanceof ServerPlayer player)) return;

        var component = player.getData(ModAttachments.DISCERNMENT_DATA.get());
        if(!component.isDiscerning()) return;

        float max = player.getMaxHealth();
        float current = player.getHealth();
        float limit = max * 0.6f;

        if(current <= limit){
            PureIdealismUtil.stopDiscernment(player);
        }

        if(VisionaryHandler.shouldBeAffectedWithMindWorldSeal(component.getPreviosSeq())){
            PureIdealismUtil.stopDiscernment(player);
        }
    }
}
