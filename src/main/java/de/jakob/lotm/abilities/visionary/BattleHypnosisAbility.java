package de.jakob.lotm.abilities.visionary;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class BattleHypnosisAbility extends Ability {
    public BattleHypnosisAbility(String id) {
        super(id, 2);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 6));
    }

    @Override
    public float getSpiritualityCost() {
        return 150;
    }

    private final DustParticleOptions dust = new DustParticleOptions(
            new Vector3f(250 / 255f, 201 / 255f, 102 / 255f),
            1.25f
    );

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 20, 2);

        if(target == null) {
            if(entity instanceof ServerPlayer player) {
                ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.translatable("ability.lotmcraft.frenzy.no_target").withColor(0xFFff124d));
                player.connection.send(packet);
            }
            return;
        }

        ParticleUtil.createParticleSpirals((ServerLevel) level, dust, target.position(), target.getBbWidth() + .25, target.getBbWidth() + .25, target.getEyeHeight(), 1, 5, 30, 15, 1);

        switch (random.nextInt(3)) {
            case 0 -> freezeTarget((ServerLevel) level, entity, target);
            case 1 -> weakenAndMoveAroundTarget((ServerLevel) level, entity, target);
            case 2 -> stopBeyonderPowersForTarget((ServerLevel) level, entity, target);
        }
    }

    private void stopBeyonderPowersForTarget(ServerLevel level, LivingEntity entity, LivingEntity target) {
        if(!BeyonderData.isBeyonder(target)) {
            switch (random.nextInt(2)) {
                case 0 -> weakenAndMoveAroundTarget(level, entity, target);
                case 1 -> freezeTarget(level, entity, target);
            }
            return;
        }

        AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.battle_hypnosis.stop_beyonder_powers").withColor(0xf5c56c));

        BeyonderData.disableAbilityUse(target, "battle_hypnosis_stop_beyonder_powers");
        ServerScheduler.scheduleDelayed(20 * 9, () -> BeyonderData.enableAbilityUse(target, "battle_hypnosis_stop_beyonder_powers"));
    }

    private void weakenAndMoveAroundTarget(ServerLevel level, LivingEntity entity, LivingEntity target) {
        AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.battle_hypnosis.weaken").withColor(0xf5c56c));

        BeyonderData.addModifier(target, "battle_hypnosis_weaken", .4);
        ServerScheduler.scheduleDelayed(20 * 12, () -> BeyonderData.removeModifier(target, "battle_hypnosis_weaken"));

        ServerScheduler.scheduleForDuration(0, 5, 20 * 8, () -> {
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20, 5, false, false, true));

            target.setDeltaMovement(new Vec3((random.nextDouble() - .5) * 2, (random.nextDouble() - .5) * .15, (random.nextDouble() - .5) * 2).scale(.75));
            target.hurtMarked = true;
        }, level);
    }

    private void freezeTarget(ServerLevel level, LivingEntity entity, LivingEntity target) {
        AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.battle_hypnosis.stop").withColor(0xf5c56c));

        BeyonderData.disableAbilityUse(target, "battle_hypnosis_freeze");
        ServerScheduler.scheduleDelayed(20 * 3, () -> BeyonderData.enableAbilityUse(target, "battle_hypnosis_freeze"));

        ServerScheduler.scheduleForDuration(0, 1, 20 * 5, () -> {
            target.setDeltaMovement(0, 0, 0);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 10, false, false, true));
            target.hurtMarked = true;
        }, level);
    }
}
