package de.jakob.lotm.abilities.visionary;


import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.abilities.visionary.handlers.VisionaryHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;

import java.util.*;

public class DiscernmentAbility extends ToggleAbility {
    private final HashMap<UUID, Set<Entity>> glowingEntities = new HashMap<>();
    private static final Map<UUID, String> ENTITY_TEAM_MAP = new HashMap<>();

    public DiscernmentAbility(String id) {
        super(id);

        canBeShared = false;
        canBeUsedInArtifact = false;
        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 2));
    }

    @Override
    protected float getSpiritualityCost() {
        return 10;
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if(level.isClientSide){
            return;
        }

        if(!(entity instanceof ServerPlayer player)) return;

        int seq = BeyonderData.getSequence(entity);
        List<LivingEntity> nearbyEntities = AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, entity.getEyePosition(), getRange(seq))
                .stream()
                .filter(nearbyEntity -> {
                    return !VisionaryHandler.shouldStayInvisible(seq, nearbyEntity) || VisionaryHandler.shouldFailAndTrigger(seq, entity, nearbyEntity, this);
                })
                .toList();

        for (LivingEntity nearbyEntity : nearbyEntities) {
            setGlowingForPlayer(nearbyEntity, player, true);
        }

        glowingEntities.putIfAbsent(entity.getUUID(), new HashSet<>(Set.of()));
        glowingEntities.get(entity.getUUID()).addAll(nearbyEntities);
    }

    @Override
    public void start(Level level, LivingEntity entity) {
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if(level.isClientSide){
            return;
        }
        if(!(entity instanceof ServerPlayer player)) return;

        if(glowingEntities.containsKey(entity.getUUID()))
            glowingEntities.get(entity.getUUID()).forEach(e -> setGlowingForPlayer(e, player, false));
        glowingEntities.remove(entity.getUUID());
    }

    private static int getRange(int seq){
        return switch (seq){
            case 2 -> 100;
            case 1 -> 150;
            case 0 -> 200;
            default -> 0;
        };
    }

    public static void setGlowingForPlayer(Entity entity, ServerPlayer player, boolean glowing) {
        Scoreboard scoreboard = player.getScoreboard();

        ChatFormatting color;
        if (entity instanceof Mob mob) {
            if (BeyonderData.isBeyonder(mob))
                color = ChatFormatting.GOLD;
            else if (mob instanceof Enemy)
                color = ChatFormatting.RED;
            else
                color = ChatFormatting.GREEN;
        }
        else {
            color = ChatFormatting.LIGHT_PURPLE;
        }

        String teamName = "glow_" + color.getName();
        PlayerTeam team = scoreboard.getPlayerTeam(teamName);

        if (team == null) {
            team = scoreboard.addPlayerTeam(teamName);
            team.setColor(color);
            team.setNameTagVisibility(Team.Visibility.NEVER);
            team.setSeeFriendlyInvisibles(true);
        }

        UUID uuid = entity.getUUID();
        String entityId = entity.getStringUUID();

        String previousTeam = ENTITY_TEAM_MAP.get(uuid);
        if (previousTeam != null && !previousTeam.equals(teamName)) {
            PlayerTeam oldTeam = scoreboard.getPlayerTeam(previousTeam);
            if (oldTeam != null) {
                scoreboard.removePlayerFromTeam(entityId, oldTeam);
            }
        }

        if (glowing) {
            scoreboard.addPlayerToTeam(entityId, team);
            entity.setGlowingTag(true);
            ENTITY_TEAM_MAP.put(uuid, teamName);
        } else {
            PlayerTeam current = scoreboard.getPlayerTeam(teamName);
            if (current != null) {
                try {
                    scoreboard.removePlayerFromTeam(entityId, current);
                } catch (IllegalStateException ignored) {
                    // already removed or never added
                }
            }

            entity.setGlowingTag(false);
            ENTITY_TEAM_MAP.remove(uuid);
        }
    }
}
