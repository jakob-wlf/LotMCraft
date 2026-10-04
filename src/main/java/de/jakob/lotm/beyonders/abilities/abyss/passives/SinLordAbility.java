package de.jakob.lotm.beyonders.abilities.abyss.passives;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.core.PassiveAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.Level;

import java.util.Map;

public class SinLordAbility extends PassiveAbility {

    private static final int REQUIRED_KILLS = 20;
    private static final double RANGE = 16.0D;
    private static final float SANITY_DAMAGE = 0.0095f;
    public SinLordAbility(String id) {
        super(id);
    }
    @Override
    public Map<String, Integer> getRequirements() {return Map.of("abyss",1);}

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level.isClientSide()) {return;}
        ServerLevel serverLevel = (ServerLevel) level;
        int entitySeq = BeyonderData.getSequence(entity);
        for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(RANGE), target -> target != entity && target.isAlive() && hasEnoughKills(target))) {
            String EntityPath = BeyonderData.getPathway(entity);
            if ("abyss".equals(EntityPath) && entitySeq <= 1){return;}
            target.getData(ModAttachments.SANITY_COMPONENT).decreaseSanityWithSequenceDifference(SANITY_DAMAGE * getKillMultiplier(target), target, entitySeq, BeyonderData.getSequence(target));
        }
    }
    private static boolean hasEnoughKills(LivingEntity entity) {
        if (!(entity instanceof Player player)) {return false;}
        return getPlayerKills(player) >= REQUIRED_KILLS;
    }
    private static int getPlayerKills(LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player)) {return 0;}
        return player.getStats().getValue(Stats.CUSTOM, Stats.PLAYER_KILLS);
    }
    private static float getKillMultiplier(LivingEntity target) {
        if (!(target instanceof ServerPlayer player)) {return 1.0f;}
        int kills = getPlayerKills(player);
        return Math.max(kills / REQUIRED_KILLS, 1);
    }
}
