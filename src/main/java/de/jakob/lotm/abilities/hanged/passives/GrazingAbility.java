package de.jakob.lotm.abilities.hanged.passives;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.PassiveAbilityItem;
import de.jakob.lotm.abilities.PassiveAbilityHandler;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.abilities.hanged.HangedPathwayConstants;
import de.jakob.lotm.abilities.hanged.ShepherdGrazingUtil;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.subordinates.SubordinateComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.Map;
import java.util.UUID;

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
        if (!(target.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        ServerPlayer player = resolveGrazer(serverLevel, target, event.getSource().getEntity(), event.getSource().getDirectEntity(), target.getKillCredit());
        if (player == null) {
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

    private static ServerPlayer resolveGrazer(ServerLevel level, LivingEntity target, Entity... candidates) {
        for (Entity candidate : candidates) {
            ServerPlayer player = resolvePlayer(level, candidate);
            if (player != null && player != target) {
                return player;
            }
        }
        return null;
    }

    private static ServerPlayer resolvePlayer(ServerLevel level, Entity entity) {
        if (entity == null) {
            return null;
        }
        if (entity instanceof ServerPlayer player) {
            return player;
        }
        if (entity instanceof Projectile projectile && projectile.getOwner() != entity) {
            ServerPlayer projectileOwner = resolvePlayer(level, projectile.getOwner());
            if (projectileOwner != null) {
                return projectileOwner;
            }
        }
        if (!(entity instanceof LivingEntity living)) {
            return null;
        }

        SubordinateComponent component = living.getData(ModAttachments.SUBORDINATE_COMPONENT.get());
        if (!component.isSubordinate() || component.getControllerUUID().isBlank()) {
            return null;
        }

        try {
            return level.getServer().getPlayerList().getPlayer(UUID.fromString(component.getControllerUUID()));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
