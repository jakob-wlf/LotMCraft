package de.jakob.lotm.abilities.error;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.SelectableAbilityItem;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.RingEffectManager;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class DeceitAbility extends SelectableAbilityItem {

    public static final HashSet<UUID> cannotBeTargeted = new HashSet<>();

    public DeceitAbility(Properties properties) {
        super(properties, 1);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("error", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 500;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.deceit.entities"};
    }

    @Override
    protected void useAbility(Level level, LivingEntity entity, int abilityIndex) {
        if(!(level instanceof ServerLevel serverLevel)) {
            entity.playSound(SoundEvents.BELL_RESONATE, 2, 1);
            return;
        }

        EffectManager.playEffect(EffectManager.Effect.DECEPTION, entity.getX(), entity.getY() + .5, entity.getZ(), serverLevel);

        switch (abilityIndex) {
            case 0 -> deceiveOthers(serverLevel, entity);
        }
    }

    private void deceiveOthers(ServerLevel serverLevel, LivingEntity entity) {
        cannotBeTargeted.add(entity.getUUID());

        AbilityUtil.getNearbyEntities(entity, serverLevel, entity.position(), 20).forEach(e -> {
            if(BeyonderData.isBeyonder(e) && BeyonderData.getSequence(e) < BeyonderData.getSequence(entity)) {
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

        ServerScheduler.scheduleDelayed(20 * 12, () -> cannotBeTargeted.remove(entity.getUUID()));
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
}
