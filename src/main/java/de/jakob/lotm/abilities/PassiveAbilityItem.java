package de.jakob.lotm.abilities;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.ClientBeyonderCache;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

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

    @Override
    public void appendHoverText(@NotNull ItemStack stack,
                                @NotNull TooltipContext context,
                                @NotNull List<Component> tooltipComponents,
                                @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        if(Component.translatable(this.getDescriptionId(stack) + ".description").getString().equals(this.getDescriptionId(stack) + ".description"))
            return;

        tooltipComponents.add(Component.translatable("lotm.description").append(":").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable(this.getDescriptionId(stack) + ".description").withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public @NotNull Component getName(ItemStack stack) {
        return Component.translatable(this.getDescriptionId(stack)).append(Component.literal(" (")).append(Component.translatable("lotm.passive")).append(Component.literal(")"));
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


}