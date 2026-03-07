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
            "minecraft:mansion",
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
        String target = selectNewTargetStructure(player);
        player.sendSystemMessage(Component.literal("Target structure selected: " + toDisplayName(target)));
    }

    @Override
    public void tick(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        String targetStructure = ensureTargetStructure(player);

        if (isPlayerInsideStructure(level, player.blockPosition(), targetStructure)) {
            QuestManager.progressQuest(player, id, 1f);
        }
    }

    @Override
    public List<ItemStack> getRewards(ServerPlayer player) {
        List<ItemStack> rewards = new ArrayList<>();

        Random random = new Random();

        int seq = random.nextBoolean() ? 7 : 8;
        BeyonderPotion potion = PotionItemHandler.selectRandomPotionOfSequence(random, seq);
        if (potion != null) {
            rewards.add(new ItemStack(potion));
        }
        return rewards;
    }

    @Override
    public float getDigestionReward() {
        return .2f;
    }

    @Override
    public MutableComponent getDescription(ServerPlayer player) {
        String targetStructure = targetStructureByPlayer.get(player.getUUID());
        if (targetStructure == null) {
            return Component.translatable("lotm.quest.impl." + id + ".description");
        }

        return Component.translatable("lotm.quest.impl." + id + ".description")
                .append(" Target: " + toDisplayName(targetStructure));
    }


    private String ensureTargetStructure(ServerPlayer player) {
        return targetStructureByPlayer.computeIfAbsent(player.getUUID(), ignored -> randomStructureId());
    }

    private String selectNewTargetStructure(ServerPlayer player) {
        String target = randomStructureId();
        targetStructureByPlayer.put(player.getUUID(), target);
        return target;
    }

    private String randomStructureId() {
        return STRUCTURE_IDS.get(java.util.concurrent.ThreadLocalRandom.current().nextInt(STRUCTURE_IDS.size()));
    }

    private boolean isPlayerInsideStructure(ServerLevel level, BlockPos pos, String structureId) {
        ResourceLocation structureKey = ResourceLocation.tryParse(structureId);
        if (structureKey == null) return false;

        var structureRegistry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        Optional<Holder.Reference<Structure>> holder = structureRegistry.getHolder(structureKey);
        if (holder.isEmpty()) return false;

        return level.structureManager()
                .getStructureWithPieceAt(pos, holder.get().value())
                .isValid();
    }
    private double horizontalDistance(BlockPos a, BlockPos b) {
        double dx = a.getX() - b.getX();
        double dz = a.getZ() - b.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    private String toDisplayName(String structureId) {
        int sep = structureId.indexOf(':');
        String clean = sep >= 0 ? structureId.substring(sep + 1) : structureId;
        return clean.replace('_', ' ');
    }
}