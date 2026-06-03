package de.jakob.lotm.abilities.sefirah;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.gui.ModMenuTypes;
import de.jakob.lotm.gui.custom.SefirotAuthority.SefirotAuthorityMenu;
import de.jakob.lotm.sefirah.SefirahHandler;
import de.jakob.lotm.sefirah.SefirotAuthorityManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        List<String> available     = new ArrayList<>(SefirotAuthorityManager.getAvailableAbilityIds(player));
        List<String> unlocked       = new ArrayList<>(SefirotAuthorityManager.getUnlockedAbilityIds(player));
        List<String> neighborPaths  = new ArrayList<>(SefirotAuthorityManager.getNeighborPaths(player));
        player.openMenu(new SimpleMenuProvider(
                (id, inv, p) -> new SefirotAuthorityMenu(id, inv, available, unlocked, neighborPaths),
                Component.literal("Sefirot Authority")
        ), buf -> {
            buf.writeCollection(available,    FriendlyByteBuf::writeUtf);
            buf.writeCollection(unlocked,     FriendlyByteBuf::writeUtf);
            buf.writeCollection(neighborPaths, FriendlyByteBuf::writeUtf);
        });
    }
}
