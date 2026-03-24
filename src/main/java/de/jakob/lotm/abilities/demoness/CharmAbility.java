package de.jakob.lotm.abilities.demoness;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.attachments.DisabledAbilitiesComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.marionettes.MarionetteComponent;
import de.jakob.lotm.util.helper.subordinates.SubordinateComponent;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class CharmAbility extends Ability {
    private static final HashMap<UUID, UUID> charmed = new HashMap<>();
    private static final HashSet<UUID> onCharmedCooldown = new HashSet<>();

    public CharmAbility(String id) {
        super(id, 2, "charm");
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("demoness", 6));
    }

    @Override
    public float getSpiritualityCost() {
        return 40;
    }

    public static HashMap<UUID, UUID> getCharmed() {
        return charmed;
    }

    public static void removeCharm(UUID targetUUID) {
        charmed.remove(targetUUID);
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 18, 1.5f);
        if(target == null) {
            if(entity instanceof ServerPlayer player) {
                ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.literal("No entity to charm found.").withColor(0xFFf980ff));
                player.connection.send(packet);
            }
            return;
        }

        if(charmed.containsKey(target.getUUID()) || onCharmedCooldown.contains(target.getUUID())) {
            if(entity instanceof ServerPlayer player) {
                ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.literal("The entity was charmed already.").withColor(0xFFf980ff));
                player.connection.send(packet);
            }
            return;
        }

        onCharmedCooldown.add(target.getUUID());

        int casterSequence = BeyonderData.getSequence(entity);

        // Charm vs Battle Hypnosis: charm prevails if caster has lower (stronger) sequence
        DisabledAbilitiesComponent disabledComp = target.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT);
        if(disabledComp.isAbilityUsageDisabled()) {
            if(!BeyonderData.isBeyonder(target) || casterSequence <= BeyonderData.getSequence(target)) {
                disabledComp.enableAbilityUsage("battle_hypnosis_disable_beyonder_powers");
                disabledComp.enableAbilityUsage("battle_hypnosis_freeze");
                target.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
            } else {
                if(entity instanceof ServerPlayer player) {
                    ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.literal("Battle Hypnosis is too strong to charm.").withColor(0xFFf980ff));
                    player.connection.send(packet);
                }
                return;
            }
        }

        // Charm overrides Instigation on the same target
        if(target instanceof Mob mob && mob.getTarget() != null) {
            mob.setTarget(null);
        }

        // Charm can temporarily suppress Frenzy (LOOSING_CONTROL effect)
        if(target.hasEffect(ModEffects.LOOSING_CONTROL)) {
            target.removeEffect(ModEffects.LOOSING_CONTROL);
        }

        // Charm can break puppet soldier loyalty
        if(target instanceof BeyonderNPCEntity npc && npc.isPuppetWarrior()) {
            SubordinateComponent subComp = target.getData(ModAttachments.SUBORDINATE_COMPONENT.get());
            if(subComp.isSubordinate()) {
                subComp.setSubordinate(false);
                subComp.setControllerUUID("");
                if(target instanceof Mob mob) {
                    mob.setTarget(null);
                }
            }
        }

        // Charm can contest marionette link - charm must be at least one seq stronger
        MarionetteComponent marionetteComp = target.getData(ModAttachments.MARIONETTE_COMPONENT.get());
        if(marionetteComp.isMarionette()) {
            String controllerUUIDStr = marionetteComp.getControllerUUID();
            if(controllerUUIDStr != null && !controllerUUIDStr.isEmpty()) {
                Entity controller = ((ServerLevel) level).getEntity(UUID.fromString(controllerUUIDStr));
                int controllerSeq = controller instanceof LivingEntity living ? BeyonderData.getSequence(living) : LOTMCraft.NON_BEYONDER_SEQ;
                if(casterSequence < controllerSeq) {
                    marionetteComp.setMarionette(false);
                    marionetteComp.setControllerUUID("");
                    // Freeing a marionette from charm kills it
                    target.kill();
                }
            }
        }

        ServerScheduler.scheduleForDuration(0, 5, 20 * 15, () -> {
           if(!charmed.containsKey(target.getUUID()))
               return;

            ParticleUtil.spawnParticles((ServerLevel) target.level(), ParticleTypes.HEART, target.getEyePosition().subtract(0, .4, 0), 6, .5);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 2, 3, false, false, false));
        }, () -> charmed.remove(target.getUUID()), (ServerLevel) level);

        charmed.put(target.getUUID(), entity.getUUID());
        ServerScheduler.scheduleDelayed(20 * 30, () -> {
            onCharmedCooldown.remove(target.getUUID());
            charmed.remove(target.getUUID());
        });
    }

    @SubscribeEvent
    public static void LivingDamageLiving(LivingDamageEvent.Pre event) {
        if(event.getSource().getEntity() == null)
            return;

        Entity damager = event.getSource().getEntity();
        if(!charmed.containsKey(damager.getUUID()) || charmed.get(damager.getUUID()) != event.getEntity().getUUID())
            return;

        charmed.remove(damager.getUUID());
        event.setNewDamage(0);
        if(event.getEntity() instanceof ServerPlayer player) {
            ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.literal("The enemy was charmed and the hit didnt hurt you.").withColor(0xFFf980ff));
            player.connection.send(packet);
        }
    }
}
