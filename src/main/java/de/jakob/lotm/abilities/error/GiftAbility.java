package de.jakob.lotm.abilities.error;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.abilities.error.handler.TheftHandler;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.AbilitySelectionPacket;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.client.multiplayer.chat.report.ReportEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockEventData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GiftAbility extends SelectableAbility {
    public GiftAbility(String id) {
        super(id, 1f);
        canBeCopied = false;
        canBeReplicated = false;
        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("error", 6));
    }

    @Override
    public float getSpiritualityCost() {
        return 60;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.gift_ability.item",
                "ability.lotmcraft.gift_ability.distance",
                "ability.lotmcraft.gift_ability.health",
                "ability.lotmcraft.gift_ability.digestion",
                "ability.lotmcraft.gift_ability.luck"
        };
    }

    @Override
    public void nextAbility(LivingEntity entity){
        if(getAbilityNames().length == 0)
            return;

        if(!selectedAbilities.containsKey(entity.getUUID())) {
            selectedAbilities.put(entity.getUUID(), 0);
        }

        int selectedAbility = selectedAbilities.get(entity.getUUID());
        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);

        selectedAbility++;
        if(selectedAbility >= getAbilityNames().length) {
            selectedAbility = 0;
        }

        if((entitySeq > 5 && selectedAbility >= 1)
                || (entitySeq > 1 && selectedAbility >= 3)){
            selectedAbility = 0;
        }

        selectedAbilities.put(entity.getUUID(), selectedAbility);
        PacketHandler.sendToServer(new AbilitySelectionPacket(getId(), selectedAbility));
    }

    @Override
    public void previousAbility(LivingEntity entity){
        if(getAbilityNames().length == 0)
            return;

        if(!selectedAbilities.containsKey(entity.getUUID())) {
            selectedAbilities.put(entity.getUUID(), 0);
        }

        int selectedAbility = selectedAbilities.get(entity.getUUID());
        selectedAbility--;
        if(selectedAbility <= -1) {
            selectedAbility = getAbilityNames().length - 1;
        }

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
        if((entitySeq > 5 && selectedAbility >= 1)
                || (entitySeq > 1 && selectedAbility >= 3)){
            selectedAbility = 0;
        }

        selectedAbilities.put(entity.getUUID(), selectedAbility);
        PacketHandler.sendToServer(new AbilitySelectionPacket(getId(), selectedAbility));
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        switch(selectedAbility){
            case 0 -> giftItem(level, entity);
            case 1 -> giftDistance(level, entity);
            case 2 -> giftHealth(level, entity);
            case 3 -> giftDigestion(level, entity);
            case 4 -> giftLuck(level, entity);
        }
    }

    private void giftLuck(Level level, LivingEntity entity){
        if(!(entity instanceof ServerPlayer player)) {
            if(entity instanceof Player player && entity.level().isClientSide) {
                player.playSound(SoundEvents.BELL_RESONATE, 1, 1);
            }
            return;
        }

        LivingEntity target = AbilityUtil.getTargetEntity(entity, (int) (15 * (multiplier(entity) * multiplier(entity))), 2);
        if(target == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.gift.no_target").withColor(0x6d32a8));
            return;
        }

        if(!BeyonderData.isBeyonder(target) || !(target instanceof ServerPlayer targetPlayer)){
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.gift.failed").withColor(0x6d32a8));
            return;
        }

        float targetDigestion = BeyonderData.getDigestionProgress(targetPlayer);
        if(targetDigestion == 1.0f){
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.gift.failed").withColor(0x6d32a8));
            return;
        }

        var luck = entity.getData(ModAttachments.LUCK_COMPONENT.get());
        if(luck.getLuck() == 0){
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.gift.failed").withColor(0x6d32a8));
            return;
        }

        EffectManager.playEffect(EffectManager.Effect.GIFTING_PARTICLES, target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(), player, entity);

        var targetLuck = target.getData(ModAttachments.LUCK_COMPONENT.get());

        targetLuck.setLuck(targetLuck.getLuck() + luck.getLuck());
        luck.setLuck(0);
    }

    private void giftDigestion(Level level, LivingEntity entity){
        if(!(entity instanceof ServerPlayer player)) {
            if(entity instanceof Player player && entity.level().isClientSide) {
                player.playSound(SoundEvents.BELL_RESONATE, 1, 1);
            }
            return;
        }

        LivingEntity target = AbilityUtil.getTargetEntity(entity, (int)(15 * (multiplier(entity) * multiplier(entity))), 2);
        if(target == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.gift.no_target").withColor(0x6d32a8));
            return;
        }

        if(!BeyonderData.isBeyonder(target) || !(target instanceof ServerPlayer targetPlayer)){
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.gift.failed").withColor(0x6d32a8));
            return;
        }

        float targetDigestion = BeyonderData.getDigestionProgress(targetPlayer);
        if(targetDigestion == 1.0f){
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.gift.failed").withColor(0x6d32a8));
            return;
        }

        if(BeyonderData.getDigestionProgress(player) <= 0.0f){
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.gift.failed").withColor(0x6d32a8));
            return;
        }

        EffectManager.playEffect(EffectManager.Effect.GIFTING_PARTICLES, target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(), player, entity);

        float base = 0.25f;
        float actual = BeyonderData.getDigestionProgress(player) - base < 0 ? (base + (BeyonderData.getDigestionProgress(player) - base)) : base;

        BeyonderData.digest(targetPlayer, actual, false);
        BeyonderData.digest(player, -actual, false);
    }

    private void giftHealth(Level level, LivingEntity entity){
        if(!(entity instanceof ServerPlayer player)) {
            if(entity instanceof Player player && entity.level().isClientSide) {
                player.playSound(SoundEvents.BELL_RESONATE, 1, 1);
            }
            return;
        }

        LivingEntity target = AbilityUtil.getTargetEntity(entity, (int) (15 * (multiplier(entity) * multiplier(entity))), 2);
        if(target == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.gift.no_target").withColor(0x6d32a8));
            return;
        }

        float maxHealth = entity.getMaxHealth();
        float healthToGift = maxHealth/4;
        float currentHealth = entity.getHealth();

        if(currentHealth - healthToGift <= maxHealth/2){
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.gift.failed").withColor(0x6d32a8));
            return;
        }

        EffectManager.playEffect(EffectManager.Effect.GIFTING_PARTICLES, target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(), player, entity);

        target.setHealth(target.getHealth() + healthToGift);
        entity.setHealth(currentHealth - healthToGift);
    }

    private void giftDistance(Level level, LivingEntity entity){
        if(!(entity instanceof ServerPlayer player)) {
            if(entity instanceof Player player && entity.level().isClientSide) {
                player.playSound(SoundEvents.BELL_RESONATE, 1, 1);
            }
            return;
        }

        LivingEntity target = AbilityUtil.getTargetEntity(entity, (int) (15 * (multiplier(entity) * multiplier(entity))), 2);
        if(target == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.gift.no_target").withColor(0x6d32a8));
            return;
        }

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
        int distance = (int) TheftHandler.getDistancePerSeq(entitySeq);

        if(!MundaneConceptualTheft.stolenDistanceMap.containsKey(entity.getUUID())){
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.gift.failed").withColor(0x6d32a8));
            return;
        }

        int storedDistance = MundaneConceptualTheft.stolenDistanceMap.get(entity.getUUID());

        if(storedDistance - distance < 0){
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.gift.failed").withColor(0x6d32a8));
            return;
        }

        EffectManager.playEffect(EffectManager.Effect.GIFTING_PARTICLES, target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(), player, entity);

        MundaneConceptualTheft.stolenDistanceMap.put(entity.getUUID(), storedDistance - distance);

        Vec3 eyePos = entity.getEyePosition();
        Vec3 lookVec = entity.getLookAngle();
        Vec3 reach = eyePos.add(lookVec.scale(distance));

        ClipContext context = new ClipContext(
                eyePos,
                reach,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                entity
        );

        BlockHitResult hitResult = level.clip(context);

        Vec3 finalPos;

        if (hitResult.getType() != HitResult.Type.MISS) {
            finalPos = hitResult.getLocation().subtract(lookVec.scale(0.5));
        } else {
            finalPos = reach;
        }

        target.teleportTo(finalPos.x, finalPos.y, finalPos.z);
    }

    private void giftItem(Level level, LivingEntity entity){
        if(!(entity instanceof ServerPlayer player)) {
            if(entity instanceof Player player && entity.level().isClientSide) {
                player.playSound(SoundEvents.BELL_RESONATE, 1, 1);
            }
            return;
        }

        ItemStack offHandItem = player.getItemInHand(InteractionHand.OFF_HAND);

        if(offHandItem.isEmpty()) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.gift.no_item").withColor(0x6d32a8));
            return;
        }

        LivingEntity target = AbilityUtil.getTargetEntity(entity, (int) (15 * (multiplier(entity) * multiplier(entity))), 2);
        if(target == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.gift.no_target").withColor(0x6d32a8));
            return;
        }

        EffectManager.playEffect(EffectManager.Effect.GIFTING_PARTICLES, target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(), player, entity);

        if(!isItemWithEffect(offHandItem)) {
            var capability = target.getCapability(Capabilities.ItemHandler.ENTITY);

            if(capability != null && hasInventory(target)) {
                ItemStack toInsert = offHandItem.copy();

                // Try to insert into all slots
                for(int i = 0; i < capability.getSlots(); i++) {
                    toInsert = capability.insertItem(i, toInsert, false);
                    if(toInsert.isEmpty()) {
                        break; // Successfully inserted everything
                    }
                }

                // Drop any remainder
                if(!toInsert.isEmpty()) {
                    target.spawnAtLocation(toInsert);
                }
            } else {
                // No inventory or capability, drop at target's location
                target.spawnAtLocation(offHandItem.copy());
            }
        }
        else {
            handleGiftEffect(level, entity, target, offHandItem);
        }

        offHandItem.setCount(0);
    }

    private void handleGiftEffect(Level level, LivingEntity entity, LivingEntity target, ItemStack offHandItem) {
        if(offHandItem.is(Items.TNT)) {
            Random rand = new Random();
            for(int i = 0; i < Math.min(offHandItem.getCount(), 30); i++) {
                PrimedTnt tnt = new PrimedTnt(level, target.getX() + rand.nextDouble(-1, 1), target.getY() + rand.nextDouble(-1, 1), target.getZ() + rand.nextDouble(-1, 1), entity);
                tnt.setFuse(10);
                level.addFreshEntity(tnt);
            }
        }
        else if(offHandItem.is(Items.ANVIL)) {
            for(int i = 0; i < offHandItem.getCount(); i++) {
                FallingBlockEntity anvil = FallingBlockEntity.fall(level, BlockPos.containing(target.getX(), target.getY() + 10 + i, target.getZ()), Blocks.ANVIL.defaultBlockState());
                anvil.disableDrop();
                anvil.setHurtsEntities(6, 50);
            }
        }
        else if(offHandItem.is(Items.ENDER_PEARL)) {
            target.teleportTo(entity.getX(), entity.getY(), entity.getZ());
        }
        else if(offHandItem.is(Items.FIRE_CHARGE)) {
            target.setRemainingFireTicks(target.getRemainingFireTicks() + 20 * offHandItem.getCount());
        }
        else if(offHandItem.is(Items.LAVA_BUCKET)) {
            target.setRemainingFireTicks(target.getRemainingFireTicks() + 30 * offHandItem.getCount());
        }
        else if(offHandItem.is(Items.WATER_BUCKET)) {
            for(int i = 0; i < offHandItem.getCount(); i++) {
                target.clearFire();
            }
        }
    }

    public boolean hasInventory(Entity entity) {
        return entity.getCapability(Capabilities.ItemHandler.ENTITY) != null;
    }

    private final Item[] itemsWithEffects = new Item[]{
            Items.TNT,
            Items.ANVIL,
            Items.ENDER_PEARL,
            Items.FIRE_CHARGE,
            Items.LAVA_BUCKET,
            Items.WATER_BUCKET,

    };

    private boolean isItemWithEffect(ItemStack offHandItem) {
        for (Item item : itemsWithEffects) {
            if (offHandItem.is(item)) {
                return true;
            }
        }
        return false;
    }
}
