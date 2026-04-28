package de.jakob.lotm.abilities.visionary;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.abilities.visionary.passives.MetaAwarenessAbility;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

import java.util.*;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class DreamWeaveAbility extends SelectableAbility {
    private static final Map<UUID, List<BeyonderNPCEntity>> VICTIM_MOBS = new HashMap<>();
    private static final Map<Integer, UUID> MOB_TO_VICTIM = new HashMap<>();

    public DreamWeaveAbility(String id) {
        super(id, 20f);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 3));
    }

    @Override
    public float getSpiritualityCost() {
        return 1550;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.dream_weave.strong",
                "ability.lotmcraft.dream_weave.weak",
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        switch (abilityIndex) {
            case 0 -> strong(level, entity);
            case 1 -> weak(level, entity);
        }
    }


    private BeyonderNPCEntity spawnPassiveMob(ServerLevel serverLevel, LivingEntity target,
                                              int mobSeq, double x, double y, double z) {
        List<String> pathways = new ArrayList<>(BeyonderData.implementedPathways);
        String pathway = pathways.get(random.nextInt(pathways.size()));

        // Spawn non-hostile — mob will not attack until harmed
        BeyonderNPCEntity mob = new BeyonderNPCEntity(
                ModEntities.BEYONDER_NPC.get(), serverLevel, false, pathway, mobSeq);
        mob.setPos(x, y, z);
        mob.getPersistentData().putBoolean("VoidSummoned", true);
        mob.setPuppetWarrior(true);
        mob.setMaxLifetimeIfPuppet(20 * 10);
        // No target set yet — mob is passive until harmed

        serverLevel.addFreshEntity(mob);

        if (target instanceof Mob targetMob) {
            targetMob.setTarget(mob);
        }

        VICTIM_MOBS.computeIfAbsent(target.getUUID(), k -> new ArrayList<>()).add(mob);
        MOB_TO_VICTIM.put(mob.getId(), target.getUUID());

        return mob;
    }

    // Spawns 1 mob, 1 sequence below the caster

    private void strong(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 20 *(int) Math.max(multiplier(entity)/4,1), 2);
        if (target == null) {
            AbilityUtil.sendActionBar(entity,
                    Component.translatable("ability.lotmcraft.frenzy.no_target").withColor(0xFFff124d));
            return;
        }

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
        int targetSeq = BeyonderData.getSequence(target);
        if(BeyonderData.getPathway(target).equals("visionary") && targetSeq < entitySeq){
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.dream_traversal.failed").withColor(0xFFff124d));

            if(targetSeq <= 1 && target instanceof ServerPlayer targetPlayer && entity instanceof ServerPlayer entityPlayer){
                MetaAwarenessAbility.onDivined(entityPlayer, targetPlayer);
            }

            return;
        }
        
        int mobSeq = Math.min(AbilityUtil.getSeqWithArt(entity, this) + 1, 9);
        Vec3 center = target.position();

        BeyonderNPCEntity mob = spawnPassiveMob(serverLevel, target, mobSeq,
                center.x, center.y, center.z);

        ServerScheduler.scheduleDelayed(20 * 10, () -> {
            if (!mob.isRemoved()) mob.discard();
            removeMob(target.getUUID(), mob);
        });
    }

    // Spawns 3 mobs, 3 sequences below the caster
    private void weak(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 20, 2);
        if (target == null) {
            AbilityUtil.sendActionBar(entity,
                    Component.translatable("ability.lotmcraft.frenzy.no_target").withColor(0xFFff124d));
            return;
        }

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
        int targetSeq = BeyonderData.getSequence(target);
        if(BeyonderData.getPathway(target).equals("visionary") && targetSeq < entitySeq){
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.dream_traversal.failed").withColor(0xFFff124d));

            if(targetSeq <= 1 && target instanceof ServerPlayer targetPlayer && entity instanceof ServerPlayer entityPlayer){
                MetaAwarenessAbility.onDivined(entityPlayer, targetPlayer);
            }

            return;
        }

        int mobSeq = Math.min(AbilityUtil.getSeqWithArt(entity, this) + 3, 9);
        Vec3 center = target.position();
        double spawnRadius = 3.0;

        List<BeyonderNPCEntity> mobs = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            double angle = Math.toRadians(i * 120.0);
            double spawnX = center.x + spawnRadius * Math.cos(angle);
            double spawnZ = center.z + spawnRadius * Math.sin(angle);

            BeyonderNPCEntity mob = spawnPassiveMob(serverLevel, target, mobSeq,
                    spawnX, center.y, spawnZ);
            mobs.add(mob);
        }

        ServerScheduler.scheduleDelayed(20 * 5, () -> {
            for (BeyonderNPCEntity mob : mobs) {
                if (!mob.isRemoved()) mob.discard();
            }
            removeAllMobs(target.getUUID(), mobs);
        });
    }


    @SubscribeEvent
    public static void onMobHurt(LivingDamageEvent.Pre event) {
        LivingEntity damagedEntity = event.getEntity();
        if (!(damagedEntity instanceof BeyonderNPCEntity mob)) return;
        if (!MOB_TO_VICTIM.containsKey(mob.getId())) return;

        UUID victimUUID = MOB_TO_VICTIM.get(mob.getId());
        net.minecraft.world.entity.Entity attacker = event.getSource().getEntity();
        if (attacker == null) return;

        if (!attacker.getUUID().equals(victimUUID)) return;

        if (!mob.isHostile() && attacker instanceof LivingEntity livingAttacker) {
            mob.setHostile(true);
            mob.setTarget(livingAttacker);

            mob.getData(ModAttachments.SANITY_COMPONENT).decreaseSanityWithSequenceDifference(.23f, livingAttacker, 2, BeyonderData.getSequence(livingAttacker));
        }
    }
    
    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEntity().level().isClientSide) return;
        if (!event.getEffect().is(ModEffects.LOOSING_CONTROL)) return;

        UUID victimUUID = event.getEntity().getUUID();
        List<BeyonderNPCEntity> mobs = VICTIM_MOBS.remove(victimUUID);
        if (mobs == null) return;

        for (BeyonderNPCEntity mob : mobs) {
            MOB_TO_VICTIM.remove(mob.getId());
            if (!mob.isRemoved()) mob.discard();
        }
    }
    
    private static void removeMob(UUID victimUUID, BeyonderNPCEntity mob) {
        MOB_TO_VICTIM.remove(mob.getId());
        List<BeyonderNPCEntity> mobs = VICTIM_MOBS.get(victimUUID);
        if (mobs == null) return;
        mobs.remove(mob);
        if (mobs.isEmpty()) VICTIM_MOBS.remove(victimUUID);
    }

    private static void removeAllMobs(UUID victimUUID, List<BeyonderNPCEntity> mobs) {
        for (BeyonderNPCEntity mob : mobs) MOB_TO_VICTIM.remove(mob.getId());
        List<BeyonderNPCEntity> tracked = VICTIM_MOBS.get(victimUUID);
        if (tracked == null) return;
        tracked.removeAll(mobs);
        if (tracked.isEmpty()) VICTIM_MOBS.remove(victimUUID);
    }
}
