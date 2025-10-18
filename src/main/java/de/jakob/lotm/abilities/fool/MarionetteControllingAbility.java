package de.jakob.lotm.abilities.fool;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.SelectableAbilityItem;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.SyncSelectedMarionettePacket;
import de.jakob.lotm.util.helper.marionettes.MarionetteComponent;
import de.jakob.lotm.util.helper.marionettes.MarionetteUtils;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.*;
import java.util.stream.StreamSupport;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class MarionetteControllingAbility extends SelectableAbilityItem {

    private static final Map<UUID, Integer> marionetteIndices = new HashMap<>();

    private static final HashSet<UUID> swapOnDamageIsActive = new HashSet<>();

    public MarionetteControllingAbility(Properties properties) {
        super(properties, .5f);
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
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.marionette_controlling.swap", "ability.lotmcraft.marionette_controlling.damage_auto_swap", "ability.lotmcraft.marionette_controlling.control", "ability.lotmcraft.marionette_controlling.get_item"};
    }

    @Override
    protected void useAbility(Level level, LivingEntity entity, int abilityIndex) {
        if(level.isClientSide || !(entity instanceof ServerPlayer player))
            return;

        switch (abilityIndex) {
            case 0 -> activateSwap((ServerLevel) level, player);
            case 1 -> toggleAutoSwap(player);
            case 3  -> getItem(player);
        }

    }

    private void getItem(ServerPlayer player) {
        LivingEntity marionette = getSelectedMarionette(player);
        if(marionette == null) {
            return;
        }

        ItemStack controller = MarionetteUtils.createMarionetteController(marionette);
        if(!player.getInventory().add(controller)) {
            player.drop(controller, false);
        }
    }

    private void activateSwap(ServerLevel level, ServerPlayer player) {
        LivingEntity marionette = getSelectedMarionette(player);

        if(marionette == null) {
            return;
        }

        swapWithMarionette(level, player, marionette);
    }

    private static void swapWithMarionette(ServerLevel level, ServerPlayer player, LivingEntity marionette) {
        Vec3 playerPos = player.position();

        Level marionetteLevel = marionette.level();
        Vec3 marionettePos = marionette.position();

        if(!(marionetteLevel instanceof ServerLevel marionetteServerLevel))
            return;

        //Store the UUID before teleporting
        UUID marionetteUUID = marionette.getUUID();

        //Load chunks
        ChunkPos playerChunkPos = new ChunkPos(player.blockPosition());
        level.setChunkForced(playerChunkPos.x, playerChunkPos.z, true);

        ChunkPos marionetteChunkPos = new ChunkPos(marionette.blockPosition());
        marionetteServerLevel.setChunkForced(marionetteChunkPos.x, marionetteChunkPos.z, true);

        marionette.teleportTo(level, playerPos.x, playerPos.y, playerPos.z, Set.of(), marionette.getYRot(), marionette.getXRot());

        player.teleportTo(marionetteServerLevel, marionettePos.x, marionettePos.y, marionettePos.z, Set.of(), player.getYRot(), player.getXRot());
        player.hurtMarked = true;

        //Schedule a task to find and refresh the new marionette instance after it has been reinitialized in the new dimension
        level.getServer().tell(new TickTask(
                level.getServer().getTickCount() + 2,
                () -> {
                    Entity newMarionetteEntity = level.getEntity(marionetteUUID);

                    if (newMarionetteEntity instanceof LivingEntity newMarionette) {
                        newMarionette.hurtMarked = true;

                        level.getChunkSource().addEntity(newMarionette);

                        //Force Position Update
                        newMarionette.teleportTo(newMarionette.getX(), newMarionette.getY(), newMarionette.getZ());
                    }
                }
        ));
    }


    private void toggleAutoSwap(ServerPlayer player) {
        if(swapOnDamageIsActive.contains(player.getUUID())) {
            swapOnDamageIsActive.remove(player.getUUID());
            player.sendSystemMessage(Component.translatable("ability.lotmcraft.marionette_controlling.auto_swap").append(Component.literal(": ")).append(Component.translatable("lotm.off")).withColor(0xFFa742f5));
        } else {
            swapOnDamageIsActive.add(player.getUUID());
            player.sendSystemMessage(Component.translatable("ability.lotmcraft.marionette_controlling.auto_swap").append(Component.literal(": ")).append(Component.translatable("lotm.on")).withColor(0xFFa742f5));
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingIncomingDamageEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer player) || !(player.level() instanceof ServerLevel level))
            return;

        if(!swapOnDamageIsActive.contains(player.getUUID()))
            return;

        LivingEntity marionette = getSelectedMarionette(player);
        if(marionette == null)
            return;

        event.setCanceled(true);
        swapWithMarionette(level, player, marionette);
        marionette.hurt(event.getSource(), event.getAmount());
    }

    private static ArrayList<LivingEntity> getMarionettesOfPlayerInAllLevelsOrderedById(LivingEntity entity) {
        Level level = entity.level();

        if(level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return new ArrayList<>();
        }

        if(entity.getServer() == null) {
            return new ArrayList<>();
        }

        final ArrayList<LivingEntity> marionettes = new ArrayList<>(StreamSupport.stream(serverLevel.getAllEntities().spliterator(), false).filter(e -> e instanceof LivingEntity).map(e -> (LivingEntity) e).toList());

        for(ServerLevel l : entity.getServer().getAllLevels()) {
            if(l == level)
                continue;
            marionettes.addAll(StreamSupport.stream(l.getAllEntities().spliterator(), false).filter(e -> e instanceof LivingEntity).map(e -> (LivingEntity) e).toList());
        }

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

    private static LivingEntity getSelectedMarionette(ServerPlayer player) {
        List<LivingEntity> marionettes = getMarionettesOfPlayerInAllLevelsOrderedById(player);

        int index = marionetteIndices.getOrDefault(player.getUUID(), 0);

        if(marionettes.isEmpty() || index >= marionettes.size()) {
            return null;
        }

        LivingEntity marionette = marionettes.get(index);
        if(marionette == null) {
            return null;
        }

        String name = marionette.getDisplayName() == null ? marionette.getName().getString(): marionette.getDisplayName().getString();

        SyncSelectedMarionettePacket packet = new SyncSelectedMarionettePacket(true, name, marionette.getHealth(), marionette.getMaxHealth());
        PacketHandler.sendToPlayer(player, packet);

        return marionette;
    }

    @Override
    public void onHold(Level level, LivingEntity entity) {
        if(level.isClientSide || !(entity instanceof ServerPlayer player))
            return;

        checkIndex(entity);

        LivingEntity marionette = getSelectedMarionette(player);

        //If no marionette is selected make sure no overlay gets rendered
        if(marionette == null) {
            SyncSelectedMarionettePacket packet = new SyncSelectedMarionettePacket(false, "", 0, 0);
            PacketHandler.sendToPlayer(player, packet);
            return;
        }

        //Make sure the overlay goes away when the player stops holding the item
        ServerScheduler.scheduleDelayed(10, () -> {
            if(!player.getItemInHand(entity.getUsedItemHand()).getItem().equals(this)) {
                SyncSelectedMarionettePacket packet1 = new SyncSelectedMarionettePacket(false, "", 0, 0);
                PacketHandler.sendToPlayer(player, packet1);
            }
        });
    }

    private void checkIndex(LivingEntity entity) {
        List<LivingEntity> marionettes = getMarionettesOfPlayerInAllLevelsOrderedById(entity);

        //Make sure the index is valid
        int currentIndex = marionetteIndices.getOrDefault(entity.getUUID(), 0);
        if(currentIndex >= marionettes.size()) {
            currentIndex = 0;
            marionetteIndices.put(entity.getUUID(), currentIndex);
        }

        //Increment index if shift key is down
        if(entity.isShiftKeyDown()) {
            currentIndex++;
            if(currentIndex >= marionettes.size())
                currentIndex = 0;
            marionetteIndices.put(entity.getUUID(), currentIndex);
        }
    }
}
