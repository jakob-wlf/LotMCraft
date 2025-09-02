package de.jakob.lotm.abilities.door;

import com.mojang.serialization.Codec;
import de.jakob.lotm.abilities.AbilityHandler;
import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.abilities.SelectableAbilityItem;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.data.ModDataComponents;
import de.jakob.lotm.entity.custom.ApprenticeBookEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class RecordingAbility extends SelectableAbilityItem {
    public RecordingAbility(Properties properties) {
        super(properties, 8f);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 6));
    }

    @Override
    protected float getSpiritualityCost() {
        return 0;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.recording.record", "ability.lotmcraft.recording.get_abilities"};
    }

    @Override
    protected void useAbility(Level level, LivingEntity entity, int abilityIndex) {
        if(level.isClientSide)
            return;

        switch(abilityIndex) {
            case 0 -> record((ServerLevel) level, entity);
        }
    }

    private void record(ServerLevel level, LivingEntity entity) {
        Vec3 playerDir = (new Vec3(entity.getLookAngle().x, 0, entity.getLookAngle().z)).normalize();
        Vec3 pos = VectorUtil.getRelativePosition(entity.getEyePosition().add(0, -.4, 0), playerDir, 1.2, 0, -.4);
        Vec3 dir = entity.getEyePosition().subtract(pos).normalize();

        ApprenticeBookEntity book = new ApprenticeBookEntity(level, pos, dir);
        level.addFreshEntity(book);

        AtomicBoolean hasRecordedAbility = new AtomicBoolean(false);

        level.playSound(null, BlockPos.containing(entity.position()), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1, 1);

        ServerScheduler.scheduleForDuration(0, 1, 20 * 5, () -> {
            if(hasRecordedAbility.get())
                return;

            Vec3 currentPlayerDir = (new Vec3(entity.getLookAngle().x, 0, entity.getLookAngle().z)).normalize();
            Vec3 currentPos = VectorUtil.getRelativePosition(entity.getEyePosition().add(0, -.8, 0), currentPlayerDir, 1.1, 0, -.2);
            Vec3 currentDir = entity.getEyePosition().subtract(currentPos).normalize();
            book.setPos(currentPos);
            book.setFacingDirection(currentDir);

            AbilityItem abilityItem = AbilityHandler.abilityUsedInArea(new Location(entity.getEyePosition(), level), 15);
            if(abilityItem == null || abilityItem instanceof RecordingAbility)
                return;

            hasRecordedAbility.set(true);
            book.discard();

            if(abilityItem.lowestSequenceUsable() + 2 < BeyonderData.getSequence(entity)) {
                entity.hurt(entity.damageSources().source(ModDamageTypes.LOOSING_CONTROL), entity.getHealth() - .5f);
                if(entity instanceof ServerPlayer player) {
                    ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.literal("The ability was too high of a sequence.").withColor(0xFF8ff4ff));
                    player.connection.send(packet);
                }
                level.playSound(null, BlockPos.containing(entity.position()), SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 1, 1);
                ParticleUtil.spawnParticles(level, ParticleTypes.PORTAL, pos, 45, .3, .02);
                return;
            }

            if(random.nextInt(4) == 0) {
                if(entity instanceof ServerPlayer player) {
                    ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.literal("Failed to Record the ability.").withColor(0xFF8ff4ff));
                    player.connection.send(packet);
                }
                level.playSound(null, BlockPos.containing(entity.position()), SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 1, 1);
                ParticleUtil.spawnParticles(level, ParticleTypes.PORTAL, pos, 45, .3, .02);
                return;
            }

            level.playSound(null, BlockPos.containing(entity.position()), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1, 1);

            ItemStack item = new ItemStack(abilityItem, 1);
            item.set(ModDataComponents.IS_RECORDED, true);
            if(entity instanceof ServerPlayer player) {
                player.addItem(item);
            }

            ParticleUtil.spawnParticles(level, ParticleTypes.END_ROD, book.position(), 60, .3, .08);
            ParticleUtil.spawnParticles(level, ParticleTypes.ENCHANT, book.position(), 60, .3, .08);
        }, () -> {
            if(hasRecordedAbility.get())
                return;
            if(entity instanceof ServerPlayer player) {
                ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.literal("No ability was recorded.").withColor(0xFF8ff4ff));
                player.connection.send(packet);
            }
            level.playSound(null, BlockPos.containing(entity.position()), SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 1, 1);
            ParticleUtil.spawnParticles(level, ParticleTypes.PORTAL, book.position(), 90, .3, .1);
            book.discard();
        }, level);
    }


}
