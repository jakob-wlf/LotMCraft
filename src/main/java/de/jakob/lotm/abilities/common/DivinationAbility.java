package de.jakob.lotm.abilities.common;

import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.TransformationComponent;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.network.packets.toClient.OpenCoordinateScreenPacket;
import de.jakob.lotm.network.packets.toClient.SyncDangerPremonitionAbilityPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.scheduling.ClientScheduler;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class DivinationAbility extends SelectableAbility {
    public static final Set<UUID> dangerPremonitionActive = new HashSet<>();

    public DivinationAbility(String id) {
        super(id, 1);

        canBeCopied = false;
        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "fool", 9,
                "door", 7,
                "hermit", 9,
                "demoness", 7,
                "wheel_of_fortune", 8
        ));
    }

    @Override
    protected float getSpiritualityCost() {
        return 10;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[] {"ability.lotmcraft.divination.danger_premonition", "ability.lotmcraft.divination.dream_divination"};
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        switch(abilityIndex) {
            case 0 -> dangerPremonition(level, entity);
            case 1 -> dreamDivination(level, entity);
        }
    }

    private void dowsingRod(Level level, LivingEntity entity) {

    }

    private void dangerPremonition(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        if(dangerPremonitionActive.contains(entity.getUUID())) {
            dangerPremonitionActive.remove(entity.getUUID());
            return;
        }

        if(!(entity instanceof ServerPlayer player))
            return;

        dangerPremonitionActive.add(entity.getUUID());

        PacketHandler.sendToPlayer(player, new SyncDangerPremonitionAbilityPacket(true));

        AtomicBoolean stop = new AtomicBoolean(false);
        ServerScheduler.scheduleUntil((ServerLevel) level,  () -> {
            if(!dangerPremonitionActive.contains(entity.getUUID())) {
                stop.set(true);
            }

            if(BeyonderData.getSpirituality(player) < 2) {
                dangerPremonitionActive.remove(entity.getUUID());
                stop.set(true);
            }

            if(stop.get()) {
                PacketHandler.sendToPlayer(player, new SyncDangerPremonitionAbilityPacket(false));
            }
            BeyonderData.reduceSpirituality(player, .5f);
        }, 2, null, stop);

    }

    private void dreamDivination(Level level, LivingEntity entity) {
        if(level.isClientSide) {
            return;
        }

        if(!(entity instanceof ServerPlayer player)) {
            return;
        }

        PacketHandler.sendToPlayer(player, new OpenCoordinateScreenPacket());
    }

    private static final HashMap<UUID, Integer> dreamDivinationUsers = new HashMap<>();
    private static final HashMap<UUID, BlockPos> previousCoordinates = new HashMap<>();

    public static void performDreamDivination(Level level, Player player, BlockPos blockPos) {
        if(!(level instanceof ServerLevel serverLevel)) {
            ClientHandler.hideGUI();
            ClientHandler.changeToThirdPerson(player);
            ClientScheduler.scheduleDelayed(20 * 10, ClientHandler::showGui);
            ClientScheduler.scheduleDelayed(20 * 10, () -> ClientHandler.changeToFirstPerson(player));
            return;
        }

        if(dreamDivinationUsers.containsKey(player.getUUID()) && dreamDivinationUsers.get(player.getUUID()) + 20 * 10 > player.tickCount) {
            return;
        }
        dreamDivinationUsers.put(player.getUUID(), player.tickCount);

        TransformationComponent transformationComponent = player.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        transformationComponent.setTransformedAndSync(true, player);
        transformationComponent.setTransformationIndexAndSync(TransformationComponent.TransformationType.DREAM_DIVINATION, player);

        previousCoordinates.put(player.getUUID(), player.blockPosition());

        player.setInvulnerable(true);

        ServerScheduler.scheduleForDuration(0, 1, 20 * 10, () -> {
            player.teleportTo((ServerLevel) level, blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5, Set.of(), player.getYRot() + 1, player.getXRot());
        }, () -> {
            transformationComponent.setTransformedAndSync(false, player);

            player.teleportTo(serverLevel, previousCoordinates.get(player.getUUID()).getX() + 0.5, previousCoordinates.get(player.getUUID()).getY(), previousCoordinates.get(player.getUUID()).getZ() + 0.5, Set.of(), player.getYRot(), player.getXRot());

            player.setInvulnerable(false);
            dreamDivinationUsers.remove(player.getUUID());
            previousCoordinates.remove(player.getUUID());
        }, serverLevel);

    }

    public static void cleanupOnLogout(Player player) {
        if(!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if(previousCoordinates.containsKey(player.getUUID())) {
            player.setInvulnerable(false);
            player.teleportTo(serverLevel, previousCoordinates.get(player.getUUID()).getX() + 0.5, previousCoordinates.get(player.getUUID()).getY(), previousCoordinates.get(player.getUUID()).getZ() + 0.5, Set.of(), player.getYRot(), player.getXRot());
            previousCoordinates.remove(player.getUUID());
        }
        dreamDivinationUsers.remove(player.getUUID());
    }
}
