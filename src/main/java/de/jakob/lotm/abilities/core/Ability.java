package de.jakob.lotm.abilities.core;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.AbilityCooldownComponent;
import de.jakob.lotm.attachments.DisabledAbilitiesComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.gamerule.ModGameRules;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.UseAbilityPacket;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Random;

public abstract class Ability {

    // Identity
    private final String id;
    protected final int cooldown;

    // Interaction behaviour
    protected double interactionRadius = 1.4;
    protected int interactionCacheTicks = 10;
    protected final String[] interactionFlags;
    protected boolean postsUsedAbilityEventManually = false;

    // Optimal distance
    public boolean hasOptimalDistance = true;
    public float optimalDistance = 5f;

    // Permissions
    public boolean canBeUsedByNPC = true;
    public boolean canBeCopied = true;
    public boolean canAlwaysBeUsed = false;

    // Misc
    public boolean doesNotIncreaseDigestion = false;
    protected boolean shouldBeHidden = false;

    // Utility
    protected final Random random = new Random();

    public Ability(String id, float cooldown, String... interactionFlags) {
        this.id = id;
        this.cooldown = Math.round(cooldown * 20);
        this.interactionFlags = interactionFlags;
    }

    public void useAbility(ServerLevel serverLevel, LivingEntity entity, boolean consumeSpirituality, boolean hasToHaveAbility, boolean hasToMeetRequirements) {
        if(LOTMCraft.abilityHandler.isDisabled(this)) {
            return;
        }

        if(!canUse(entity, hasToHaveAbility, consumeSpirituality) && hasToMeetRequirements) {
            return;
        }

        // Fire event
        AbilityUseEvent event = new AbilityUseEvent(entity, this);
        NeoForge.EVENT_BUS.post(event);

        if(event.isCanceled()) {
            return;
        }

        LivingEntity newUser = event.getEntity();
        if(!canUse(newUser, false, consumeSpirituality)) {
            return;
        }

        // Consume spirituality
        if(shouldConsumeSpirituality(newUser) && consumeSpirituality) {
            BeyonderData.reduceSpirituality(newUser, getSpiritualityCost());
        }

        // Digest potion
        if(!doesNotIncreaseDigestion && newUser instanceof Player player) {
            BeyonderData.digest(player, getDigestionProgressForUse(newUser), true);
        }

        // Handle Cooldown
        AbilityCooldownComponent component = newUser.getData(ModAttachments.COOLDOWN_COMPONENT);
        component.setCooldown(id, cooldown);

        // Use ability client and server sided
        onAbilityUse(serverLevel, newUser);
        PacketHandler.sendToAllPlayersInSameLevel(new UseAbilityPacket(getId(), newUser.getId()), serverLevel);

        // Track ability use for Recording/Replicating detection
        AbilityUseTracker.trackUse(newUser, this, newUser.position(), serverLevel);

        if(!postsUsedAbilityEventManually) NeoForge.EVENT_BUS.post(new AbilityUsedEvent(serverLevel, newUser.position(), newUser, this, interactionFlags, interactionRadius, interactionCacheTicks));
    }

    public void useAbility(ServerLevel serverLevel, LivingEntity entity) {
        useAbility(serverLevel, entity, true, true, true);
    }

    public abstract void onAbilityUse(Level level, LivingEntity entity);

    public abstract Map<String, Integer> getRequirements();

    protected abstract float getSpiritualityCost();

    protected float multiplier(LivingEntity entity) {
        return (float) BeyonderData.getMultiplier(entity);
    }

    public void onHold(Level level, LivingEntity entity) {

    }

    public boolean shouldUseAbility(LivingEntity entity) {
        return true;
    }

    public boolean hasAbility(LivingEntity entity) {
        if(!BeyonderData.isBeyonder(entity)) return false;

        String pathway = BeyonderData.getPathway(entity);
        int sequence = BeyonderData.getSequence(entity);

        // Creative + OP players can use any ability up to their sequence
        if(entity instanceof Player player && player.isCreative() && player.hasPermissions(2)) {
            return getRequirements().values().stream().anyMatch(reqSeq -> reqSeq >= sequence);
        }

        if(!getRequirements().containsKey(pathway)) return false;
        if(getRequirements().get(pathway) < sequence) return false;

        return true;
    }

    public boolean canUse(LivingEntity entity) {
        return canUse(entity, true, true);
    }

    public boolean canUse(LivingEntity entity, boolean hasToHaveAbility, boolean doesConsumeSpirituality) {
        if(!hasAbility(entity) && hasToHaveAbility) return false;

        AbilityCooldownComponent component = entity.getData(ModAttachments.COOLDOWN_COMPONENT);
        if(component.isOnCooldown(id)) return false;

        if(shouldConsumeSpirituality(entity) && doesConsumeSpirituality && BeyonderData.getSpirituality(entity) <= getSpiritualityCost()) return false;

        if(!(entity instanceof Player) && !canBeUsedByNPC) return false;

        if(entity instanceof Player player && player.isSpectator()) return false;

        DisabledAbilitiesComponent disabledComponent = entity.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT);
        if((disabledComponent.isAbilityUsageDisabled() || disabledComponent.isSpecificAbilityDisabled(this.getId())) && !this.canAlwaysBeUsed) return false;

        if(LOTMCraft.abilityHandler.isDisabled(this)) return false;

        return true;
    }

    protected boolean shouldConsumeSpirituality(LivingEntity entity) {
        return !entity.hasInfiniteMaterials() && entity instanceof Player;
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

        return (1f / (100f * Math.max(.5f, ((10 - sequence) * .5f)))) * (entity.level().getGameRules().getInt(ModGameRules.DIGESTION_RATE) / 100f);
    }

    public ResourceLocation getTextureLocation() {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/abilities/" + id + ".png");
    }

    public MutableComponent getName() {
        return Component.translatable("lotmcraft." + getId());
    }

    public Component getNameFormatted() {
        if(getRequirements().isEmpty()) {
            return Component.translatable("lotmcraft." + getId()).withStyle(ChatFormatting.BOLD);
        }

        String pathway = getRequirements().keySet()
                .stream()
                .sorted()
                .findFirst()
                .orElse(null);

        int color = BeyonderData.pathwayInfos.get(pathway).color();
        return getName().withStyle(ChatFormatting.BOLD).withColor(color);
    }

    @Nullable
    public Component getDescription() {
        MutableComponent description = Component.translatable("lotmcraft." + getId() + ".description");
        if(description.getString().equals("lotmcraft." + getId() + ".description")) {
            return null;
        }
        return description.withStyle(ChatFormatting.DARK_GRAY);
    }

    public String getId() {
        return id;
    }

    public boolean getShouldBeHidden(){
        return shouldBeHidden;
    }
}
