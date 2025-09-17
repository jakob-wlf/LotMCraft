package de.jakob.lotm.abilities.visionary;

import de.jakob.lotm.abilities.ToggleAbilityItem;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SpectatingAbility extends ToggleAbilityItem {
    private static final HashMap<UUID, LivingEntity> entitiesLookedAtByPlayerWithActiveAbility = new HashMap<>();

    public SpectatingAbility(Properties properties) {
        super(properties);

        canBeUsedByNPC = false;
    }

    @Override
    protected float getSpiritualityCost() {
        return .125f;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 9));
    }

    @Override
    protected void start(Level level, LivingEntity entity) {
        if(!level.isClientSide)
            return;

        entity.playSound(SoundEvents.AMETHYST_BLOCK_BREAK, 3, .01f);

        entitiesLookedAtByPlayerWithActiveAbility.put(entity.getUUID(), null);
    }

    @Override
    protected void tick(Level level, LivingEntity entity) {
        if(!(entity instanceof ServerPlayer player) || level.isClientSide)
            return;

        LivingEntity lookedAt = AbilityUtil.getTargetEntity(entity, 40, 1.2f);
        entitiesLookedAtByPlayerWithActiveAbility.replace(entity.getUUID(), lookedAt);

        entity.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 10, 1, false, false, false));

        Component message = Component.translatable("item.lotmcraft.spectating_ability");
        player.displayClientMessage(message, true);
    }

    @Override
    protected void stop(Level level, LivingEntity entity) {
        if(!level.isClientSide)
            return;
        entitiesLookedAtByPlayerWithActiveAbility.remove(entity.getUUID());
    }

    public static @Nullable LivingEntity getLookedAtEntityIfActive(Player player) {
        if(!entitiesLookedAtByPlayerWithActiveAbility.containsKey(player.getUUID()))
            return null;

        return entitiesLookedAtByPlayerWithActiveAbility.get(player.getUUID());
    }
}
