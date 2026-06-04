package de.jakob.lotm.abilities.sefirah;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.attachments.DeathImprintData;
import de.jakob.lotm.gui.ModMenuTypes;
import de.jakob.lotm.gui.custom.RiverAuthority.RiverAuthorityMenu;
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
import java.util.stream.Collectors;

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
            // Build imprint entry list from DeathImprintData
            DeathImprintData imprintData = DeathImprintData.get(player.getServer());
            Set<UUID> allImprinted = imprintData.getAllImprintedPlayers();
            List<RiverAuthorityMenu.ImprintEntry> entries = allImprinted.stream()
                    .map(uuid -> new RiverAuthorityMenu.ImprintEntry(
                            uuid,
                            imprintData.getSnapshotName(uuid),
                            imprintData.getSnapshotPathway(uuid),
                            imprintData.getSnapshotSequence(uuid),
                            imprintData.getImprintCount(uuid)
                    ))
                    .sorted(Comparator.comparingInt(RiverAuthorityMenu.ImprintEntry::imprintTier).reversed())
                    .collect(Collectors.toList());

            player.openMenu(new SimpleMenuProvider(
                    (id, inv, p) -> new RiverAuthorityMenu(id, inv, entries),
                    Component.literal("River Authority")
            ), buf -> RiverAuthorityMenu.writeEntries(buf, entries));
            return;
        }

        // Default: Sefirah Castle (and any future sefirot) → cross-path ability selection
        List<String> available    = new ArrayList<>(SefirotAuthorityManager.getAvailableAbilityIds(player));
        List<String> unlocked      = new ArrayList<>(SefirotAuthorityManager.getUnlockedAbilityIds(player));
        List<String> neighborPaths = new ArrayList<>(SefirotAuthorityManager.getNeighborPaths(player));
        player.openMenu(new SimpleMenuProvider(
                (id, inv, p) -> new SefirotAuthorityMenu(id, inv, available, unlocked, neighborPaths),
                Component.literal("Sefirot Authority")
        ), buf -> {
            buf.writeCollection(available,     FriendlyByteBuf::writeUtf);
            buf.writeCollection(unlocked,      FriendlyByteBuf::writeUtf);
            buf.writeCollection(neighborPaths, FriendlyByteBuf::writeUtf);
        });
    }
}
