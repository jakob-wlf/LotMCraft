package de.jakob.lotm.beyonders.abilities.core;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.fool.marionettes.ControllingUtils;
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
        if(
                entity instanceof Player player &&
                ControllingUtils.isControlling(player)
        ) {
            String pathway = ControllingUtils.getControlledPathway(player);
            int sequence = ControllingUtils.getControlledSequence(player);

            if(pathway != null) {
                if(getRequirements().containsKey(pathway)) {
                    Integer minSeq = getRequirements().get(pathway);
                    if (minSeq != null && sequence <= minSeq) return true;
                }
            }
            if(!ControllingUtils.canUseOwnAbilitiesWhileControlling(player)) return false;
            if(this instanceof PhysicalEnhancementsAbility) return false;
        }


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

    public ResourceLocation getTexture() {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/abilities/passive/" + id + ".png");
    }

    public String getId() {
        return id;
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


}