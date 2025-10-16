package de.jakob.lotm.abilities.fool;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.SyncSelectedMarionettePacket;
import de.jakob.lotm.util.helper.marionettes.MarionetteComponent;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.*;

public class MarionetteControllingAbility extends AbilityItem {

    private static final Map<UUID, Integer> marionetteIndices = new HashMap<>();

    public MarionetteControllingAbility(Properties properties) {
        super(properties, 1);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("fool", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 0;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;


    }

    private ArrayList<LivingEntity> getMarionettesOfPlayerInLevelOrderedById(LivingEntity entity) {
        Level level = entity.level();
        if(level.isClientSide)
            return new ArrayList<>();

        final ArrayList<LivingEntity> marionettes = new ArrayList<>(level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(100000)));
        marionettes.removeIf(e -> {
            if(e == entity)
                return true;
            MarionetteComponent component = e.getData(ModAttachments.MARIONETTE_COMPONENT.get());
            if (!component.isMarionette()) {
                return true;
            }

            if(!component.getControllerUUID().equals(entity.getStringUUID())) {
                return true;
            }

            return false;
        });

        marionettes.sort(Comparator.comparingInt(LivingEntity::getId));
        return marionettes;
    }

    //TODO: Cache entities and only check every 2 seconds or so
    @Override
    public void onHold(Level level, LivingEntity entity) {
        if(level.isClientSide || !(entity instanceof ServerPlayer player))
            return;
        List<LivingEntity> marionettes = getMarionettesOfPlayerInLevelOrderedById(entity);
        //Increment index if shift key is down
        if(entity.isShiftKeyDown()) {
            int currentIndex = marionetteIndices.getOrDefault(entity.getUUID(), 0);
            currentIndex++;
            if(currentIndex >= marionettes.size())
                currentIndex = 0;
            marionetteIndices.put(entity.getUUID(), currentIndex);
        }

        int index = marionetteIndices.getOrDefault(entity.getUUID(), 0);
        //When no marionette or invalid marionette selected make sure no overlay gets rendered
        if(marionettes.isEmpty() || index >= marionettes.size()) {
            SyncSelectedMarionettePacket packet = new SyncSelectedMarionettePacket(false, -1);
            PacketHandler.sendToPlayer(player, packet);
            return;
        }

        LivingEntity marionette = marionettes.get(index);
        SyncSelectedMarionettePacket packet = new SyncSelectedMarionettePacket(true, marionette.getId());
        PacketHandler.sendToPlayer(player, packet);

        //Make sure the overlay goes away when the player stops holding the item
        ServerScheduler.scheduleDelayed(10, () -> {
            if(!player.getItemInHand(entity.getUsedItemHand()).getItem().equals(this)) {
                SyncSelectedMarionettePacket packet1 = new SyncSelectedMarionettePacket(false, -1);
                PacketHandler.sendToPlayer(player, packet1);
            }
        });
    }
}
