package de.jakob.lotm.abilities.fool.passives;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.PassiveAbilityHandler;
import de.jakob.lotm.abilities.PassiveAbilityItem;
import de.jakob.lotm.entity.custom.projectiles.PaperDaggerProjectileEntity;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class AcrobaticsAbility extends PassiveAbilityItem {

    public AcrobaticsAbility(Properties properties) {
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

    @SubscribeEvent
    public static void onJump(LivingEvent.LivingJumpEvent event) {
        if(!(event.getEntity() instanceof Player player) || !((AcrobaticsAbility) PassiveAbilityHandler.ACROBATICS.get()).shouldApplyTo(player))
            return;

        if(!player.isShiftKeyDown())
            return;

        player.setDeltaMovement(player.getDeltaMovement().scale(2));
        player.hurtMarked = true;
    }
}
