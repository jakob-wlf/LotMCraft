package de.jakob.lotm.abilities.visionary;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.item.custom.MarionetteControllerItem;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ManipulationAbility extends SelectableAbility {

    public ManipulationAbility(String id) {
        super(id, 30, "charm");
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 4));
    }

    @Override
    public float getSpiritualityCost() {
        return 750;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.manipulation.group_incite",
                "ability.lotmcraft.manipulation.control"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        switch (abilityIndex) {
            case 0 -> groupIncite(level, entity);
            case 1 -> control(level, entity);
        }
    }


    public void triggerGroupIncite(ServerLevel level, ServerPlayer author, LivingEntity target) {
        int casterSeq = BeyonderData.getSequence(author); //this leave as is, its for story writing
        List<LivingEntity> nearby = AbilityUtil.getNearbyEntities(
                author, level, target.position(), 20, false, true);

        for (LivingEntity nearby_entity : nearby) {
            if (nearby_entity.getUUID().equals(author.getUUID())) continue;
            if (nearby_entity.getUUID().equals(target.getUUID())) continue;

            if (nearby_entity instanceof ServerPlayer nearbyPlayer) {
                if (!BeyonderData.isBeyonder(nearbyPlayer)) continue;
                if (BeyonderData.getSequence(nearbyPlayer) <= casterSeq) continue;
                forcePlayerAbilities(nearbyPlayer, target, level);
            } else if (nearby_entity instanceof Mob mob) {
                if (BeyonderData.isBeyonder(mob) && BeyonderData.getSequence(mob) <= casterSeq) continue;
                LivingEntity originalTarget = mob.getTarget();
                mob.setTarget(target);
                ServerScheduler.scheduleDelayed(20 * 10, () -> {
                    if (!mob.isRemoved())
                        mob.setTarget(originalTarget != null && originalTarget.isAlive() ? originalTarget : null);
                });
            }
        }
    }

    public void groupIncite(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 20, 2);
        if (target == null) {
            AbilityUtil.sendActionBar(entity,
                    Component.translatable("ability.lotmcraft.frenzy.no_target").withColor(0xFFff124d));
            return;
        }

        int casterSeq = AbilityUtil.getSeqWithArt(entity, this);
        List<LivingEntity> nearby = AbilityUtil.getNearbyEntities(
                entity, serverLevel, entity.position(), 20, false, true);

        for (LivingEntity nearby_entity : nearby) {
            if (nearby_entity.getUUID().equals(entity.getUUID())) continue;
            if (nearby_entity.getUUID().equals(target.getUUID())) continue;

            if (nearby_entity instanceof ServerPlayer nearbyPlayer) {
                // Force beyonder players of lower sequence to use abilities
                if (!BeyonderData.isBeyonder(nearbyPlayer)) continue;
                if (BeyonderData.getSequence(nearbyPlayer) <= casterSeq) continue;
                forcePlayerAbilities(nearbyPlayer, target, serverLevel);
            } else if (nearby_entity instanceof Mob mob) {
                // For beyonder mobs, check sequence. For non-beyonder mobs, always incite.
                if (BeyonderData.isBeyonder(mob) && BeyonderData.getSequence(mob) <= casterSeq) continue;

                LivingEntity originalTarget = mob.getTarget();
                mob.setTarget(target);

                ServerScheduler.scheduleDelayed(20 * 10, () -> {
                    if (!mob.isRemoved()) {
                        mob.setTarget(originalTarget != null && originalTarget.isAlive()
                                ? originalTarget : null);
                    }
                });
            }
        }
    }

    private void forcePlayerAbilities(ServerPlayer player, LivingEntity target, ServerLevel level) {
        int interval = 20 * 3;
        int duration = 20 * 7;

        String pathway = BeyonderData.getPathway(player);
        int sequence = BeyonderData.getSequence(player);

        ServerScheduler.scheduleForDuration(0, interval, duration, () -> {
            if (player.isRemoved() || !player.isAlive()) return;
            if (target.isRemoved() || !target.isAlive()) return;

            List<Ability> abilities = new ArrayList<>(
                    LOTMCraft.abilityHandler.getByPathwayAndSequence(pathway, sequence));
            if (abilities.isEmpty()) return;

            Ability chosen = abilities.get(random.nextInt(abilities.size()));
            chosen.useAbility(level, player);
        }, level);
    }

    // control indivudlal

    private void control(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (!(entity instanceof Player player)) return;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 20, 2);
        if (target == null) {
            AbilityUtil.sendActionBar(entity,
                    Component.translatable("ability.lotmcraft.frenzy.no_target").withColor(0xFFff124d));
            return;
        }

        int casterSeq = AbilityUtil.getSeqWithArt(entity, this);  
        if (BeyonderData.isBeyonder(target) && BeyonderData.getSequence(target) <= casterSeq) {
            AbilityUtil.sendActionBar(entity,
                    Component.translatable("ability.lotmcraft.manipulation.control.too_strong").withColor(0xFFff124d));
            return;
        }

        // Mark the target as a marionette (movement only)
        var marionetteComponent = target.getData(ModAttachments.MARIONETTE_COMPONENT.get());
        marionetteComponent.setMarionette(true);
        marionetteComponent.setControllerUUID(player.getUUID().toString());

        // Give the player a movement-only controller item
        ItemStack controllerStack = new ItemStack(ModItems.MARIONETTE_CONTROLLER.get());

        CompoundTag tag = new CompoundTag();
        tag.putString("MarionetteUUID", target.getUUID().toString());
        tag.putString("MarionetteType", target.getName().getString());
        tag.putBoolean("MovementOnly", true); // flag to strip non-movement functionality
        controllerStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        // Single use — item is consumed when the 7s expires
        player.addItem(controllerStack);

        AbilityUtil.sendActionBar(entity,
                Component.translatable("ability.lotmcraft.manipulation.control.success").withColor(0xFFe3ffff));

        // After 7s, release the marionette and remove the controller item
        ServerScheduler.scheduleDelayed(20 * 7, () -> {
            if (!target.isRemoved()) {
                marionetteComponent.setMarionette(false);
                marionetteComponent.setControllerUUID(null);
            }
            // Remove the controller item from player inventory
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (!stack.isEmpty() && stack.getItem() instanceof MarionetteControllerItem) {
                    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
                    if (data != null) {
                        String uuid = data.copyTag().getString("MarionetteUUID");
                        if (uuid.equals(target.getUUID().toString())) {
                            player.getInventory().removeItem(i, 1);
                            break;
                        }
                    }
                }
            }
        });

        // If the controller is used to point at another mob, make target aggressive toward that mob
        // This is handled in MarionetteControllerItem via  MovementOnly
    }
}
