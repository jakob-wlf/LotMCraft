package de.jakob.lotm.abilities.error;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.abilities.error.handler.TheftHandler;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SendPassiveTheftEffectPacket;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class TheftAbility extends AbilityItem {
    public TheftAbility(Properties properties) {
        super(properties, 1.75f);

        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("error", 8));
    }

    @Override
    protected float getSpiritualityCost() {
        return 10.5f;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel) || !(entity instanceof ServerPlayer player)) {
            return;
        }

        LivingEntity target = AbilityUtil.getTargetEntity(player, 8 * (int) multiplier(entity), 1.5f);
        if(target == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.theft.no_target").withColor(0x4742c9));
            return;
        }

        TheftHandler.stealItemsFromEntity(target, player);
        PacketHandler.sendToPlayer(player, new SendPassiveTheftEffectPacket(target.getEyePosition().x, target.getEyePosition().y, target.getEyePosition().z));
    }
}
