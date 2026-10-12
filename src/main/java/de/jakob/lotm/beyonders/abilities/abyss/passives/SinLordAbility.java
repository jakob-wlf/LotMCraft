package de.jakob.lotm.beyonders.abilities.abyss.passives;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.core.PassiveAbility;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.Level;

import java.util.Map;

public class SinLordAbility extends PassiveAbility {
    private static final int REQUIRED_KILLS = 50;
    private static final double RANGE = 16.0D;
    private static final float SANITY_DAMAGE = 0.001f;
    public SinLordAbility(String id) {
        super(id);
    }
    @Override
    public Map<String, Integer> getRequirements() {return Map.of("abyss",1);}

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level.isClientSide()) {return;}
        ServerLevel serverLevel = (ServerLevel) level;
        for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(RANGE), target -> target != entity && target.isAlive() && hasEnoughKills(target) && !(("abyss".equals(BeyonderData.getPathway(target)) && BeyonderData.getSequence(target) <= 2)||("sun".equals(BeyonderData.getPathway(target)) && BeyonderData.getSequence(target) <= 4)) && getPlayerKills(target) <= getPlayerKills(entity))) {
            if (!AbilityUtil.mayTarget(entity, target)) continue;
            target.getData(ModAttachments.SANITY_COMPONENT).decreaseSanityWithSequenceDifference(SANITY_DAMAGE * getKillMultiplier(target), target, BeyonderData.getSequence(entity), BeyonderData.getSequence(target));
        }
    }
    private static boolean hasEnoughKills(LivingEntity entity) {
        if (!(entity instanceof Player player)) {return false;}
        return getPlayerKills(player) >= REQUIRED_KILLS;
    }
    private static int getPlayerKills(LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player)) {
            return 0;
        }
        int playerKills = player.getStats().getValue(Stats.CUSTOM, Stats.PLAYER_KILLS);
        int villagerKills = player.getStats().getValue(Stats.ENTITY_KILLED, EntityType.VILLAGER);
        int beyonderKills = player.getStats().getValue(Stats.ENTITY_KILLED, BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse("lotmcraft:beyonder_npc")));
        return playerKills + (villagerKills / 40) + (beyonderKills / 20);
    }
    private static float getKillMultiplier(LivingEntity target) {
        if (!(target instanceof ServerPlayer player)) {return 1.0f;}
        int kills = getPlayerKills(player);
        return Math.max(kills / REQUIRED_KILLS, 1);
    }
}
