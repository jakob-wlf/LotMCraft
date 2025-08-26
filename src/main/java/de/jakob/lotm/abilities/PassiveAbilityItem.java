package de.jakob.lotm.abilities;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.ClientBeyonderCache;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public abstract class PassiveAbilityItem extends Item {

    protected final Random random = new Random();

    private static final Map<Player, Integer> cooldowns = new HashMap<>();

    public PassiveAbilityItem(Properties properties) {
        super(properties);
    }

    public abstract Map<String, Integer> getRequirements();

    public boolean shouldApplyTo(LivingEntity entity) {
        if (entity.level().isClientSide()) {
            // Client-side: use cached data
            String pathway = ClientBeyonderCache.getPathway(entity.getUUID());
            int sequence = ClientBeyonderCache.getSequence(entity.getUUID());

            if(pathway == null) {
                return false;
            }

            if(!getRequirements().containsKey(pathway))
                return false;

            // Check if pathway has requirements
            Integer minSeq = getRequirements().get(pathway);
            if (minSeq == null) {
                return false;
            }

            // Check sequence
            return sequence <= minSeq;
        } else {
            // Server-side: use your existing logic
            String pathway = BeyonderData.getPathway(entity);
            int sequence = BeyonderData.getSequence(entity);

            if(!getRequirements().containsKey(pathway))
                return false;

            // Check if pathway has requirements
            Integer minSeq = getRequirements().get(pathway);
            if (minSeq == null) {
                return false;
            }

            return sequence <= minSeq;
        }
    }

    /**
     * Gets called every 5 ticks from BeyonderDataTickHandler
     */
    public abstract void tick(Level level, LivingEntity entity);

    protected void applyPotionEffects(LivingEntity entity, List<MobEffectInstance> effects) {
        for (MobEffectInstance effect : effects) {
            entity.addEffect(new MobEffectInstance(
                    effect.getEffect(),
                    effect.getDuration(),
                    effect.getAmplifier(),
                    effect.isAmbient(),
                    effect.isVisible(),
                    effect.showIcon()
            ));
        }
    }

    public ResourceLocation getIconTexture() {
        ResourceLocation registryName = BuiltInRegistries.ITEM.getKey(this);

        String texturePath = "textures/gui/passives/" + registryName.getPath();
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, texturePath);
    }
}