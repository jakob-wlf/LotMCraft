package de.jakob.lotm.item.custom;

import de.jakob.lotm.entity.client.BigSunRenderer;
import de.jakob.lotm.entity.custom.BigSunEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.DamageLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SunItem extends Item {
    public SunItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.fail(player.getItemInHand(usedHand));
        }

        player.getCooldowns().addCooldown(this, 20 * 35);

        setTimeToNoon(serverLevel);
        BigSunEntity sunEntity = new BigSunEntity(serverLevel, (float) DamageLookup.lookupDps(2, .7f, 2, 20) * (float) BeyonderData.getMultiplierForSequence(2), BeyonderData.isGriefingEnabled(player), player.getUUID(), 20 * 30);
        sunEntity.setPos(player.getX(), player.getY() + 25, player.getZ());
        serverLevel.addFreshEntity(sunEntity);

        if(!player.getAbilities().instabuild) {
            ItemStack itemStack = player.getItemInHand(usedHand);
            itemStack.shrink(1);
        }

        return InteractionResultHolder.success(player.getItemInHand(usedHand));
    }

    private void setTimeToNoon(ServerLevel serverLevel) {
        long currentTime = serverLevel.getDayTime();
        long currentDayTime = currentTime % 24000;

        long targetTime;
        targetTime = 6000;

        // Calculate ticks to add to reach target time
        long ticksToAdd;
        if (currentDayTime <= targetTime) {
            // Target time hasn't occurred yet today
            ticksToAdd = targetTime - currentDayTime;
        } else {
            // Target time has already passed, advance to target time tomorrow
            ticksToAdd = (24000 - currentDayTime) + targetTime;
        }

        serverLevel.setDayTime(currentTime + ticksToAdd);
    }
}
