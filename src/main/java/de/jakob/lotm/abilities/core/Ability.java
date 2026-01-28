package de.jakob.lotm.abilities.core;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.AbilityCooldownComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.UseAbilityPacket;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public abstract class Ability {

    private final String id;
    protected final int cooldown;


    public boolean canBeUsedByNPC = true;
    public boolean canBeCopied = true;

    public boolean doesNotIncreaseDigestion = false;
    public boolean hasOptimalDistance = false;
    public float optimalDistance = 1f;

    protected final Random random = new Random();

    protected Ability(String id, float cooldown) {
        this.id = id;
        this.cooldown = Math.round(cooldown * 20);
    }

    public void useAbility(ServerLevel serverLevel, LivingEntity entity) {
        if(!canUse(entity)) {
            return;
        }

        AbilityUseEvent event = new AbilityUseEvent(entity, this);
        NeoForge.EVENT_BUS.post(event);

        if(event.isCanceled()) {
            return;
        }

        AbilityCooldownComponent component = entity.getData(ModAttachments.COOLDOWN_COMPONENT);
        component.setCooldown(id, cooldown);

        onAbilityUse(serverLevel, entity);
        PacketHandler.sendToAllPlayersInSameLevel(new UseAbilityPacket(getId(), entity.getId()), serverLevel);
    }

    public abstract void onAbilityUse(Level level, LivingEntity entity);

    public abstract Map<String, Integer> getRequirements();

    protected abstract float getSpiritualityCost();

    protected float multiplier(LivingEntity entity) {
        return (float) BeyonderData.getMultiplier(entity);
    }

    public boolean hasAbility(LivingEntity entity) {
        if(!BeyonderData.isBeyonder(entity)) return false;

        String pathway = BeyonderData.getPathway(entity);
        int sequence = BeyonderData.getSequence(entity);

        if(!getRequirements().containsKey(pathway)) return false;
        if(getRequirements().get(pathway) < sequence) return false;

        return true;
    }

    public boolean canUse(LivingEntity entity) {
        if(!hasAbility(entity)) return false;

        AbilityCooldownComponent component = entity.getData(ModAttachments.COOLDOWN_COMPONENT);
        if(component.isOnCooldown(id)) return false;

        if(consumeSpirituality(entity) && BeyonderData.getSpirituality(entity) <= getSpiritualityCost()) return false;

        return true;
    }

    private boolean consumeSpirituality(LivingEntity entity) {
        return entity.hasInfiniteMaterials();
    }

    public int lowestSequenceUsable() {
        return getRequirements().values().stream()
                .max(Integer::compareTo)
                .orElse(-1);
    }

    private float getDigestionProgressForUse(LivingEntity entity) {
        int sequence = BeyonderData.getSequence(entity);

        if (!getRequirements().containsKey(BeyonderData.getPathway(entity))) {
            return 0f;
        }

        int requiredSequence = getRequirements().get(BeyonderData.getPathway(entity));

        // If user's sequence is numerically higher (weaker) → cannot digest at all
        if (sequence > requiredSequence) {
            return 0f;
        }

        // If same sequence → requires ~100 uses
        if (sequence == requiredSequence) {
            return 1f / 100f;
        }

        // Ability sequence is stronger (numerically lower) → digestion is slower
        int diff = requiredSequence - sequence; // always >= 1 here
        return 1f / (100f * (diff + 1));
    }

    public ResourceLocation getTextureLocation() {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/abilities/" + id + ".png");
    }

    public String getId() {
        return id;
    }
}
