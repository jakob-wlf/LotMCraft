package de.jakob.lotm.abilities;

import de.jakob.lotm.abilities.door.RecordingAbility;
import de.jakob.lotm.data.ModDataComponents;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.ClientBeyonderCache;
import de.jakob.lotm.util.data.Location;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbilityItem extends Item {

    protected final Random random = new Random();

    static final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    protected final int cooldown;

    public boolean canBeUsedByNPC = true;
    public boolean canBeCopied = true;
    public boolean hasOptimalDistance = false;
    public float optimalDistance = 1f;

    public AbilityItem(Item.Properties properties, float cooldown) {
        super(properties);

        this.cooldown = (int) (cooldown * 20);
    }

    public int lowestSequenceUsable() {
        return getRequirements().values().stream()
                .max(Integer::compareTo)
                .orElse(-1);
    }

    public abstract Map<String, Integer> getRequirements();

    protected abstract float getSpiritualityCost();

    public void useAsNpcAbility(Level level, BeyonderNPCEntity beyonderNPC) {
        if(!this.canBeUsedByNPC)
            return;

        if(this.cooldown > 0 && cooldowns.containsKey(beyonderNPC.getUUID()) && (System.currentTimeMillis() - cooldowns.get(beyonderNPC.getUUID())) < (this.cooldown * 50L)) {
            return;
        }

        if(!level.isClientSide)
            AbilityHandler.useAbilityInArea(this, new Location(beyonderNPC.position(), level));

        cooldowns.put(beyonderNPC.getUUID(), System.currentTimeMillis());

        onAbilityUse(level, beyonderNPC);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!canUse(player) && !isRecorded(itemStack)) {
            return InteractionResultHolder.fail(itemStack);
        }

        // Check cooldown on BOTH client and server sides
        if(cooldown > 0 && cooldowns.containsKey(player.getUUID()) &&
                (System.currentTimeMillis() - cooldowns.get(player.getUUID())) < (cooldown * 50L)) {
            return InteractionResultHolder.fail(itemStack);
        }

        // Only reduce spirituality on server side
        if (!player.isCreative() && !level.isClientSide) {
            BeyonderData.reduceSpirituality(player, getSpiritualityCost());
        }

        // Set cooldown on both sides, but only add to player cooldowns on server
        if (cooldown > 0) {
            cooldowns.put(player.getUUID(), System.currentTimeMillis());
            if (!level.isClientSide) {
                player.getCooldowns().addCooldown(this, cooldown);
            }
        }

        if(!level.isClientSide) {
            AbilityHandler.useAbilityInArea(this, new Location(player.position(), level));
        }

        onAbilityUse(level, player);

        if(isRecorded(itemStack)) {
            player.setItemInHand(hand, ItemStack.EMPTY);
        }

        return InteractionResultHolder.success(itemStack);
    }

    private boolean isRecorded(ItemStack item) {
        return item.getOrDefault(ModDataComponents.IS_RECORDED, false);
    }

    protected double multiplier(LivingEntity entity) {
        return BeyonderData.getMultiplier(entity);
    }

    public boolean canUse(Player player) {
        return canUse(player, false);
    }

    public boolean canUse(Player player, ItemStack itemStack) {
        return canUse(player, false) || isRecorded(itemStack);
    }

    public boolean canUse(Player player, boolean ignoreCreative) {
        return AbilityHandler.canUse(player, ignoreCreative, getRequirements(), getSpiritualityCost());
    }

    protected abstract void onAbilityUse(Level level, LivingEntity entity);

    public void onHold(Level level, LivingEntity entity) {

    }

    @Override
    public void appendHoverText(ItemStack stack,
                                TooltipContext context,
                                List<Component> tooltipComponents,
                                TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        if(Component.translatable(this.getDescriptionId(stack) + ".description").getString().equals(this.getDescriptionId(stack) + ".description"))
            return;

        tooltipComponents.add(Component.translatable("lotm.description").append(":").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable(this.getDescriptionId(stack) + ".description").withStyle(ChatFormatting.DARK_GRAY));
    }

    public boolean shouldUseAbility(LivingEntity entity) {
        if(entity instanceof Mob mob && mob.getTarget() == null)
            return false;
        if(!hasOptimalDistance)
            return true;
        else if(entity instanceof Mob mob && mob.getTarget() != null) {
            return mob.distanceTo(mob.getTarget()) <= optimalDistance;
        }
        return false;
    }
}