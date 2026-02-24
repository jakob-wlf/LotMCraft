package de.jakob.lotm.quest.impl;

import de.jakob.lotm.quest.Quest;
import de.jakob.lotm.quest.QuestManager;
import de.jakob.lotm.potions.BeyonderPotion;
import de.jakob.lotm.potions.PotionItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.*;

public class FindStructureQuest extends Quest {

    private static final List<String> STRUCTURE_IDS = List.of(
            "minecraft:stronghold",
            "minecraft:jungle_pyramid",
            "minecraft:woodland_mansion",
            "minecraft:trial_chambers",
            "minecraft:desert_pyramid",
            "minecraft:monument"
    );

    private final Map<UUID, String> targetStructureByPlayer = new HashMap<>();

    public FindStructureQuest(String id, int sequence) {
        super(id, sequence);
    }

    @Override
    public void startQuest(ServerPlayer player) {
        String target = STRUCTURE_IDS.get(new Random().nextInt(STRUCTURE_IDS.size()));
        targetStructureByPlayer.put(player.getUUID(), target);
        player.sendSystemMessage(Component.literal("Target structure selected: " + toDisplayName(target)));
    }

    @Override
    public void tick(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        String targetStructure = targetStructureByPlayer.computeIfAbsent(player.getUUID(), ignored ->
                STRUCTURE_IDS.get(new Random().nextInt(STRUCTURE_IDS.size())));

        BlockPos structurePos = findNearestStructure(level, player.blockPosition(), targetStructure);
        if (structurePos == null) {
            return;
        }

        if (structurePos.distSqr(player.blockPosition()) <= 160 * 160) {
            QuestManager.progressQuest(player, id, 1f);
        }
    }

    @Override
    public List<ItemStack> getRewards(ServerPlayer player) {
        List<ItemStack> rewards = new ArrayList<>();
        int seq = new Random().nextBoolean() ? 7 : 8;
        BeyonderPotion potion = PotionItemHandler.selectRandomPotionOfSequence(new Random(), seq);
        if (potion != null) {
            rewards.add(new ItemStack(potion));
        }
        return rewards;
    }

    @Override
    public float getDigestionReward() {
        return .3f;
    }

    @Override
    public MutableComponent getDescription() {
        return Component.translatable("lotm.quest.impl." + id + ".description");
    }

    private BlockPos findNearestStructure(ServerLevel level, BlockPos origin, String structureId) {
        ResourceLocation structureKey = ResourceLocation.tryParse(structureId);
        if (structureKey == null) {
            return null;
        }

        var structureRegistry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        Optional<Holder.Reference<Structure>> structureHolder = structureRegistry.getHolder(structureKey);
        if (structureHolder.isEmpty()) {
            return null;
        }

        var result = level.getChunkSource().getGenerator().findNearestMapStructure(
                level,
                HolderSet.direct(structureHolder.get()),
                origin,
                500,
                false
        );
        return result == null ? null : result.getFirst();
    }

    private String toDisplayName(String structureId) {
        int sep = structureId.indexOf(':');
        String clean = sep >= 0 ? structureId.substring(sep + 1) : structureId;
        return clean.replace('_', ' ');
    }
}