package de.jakob.lotm.abilities;

import de.jakob.lotm.abilities.door.RecordingAbility;
import de.jakob.lotm.data.ModDataComponents;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.ClientBeyonderCache;
import de.jakob.lotm.util.data.Location;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class AbilityItem extends Item {

    protected final Random random = new Random();

    static final Map<UUID, Integer> cooldowns = new HashMap<>();

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

        if(cooldown > 0 && cooldowns.containsKey(beyonderNPC.getUUID()) && (System.currentTimeMillis() - cooldowns.get(beyonderNPC.getUUID())) < cooldown) {
            return;
        }

        if(!level.isClientSide)
            AbilityHandler.useAbilityInArea(this, new Location(beyonderNPC.position(), level));

        cooldowns.put(beyonderNPC.getUUID(), (int) System.currentTimeMillis());

        onAbilityUse(level, beyonderNPC);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {

        if (!canUse(player) && !isRecorded(player.getItemInHand(hand))) return InteractionResultHolder.fail(player.getItemInHand(hand));

        if(cooldown > 0 && cooldowns.containsKey(player.getUUID()) && (System.currentTimeMillis() - cooldowns.get(player.getUUID())) < cooldown && !level.isClientSide) {
            return InteractionResultHolder.fail(player.getItemInHand(hand));
        }

        if (!player.isCreative() && !level.isClientSide) {
            BeyonderData.reduceSpirituality(player, getSpiritualityCost());
        }

        if (cooldown > 0 && !level.isClientSide) {
            cooldowns.put(player.getUUID(), (int) System.currentTimeMillis());
            player.getCooldowns().addCooldown(this, cooldown);
        }

        if(!level.isClientSide)
            AbilityHandler.useAbilityInArea(this, new Location(player.position(), level));

        onAbilityUse(level, player);

        if(isRecorded(player.getItemInHand(hand)))
            player.setItemInHand(hand, ItemStack.EMPTY);

        return InteractionResultHolder.success(player.getItemInHand(hand));
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
}