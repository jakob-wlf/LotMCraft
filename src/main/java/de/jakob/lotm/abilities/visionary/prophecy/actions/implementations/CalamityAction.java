package de.jakob.lotm.abilities.visionary.prophecy.actions.implementations;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.abyss.LanguageOfFoulnessAbility;
import de.jakob.lotm.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.abilities.visionary.prophecy.TokenStream;
import de.jakob.lotm.abilities.visionary.prophecy.actions.ActionBase;
import de.jakob.lotm.abilities.visionary.prophecy.actions.ActionsEnum;
import de.jakob.lotm.abilities.visionary.prophecy.actions.context.ActionContextBase;
import de.jakob.lotm.abilities.visionary.prophecy.actions.context.ActionContextEnum;
import de.jakob.lotm.abilities.visionary.prophecy.actions.context.implementations.ActionStringContext;
import de.jakob.lotm.abilities.wheel_of_fortune.calamities.Earthquake;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.ability_entities.MeteorEntity;
import de.jakob.lotm.entity.custom.ability_entities.TornadoEntity;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Random;

public class CalamityAction extends ActionBase {
    public static final int METEOR_COUNT = 25;
    private static final double METEOR_RADIUS = 50.0;

    private static final DustParticleOptions plagueDust = new DustParticleOptions(new Vector3f(0, 0, 0), 10f);

    public CalamityAction(ActionContextBase context) {
        super(context);
    }

    @Override
    public ActionsEnum getType() {
        return ActionsEnum.CALAMITY;
    }

    @Override
    public int getRequiredSeq() {
        return 1;
    }

    @Override
    public void action(Level level, LivingEntity entity) {
        if(!(context instanceof ActionStringContext string)) return;
        if(!(level instanceof ServerLevel serverLevel)) return;

        TokenStream stream = new TokenStream(string.string);

        float multiplier = (float) BeyonderData.getMultiplier(entity);
        boolean griefing = BeyonderData.isGriefingEnabled(entity);
        Vec3 center = entity.position();

        switch (stream.peek()){
            case "meteor", "meteors" -> spawnMeteorShower(serverLevel,center,multiplier,griefing,entity);
            case "tornado" -> createTornados(serverLevel, entity, multiplier, center);
            case "earthquake" -> new Earthquake().spawnCalamity(serverLevel, center, multiplier, griefing, 65, (float) DamageLookup.lookupDps(4, .925, 8, 20) * (int) multiplier, entity, true);
            case "plague" -> createPlague(serverLevel, entity, multiplier);
        }


    }

    public static CalamityAction load(CompoundTag tag, HolderLookup.Provider provider) {
        return new CalamityAction(ActionContextBase.load(ActionContextEnum.STRING, tag, provider));
    }

    private static void spawnMeteorShower(ServerLevel level, Vec3 center,
                                          float multiplier, boolean griefing, LivingEntity entity) {
        Random rand = new Random();
        for (int i = 0; i < METEOR_COUNT; i++) {
            ServerScheduler.scheduleDelayed(i * 4, () -> {
                double angle = rand.nextDouble() * 2 * Math.PI;
                double distance = rand.nextDouble() * METEOR_RADIUS;
                double offsetX = Math.cos(angle) * distance;
                double offsetZ = Math.sin(angle) * distance;
                Vec3 meteorPos = new Vec3(center.x + offsetX, center.y, center.z + offsetZ);

                MeteorEntity meteor = new MeteorEntity(level, 2.5f,  (float) DamageLookup.lookupDamage(2, 1)  * (int)multiplier, 3, null, griefing, 13, 12);
                meteor.setPosition(meteorPos);
                level.addFreshEntity(meteor);
            }, level, () -> AbilityUtil.getTimeInArea(null, new Location(center, level)));
        }
    }

    private void createTornados(ServerLevel serverLevel, LivingEntity entity, float multiplier, Vec3 pos) {
        TornadoEntity tornado =  new TornadoEntity(ModEntities.TORNADO.get(), serverLevel, .15f, (float) DamageLookup.lookupDamage(2, .35)  * (int)multiplier, null) ;
        tornado.setPos(pos);
        serverLevel.addFreshEntity(tornado);

        for(int i = 0; i < 30; i++) {
            TornadoEntity additionalTornado = new TornadoEntity(ModEntities.TORNADO.get(), serverLevel, .15f, 17f, null);
            Vec3 randomOffset = new Vec3((serverLevel.random.nextDouble() - 0.5) * 120, 3, (serverLevel.random.nextDouble() - 0.5) * 120);
            additionalTornado.setPos(pos.add(randomOffset));
            serverLevel.addFreshEntity(additionalTornado);
        }
    }

    private void createPlague(Level level, LivingEntity entity, float multiplier){
        if(level.isClientSide || !(level instanceof ServerLevel serverLevel))
            return;

        ServerScheduler.scheduleForDuration(0, 20, 20 * 80, () -> {
            Location currentLoc = new Location(entity.position(), entity.level());
            int seq = BeyonderData.getSequence(entity);
            if(InteractionHandler.isInteractionPossible(currentLoc, "purification", seq) ||
                    InteractionHandler.isInteractionPossible(currentLoc, "cleansing", seq))
                return;

            boolean bloomingNearby = InteractionHandler.isInteractionPossible(currentLoc, "blooming", seq);
            float damageMult = (bloomingNearby) ? 0.4f : 1f;

            ParticleUtil.spawnParticles((ServerLevel) entity.level(), ModParticles.DISEASE.get(), entity.position(), 160, 50, 0.02);
            ParticleUtil.spawnParticles((ServerLevel) entity.level(), plagueDust, entity.position(), 160, 50, 0.02);
            AbilityUtil.addPotionEffectToNearbyEntities((ServerLevel) entity.level(), entity, 45*(int) multiplier, entity.position(), new MobEffectInstance(MobEffects.WITHER, 20, 3, false, false, false));
            AbilityUtil.addPotionEffectToNearbyEntities((ServerLevel) entity.level(), entity, 45*(int) multiplier, entity.position(), new MobEffectInstance(MobEffects.BLINDNESS, 20, 4, false, false, false));
            AbilityUtil.addPotionEffectToNearbyEntities((ServerLevel) entity.level(), entity, 45*(int) multiplier, entity.position(), new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 2, false, false, false));
            AbilityUtil.damageNearbyEntities((ServerLevel) entity.level(), entity, 45*(int) multiplier, DamageLookup.lookupDps(4, .3, 35, 20) *(int) Math.max(multiplier/6,1) * damageMult, entity.position(), true, false, true, 0, ModDamageTypes.source(level, ModDamageTypes.DEMONESS_GENERIC, entity));

            entity.hurt(ModDamageTypes.source(level, ModDamageTypes.DEMONESS_GENERIC, null), (float) (DamageLookup.lookupDps(4, .3, 35, 20) *(int) Math.max(multiplier/6,1) * damageMult));
        });
    }
}
