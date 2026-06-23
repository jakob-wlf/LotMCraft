package de.jakob.lotm.beyonders.abilities.visionary;


import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.core.AbilityUseTracker;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.beyonders.abilities.visionary.handlers.VisionaryHandler;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.StartStopDiscernmentS2CPacket;
import de.jakob.lotm.network.packets.toClient.SyncSpectatingAbilityS2CPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.*;

import static de.jakob.lotm.beyonders.abilities.visionary.TelepathyAbility.performTelepaty;

public class DiscernmentAbility extends ToggleAbility {
    private final HashMap<UUID, Set<Entity>> glowingEntities = new HashMap<>();
    private static final Map<UUID, String> ENTITY_TEAM_MAP = new HashMap<>();
    private static final Map<UUID, Set<String>> SENT_TEAMS = new HashMap<>();

    private static final int COOLDOWN = 20 * 3;
    private static final Map<UUID, Integer> cooldown = new HashMap<>();

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

        PacketHandler.sendToPlayer(player, new SyncSpectatingAbilityS2CPacket(true, lookedAt == null ? -1 : lookedAt.getId()));

        if(lookedAt != null)
            performTelepaty(player, lookedAt, seq);


        AbilityUseTracker.AbilityUseRecord tracker = AbilityUseTracker.getRecentUseInArea(
                entity.getEyePosition(), level, getRangeForAbilityDetection(seq), entity);

        if(tracker == null) return;

        if(VisionaryHandler.shouldFailAndTrigger(seq, entity, tracker.entity(), this))
            return;

        Ability usedSkill = tracker.ability();
        if(usedSkill.getRequirements().containsKey("visionary") && !cooldown.containsKey(entity.getUUID())){
            String pos = "x=" + (int) tracker.position().x + " y=" + (int) tracker.position().y + " z=" + (int) tracker.position().z;

            entity.sendSystemMessage(Component.literal("You sense the usage of "
                    + usedSkill.getId() + " at " + pos + " by " + tracker.entity().getName().getString())
                    .withColor(0xf5c56c));

            cooldown.put(entity.getUUID(), 0);
        }

        if(cooldown.containsKey(entity.getUUID())) {
            cooldown.put(entity.getUUID(), cooldown.get(entity.getUUID()) + 1);
            if (cooldown.get(entity.getUUID()) >= COOLDOWN)
                cooldown.remove(entity.getUUID());
        }

        int entitySeq = BeyonderData.getSequence(entity);
        if(VisionaryHandler.shouldBeAffectedWithMindWorldSeal(entitySeq)){
            AbilityUtil.sendActionBar(entity,
                    Component.translatable("ability.lotmcraft.mind_world_authority_ability.is_sealed")
                            .withColor(0xFFff124d));
            stop(level, entity);
        }
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        int entitySeq = BeyonderData.getSequence(entity);
        if(VisionaryHandler.shouldBeAffectedWithMindWorldSeal(entitySeq)){
            AbilityUtil.sendActionBar(entity,
                    Component.translatable("ability.lotmcraft.mind_world_authority_ability.is_sealed")
                            .withColor(0xFFff124d));
            return;
        }

        if(entity instanceof ServerPlayer player)
            PacketHandler.sendToPlayer(player, new StartStopDiscernmentS2CPacket(true, getRange(BeyonderData.getSequence(entity))));
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if(level.isClientSide){
            return;
        }
        if(!(entity instanceof ServerPlayer player)) return;

        PacketHandler.sendToPlayer(player, new StartStopDiscernmentS2CPacket(false, getRange(BeyonderData.getSequence(entity))));
        PacketHandler.sendToPlayer(player, new SyncSpectatingAbilityS2CPacket(false, -1));
        AbilityUtil.sendActionBar(entity, Component.literal(""));
    }

    private static int getRange(int seq){
        return switch (seq){
            case 2 -> 100;
            case 1 -> 400;
            case 0 -> 1000;
            default -> 0;
        };
    }

    private static int getRangeForAbilityDetection(int seq){
        return switch (seq){
          case 2 -> 500;
          case 1 -> 2000;
          case 0 -> 10000;
          default -> 0;
        };
    }
}
