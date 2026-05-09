package de.jakob.lotm.abilities.fool;

import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.attachments.MemorisedEntities;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.network.packets.toClient.OpenShapeShiftingScreenPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.shapeShifting.ShapeShiftingUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShapeShiftingAbility extends SelectableAbility {
    private static final float DEFAULT_SPIRITUALITY_COST = 100.0f;
    private static final float HANGED_SPIRITUALITY_COST = 120.0f;

    public ShapeShiftingAbility(String id) {
        super(id, 5);
        canBeCopied = false;
        canBeUsedByNPC = false;
        canBeReplicated = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "fool", 6,
                "hanged_man", 6
        ));
    }

    @Override
    public float getSpiritualityCost() {
        return DEFAULT_SPIRITUALITY_COST;
    }

    @Override
    protected float getSpiritualityCostForEntity(LivingEntity entity) {
        return "hanged_man".equals(BeyonderData.getPathway(entity)) ? HANGED_SPIRITUALITY_COST : DEFAULT_SPIRITUALITY_COST;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.shape_shifting_ability.change_shape",
                "ability.lotmcraft.shape_shifting_ability.reset_shape"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if (!(entity instanceof ServerPlayer player)) return;
        switch (abilityIndex){
            case 0 -> changeShape(player);
            case 1 -> resetShape(player);
        }
    }

    public void changeShape(ServerPlayer player) {
        MemorisedEntities memorisedEntities = player.getData(ModAttachments.MEMORISED_ENTITIES);
        List<String> entityTypes = memorisedEntities.getMemorisedEntityTypes();
        PacketDistributor.sendToPlayer(
                player,
                new OpenShapeShiftingScreenPacket(entityTypes)
        );
    }

    public void resetShape(ServerPlayer player) {
        ShapeShiftingUtil.resetShape(player);
    }
}
