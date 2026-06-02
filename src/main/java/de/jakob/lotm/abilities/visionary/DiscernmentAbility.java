package de.jakob.lotm.abilities.visionary;


import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.abilities.visionary.handlers.VisionaryHandler;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncSpectatingAbilityPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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

import static de.jakob.lotm.abilities.visionary.TelepathyAbility.performTelepaty;

public class DiscernmentAbility extends ToggleAbility {
    private final HashMap<UUID, Set<Entity>> glowingEntities = new HashMap<>();
    private static final Map<UUID, String> ENTITY_TEAM_MAP = new HashMap<>();
    private static final Map<UUID, Set<String>> SENT_TEAMS = new HashMap<>();

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
        int range = getRange(seq);
        List<LivingEntity> nearbyEntities = AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, entity.getEyePosition(), range)
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

        LivingEntity lookedAt = AbilityUtil.getTargetEntity(entity, range, 1.2f, false, true);
        if(lookedAt != null) {
            if (VisionaryHandler.shouldStayInvisible(seq, lookedAt)){
                return;
            }
            else if(VisionaryHandler.shouldFailAndTrigger(seq, entity, lookedAt, this, false)){
                return;
            }
            else if(AbilityUtil.isTargetSignificantlyStronger(seq, BeyonderData.getSequence(lookedAt))){
                return;
            }
        }

        PacketHandler.sendToPlayer(player, new SyncSpectatingAbilityPacket(true, lookedAt == null ? -1 : lookedAt.getId()));

        if(lookedAt != null)
            performTelepaty(player, lookedAt, seq);
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

    private static final EntityDataAccessor<Byte> DATA_SHARED_FLAGS =
            new EntityDataAccessor<>(0, EntityDataSerializers.BYTE);

    public static void setGlowingForPlayer(Entity entity, ServerPlayer player, boolean glowing) {

        ChatFormatting color = ChatFormatting.WHITE;
        if (entity instanceof Mob mob) {
            if (BeyonderData.isBeyonder(mob))
                color = ChatFormatting.GOLD;
            else if (mob instanceof Enemy)
                color = ChatFormatting.RED;
            else
                color = ChatFormatting.GREEN;
        }

        String teamName = "glow_" + color.getName();

        byte flags = entity.getEntityData().get(DATA_SHARED_FLAGS);

        // glowing bit
        if (glowing) {
            flags |= 0x40;
        } else {
            flags &= ~0x40;
        }

        ClientboundSetEntityDataPacket metadataPacket =
                new ClientboundSetEntityDataPacket(
                        entity.getId(),
                        List.of(
                                SynchedEntityData.DataValue.create(
                                        DATA_SHARED_FLAGS,
                                        flags
                                )
                        )
                );

        player.connection.send(metadataPacket);

        UUID uuid = entity.getUUID();
        String entityId = entity.getStringUUID();

        if (glowing) {

            PlayerTeam team = new PlayerTeam(
                    player.server.getScoreboard(),
                    teamName
            );

            team.setColor(color);
            team.setNameTagVisibility(Team.Visibility.NEVER);
            team.setSeeFriendlyInvisibles(true);

            Set<String> sentTeams =
                    SENT_TEAMS.computeIfAbsent(
                            player.getUUID(),
                            k -> new HashSet<>()
                    );

            if (!sentTeams.contains(teamName)) {
                player.connection.send(
                        ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true)
                );

                sentTeams.add(teamName);
            }

            player.connection.send(
                    ClientboundSetPlayerTeamPacket.createPlayerPacket(
                            team,
                            entityId,
                            ClientboundSetPlayerTeamPacket.Action.ADD
                    )
            );

            ENTITY_TEAM_MAP.put(uuid, teamName);

        } else {

            String previousTeam = ENTITY_TEAM_MAP.remove(uuid);

            if (previousTeam != null) {

                PlayerTeam removeTeam = new PlayerTeam(
                        player.server.getScoreboard(),
                        previousTeam
                );

                player.connection.send(
                        ClientboundSetPlayerTeamPacket.createPlayerPacket(
                                removeTeam,
                                entityId,
                                ClientboundSetPlayerTeamPacket.Action.REMOVE
                        )
                );
            }
        }
    }
}
