package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import de.jakob.lotm.beyonders.abilities.death.NationOfTheDeadAbility;
import de.jakob.lotm.beyonders.abilities.fool.GraftingAbility;
import de.jakob.lotm.beyonders.abilities.justiciar.ProhibitionAbility;
import de.jakob.lotm.entity.custom.ability_entities.BigMoonEntity;
import de.jakob.lotm.entity.custom.ability_entities.BigSunEntity;
import de.jakob.lotm.entity.custom.ability_entities.LocationGraftingEntity;
import de.jakob.lotm.entity.custom.ability_entities.death_pathway.DeathDivineKingdomEntity;
import de.jakob.lotm.entity.custom.ability_entities.door_pathway.BlackHoleEntity;
import de.jakob.lotm.entity.custom.ability_entities.mother_pathway.BloomingAreaEntity;
import de.jakob.lotm.entity.custom.ability_entities.mother_pathway.DesolateAreaEntity;
import de.jakob.lotm.entity.custom.ability_entities.sun_pathway.SunKingdomEntity;
import de.jakob.lotm.entity.custom.ability_entities.wheel_of_fortune_pathway.MisfortuneWordsEntity;
import de.jakob.lotm.item.ModItems;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;
import java.util.function.Predicate;

// Cancels the standing activation (domain/entity) of abilities whose effect
// persists beyond the initial use: Sun's Divine Kingdom, Words of Misfortune,
// Nation of the Dead, Death's Divine Kingdom, Blackhole, Mother's Blooming/Desolate
// Area, and the summoned Sun/Moon (from the stolen Sun/Moon items or Wrath of Nature).
// The "all" variant additionally lifts every active Justiciar Prohibition zone, removes
// every Sun/Moon stolen through Error's Conceptual Theft, clears every Mother area, and ends
// every Fool Grafting (grafted locations, damage, abilities and targets).
public class CancelActivationCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cancel_activation")
            .requires(source -> source.hasPermission(2)) // Requires OP level 2
            .executes(context -> {
                CommandSourceStack source = context.getSource();
                if (!(source.getEntity() instanceof LivingEntity livingEntity)) {
                    source.sendFailure(Component.literal("Only living entities can use this command!"));
                    return 0;
                }
                return cancelActivations(source, livingEntity);
            })
            .then(Commands.literal("all")
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    int total = 0;
                    for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
                        total += cancelActivations(source, player, false);
                    }

                    // Prohibition zones can be owned by non-player casters too, so clear every zone
                    total += ProhibitionAbility.ACTIVE_ZONES.size();
                    ProhibitionAbility.ACTIVE_ZONES.clear();

                    total += clearStolenSunMoon(source);
                    total += clearMotherAreas(source);
                    total += clearGrafting(source);

                    final int finalTotal = total;
                    source.sendSuccess(() -> Component.literal("Cancelled " + finalTotal + " active ability activation(s) across all players"), true);
                    return total;
                })
            )
            .then(Commands.argument("target", EntityArgument.entity())
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    var targetEntity = EntityArgument.getEntity(context, "target");

                    if (!(targetEntity instanceof LivingEntity livingEntity)) {
                        source.sendFailure(Component.literal("Target must be a living entity!"));
                        return 0;
                    }

                    return cancelActivations(source, livingEntity);
                })
            )
        );
    }

    // Removes every Sun/Moon stolen via Error's Conceptual Theft, whether held by a player or dropped in the world,
    // along with any Sun or Moon already summoned in the sky
    private static int clearStolenSunMoon(CommandSourceStack source) {
        Predicate<ItemStack> isSunOrMoon = stack -> stack.is(ModItems.SUN_ITEM.get()) || stack.is(ModItems.MOON_ITEM.get());
        int cleared = 0;

        for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
            cleared += player.getInventory().clearOrCountMatchingItems(isSunOrMoon, -1, player.inventoryMenu.getCraftSlots());
            player.containerMenu.broadcastChanges();
            player.inventoryMenu.slotsChanged(player.getInventory());
        }

        for (ServerLevel level : source.getServer().getAllLevels()) {
            for (var entity : level.getAllEntities()) {
                if (entity instanceof ItemEntity itemEntity && isSunOrMoon.test(itemEntity.getItem())) {
                    cleared += itemEntity.getItem().getCount();
                    itemEntity.discard();
                } else if ((entity instanceof BigSunEntity || entity instanceof BigMoonEntity) && !entity.isRemoved()) {
                    // Catches Suns/Moons whose summoner is offline, which the per-player pass misses
                    cleared++;
                    entity.discard();
                }
            }
        }

        return cleared;
    }

    // Removes every Mother Blooming/Desolate Area, including ones whose caster is offline or that lost
    // their owner on reload, which the per-player pass misses
    private static int clearMotherAreas(CommandSourceStack source) {
        int cleared = 0;
        for (ServerLevel level : source.getServer().getAllLevels()) {
            for (var entity : level.getAllEntities()) {
                if ((entity instanceof BloomingAreaEntity || entity instanceof DesolateAreaEntity) && !entity.isRemoved()) {
                    cleared++;
                    entity.discard();
                }
            }
        }
        return cleared;
    }

    // Removes every grafted location (which has no recorded owner) and ends every damage/ability/target graft
    private static int clearGrafting(CommandSourceStack source) {
        int cleared = GraftingAbility.endAll();
        for (ServerLevel level : source.getServer().getAllLevels()) {
            for (var entity : level.getAllEntities()) {
                if (entity instanceof LocationGraftingEntity && !entity.isRemoved()) {
                    cleared++;
                    entity.discard();
                }
            }
        }
        return cleared;
    }

    private static int cancelActivations(CommandSourceStack source, LivingEntity target) {
        return cancelActivations(source, target, true);
    }

    private static int cancelActivations(CommandSourceStack source, LivingEntity target, boolean announce) {
        UUID targetUUID = target.getUUID();
        int cancelled = 0;

        if (NationOfTheDeadAbility.cancelDomain(targetUUID)) {
            cancelled++;
        }

        for (ServerLevel level : source.getServer().getAllLevels()) {
            for (var entity : level.getAllEntities()) {
                boolean owned = switch (entity) {
                    case SunKingdomEntity e -> targetUUID.equals(e.getCasterUUID());
                    case DeathDivineKingdomEntity e -> targetUUID.equals(e.getOwnerUUID());
                    case BlackHoleEntity e -> targetUUID.equals(e.getOwnerUUID());
                    case MisfortuneWordsEntity e -> targetUUID.equals(e.getCasterUUID());
                    case BigSunEntity e -> e.getOwnerUUID().map(targetUUID::equals).orElse(false);
                    case BigMoonEntity e -> e.getOwnerUUID().map(targetUUID::equals).orElse(false);
                    case BloomingAreaEntity e -> targetUUID.equals(e.getOwnerUUID());
                    case DesolateAreaEntity e -> targetUUID.equals(e.getOwnerUUID());
                    default -> false;
                };

                if (owned) {
                    entity.discard();
                    cancelled++;
                }
            }
        }

        final int total = cancelled;
        if (announce) {
            String targetName = target.getDisplayName().getString();
            if (total > 0) {
                source.sendSuccess(() -> Component.literal("Cancelled " + total + " active ability activation(s) for " + targetName), true);
            } else {
                source.sendFailure(Component.literal(targetName + " has no cancellable active ability activations"));
            }
        }

        return total;
    }
}
