package de.jakob.lotm.beyonders.abilities.error;

import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.attachments.CorruptionComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.AllyUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class HostControllingAbility extends SelectableAbility {
    public HostControllingAbility(String id) {
        super(id, .5f);
        canBeCopied = false;
        canBeReplicated = false;
        canBeUsedInArtifact = false;
        canAlwaysBeUsed = true;
        canBeShared = false;
        cannotBeStolen = true;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("error", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 0;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.host_controlling.drain_health",
                "ability.lotmcraft.host_controlling.kill",
                "ability.lotmcraft.host_controlling.switch_to_control",
                "ability.lotmcraft.host_controlling.control_movement",
                "ability.lotmcraft.host_controlling.heal",
                "ability.lotmcraft.host_controlling.take_corruption",
                "ability.lotmcraft.host_controlling.give_corruption"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        LivingEntity host = ParasitationAbility.getHostForEntity(serverLevel, entity);
        if(host == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.host_controlling.no_host").withColor(0x3240bf));
            return;
        }



        switch (abilityIndex) {
            case 0 -> {
                if(AllyUtil.areAllies(entity, host)){
                    AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.host_controlling.no_host").withColor(0x3240bf));
                    return;
                }
                float healthToDrain = (float) (DamageLookup.lookupDamage(4, .75f) * multiplier(entity));

                if(BeyonderData.getSequence(host) <= BeyonderData.getSequence(entity))
                    healthToDrain = healthToDrain/2.0f;

                host.hurt(entity.damageSources().magic(), healthToDrain);
                entity.heal(healthToDrain);
            }
            case 1 -> {
                if(AllyUtil.areAllies(entity, host)){
                    AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.host_controlling.no_host").withColor(0x3240bf));
                    return;
                }
                if(BeyonderData.getSequence(host) <= BeyonderData.getSequence(entity)){
                    return;
                }
                host.setHealth(0.5f);
                host.hurt(entity.damageSources().magic(), 1000);
            }
            case 2 -> {
                if(AllyUtil.areAllies(entity, host)){
                    AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.host_controlling.no_host").withColor(0x3240bf));
                    return;
                }
                if (entity instanceof ServerPlayer player) {
                    ParasitationAbility.switchToControl(serverLevel, player);
                }
            }
            case 3 -> {
                if(AllyUtil.areAllies(entity, host)){
                    AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.host_controlling.no_host").withColor(0x3240bf));
                    return;
                }
                if (entity instanceof ServerPlayer player) {
                    ParasitationAbility.switchToMovementControl(serverLevel, player);
                }
            }
            case 4 -> {
                float healthToDrain = (float) (DamageLookup.lookupDamage(4, .75f) * multiplier(entity));

                if(BeyonderData.getSequence(host) <= BeyonderData.getSequence(entity))
                    healthToDrain = healthToDrain/2.0f;
                host.heal(healthToDrain);

            }
            case 5 -> {
                CorruptionComponent targetComp = host.getData(ModAttachments.CORRUPTION_COMPONENT);
                CorruptionComponent casterComp = entity.getData(ModAttachments.CORRUPTION_COMPONENT);

                float transfer = Math.min( targetComp.getCorruption(), 10);
                targetComp.decreaseCorruptionAndSync(transfer, host);
                casterComp.increaseCorruptionAndSync(transfer, entity);

            }
            case 6 -> {
                CorruptionComponent targetComp = host.getData(ModAttachments.CORRUPTION_COMPONENT);
                CorruptionComponent casterComp = entity.getData(ModAttachments.CORRUPTION_COMPONENT);

                float transfer = Math.min( casterComp.getCorruption(), 10);
                targetComp.increaseCorruptionAndSync(transfer, host);
                casterComp.decreaseCorruptionAndSync(transfer, entity);

            }
        }
    }
}
