package de.jakob.lotm.abilities.hanged.passives;

import de.jakob.lotm.abilities.PassiveAbilityItem;
import de.jakob.lotm.abilities.hanged.HangedPathwayConstants;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Map;

public class RoseBishopRegenerationAbility extends PassiveAbilityItem {
    private static final String REPLENISHED_MESSAGE = "ability.lotmcraft.rose_bishop_regeneration.replenished";
    private static final String STARVING_MESSAGE = "ability.lotmcraft.rose_bishop_regeneration.depleted";

    public RoseBishopRegenerationAbility(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return Map.of(HangedPathwayConstants.PATHWAY_ID, HangedPathwayConstants.SEQUENCE_ROSE_BISHOP);
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level.isClientSide) {
            return;
        }

        if (entity.tickCount % HangedPathwayConstants.ROSE_BISHOP_REGEN_INTERVAL_TICKS == 0) {
            float missingHealth = entity.getMaxHealth() - entity.getHealth();
            if (missingHealth > 0.1f) {
                float healAmount = Math.min(missingHealth, 0.35f + (missingHealth / entity.getMaxHealth()));
                entity.heal(healAmount);
            }
        }

        if (!(entity instanceof ServerPlayer player)) {
            return;
        }

        if (player.tickCount % HangedPathwayConstants.ROSE_BISHOP_REPLENISH_INTERVAL_TICKS != 0) {
            return;
        }

        float lowThreshold = player.getMaxHealth() * 0.5f;
        float criticalThreshold = player.getMaxHealth() * 0.35f;
        if (player.getHealth() > lowThreshold) {
            return;
        }

        if (consumeBlood(player)) {
            player.heal(6.0f);
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 1, false, false, false));
            AbilityUtil.sendActionBar(player,
                    Component.translatable(REPLENISHED_MESSAGE).withColor(HangedPathwayConstants.pathwayColor()));
        } else if (player.getHealth() <= criticalThreshold) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, false, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 60, 0, false, false, false));
            player.getData(ModAttachments.SANITY_COMPONENT).decreaseSanityAndSync(0.012f, player);
            AbilityUtil.sendActionBar(player,
                    Component.translatable(STARVING_MESSAGE).withColor(HangedPathwayConstants.pathwayColor()));
        }
    }

    static boolean consumeBlood(Player player) {
        ItemStack offhand = player.getOffhandItem();
        if (offhand.is(ModItems.BLOOD)) {
            offhand.shrink(1);
            if (offhand.isEmpty()) {
                player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, ItemStack.EMPTY);
            }
            return true;
        }

        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(ModItems.BLOOD)) {
                stack.shrink(1);
                return true;
            }
        }

        return false;
    }
}
