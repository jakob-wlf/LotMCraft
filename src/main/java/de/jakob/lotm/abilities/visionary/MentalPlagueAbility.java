package de.jakob.lotm.abilities.visionary;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.abilities.visionary.handlers.VisionaryHandler;
import de.jakob.lotm.abilities.visionary.handlers.VisionaryLoosingControlHandler;
import de.jakob.lotm.abilities.visionary.passives.MetaAwarenessAbility;
import de.jakob.lotm.attachments.MentalPlagueComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class MentalPlagueAbility extends SelectableAbility {
    public MentalPlagueAbility(String id) {
        super(id, 10, "plague");
        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 4));
    }

    @Override
    public float getSpiritualityCost() {
        return 600;
    }

    private final DustParticleOptions dust = new DustParticleOptions(
            new Vector3f(250 / 255f, 201 / 255f, 102 / 255f),
            1f
    );

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.mental_plague_ability.place",
                "ability.lotmcraft.mental_plague_ability.activate_sight",
                "ability.lotmcraft.mental_plague_ability.activate_aoe",
                "ability.lotmcraft.mental_plague_ability.list"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        switch (selectedAbility){
            case 0 -> place(entity, level);
            case 1 -> activateSight(entity, level);
            case 2 -> activateAoe(entity, level);
            case 3 -> list(entity, level);
        }
    }

    private void place(LivingEntity entity, Level level){
        if(level.isClientSide) return;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 30* (int) Math.max(multiplier(entity)/4,1), 2);
        if(target == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.mental_plague.no_target").withColor(0xf5ca7f));
            return;
        }

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
        int targetSeq = BeyonderData.getSequence(target);
        if(VisionaryHandler.shouldFailAndTrigger(entitySeq, entity, target, this)){
            return;
        }

        if(AbilityUtil.isTargetSignificantlyStronger(entitySeq, targetSeq)) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.mental_plague.target_too_strong").withColor(0xf5ca7f));
            return;
        }

        Location targetLoc = new Location(target.position(), level);
        int seq = AbilityUtil.getSeqWithArt(entity, this);
        boolean weakened = InteractionHandler.isInteractionPossible(targetLoc, "purification", seq);

        var component = target.getData(ModAttachments.MENTAL_PLAGUE.get());
        component.place(entity.getName().getString(), seq);
        component.setWeakened(weakened);
    }

    private void activateSight(LivingEntity entity, Level level){
        if(level.isClientSide) return;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 30* (int) Math.max(multiplier(entity)/4,1), 2);
        if(target == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.mental_plague.no_target").withColor(0xf5ca7f));
            return;
        }

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
        if(VisionaryHandler.shouldFailAndTrigger(entitySeq, entity, target, this)){
            return;
        }

        var component = target.getData(ModAttachments.MENTAL_PLAGUE.get());
        if(component.isOwner((ServerPlayer) entity) && component.hasMentalPlague()){
            component.activate();
        }
    }

    private void activateAoe(LivingEntity entity, Level level){
        if(level.isClientSide) return;

        float multiplier = multiplier(entity);
        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);

        var nearby = AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, entity.position(), 30 * multiplier);

        for(var e : nearby){
            if(VisionaryHandler.shouldFailAndTrigger(entitySeq, entity, e, this)) continue;

            var component = e.getData(ModAttachments.MENTAL_PLAGUE.get());
            if(component.isOwner((ServerPlayer) entity)){
                component.activate();
            }
        }
    }

    private void list(LivingEntity entity, Level level){
        if(level.isClientSide) return;
        if(!(entity instanceof ServerPlayer player)) return;

        var list = player.server.getPlayerList().getPlayers();

        for (var obj : list){
            var component = obj.getData(ModAttachments.MENTAL_PLAGUE.get());

            if(component.isOwner(player)){
                player.sendSystemMessage(Component.literal(obj.getName().getString() +
                        " has mental plague of stage " +
                        component.getStage()).withColor(0xFFff124d));
            }
        }
    }

    public static void activate(LivingEntity entity){
        if(!(entity.level() instanceof ServerLevel serverLevel)) return;

        Random random = new Random();

        var component = entity.getData(ModAttachments.MENTAL_PLAGUE.get());
        int stage = component.getStage() + 1;
        int seq = component.getSequence();

        stage = component.isWeakened() ? stage / 2 : stage;
        stage = stage <= 0 ? 1 : stage;

        int finalStage = stage;

        ServerScheduler.scheduleForDuration(0, 20, 20 * 5 * stage, () -> {
            if(!component.hasMentalPlague()){
                component.reset();
                return;
            }

            entity.setDeltaMovement((new Vec3(random.nextDouble(-1, 1), random.nextDouble(0, .1), random.nextDouble(-1, 1))).normalize().scale(0.3));

            if(!entity.hasEffect(ModEffects.LOOSING_CONTROL)){
                VisionaryLoosingControlHandler.forceApplyEffect(entity, seq, finalStage);
            }

            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 5, 10));
            entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 20 * 5, 10));
            entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20 * 5, 10));
            entity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 20 * 5, 10));
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20 * 5, 3));

            entity.hurtMarked = true;
        }, component::reset, serverLevel);

        var nearby = AbilityUtil.getNearbyEntities(entity, serverLevel, entity.position(), 30);

        for (var e : nearby){
            var eComponent = e.getData(ModAttachments.MENTAL_PLAGUE.get());

            if(!eComponent.hasMentalPlague()) continue;

            if(eComponent.getOwnerName().equals(component.getOwnerName())){
                eComponent.activate();
            }
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        var entity = event.getEntity();
        if(!(entity instanceof LivingEntity livingEntity)) return;
        if(!(livingEntity.level() instanceof ServerLevel level)) return;

        var component = livingEntity.getData(ModAttachments.MENTAL_PLAGUE.get());
        if(!component.hasMentalPlague()) return;

        if(livingEntity.tickCount % 5000 * (component.getStage() + 1) == 0){
            if(component.getStage() < MentalPlagueComponent.MAX_STAGE)
                component.setStage(component.getStage() + 1);
        }

        if(component.shouldBeActivated()){
            activate(livingEntity);
        }
        else{
            if(component.getInfected() >= MentalPlagueComponent.MAX_INFECTED) return;

            var nearBy = AbilityUtil.getNearbyEntities(livingEntity, level, livingEntity.position(), 20);
            for(var e : nearBy){
                if(e.getName().getString().equals(component.getOwnerName())) continue;

                if(!VisionaryHandler.shouldFail(component.getSequence(), e))
                    e.getData(ModAttachments.MENTAL_PLAGUE.get()).place(component.getOwnerName(), component.getSequence());

                component.setInfected(component.getInfected() + 1);
            }
        }
    }

}
