package de.jakob.lotm.beyonders.boss;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Locale;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public final class BossCommands {
    @FunctionalInterface
    private interface BossUpdate {
        BossDefinition apply(Mob mob, BossDefinition definition) throws CommandSyntaxException;
    }

    private BossCommands() {
    }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        CommandBuildContext buildContext = event.getBuildContext();
        dispatcher.register(Commands.literal("boss")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("add")
                .then(Commands.argument("targets", EntityArgument.entities()).executes(context -> add(context))))
            .then(Commands.literal("remove")
                .then(Commands.argument("targets", EntityArgument.entities()).executes(context -> remove(context))))
            .then(Commands.literal("clearoverride")
                .then(Commands.argument("targets", EntityArgument.entities()).executes(context -> clearOverride(context))))
            .then(Commands.literal("hostility")
                .then(Commands.argument("targets", EntityArgument.entities())
                    .then(Commands.argument("mode", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(new String[]{"always", "retaliate"}, builder))
                        .executes(context -> update(context, (mob, definition) -> {
                            String mode = StringArgumentType.getString(context, "mode").toLowerCase(Locale.ROOT);
                            BossDefinition.Hostility hostility = switch (mode) {
                                case "always" -> BossDefinition.Hostility.ALWAYS;
                                case "retaliate" -> BossDefinition.Hostility.RETALIATE;
                                default -> throw new SimpleCommandExceptionType(Component.literal("Use always or retaliate")).create();
                            };
                            return definition.toBuilder().hostility(hostility).build();
                        }))))
            .then(Commands.literal("idle")
                .then(Commands.argument("targets", EntityArgument.entities())
                    .then(Commands.argument("mode", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(new String[]{"stay", "wander"}, builder))
                        .then(Commands.argument("radius", DoubleArgumentType.doubleArg(0.0, 512.0))
                            .executes(context -> update(context, (mob, definition) -> {
                                String mode = StringArgumentType.getString(context, "mode").toLowerCase(Locale.ROOT);
                                BossDefinition.IdleBehavior behavior = switch (mode) {
                                    case "stay" -> BossDefinition.IdleBehavior.STAY;
                                    case "wander" -> BossDefinition.IdleBehavior.WANDER;
                                    default -> throw new SimpleCommandExceptionType(Component.literal("Use stay or wander")).create();
                                };
                                return definition.toBuilder().idleBehavior(behavior, DoubleArgumentType.getDouble(context, "radius")).build();
                            }))))))
            .then(Commands.literal("bar")
                .then(Commands.argument("targets", EntityArgument.entities())
                    .then(Commands.argument("color", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                            java.util.Arrays.stream(BossEvent.BossBarColor.values()).map(value -> value.name().toLowerCase(Locale.ROOT)), builder))
                        .then(Commands.argument("title", ComponentArgument.textComponent(buildContext))
                            .executes(context -> update(context, (mob, definition) -> {
                                BossEvent.BossBarColor color;
                                try {
                                    color = BossEvent.BossBarColor.valueOf(StringArgumentType.getString(context, "color").toUpperCase(Locale.ROOT));
                                } catch (IllegalArgumentException exception) {
                                    throw new SimpleCommandExceptionType(Component.literal("Unknown boss bar color")).create();
                                }
                                Component title = ComponentArgument.getComponent(context, "title");
                                return definition.toBuilder().barColor(color).barTitle(title).build();
                            }))))))
            .then(Commands.literal("alert")
                .then(Commands.argument("targets", EntityArgument.entities())
                    .then(Commands.argument("enabled", BoolArgumentType.bool())
                        .executes(context -> update(context, (mob, definition) -> {
                            boolean enabled = BoolArgumentType.getBool(context, "enabled");
                            if (!enabled) {
                                return definition.toBuilder().noAlert().build();
                            }
                            Component title = definition.alertTitle() == null
                                ? Component.literal(mob.getName().getString() + " has noticed you!")
                                : definition.alertTitle();
                            return definition.toBuilder().alert(title, definition.alertSubtitle(), definition.alertFadeIn(), definition.alertStay(), definition.alertFadeOut()).build();
                        })))))
            .then(Commands.literal("alert-title")
                .then(Commands.argument("targets", EntityArgument.entities())
                    .then(Commands.argument("title", ComponentArgument.textComponent(buildContext))
                        .executes(context -> update(context, (mob, definition) -> definition.toBuilder()
                            .alert(ComponentArgument.getComponent(context, "title"), definition.alertSubtitle(), definition.alertFadeIn(), definition.alertStay(), definition.alertFadeOut())
                            .build())))))
            .then(Commands.literal("alert-subtitle")
                .then(Commands.argument("targets", EntityArgument.entities())
                    .then(Commands.argument("subtitle", ComponentArgument.textComponent(buildContext))
                        .executes(context -> update(context, (mob, definition) -> definition.toBuilder()
                            .alert(definition.alertTitle(), ComponentArgument.getComponent(context, "subtitle"), definition.alertFadeIn(), definition.alertStay(), definition.alertFadeOut())
                            .build())))))
            .then(Commands.literal("alert-times")
                .then(Commands.argument("targets", EntityArgument.entities())
                    .then(Commands.argument("fadeIn", IntegerArgumentType.integer(0, 12000))
                        .then(Commands.argument("stay", IntegerArgumentType.integer(0, 12000))
                            .then(Commands.argument("fadeOut", IntegerArgumentType.integer(0, 12000))
                                .executes(context -> update(context, (mob, definition) -> definition.toBuilder()
                                    .alertTiming(
                                        IntegerArgumentType.getInteger(context, "fadeIn"),
                                        IntegerArgumentType.getInteger(context, "stay"),
                                        IntegerArgumentType.getInteger(context, "fadeOut"))
                                    .build())))))))
            .then(Commands.literal("status")
                .then(Commands.argument("targets", EntityArgument.entities()).executes(context -> status(context))))));
    }

    private static int add(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgument.getEntities(context, "targets");
        int count = 0;
        for (Entity entity : targets) {
            if (!(entity instanceof Mob mob)) {
                continue;
            }
            BossDefinition definition = BossRegistry.getDefinition(mob)
                .or(() -> BossRegistry.getRegisteredDefinition(mob))
                .orElseGet(() -> BossDefinition.defaults(mob.getDisplayName()));
            BossRegistry.setInstanceDefinition(mob, definition);
            BossRuntime.refresh(mob);
            count++;
        }
        final int result = count;
        context.getSource().sendSuccess(() -> Component.literal("Designated " + result + " mob(s) as boss."), true);
        return result;
    }

    private static int remove(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgument.getEntities(context, "targets");
        int count = 0;
        for (Entity entity : targets) {
            if (entity instanceof Mob mob) {
                BossRegistry.disableInstance(mob);
                BossRuntime.refresh(mob);
                count++;
            }
        }
        final int result = count;
        context.getSource().sendSuccess(() -> Component.literal("Removed boss behavior from " + result + " mob(s)."), true);
        return result;
    }

    private static int clearOverride(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgument.getEntities(context, "targets");
        int count = 0;
        for (Entity entity : targets) {
            if (entity instanceof Mob mob) {
                BossRegistry.clearInstanceOverride(mob);
                BossRuntime.refresh(mob);
                count++;
            }
        }
        final int result = count;
        context.getSource().sendSuccess(() -> Component.literal("Cleared boss overrides for " + result + " mob(s)."), true);
        return result;
    }

    private static int update(CommandContext<CommandSourceStack> context, BossUpdate updater) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgument.getEntities(context, "targets");
        int count = 0;
        for (Entity entity : targets) {
            if (!(entity instanceof Mob mob)) {
                continue;
            }
            BossDefinition current = BossRegistry.getDefinition(mob)
                .orElseGet(() -> BossDefinition.defaults(mob.getDisplayName()));
            BossDefinition updated = updater.apply(mob, current);
            BossRegistry.setInstanceDefinition(mob, updated);
            BossRuntime.refresh(mob);
            count++;
        }
        final int result = count;
        context.getSource().sendSuccess(() -> Component.literal("Updated " + result + " boss mob(s)."), true);
        return result;
    }

    private static int status(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgument.getEntities(context, "targets");
        int count = 0;
        for (Entity entity : targets) {
            if (!(entity instanceof Mob mob)) {
                continue;
            }
            BossDefinition definition = BossRegistry.getDefinition(mob).orElse(null);
            if (definition == null) {
                context.getSource().sendSuccess(() -> Component.literal(mob.getName().getString() + ": not a boss"), false);
            } else {
                String alert = definition.hasAlert() ? definition.alertTitle().getString() : "disabled";
                String status = mob.getName().getString() + ": hostility=" + definition.hostility().name().toLowerCase(Locale.ROOT)
                    + ", idle=" + definition.idleBehavior().name().toLowerCase(Locale.ROOT)
                    + ", radius=" + definition.wanderRadius()
                    + ", bar=" + definition.barTitle().getString()
                    + ", color=" + definition.barColor().name().toLowerCase(Locale.ROOT)
                    + ", alert=" + alert;
                context.getSource().sendSuccess(() -> Component.literal(status), false);
                count++;
            }
        }
        return count;
    }
}
