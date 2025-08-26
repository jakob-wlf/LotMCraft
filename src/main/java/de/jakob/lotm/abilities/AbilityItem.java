package de.jakob.lotm.abilities;

import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.ClientBeyonderCache;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public abstract class AbilityItem extends Item {

    protected final Random random = new Random();

    private static final Map<Player, Integer> cooldowns = new HashMap<>();

    protected final int cooldown;

    public boolean canBeUsedByNPC = true;
    public boolean canBeCopied = true;

    public AbilityItem(Item.Properties properties, float cooldown) {
        super(properties);

        this.cooldown = (int) (cooldown * 20);
    }

    public abstract Map<String, Integer> getRequirements();

    protected abstract float getSpiritualityCost();

    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {

        if (!canUse(player)) return InteractionResultHolder.fail(player.getItemInHand(hand));

        if(cooldown > 0 && cooldowns.containsKey(player) && (System.currentTimeMillis() - cooldowns.get(player)) < cooldown && !level.isClientSide) {
            return InteractionResultHolder.fail(player.getItemInHand(hand));
        }

        if (!player.isCreative() && !level.isClientSide) {
            BeyonderData.reduceSpirituality(player, getSpiritualityCost());
        }

        if (cooldown > 0 && !level.isClientSide) {
            cooldowns.put(player, (int) System.currentTimeMillis());
            player.getCooldowns().addCooldown(this, cooldown);
        }

        onAbilityUse(level, player);

        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    protected double multiplier(LivingEntity entity) {
        return BeyonderData.getMultiplier(entity);
    }

    public boolean canUse(Player player) {
        return canUse(player, false);
    }

    public boolean canUse(Player player, boolean ignoreCreative) {
        // Creative mode always works
        if (player.isCreative() && !ignoreCreative) {
            return true;
        }

        if (player.level().isClientSide()) {
            // Client-side: use cached data
            String pathway = ClientBeyonderCache.getPathway(player.getUUID());
            int sequence = ClientBeyonderCache.getSequence(player.getUUID());
            float spirituality = ClientBeyonderCache.getSpirituality(player.getUUID());

            // Debug pathway always works
            if (pathway.equalsIgnoreCase("debug")) {
                return true;
            }

            if(!getRequirements().containsKey(pathway))
                return false;

            // Check if pathway has requirements
            Integer minSeq = getRequirements().get(pathway);
            if (minSeq == null) {
                return false;
            }

            // Check sequence and spirituality requirements
            return sequence <= minSeq && spirituality >= getSpiritualityCost();
        } else {
            // Server-side: use your existing logic
            String pathway = BeyonderData.getPathway(player);
            int sequence = BeyonderData.getSequence(player);

            // Debug pathway always works
            if (pathway.equalsIgnoreCase("debug")) {
                return true;
            }

            if(!getRequirements().containsKey(pathway))
                return false;

            // Check if pathway has requirements
            Integer minSeq = getRequirements().get(pathway);
            if (minSeq == null) {
                return false;
            }

            // Check sequence and spirituality requirements
            return sequence <= minSeq && BeyonderData.getSpirituality(player) >= getSpiritualityCost();
        }
    }

    protected abstract void onAbilityUse(Level level, LivingEntity entity);

    public void onHold(Level level, LivingEntity entity) {

    }
}