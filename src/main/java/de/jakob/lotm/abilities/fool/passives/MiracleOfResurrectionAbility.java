package de.jakob.lotm.abilities.fool.passives;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.PassiveAbilityItem;
import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.abilities.fool.HistoricalVoidHidingAbility;
import de.jakob.lotm.abilities.justiciar.LawAbility;
import de.jakob.lotm.attachments.DisabledAbilitiesComponent;
import de.jakob.lotm.attachments.MiracleOfResurrectionComponent;
import de.jakob.lotm.attachments.ModAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class MiracleOfResurrectionAbility extends PassiveAbilityItem {

    public MiracleOfResurrectionAbility(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "fool", 2
        ));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {

    }

    static Random random = new Random();

    @SubscribeEvent
    public static void beforePlayerDies(LivingIncomingDamageEvent event) {
        Entity entity = event.getEntity();
        Level level = entity.level();

        if(level.isClientSide)
            return;

        if (entity instanceof ServerPlayer serverPlayer) {

            if (!(event.getAmount() >= serverPlayer.getHealth())) return;

            if (LawAbility.SOLACE_KILLED.contains(entity.getUUID())) return;
            MiracleOfResurrectionComponent data = serverPlayer.getData(ModAttachments.MIRACLE_OF_RESURRECTION);
            if (data.getResurrectionAttempts() > 0) {
                data.setResurrectionAttempts(data.getResurrectionAttempts() - 1);
                // cancel the death

                event.setCanceled(true);

                // drop the inventory
                serverPlayer.getInventory().dropAll();

                // teleport the player to a random place in 50x50 aria and hide him in history
                if (level instanceof ServerLevel serverLevel) {
                    double x = serverPlayer.getX() + (random.nextDouble() * 100 - 50);
                    double z = serverPlayer.getZ() + (random.nextDouble() * 100 - 50);

                    ServerLevel overworld = serverPlayer.getServer().getLevel(Level.OVERWORLD);

                    if (overworld != null) {
                        BlockPos targetPos = overworld.getHeightmapPos(
                                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                                BlockPos.containing(x, 0, z)
                        );

                        serverPlayer.teleportTo(overworld, targetPos.getX() + 0.5, targetPos.getY() + 1.0, targetPos.getZ() + 0.5, serverPlayer.getYRot(), serverPlayer.getXRot());
                    } else {
                        serverPlayer.teleportTo(serverLevel, 0.5, 0.0, 0.5, serverPlayer.getYRot(), serverPlayer.getXRot());
                    }

                    // reset fall distance to prevent death on arrival
                    serverPlayer.fallDistance = 0;

                    ToggleAbility.setActiveAbilities(serverPlayer, new HashSet<>());

                    // put the player in historical hiding state
                    HistoricalVoidHidingAbility ability = new HistoricalVoidHidingAbility("historical_void_hiding_ability");
                    ability.useAbility(serverLevel, serverPlayer);
                }

                // reset all of his effects and abilities and state
                serverPlayer.setHealth(serverPlayer.getMaxHealth());

                serverPlayer.removeAllEffects();

                // disable all abilities for 10 mins
                DisabledAbilitiesComponent disabledComponent = serverPlayer.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT);
                disabledComponent.disableAbilityUsageForTime("miracle_of_resurrection_" + entity.getUUID(), 10 * 60 * 20, serverPlayer);
            }
        }


    }
}
