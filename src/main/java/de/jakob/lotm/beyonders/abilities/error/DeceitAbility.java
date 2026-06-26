package de.jakob.lotm.beyonders.abilities.error;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.RingEffectManager;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class DeceitAbility extends SelectableAbility {

    public static final HashSet<UUID> cannotBeTargeted = new HashSet<>();
    public static final HashSet<UUID> cannotBeHarmed = new HashSet<>();
    public static final HashMap<UUID, Integer> seqMap = new HashMap<>(50);

    public DeceitAbility(String id) {
        super(id, 15);
        autoClear = false;
        canBeUsedInArtifact = false;
        canBeShared = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("error", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 4000;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.deceit.entities", "ability.lotmcraft.deceit.world"};
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if(!(level instanceof ServerLevel serverLevel)) {
            entity.playSound(SoundEvents.BELL_RESONATE, 2, 1);
            return;
        }

        EffectManager.playEffect(EffectManager.Effect.DECEPTION, entity.getX(), entity.getY() + .5, entity.getZ(), serverLevel);

        seqMap.put(entity.getUUID(), AbilityUtil.getSeqWithArt(entity, this));

        switch (abilityIndex) {
            case 0 -> deceiveOthers(serverLevel, entity);
            case 1 -> deceiveWorld(serverLevel, entity);
        }
    }

    private void deceiveWorld(ServerLevel serverLevel, LivingEntity entity) {
        cannotBeHarmed.add(entity.getUUID());
        ServerScheduler.scheduleDelayed((int) (20 * 7*multiplier(entity)), () -> {
            cannotBeHarmed.remove(entity.getUUID());
            clearArtifactScaling(entity);
            seqMap.remove(entity.getUUID());
        });
    }

    private void deceiveOthers(ServerLevel serverLevel, LivingEntity entity) {
        cannotBeTargeted.add(entity.getUUID());

        AbilityUtil.getNearbyEntities(entity, serverLevel, entity.position(), 20*(int) Math.max(multiplier(entity)/2,1)).forEach(e -> {
            if(BeyonderData.isBeyonder(e) && BeyonderData.getSequence(e) < AbilityUtil.getSeqWithArt(entity, this)) {
                return;
            }

            BeyonderData.addModifierWithTimeLimit(e, "deceit", .65, 1000 * 20);
            RingEffectManager.createRingForAll(e.position().add(0, e.getEyeHeight() / 2, 0), 1.8f, 120, 156 / 255f, 72 / 155f, 219 / 255f, .7f, .75f, 1, serverLevel);
            RingEffectManager.createRingForAll(e.position().add(0, e.getEyeHeight() / 2, 0), 1f, 120, 106 / 255f, 237 / 255f,102 / 255f, .9f, .75f, 1, serverLevel);

            if(e instanceof Mob mob) {
                mob.setTarget(null);
            }
            if(e instanceof BeyonderNPCEntity npc) {
                npc.setTarget(null);
            }
        });

        ServerScheduler.scheduleDelayed((int) (20 * 12*multiplier(entity)), () -> {
            cannotBeTargeted.remove(entity.getUUID());
            clearArtifactScaling(entity);
            seqMap.remove(entity.getUUID());
        });
    }

    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) {
            return;
        }

        LivingEntity newTarget = event.getNewAboutToBeSetTarget();
        if (newTarget == null || !cannotBeTargeted.contains(newTarget.getUUID())) {
            return;
        }

        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();

        var source = event.getSource().getEntity();

        if(!cannotBeHarmed.contains(entity.getUUID()) || !(entity.level() instanceof ServerLevel serverLevel)) return;

        if(!(source instanceof LivingEntity livingSource) ||
                BeyonderData.getSequence(livingSource) < seqMap.get(entity.getUUID())){
           return;
        }

        ParticleUtil.spawnParticles(serverLevel, ParticleTypes.END_ROD, entity.position().add(0, entity.getEyeHeight() / 2, 0), 60, 0.5, 0.5, 0.5, 0.1);
        event.setCanceled(true);
    }
}
