package de.jakob.lotm.abilities.hanged;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Map;

public class ShepherdGrazingAbility extends SelectableAbility {
    private static final Map<String, Integer> REQUIREMENTS =
            Map.of(HangedPathwayConstants.PATHWAY_ID, HangedPathwayConstants.SEQUENCE_SHEPHERD);
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            LOTMCraft.MOD_ID, "textures/abilities/recording_ability.png");
    private static final float SPIRITUALITY_COST = 0.0f;

    public ShepherdGrazingAbility(String id) {
        super(id, 1.0f);
        canBeCopied = false;
        canBeReplicated = false;
        cannotBeStolen = true;
        canBeUsedByNPC = false;
        autoClear = false;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.shepherd_grazing.archive",
                "ability.lotmcraft.shepherd_grazing.use_soul",
                "ability.lotmcraft.shepherd_grazing.manifest"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }

        switch (selectedAbility) {
            case 0 -> ShepherdGrazingUtil.openSoulArchive(player);
            case 1 -> ShepherdGrazingUtil.openActiveSoulWheel(player);
            case 2 -> ShepherdGrazingUtil.manifestOrRetrieveActiveSoul(player);
            default -> AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.shepherd_grazing.archive")
                    .withColor(HangedPathwayConstants.pathwayColor()));
        }
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return REQUIREMENTS;
    }

    @Override
    protected float getSpiritualityCost() {
        return SPIRITUALITY_COST;
    }

    @Override
    public ResourceLocation getTextureLocation() {
        return TEXTURE;
    }
}
