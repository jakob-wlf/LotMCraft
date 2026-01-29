package de.jakob.lotm.abilities.visionary;

import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncTelepathyAbilityPacket;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TelepathyAbility extends ToggleAbility {
    public TelepathyAbility(String id) {
        super(id);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 8));
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if(!level.isClientSide) {
            if(entity instanceof ServerPlayer player) {
                PacketHandler.sendToPlayer(player, new SyncTelepathyAbilityPacket(true, -1, new ArrayList<>()));
            }
            return;
        }

        entity.playSound(SoundEvents.AMETHYST_BLOCK_BREAK, 3, .01f);
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if(!(entity instanceof ServerPlayer player) || level.isClientSide) {
            return;
        }

        LivingEntity lookedAt = AbilityUtil.getTargetEntity(entity, 40, 1.2f);

        List<String> goalNames = new ArrayList<>();
        if(lookedAt instanceof Mob mob) {
            List<WrappedGoal> goals = new ArrayList<>(mob.goalSelector.getAvailableGoals());
            goals.addAll(mob.targetSelector.getAvailableGoals());

            goalNames = goals.stream().filter(g -> !(g.getGoal() instanceof FloatGoal)).map(g -> {
                String name = g.getGoal().toString();
                String formattedName = formatGoalName(name);
                if(g.canUse() || g.isRunning()) {
                    formattedName += "%";
                }
                return formattedName;
            }).toList();
        }

        PacketHandler.sendToPlayer(player, new SyncTelepathyAbilityPacket(true, lookedAt == null ? -1 : lookedAt.getId(), goalNames));
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if(!level.isClientSide) {
            if(entity instanceof ServerPlayer player) {
                PacketHandler.sendToPlayer(player, new SyncTelepathyAbilityPacket(false, -1, new ArrayList<>()));
            }
            return;
        }

        entity.playSound(SoundEvents.AMETHYST_BLOCK_BREAK, 3, .01f);


    }

    private static String formatGoalName(String goalName) {
        String withSpaces = goalName.replaceAll("([a-z])([A-Z])", "$1 $2");
        if(withSpaces.length() <= 1)
            return withSpaces;
        return withSpaces.substring(0, 1).toUpperCase() + withSpaces.substring(1);
    }

    @Override
    public float getSpiritualityCost() {
        return 1;
    }
}
