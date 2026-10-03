package de.jakob.lotm.beyonders.abilities.red_priest;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.subordinates.SubordinateUtils;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;


public class FlameClonesAbility extends Ability {
    public FlameClonesAbility(String id) {
        super(id, 1 ); //20 * 60 * 2
        canBeShared = false;
    }

    private final HashMap<Integer, Integer> npcDetails = new HashMap<>(Map.ofEntries(
            Map.entry(5, 3),
            Map.entry(4, 4),
            Map.entry(3, 4),
            Map.entry(2, 5),       // PLAYER_SEQUENCE, AMOUNT_SUMMONED
            Map.entry(1, 5),
            Map.entry(0, 6)
    ));

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("red_priest", 5));
    }

    @Override
    protected float getSpiritualityCost() {
        return 0;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) return;

        if(level.isClientSide()) return;
        if(!(entity instanceof Player player)) return;
        int Seq = BeyonderData.getSequence(player);
        int npcCount = getNpcCount(Seq);
        int npcSeq = 8;
        int SP_COST = npcCount*20;
        if(BeyonderData.getSpirituality(entity) < SP_COST)  return;
        List<BeyonderNPCEntity> clones = new ArrayList<>();
        for(int i = 0; i < npcCount; i++) {
            Vec3 spawnPos = entity.position().add(random.nextDouble(-3, 3), 0, random.nextDouble(-3, 3));

            BeyonderNPCEntity flameClone = new BeyonderNPCEntity(ModEntities.BEYONDER_NPC.get(), serverLevel, false, "knight", "red_priest", npcSeq, false, false);
            flameClone.setPos(spawnPos);
            flameClone.setPuppetWarrior(true);
            flameClone.setHealth(flameClone.getHealth() * 20 / 100);
            flameClone.setMaxLifetimeIfPuppet(200);
            flameClone.getPersistentData().putBoolean("VoidSummoned", true);
            flameClone.getPersistentData().putUUID("VoidSummonOwner", entity.getUUID());
            clones.add(flameClone);

            serverLevel.addFreshEntity(flameClone);
            SubordinateUtils.turnEntityIntoSubordinate(flameClone, entity);

            BeyonderData.addModifier(flameClone, "puppet_soldier", 0.3f);
        }
        AtomicBoolean shouldStop = new AtomicBoolean(false);
        ServerScheduler.scheduleUntil(serverLevel, () -> {
            clones.removeIf(flameClone -> !flameClone.isAlive());
            if(clones.isEmpty()) shouldStop.set(true);
            BeyonderData.reduceSpirituality(entity,
                    clones.size()*(BeyonderData.getSpirituality(entity)/100)
                    );
        }, 20, null, shouldStop);
    }

    public int getNpcCount(int seq) {
        return npcDetails.get(seq);
    }
}
