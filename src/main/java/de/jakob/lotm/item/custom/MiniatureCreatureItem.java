package de.jakob.lotm.item.custom;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MiniatureCreatureItem extends Item {
    public MiniatureCreatureItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResultHolder.success(stack);

        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return InteractionResultHolder.fail(stack);
        CompoundTag stored = data.copyTag();
        var type = EntityType.byString(stored.getString("EntityType"));
        if (type.isEmpty() || !stored.contains("EntityNBT")) return InteractionResultHolder.fail(stack);

        Entity creature = type.get().create(level);
        if (creature == null) return InteractionResultHolder.fail(stack);
        CompoundTag nbt = stored.getCompound("EntityNBT").copy();
        creature.load(nbt);
        Vec3 pos = player.position().add(player.getLookAngle().scale(1.5));
        creature.moveTo(pos.x, player.getY(), pos.z, player.getYRot(), 0);
        level.addFreshEntity(creature);
        level.playSound(null, player.blockPosition(), SoundEvents.CHICKEN_EGG, player.getSoundSource(), 1f, 0.6f);

        if (!player.isCreative()) stack.shrink(1);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return;
        CompoundTag stored = data.copyTag();
        String name = stored.getString("Name");
        if (!name.isEmpty()) tooltip.add(Component.translatable("item.lotmcraft.miniature_creature.held", name));

        CompoundTag beyonder = stored.getCompound("EntityNBT").getCompound("neoforge:attachments").getCompound("lotmcraft:beyonder_component");
        if (!beyonder.contains("pathway") || !beyonder.contains("sequence")) return;
        String pathway = beyonder.getString("pathway");
        int sequence = beyonder.getInt("sequence");
        if (pathway.isBlank() || pathway.equals("none") || pathway.equals("placeholder") || sequence < 0 || sequence >= LOTMCraft.NON_BEYONDER_SEQ) return;
        if (!BeyonderData.pathwayInfos.containsKey(pathway)) return;

        int color = BeyonderData.pathwayInfos.get(pathway).color();
        tooltip.add(Component.translatable("lotm.pathway").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.translatable("lotm.pathway." + pathway).withColor(color)));
        tooltip.add(Component.translatable("lotm.sequence").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.literal(sequence + " " + BeyonderData.getSequenceName(pathway, sequence)).withColor(color)));
    }
}
