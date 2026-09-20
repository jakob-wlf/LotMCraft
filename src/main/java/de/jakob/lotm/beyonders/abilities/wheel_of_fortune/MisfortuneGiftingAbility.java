package de.jakob.lotm.beyonders.abilities.wheel_of_fortune;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.wheel_of_fortune.passives.PassiveLuckAccumulationAbility;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.LuckManager;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class MisfortuneGiftingAbility extends Ability {
    private static final int luckCost = 50;
    private static final float baseLuckDrainRatePerMinute = 120;
    private static final int luckDrainDurationTicks = 20 * 60;

    public MisfortuneGiftingAbility(String id) {
        super(id, 5);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("wheel_of_fortune", 5));
    }

    @Override
    public float getSpiritualityCost() {
        return 120;
    }

    @Override
    public int luckCost() {
        return luckCost;
    }

    private static final DustParticleOptions dust = new DustParticleOptions(new Vector3f(201 / 255f, 150 / 255f, 79 / 255f), 1.5f);

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        LivingEntity target = AbilityUtil.getTargetEntity(entity, (int) (15 * (multiplier(entity) * multiplier(entity))), 2);

        if(target == null) {
            if(entity instanceof ServerPlayer player) {
                Component actionBarText = Component.translatable("ability.lotmcraft.misfortune_gifting.no_target").withColor(0xFFc0f6fc);
                ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(actionBarText);
                player.connection.send(packet);
            }

            return;
        }
        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
        int targetSeq = BeyonderData.getSequence(target);
        double eyeHeight = target.getEyeHeight();
        ParticleUtil.spawnParticles(serverLevel, dust, target.position().add(0, eyeHeight / 2, 0), 120, .3, eyeHeight / 2, .3, 0);
        float drainRate = Math.min(LuckManager.getSequenceScaledEffectRate(entity,
            Math.round(multiplier(entity) * 3.125f) * baseLuckDrainRatePerMinute), 6500);
        if (drainRate <= 0) {
            return;
        }
        if (!PassiveLuckAccumulationAbility.consumeStoredLuck(entity, luckCost)) {
            AbilityUtil.sendActionBar(entity, Component.literal("\u00A7cMisfortune Gifting requires more luck."));
            return;
        }
        LuckManager.applyLuckDrain(entity, target, "misfortune_gifting:" + entity.getUUID(),
            drainRate, luckDrainDurationTicks);
    }
}
