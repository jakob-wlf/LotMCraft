package de.jakob.lotm.abilities.visionary;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.abilities.ToggleAbilityItem;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.SyncSpectatingAbilityPacket;
import de.jakob.lotm.network.packets.SyncTelepathyAbilityPacket;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.mixin.EntityAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.level.Level;

import java.util.*;

public class TelepathyAbility extends ToggleAbilityItem {
    public TelepathyAbility(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 8));
    }

    @Override
    protected void start(Level level, LivingEntity entity) {
        if(!level.isClientSide) {
            if(entity instanceof ServerPlayer player) {
                PacketHandler.sendToPlayer(player, new SyncTelepathyAbilityPacket(true, -1, new ArrayList<>()));
            }
            return;
        }

        entity.playSound(SoundEvents.AMETHYST_BLOCK_BREAK, 3, .01f);
    }

    @Override
    protected void tick(Level level, LivingEntity entity) {
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
    protected void stop(Level level, LivingEntity entity) {
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
        return withSpaces.substring(0, 1).toUpperCase() + withSpaces.substring(1);
    }

    @Override
    protected float getSpiritualityCost() {
        return 1;
    }
}
