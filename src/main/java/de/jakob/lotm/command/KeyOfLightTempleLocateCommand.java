package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import de.jakob.lotm.attachments.SefirotData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

public final class KeyOfLightTempleLocateCommand {
    private static final String TEMPLE_ID = "lotmcraft:key_of_light_temple";

    private KeyOfLightTempleLocateCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("locate")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("structure")
                .then(Commands.literal("key_of_light_temple")
                    .executes(context -> locate(context.getSource())))));
    }

    private static int locate(CommandSourceStack source) {
        BlockPos templePos = SefirotData.get(source.getServer())
            .getKeyOfLightShrinePos().orElse(null);
        if (templePos == null) {
            source.sendFailure(Component.literal("The Key of Light temple has not been generated yet."));
            return 0;
        }

        BlockPos sourcePos = BlockPos.containing(source.getPosition());
        int deltaX = templePos.getX() - sourcePos.getX();
        int deltaZ = templePos.getZ() - sourcePos.getZ();
        int distance = (int) Math.round(Math.sqrt((double) deltaX * deltaX + (double) deltaZ * deltaZ));
        source.sendSuccess(() -> Component.literal("Located " + TEMPLE_ID + " at ")
            .append(coordinatesLink(templePos))
            .append(Component.literal(" (" + distance + " blocks away, in the Overworld)")), false);
        return distance;
    }

    private static MutableComponent coordinatesLink(BlockPos pos) {
        String coordinates = "[" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]";
        String teleportCommand = "/execute in minecraft:overworld run tp @s "
            + pos.getX() + " " + pos.getY() + " " + pos.getZ();
        return Component.literal(coordinates).withStyle(style -> style
            .withColor(ChatFormatting.GREEN)
            .withUnderlined(true)
            .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, teleportCommand))
            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                Component.literal("Click to fill a teleport command").withStyle(ChatFormatting.GRAY))));
    }
}