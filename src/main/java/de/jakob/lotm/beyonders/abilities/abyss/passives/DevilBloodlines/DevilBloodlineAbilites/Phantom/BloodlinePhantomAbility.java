package de.jakob.lotm.beyonders.abilities.abyss.passives.DevilBloodlines.DevilBloodlineAbilites.Phantom;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.abyss.passives.DevilBloodlines.DevilBloodlineAbility;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.entity.custom.AvatarEntity;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class BloodlinePhantomAbility extends ToggleAbility {
    private static final int TOGGLE_COOLDOWN_TICKS = 20 * 10;
    private static BloodlinePhantomAbility instance;
    public BloodlinePhantomAbility(String id) {
        super(id);
        canBeUsedByNPC = false;
        instance = this;
    }

    @Override
    public Map<String, Integer> getRequirements() {return new HashMap<>(Map.of("abyss", 6));}
    @Override
    protected float getSpiritualityCost() {return 30;}
    @Override
    public boolean meetsBloodlineRequirement(LivingEntity entity) {return hasPhantomBloodline(entity);}
    // copies never have bloodline to stop the shadow clone world destoryer
    private static boolean hasPhantomBloodline(LivingEntity entity) {
        if (entity instanceof AvatarEntity) return false;
        return DevilBloodlineAbility.hasBloodline(entity, DevilBloodlineAbility.Bloodline.PHANTOM);
    }
    //State helpers
    public static boolean isInPhantomForm(Entity entity) {
        return PhantomFormAttachment.isGhost(entity);
    }
    public static boolean blocksAbilities(LivingEntity entity) {
        return isInPhantomForm(entity) && BeyonderData.getSequence(entity) > 5;
    }
    //Toggle
    @Override
    public void start(Level level, LivingEntity entity) {
        if (level.isClientSide()) return;
        PhantomFormAttachment.setGhost(entity, true);
        entity.setInvisible(true);
        applyToggleCooldown(entity);
        if (entity instanceof ServerPlayer player) {
            player.displayClientMessage(Component.translatable("ability.lotmcraft.wraith_ability.activated").withColor(0xBFA7FF), true);
        }
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level.isClientSide()) return;
        // Restore the form if the toggle is still on but the attachment was lost (e.g. after relogging)
        if (!isInPhantomForm(entity)) {PhantomFormAttachment.setGhost(entity, true);}
        entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40, 0, false, false, false));
        entity.fallDistance = 0;
    }
    @Override
    public void stop(Level level, LivingEntity entity) {
        if (level.isClientSide()) return;
        endGhostForm(entity);
        applyToggleCooldown(entity);
        if (entity instanceof ServerPlayer player) {player.displayClientMessage(Component.translatable("ability.lotmcraft.wraith_ability.deactivated").withColor(0xBFA7FF), true);}
    }
    private static void endGhostForm(LivingEntity entity) {
        PhantomFormAttachment.setGhost(entity, false);
        entity.setInvisible(false);
        entity.removeEffect(MobEffects.INVISIBILITY);
        if (entity instanceof ServerPlayer player) {moveOutOfBlocks(player);}
    }

    // If the player ends the form inside a wall they would suffocate so move it to the nearest free space.
    private static void moveOutOfBlocks(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        if (level.noCollision(player)) return;
        Vec3 origin = player.position();
        int[] verticalOrder = {0, 1, -1, 2, -2, 3, -3, 4, -4, 5, -5, 6, -6, 8, -8, 10, -10};
        for (int radius = 0; radius <= 8; radius++) {
            for (int dy : verticalOrder) {
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) continue;
                        Vec3 candidate = new Vec3(Math.floor(origin.x) + dx + 0.5, Math.floor(origin.y) + dy, Math.floor(origin.z) + dz + 0.5);
                        AABB box = player.getDimensions(player.getPose()).makeBoundingBox(candidate);
                        if (level.noCollision(player, box)) {
                            player.teleportTo(candidate.x, candidate.y, candidate.z);
                            player.fallDistance = 0;
                            return;
                        }
                    }
                }
            }
        }
    }
    private static boolean isBeyonderAbilityDamage(DamageSource source) {return source.typeHolder().unwrapKey().map(key -> key.location().getNamespace().equals(LOTMCraft.MOD_ID)).orElse(false);}
    //A phantom can only be damaged by beyonder abilities and that damage breaks the form
    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(victim.level() instanceof ServerLevel serverLevel)) return;
        DamageSource source = event.getSource();
        // A ghost cannot hurt anyone with normal attacks. Ability damage is allowed (Wraith).
        if (source.getEntity() instanceof LivingEntity attacker && isInPhantomForm(attacker) && !isBeyonderAbilityDamage(source)) {
            event.setCanceled(true);
            return;
        }
        if (!isInPhantomForm(victim)) return;
        // The void and /kill must still work, or a ghost could never be removed
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        if (isBeyonderAbilityDamage(source)) {
            // The hit lands and breaks the ghost form
            if (instance != null) {instance.cancel(serverLevel, victim);}
            else {endGhostForm(victim);}
            return;
        }
        event.setCanceled(true);
    }
    //No interacting with the world (runs on both sides, the attachment is synced)
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {if (isInPhantomForm(event.getEntity())) event.setCanceled(true);}
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {if (isInPhantomForm(event.getEntity())) event.setCanceled(true);}
    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {if (isInPhantomForm(event.getEntity())) event.setCanceled(true);}
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {if (isInPhantomForm(event.getEntity())) event.setCanceled(true);}
    @SubscribeEvent
    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {if (isInPhantomForm(event.getEntity())) event.setCanceled(true);}
    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {if (isInPhantomForm(event.getEntity())) event.setCanceled(true);}
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {if (isInPhantomForm(event.getPlayer())) event.setCanceled(true);}
    //Normal mobs cannot target a ghost. Beyonder NPC can.
    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity newTarget = event.getNewAboutToBeSetTarget();
        if (newTarget == null || !isInPhantomForm(newTarget)) return;
        if (!BeyonderData.isBeyonder(event.getEntity())) {event.setCanceled(true);}
    }
    private void applyToggleCooldown(LivingEntity entity) {entity.getData(ModAttachments.COOLDOWN_COMPONENT).setCooldown(getId(), TOGGLE_COOLDOWN_TICKS);}
    public static boolean isWraith(LivingEntity entity) {return isInPhantomForm(entity) && BeyonderData.getSequence(entity) <= 5;}
}