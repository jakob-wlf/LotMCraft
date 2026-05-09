package de.jakob.lotm.abilities.hanged.passives;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.PassiveAbilityItem;
import de.jakob.lotm.abilities.PassiveAbilityHandler;
import de.jakob.lotm.abilities.hanged.HangedPathwayConstants;
import de.jakob.lotm.abilities.hanged.ShepherdGrazingUtil;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.Map;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class GrazingAbility extends PassiveAbilityItem {
    private static final Map<String, Integer> REQUIREMENTS =
            Map.of(HangedPathwayConstants.PATHWAY_ID, HangedPathwayConstants.SEQUENCE_SHEPHERD);

    public GrazingAbility(Item.Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return REQUIREMENTS;
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            ShepherdGrazingUtil.tickPlayer(player);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity target = event.getEntity();
        Entity sourceEntity = event.getSource().getEntity();
        if (!(sourceEntity instanceof ServerPlayer player)) {
            return;
        }
        if (!(PassiveAbilityHandler.GRAZING.get() instanceof GrazingAbility ability) || !ability.shouldApplyTo(player)) {
            return;
        }
        if (!BeyonderData.isBeyonder(target) && !(target instanceof Player)) {
            return;
        }
        if (target == player) {
            return;
        }

        ShepherdGrazingUtil.openGrazePrompt(player, target);
    }
}
