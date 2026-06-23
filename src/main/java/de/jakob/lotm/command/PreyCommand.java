package de.jakob.lotm.command;

import com.mojang.brigadier.CommandDispatcher;
import de.jakob.lotm.gui.custom.Prey.PreyMenuProvider;
import de.jakob.lotm.gui.custom.Prey.PreyMenu;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.playerMap.HonorificName;
import de.jakob.lotm.util.playerMap.StoredData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PreyCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("prey")
                .requires(source -> source.getPlayer() != null)
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    
                    Map<UUID, HonorificName> honorificNames = new HashMap<>();
                    for (Map.Entry<UUID, StoredData> entry : BeyonderData.playerMap.entrySet()) {
                        HonorificName name = entry.getValue().honorificName();
                        if (name != null && !name.isEmpty()) {
                            honorificNames.put(entry.getKey(), name);
                        }
                    }

                    player.openMenu(new PreyMenuProvider(honorificNames), buf -> {
                        PreyMenu.writeHonorificNames(buf, honorificNames);
                    });
                    
                    return 1;
                })
        );
    }
}
