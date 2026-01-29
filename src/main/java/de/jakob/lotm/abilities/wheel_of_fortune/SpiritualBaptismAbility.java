package de.jakob.lotm.abilities.wheel_of_fortune;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SanityComponent;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class SpiritualBaptismAbility extends Ability {
    public SpiritualBaptismAbility(String id) {
        super(id, 6);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("wheel_of_fortune", 3));
    }

    @Override
    public float getSpiritualityCost() {
        return 900;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 20, 2);

        if(target == null) {
            if(entity instanceof ServerPlayer player) {
                Component actionBarText = Component.translatable("ability.lotmcraft.misfortune_gifting.no_target").withColor(0xFFc0f6fc);
                ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(actionBarText);
                player.connection.send(packet);
            }

            return;
        }

        EffectManager.playEffect(EffectManager.Effect.SPIRITUAL_BAPTISM, target.getX(), target.getY(), target.getZ(), serverLevel);
        target.addEffect(new MobEffectInstance(MobEffects.HEAL, 5, 40, false, false, false));

        target.setRemainingFireTicks(0);

        target.getActiveEffects()
                .stream()
                .map(MobEffectInstance::getEffect)
                .filter(effect -> effect.value().getCategory() == MobEffectCategory.HARMFUL)
                .forEach(target::removeEffect);

        if(target instanceof Player player) {
            player.getFoodData().setSaturation(20);
            player.getFoodData().setFoodLevel(20);
        }

        SanityComponent sanityComponent = target.getData(ModAttachments.SANITY_COMPONENT);
        sanityComponent.increaseSanityAndSync(.5f, target);
    }
}
