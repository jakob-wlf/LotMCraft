package de.jakob.lotm.addons.rituals.mixins;

import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Collection;
import java.util.List;

@Mixin(ClientSuggestionProvider.class)
public class ClientSuggestionProviderMixin {

//    @Overwrite
//    public Collection<String> getOnlinePlayerNames() {
//        return List.of();
//    }
//
//    @Overwrite
//    public Collection<String> getSelectedEntities() {
//        return List.of();
//    }
}