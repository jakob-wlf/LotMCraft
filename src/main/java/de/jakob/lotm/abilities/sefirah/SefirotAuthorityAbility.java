package de.jakob.lotm.abilities.sefirah;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.gui.custom.RiverSefirotAuthority.RiverSefirotAuthorityMenu;
import de.jakob.lotm.gui.custom.SefirotAuthority.SefirotAuthorityMenu;
import de.jakob.lotm.sefirah.SefirahHandler;
import de.jakob.lotm.sefirah.SefirotAuthorityManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.*;

public class SefirotAuthorityAbility extends Ability {

    public SefirotAuthorityAbility(String id) {
        super(id, 0f);
        this.canBeCopied = false;
        this.canBeShared = false;
        this.canBeReplicated = false;
        this.canBeUsedInArtifact = false;
        this.cannotBeStolen = true;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return Map.of();
    }

    @Override
    protected float getSpiritualityCost() {
        return 0f;
    }

    /** Granted to anyone who currently holds a sefirot — not pathway/sequence based. */
    @Override
    public boolean hasAbility(LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player)) return false;
        return SefirahHandler.hasSefirot(player);
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (level.isClientSide()) return;
        if (!(entity instanceof ServerPlayer player)) return;

        String sefirot = SefirahHandler.getClaimedSefirot(player);

        if (sefirot.equals("river_of_eternal_darkness")) {
            // Open the river-themed ability selection screen (Death/Darkness/Twilight Giant
            // cross-path abilities). That screen has a "Death Imprints" tab to navigate to
            // the imprint list via RequestRiverImprintScreenPacket.
            List<String> available    = new ArrayList<>(SefirotAuthorityManager.getAvailableAbilityIds(player));
            List<String> unlocked     = new ArrayList<>(SefirotAuthorityManager.getUnlockedAbilityIds(player));
            List<String> neighborPaths = new ArrayList<>(SefirotAuthorityManager.getNeighborPaths(player));
            player.openMenu(new SimpleMenuProvider(
                    (id, inv, p) -> new RiverSefirotAuthorityMenu(id, inv, available, unlocked, neighborPaths),
                    Component.literal("River Authority")
            ), buf -> {
                buf.writeCollection(available,      FriendlyByteBuf::writeUtf);
                buf.writeCollection(unlocked,       FriendlyByteBuf::writeUtf);
                buf.writeCollection(neighborPaths,  FriendlyByteBuf::writeUtf);
            });
            return;
        }

        // Default: Sefirah Castle (and any future sefirot) → cross-path ability selection
        List<String> available    = new ArrayList<>(SefirotAuthorityManager.getAvailableAbilityIds(player));
        List<String> unlocked      = new ArrayList<>(SefirotAuthorityManager.getUnlockedAbilityIds(player));
        List<String> neighborPaths = new ArrayList<>(SefirotAuthorityManager.getNeighborPaths(player));
        String sefirotName = sefirot;
        player.openMenu(new SimpleMenuProvider(
                (id, inv, p) -> new SefirotAuthorityMenu(id, inv, available, unlocked, neighborPaths, sefirotName),
                Component.literal("Sefirot Authority")
        ), buf -> {
            buf.writeCollection(available,     FriendlyByteBuf::writeUtf);
            buf.writeCollection(unlocked,      FriendlyByteBuf::writeUtf);
            buf.writeCollection(neighborPaths, FriendlyByteBuf::writeUtf);
            buf.writeUtf(sefirotName);
        });
    }
}
