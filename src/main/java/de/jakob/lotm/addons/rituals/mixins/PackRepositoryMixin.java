package de.jakob.lotm.addons.rituals.mixins;

import net.minecraft.server.packs.repository.PackRepository;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Collection;
import java.util.List;

@Mixin(PackRepository.class)
public class PackRepositoryMixin {

    @ModifyVariable(
            method = "setSelected",
            at = @At("HEAD"),
            argsOnly = true
    )
    private Collection<String> lotmcraft$disableResourcePacks(Collection<String> ids) {
        return List.of("vanilla");
    }
}