package de.jakob.lotm.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.jakob.lotm.attachments.SequenceGuardComponent;
import de.jakob.lotm.attachments.ModAttachments;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class SequenceGuardCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("sequence_guard")

                // victim side - let target player kill you without facing consequences
                .then(Commands.literal("consent")
                        .then(Commands.literal("add")
                                .then(Commands.argument("player", GameProfileArgument.gameProfile())
                                        .executes(context -> editConsent(context, true))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("player", GameProfileArgument.gameProfile())
                                        .executes(context -> editConsent(context, false)))))

                // attacker side - be able to target and attack targeted player regardless of guard - facing punishments after killing the target
                .then(Commands.literal("attack")
                        .then(Commands.literal("add")
                                .then(Commands.argument("player", GameProfileArgument.gameProfile())
                                        .executes(context -> editAttack(context, true))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("player", GameProfileArgument.gameProfile())
                                        .executes(context -> editAttack(context, false)))))

                // turn your own guard on/off
                .then(Commands.literal("toggle")
                        .executes(SequenceGuardCommand::toggle))

                // show your guard state and exception lists
                .then(Commands.literal("status")
                        .executes(SequenceGuardCommand::status))

                // admin only - remove the punishment and turn the guard back on (online players only)
                .then(Commands.literal("reset")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.literal("all")
                                        .executes(SequenceGuardCommand::all))
                                .then(Commands.literal("kills")
                                        .executes(SequenceGuardCommand::kills))
                        ))
        );
    }

    private static int editConsent(CommandContext<CommandSourceStack> context, boolean add) {
        try {
            ServerPlayer self = context.getSource().getPlayerOrException();
            GameProfile other = GameProfileArgument.getGameProfiles(context, "player").iterator().next();

            if (self.getUUID().equals(other.getId())) {
                context.getSource().sendFailure(Component.literal("§eYou can't add yourself!"));
                return 0;
            }

            var data = self.getData(ModAttachments.SEQUENCE_GUARD);
            boolean alreadyConsented = data.hasConsentAttackers(other.getId());
            String name = other.getName();

            if (add && alreadyConsented) {
                context.getSource().sendFailure(Component.literal("§e" + name + " is already allowed to kill you."));
                return 0;
            }

            if (!add && !alreadyConsented) {
                context.getSource().sendFailure(Component.literal("§e" + name + " was not in your consent list."));
                return 0;
            }

            if (add) {
                data.addConsentAttackers(other.getId());
            } else {
                data.removeConsentAttackers(other.getId());
            }

            context.getSource().sendSuccess(() -> Component.literal(add
                    ? "§e" + name + " may now kill you without suffering any punishment."
                    : "§e" + name + " can no longer kill you without punishment."), false);
            return 1;
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(Component.literal("§eFailed to edit consent list: " + e.getMessage()));
            return 0;
        }
    }

    private static int editAttack(CommandContext<CommandSourceStack> context, boolean add) {
        try {
            ServerPlayer self = context.getSource().getPlayerOrException();
            GameProfile other = GameProfileArgument.getGameProfiles(context, "player").iterator().next();

            if (self.getUUID().equals(other.getId())) {
                context.getSource().sendFailure(Component.literal("§eYou can't add yourself!"));
                return 0;
            }

            var data = self.getData(ModAttachments.SEQUENCE_GUARD);
            boolean contains = data.getAllowedToKill().contains(other.getId());
            String name = other.getName();

            if (add && contains) {
                context.getSource().sendFailure(Component.literal(add
                        ? "§eYou can already attack " + name + "."
                        : "§e" + name + " was not in your attack list."));
                return 0;
            }

            if (add) {
                data.addTargetUuid(other.getId());
            } else {
                data.removeTargetUuid(other.getId());
            }

            context.getSource().sendSuccess(() -> Component.literal(add
                    ? "§eYou may now attack " + name + ". If you kill them without being provoked "
                    + "or having their consent, your Sequence Guard will be disabled for 24 hours."
                    : "§eYou may no longer attack " + name + "."), false);
            return 1;
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(Component.literal("§eFailed to edit attack list: " + e.getMessage()));
            return 0;
        }
    }

    private static int toggle(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer self = context.getSource().getPlayerOrException();
            var data = self.getData(ModAttachments.SEQUENCE_GUARD);

            if (data.isSequenceGuard() && !data.isGuardActive()) {
                context.getSource().sendFailure(Component.literal("§eYou can't toggle your Sequence Guard while you are being punished."));
                return 0;
            }

            data.setSequenceGuard(!data.isSequenceGuard());

            context.getSource().sendSuccess(() -> Component.literal(data.isSequenceGuard()
                    ? "§eSequence Guard enabled."
                    : "§eSequence Guard disabled. Anyone can now kill you."), false);
            return 1;
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(Component.literal("§eOnly players can use this command!"));
            return 0;
        }
    }

    private static int status(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();
            ServerPlayer self = source.getPlayerOrException();
            SequenceGuardComponent data = self.getData(ModAttachments.SEQUENCE_GUARD);

            String state;
            if (data.isGuardActive()) {
                state = "ACTIVE";
            } else if (data.isSequenceGuard()) {
                long minutes = Math.max(1, (data.getGuardDisabledUntil() - System.currentTimeMillis()) / 60000);
                state = "DISABLED (punishment, " + (minutes / 60) + "h " + (minutes % 60) + "m left)";
            } else {
                state = "OFF";
            }

            source.sendSuccess(() -> Component.literal("§eSequence Guard: " + state + "\n"), false);
            source.sendSuccess(() -> Component.literal("§eAttack list (" + data.getAllowedToKill().size() + ") : "
                    + namesOf(self, data.getAllowedToKill()) + "\n"), false);
            source.sendSuccess(() -> Component.literal("§eConsented attackers (" + data.getConsentedAttackers().size() + ") : "
                    + namesOf(self, data.getConsentedAttackers()) + "\n"), false);
            source.sendSuccess(() -> Component.literal("§eProvoked by, last 60 min (" + data.getProvokers().size() + ") : "
                    + namesOf(self, data.getProvokers()) + "\n"), false);
            source.sendSuccess(() -> Component.literal("§eYou provoked, last 60 min (" + data.getProvoked().size() + ") : "
                    + namesOf(self, data.getProvoked()) + "\n"), false);
            source.sendSuccess(() -> Component.literal("§eTotal Kills : " + data.getTotalKills()), false);

            return 1;
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(Component.literal("§eOnly players can use this command!"));
            return 0;
        }
    }

    private static int all(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(context, "player");
            var data = target.getData(ModAttachments.SEQUENCE_GUARD);
            data.clearPunishment();
            data.clearProvokers();
            data.setSequenceGuard(true);

            context.getSource().sendSuccess(() -> Component.literal("Reset Sequence Guard for " + target.getGameProfile().getName()), true);
            return 1;
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(Component.literal("Failed to reset: " + e.getMessage()));
            return 0;
        }
    }

    private static int kills(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(context, "player");
            var data = target.getData(ModAttachments.SEQUENCE_GUARD);
            data.resetTotalKills();

            context.getSource().sendSuccess(() -> Component.literal("Reset kills for " + target.getGameProfile().getName()), true);
            return 1;
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(Component.literal("Failed to reset: " + e.getMessage()));
            return 0;
        }
    }

    private static String namesOf(ServerPlayer viewer, Set<UUID> uuids) {
        if (uuids.isEmpty()) return "";

        List<String> names = new ArrayList<>();
        for (UUID uuid : uuids) {
            names.add(viewer.server.getProfileCache().get(uuid).map(GameProfile::getName).orElse("unknown"));
        }
        return String.join(", ", names);
    }
}