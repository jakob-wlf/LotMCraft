package de.jakob.lotm.beyonders.abilities.core;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.ClientBeyonderCache;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class PassiveAbility {

    protected final Random random = new Random();
    private final String id;

    private static final Map<Player, Integer> cooldowns = new HashMap<>();

    public PassiveAbility(String id) {
        this.id = id;
    }

    public abstract Map<String, Integer> getRequirements();

    public boolean shouldApplyTo(LivingEntity entity) {
        if (entity.level().isClientSide()) {
            String pathway = ClientBeyonderCache.getPathway(entity.getUUID());
            int sequence = ClientBeyonderCache.getSequence(entity.getUUID());

            if(pathway == null) {
                return false;
            }

            if(getRequirements().containsKey(pathway)) {
                Integer minSeq = getRequirements().get(pathway);
                if (minSeq != null && sequence <= minSeq) return true;
            }

            if (!(this instanceof PhysicalEnhancementsAbility)) {
                String[] history = ClientBeyonderCache.getPathwayHistory(entity.getUUID());
                for (int i = sequence + 1; i < history.length; i++) {
                    String histPathway = history[i];
                    if (histPathway == null || histPathway.isEmpty()) continue;
                    Integer minSeq = getRequirements().get(histPathway);
                    if (minSeq != null && i <= minSeq) return true;
                }
            }

            return false;
        } else {
            String pathway = BeyonderData.getPathway(entity);
            int sequence = BeyonderData.getSequence(entity);

            if(getRequirements().containsKey(pathway)) {
                Integer minSeq = getRequirements().get(pathway);
                if (minSeq != null && sequence <= minSeq) return true;
            }

            if (!(this instanceof PhysicalEnhancementsAbility)) {
                String[] history = BeyonderData.getPathwayHistory(entity);
                for (int i = sequence + 1; i < history.length; i++) {
                    String histPathway = history[i];
                    if (histPathway == null || histPathway.isEmpty()) continue;
                    Integer minSeq = getRequirements().get(histPathway);
                    if (minSeq != null && i <= minSeq) return true;
                }
            }

            return false;
        }
    }

    public ResourceLocation getTexture() {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/abilities/passive/" + id + ".png");
    }

    public String getId() {
        return id;
    }

    protected void applyRegenReduce(ArrayList<MobEffectInstance> effects, Entity entity, HashMap<UUID, Long> reducedRegen) {
        if(!(entity instanceof Player)) {
            return;
        }
        if(effects == null) {
            return;
        }
        if(!reducedRegen.containsKey(entity.getUUID())) {
            return;
        }

        if ((reducedRegen.get(entity.getUUID()) - System.currentTimeMillis()) <= 0) {
            reducedRegen.remove(entity.getUUID());
        }

        MobEffectInstance regen = null;

        for (MobEffectInstance effect : effects) {
            if (effect.getEffect() == MobEffects.REGENERATION) {
                regen = effect;
                break;
            }
        }

        if (regen != null) {
            int newAmplifier = regen.getAmplifier() - 5;

            if (newAmplifier < 0) {
                effects.remove(regen);
            } else {
                effects.remove(regen);
                effects.add(new MobEffectInstance(
                        MobEffects.REGENERATION,
                        regen.getDuration(),
                        newAmplifier,
                        regen.isAmbient(),
                        regen.isVisible(),
                        regen.showIcon()
                ));
            }
        }
    }
    /**
     * Gets called every 5 ticks from BeyonderDataTickHandler
     */
    public abstract void tick(Level level, LivingEntity entity);

    public void onPassiveAbilityGained(LivingEntity entity, ServerLevel serverLevel) {
    }

    public void onPassiveAbilityRemoved(LivingEntity entity, ServerLevel serverLevel) {

    }

    protected static int getColorForPathway(String pathway) {
        return BeyonderData.pathwayInfos.containsKey(pathway) ? BeyonderData.pathwayInfos.get(pathway).color() : 0xFFFFFF;
    }

    public MutableComponent getName() {
        return Component.translatable("ability.lotmcraft." + id).withStyle(ChatFormatting.BOLD);
    }

    public MutableComponent getDescription() {
        return Component.translatable("ability.lotmcraft." + id + ".description");
    }

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


}