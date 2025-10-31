package de.jakob.lotm.abilities.fool.passives;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.PassiveAbilityHandler;
import de.jakob.lotm.abilities.PassiveAbilityItem;
import de.jakob.lotm.entity.custom.PaperDaggerProjectileEntity;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class PaperDaggersAbility extends PassiveAbilityItem {

    public PaperDaggersAbility(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "fool", 8
        ));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {

    }

    static Random random = new Random();

    @SubscribeEvent
    public static void onRightClickWithPaper(PlayerInteractEvent.RightClickItem event) {
        Level level = event.getLevel();
        if(level.isClientSide)
            return;

        if(event.getItemStack().getItem() != Items.PAPER)
            return;

        if(!((PaperDaggersAbility) PassiveAbilityHandler.PAPER_DAGGERS.get()).shouldApplyTo(event.getEntity()))
            return;

        level.playSound(null, event.getPos(), SoundEvents.SNOWBALL_THROW, event.getEntity().getSoundSource(), 1, 1);

        Player player = event.getEntity();

        int index = player.getInventory().selected;

        player.getInventory().removeItem(index, 1);
        Vec3 startPos = VectorUtil.getRelativePosition(player.getEyePosition().add(player.getLookAngle().normalize().multiply(1.5, 1.5, 1.5)), player.getLookAngle().normalize(), 0, random.nextDouble(1, 2.85f), random.nextDouble(-.1, .6));
        Vec3 direction = AbilityUtil.getTargetLocation(player, 50, 1.4f).subtract(startPos).normalize();

        PaperDaggerProjectileEntity dagger = new PaperDaggerProjectileEntity(level, player, 8.5);
        dagger.setPos(startPos.x, startPos.y, startPos.z);
        dagger.shoot(direction.x, direction.y, direction.z, 1.2f, 0);
        level.addFreshEntity(dagger);
    }
}
