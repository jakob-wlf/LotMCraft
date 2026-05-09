package de.jakob.lotm.abilities.hanged;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ListeningAbility extends ToggleAbility {
    private static final Map<String, Integer> REQUIREMENTS =
            Map.of(HangedPathwayConstants.PATHWAY_ID, HangedPathwayConstants.SEQUENCE_SHADOW_ASCETIC);
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/abilities/spirit_vision_ability.png");
    private static final float SPIRITUALITY_COST = 30.0f;
    private static final Set<UUID> ACTIVE_LISTENERS = new HashSet<>();
    private static final String ENABLED_MESSAGE = "ability.lotmcraft.listening_ability.enabled";
    private static final String DISABLED_MESSAGE = "ability.lotmcraft.listening_ability.disabled";

    public ListeningAbility(String id) {
        super(id);
        canBeCopied = false;
        canBeReplicated = false;
        cannotBeStolen = true;
        autoClear = false;
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (entity.tickCount % 12 == 0) {
            HangedEffectUtil.spawnShadowBurst(serverLevel, entity.getEyePosition(), 0.25, 8);
        }
        if (entity.tickCount % 80 == 0) {
            HangedEffectUtil.playShadowPulse(serverLevel, entity.getEyePosition(), 1.15f);
        }
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        ACTIVE_LISTENERS.add(entity.getUUID());
        if (level instanceof ServerLevel serverLevel) {
            HangedEffectUtil.spawnShadowBurst(serverLevel, entity.getEyePosition(), 0.45, 14);
            HangedEffectUtil.playShadowCast(serverLevel, entity.position());
        }
        AbilityUtil.sendActionBar(entity,
                Component.translatable(ENABLED_MESSAGE)
                        .withColor(HangedPathwayConstants.pathwayColor()));
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        ACTIVE_LISTENERS.remove(entity.getUUID());
        if (level instanceof ServerLevel serverLevel) {
            HangedEffectUtil.spawnShadowBurst(serverLevel, entity.getEyePosition(), 0.35, 10);
            HangedEffectUtil.playShadowPulse(serverLevel, entity.position(), 0.9f);
        }
        AbilityUtil.sendActionBar(entity,
                Component.translatable(DISABLED_MESSAGE)
                        .withColor(HangedPathwayConstants.pathwayColor()));
        clearArtifactScaling(entity);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return REQUIREMENTS;
    }

    @Override
    protected float getSpiritualityCost() {
        return SPIRITUALITY_COST;
    }

    @Override
    public ResourceLocation getTextureLocation() {
        return TEXTURE;
    }

    public static boolean isListening(LivingEntity entity) {
        return ACTIVE_LISTENERS.contains(entity.getUUID());
    }
}
