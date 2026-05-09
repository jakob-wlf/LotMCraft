package de.jakob.lotm.abilities.hanged.passives;

import de.jakob.lotm.abilities.PassiveAbilityItem;
import de.jakob.lotm.abilities.demoness.ShadowConcealmentAbility;
import de.jakob.lotm.abilities.hanged.HangedPathwayConstants;
import de.jakob.lotm.abilities.visionary.PsychologicalInvisibilityAbility;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Map;

public class SecretsSuppliantPerceptionAbility extends PassiveAbilityItem {
    private static final double DETECTION_RADIUS = 32.0;
    private static final int DREADFUL_EXISTENCE_SEQUENCE = 4;
    private static final String BEYONDER_AURA_MESSAGE = "ability.lotmcraft.secrets_suppliant_perception.beyonder_aura";
    private static final String DREAD_PRESENCE_MESSAGE = "ability.lotmcraft.secrets_suppliant_perception.dread_presence";

    public SecretsSuppliantPerceptionAbility(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return Map.of(HangedPathwayConstants.PATHWAY_ID, HangedPathwayConstants.SEQUENCE_SECRETS_SUPPLIANT);
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel) || !(entity instanceof ServerPlayer player)) {
            return;
        }

        if (player.tickCount % 20 != 0) {
            return;
        }

        List<LivingEntity> nearbyEntities = AbilityUtil.getNearbyEntities(player, serverLevel, player.getEyePosition(), DETECTION_RADIUS);

        boolean sensedBeyonderAura = false;
        boolean sensedDreadPresence = false;

        for (LivingEntity nearby : nearbyEntities) {
            if (isAuraConcealed(nearby)) {
                continue;
            }

            if (!BeyonderData.isBeyonder(nearby)) {
                continue;
            }

            sensedBeyonderAura = true;

            if (AbilityUtil.isTargetSignificantlyStronger(entity, nearby)
                    || BeyonderData.getSequence(nearby) <= DREADFUL_EXISTENCE_SEQUENCE) {
                sensedDreadPresence = true;
                break;
            }
        }

        int pathwayColor = HangedPathwayConstants.pathwayColor();
        if (sensedDreadPresence) {
            AbilityUtil.sendActionBar(player,
                    Component.translatable(DREAD_PRESENCE_MESSAGE).withColor(pathwayColor));
        } else if (sensedBeyonderAura) {
            AbilityUtil.sendActionBar(player,
                    Component.translatable(BEYONDER_AURA_MESSAGE).withColor(pathwayColor));
        }
    }

    private static boolean isAuraConcealed(LivingEntity entity) {
        if (entity.hasEffect(ModEffects.CONCEALMENT)) {
            return true;
        }

        return PsychologicalInvisibilityAbility.invisiblePlayers.containsKey(entity.getUUID())
                || ShadowConcealmentAbility.invisiblePlayers.contains(entity.getUUID());
    }
}
