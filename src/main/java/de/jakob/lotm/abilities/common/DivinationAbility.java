package de.jakob.lotm.abilities.common;

import de.jakob.lotm.abilities.SelectableAbilityItem;
import de.jakob.lotm.gui.custom.CoordinateInputScreen;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.scheduling.ClientScheduler;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class DivinationAbility extends SelectableAbilityItem {
    public static final HashMap<UUID, BlockPos> dreamDivinationUsers = new HashMap<>();
    public static final Set<UUID> dangerPremonitionActive = new HashSet<>();

    public DivinationAbility(Properties properties) {
        super(properties, 1);

        canBeCopied = false;
        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "fool", 9,
                "door", 7,
                "hermit", 9,
                "demoness", 7
        ));
    }

    @Override
    protected float getSpiritualityCost() {
        return 10;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[] {"Dowsing Rod", "Danger Premonition", "Dream Divination"};
    }

    @Override
    protected void useAbility(Level level, LivingEntity entity, int abilityIndex) {
        switch(abilityIndex) {
            case 0 -> dowsingRod(level, entity);
            case 1 -> dangerPremonition(level, entity);
            case 2 -> dreamDivination(level, entity);
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

        AtomicBoolean stop = new AtomicBoolean(false);
        ServerScheduler.scheduleUntil((ServerLevel) level,  () -> {
            if(!dangerPremonitionActive.contains(entity.getUUID())) {
                stop.set(true);
                return;
            }

            if(BeyonderData.getSpirituality(player) < 2) {
                stop.set(true);
                return;
            }
            BeyonderData.reduceSpirituality(player, .5f);
        }, 2, null, stop);

    }

    private void dreamDivination(Level level, LivingEntity entity) {
        if(level.isClientSide) {
            Minecraft.getInstance().setScreen(new CoordinateInputScreen(entity));
            return;
        }

        if(dreamDivinationUsers.containsKey(entity.getUUID()))
            return;

        if(!(entity instanceof ServerPlayer player))
            return;

        AtomicBoolean hasInputCoordinates = new AtomicBoolean(false);
        ServerScheduler.scheduleForDuration(0, 5, 20 * 60 * 5, () -> {
            if(hasInputCoordinates.get())
                return;

            if(dreamDivinationUsers.containsKey(entity.getUUID())) {
                hasInputCoordinates.set(true);

                BlockPos pos = dreamDivinationUsers.get(entity.getUUID());
                level.addFreshEntity(entity);
                final GameType prevGameMode = player.gameMode.getGameModeForPlayer();
                Vec3 prevPos = player.position();
                ServerScheduler.scheduleForDuration(0, 1, 20 * 30, () -> {
                    player.setGameMode(GameType.SPECTATOR);
                    player.teleportTo((ServerLevel) level, pos.getX(), pos.getY(), pos.getZ(), player.getYRot() + 1, 0);
                    player.hurtMarked = true;
                }, () -> {
                    dreamDivinationUsers.remove(entity.getUUID());
                    player.teleportTo(prevPos.x, prevPos.y, prevPos.z);
                    player.setGameMode(prevGameMode);
                    player.hurtMarked = true;
                }, (ServerLevel) level);
            }
        }, () -> {
            if(!hasInputCoordinates.get())
                dreamDivinationUsers.remove(entity.getUUID());
        }, (ServerLevel) level);
    }
}
