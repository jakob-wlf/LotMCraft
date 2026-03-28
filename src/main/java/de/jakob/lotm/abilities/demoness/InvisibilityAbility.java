package de.jakob.lotm.abilities.demoness;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class InvisibilityAbility extends Ability {
    public static final HashSet<UUID> invisiblePlayers = new HashSet<>();

    public InvisibilityAbility(String id) {
        super(id, 180);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("demoness", 7));
    }

    @Override
    public float getSpiritualityCost() {
        return 13;
    }

    private final DustParticleOptions dust = new DustParticleOptions(
            new Vector3f(0, 0, 0),
            2
    );

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(!level.isClientSide) {

            // make invisible
            invisiblePlayers.add(entity.getUUID());
            entity.setInvisible(true);
            entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 20 * 60, 20, false, false, false));

            //make visible again
            AtomicReference<UUID> taskIdRef = new AtomicReference<>();
            UUID taskId = ServerScheduler.scheduleForDuration(0, 10, 20 * 20, () -> {
                if(InteractionHandler.isInteractionPossible(new Location(entity.position(), entity.level()), "light_strong", BeyonderData.getSequence(entity))) {
                    entity.setInvisible(false);
                    entity.removeEffect(MobEffects.INVISIBILITY);
                    ServerScheduler.cancel(taskIdRef.get());
                }
            }, () -> invisiblePlayers.remove(entity.getUUID()), (ServerLevel) level, () -> AbilityUtil.getTimeInArea(entity, new de.jakob.lotm.util.data.Location(entity.position(), level)));
            taskIdRef.set(taskId);
        }
    }

    @SubscribeEvent
    public static void onLivingTarget(LivingChangeTargetEvent event) {
        if(event.getNewAboutToBeSetTarget() != null && invisiblePlayers.contains(event.getNewAboutToBeSetTarget().getUUID())) {
            LivingEntity invisible = event.getNewAboutToBeSetTarget();
            LivingEntity attacker = event.getEntity();

            // Light source nearby reveals invisible entity
            Location invisLoc = new Location(invisible.position(), invisible.level());
            if(InteractionHandler.isInteractionPossible(invisLoc, "light_source")) {
                return; // Allow targeting
            }

            // Spirit Vision, Spectating, or Cull can see through invisibility
            ToggleAbility spiritVision = (ToggleAbility) LOTMCraft.abilityHandler.getById("spirit_vision_ability");
            if(spiritVision != null && spiritVision.isActiveForEntity(attacker)) {
                return;
            }
            Ability spectating = LOTMCraft.abilityHandler.getById("spectating_ability");
            if(spectating instanceof ToggleAbility spectatingToggle && spectatingToggle.isActiveForEntity(attacker)) {
                return;
            }
            Ability cull = LOTMCraft.abilityHandler.getById("cull_ability");
            if(cull instanceof ToggleAbility cullToggle && cullToggle.isActiveForEntity(attacker)) {
                return;
            }

            event.setCanceled(true);
        }
    }
}
