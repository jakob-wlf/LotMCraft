package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import de.jakob.lotm.attachments.MysteriousTabletData;
import de.jakob.lotm.events.VillagerTradesEventHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.phys.Vec3;

public class AncientTraderCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("summon_ancient_trader")
                .requires(source -> source.hasPermission(2))
                .executes(context -> spawn(context.getSource(), context.getSource().getPosition()))
                .then(Commands.argument("pos", Vec3Argument.vec3())
                    .executes(context -> spawn(context.getSource(), Vec3Argument.getVec3(context, "pos"))))
        );
    }

    private static int spawn(CommandSourceStack source, Vec3 pos) {
        ServerLevel level = source.getLevel();
        WanderingTrader trader = EntityType.WANDERING_TRADER.create(level);
        if (trader == null) {
            source.sendFailure(Component.literal("Failed to summon Ancient Trader."));
            return 0;
        }

        trader.getPersistentData().putBoolean("AncientTrader", true);
        trader.setCustomName(Component.literal("Ancient Trader"));
        trader.setCustomNameVisible(true);
        trader.setDespawnDelay(Integer.MAX_VALUE);
        trader.setPersistenceRequired();

        boolean includeFragment = MysteriousTabletData.get(level.getServer())
                .canSpawnFragment(MysteriousTabletData.FragmentType.UPPER);
        trader.getOffers().clear();
        trader.getOffers().addAll(VillagerTradesEventHandler.buildAncientTraderOffers(level.random, includeFragment));

        trader.moveTo(pos.x, pos.y, pos.z, level.random.nextFloat() * 360.0f, 0.0f);
        level.addFreshEntity(trader);

        source.sendSuccess(() -> Component.literal("Summoned Ancient Trader."), true);
        return 1;
    }
}
