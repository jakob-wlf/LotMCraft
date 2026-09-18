package de.jakob.lotm.gui.custom.mass_puppeteering;

import de.jakob.lotm.gui.ModMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MassPuppeteeringMenu extends AbstractContainerMenu {

    public record PuppetTarget(LivingEntity entity, int time) {}

    private final List<PuppetTarget> targets = new ArrayList<>();

    public MassPuppeteeringMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, readTargetsFromBuf(buf, playerInventory.player));
    }

    private static Map<LivingEntity, Integer> readTargetsFromBuf(RegistryFriendlyByteBuf buf, Player player) {
        int size = buf.readVarInt();
        Map<LivingEntity, Integer> map = new LinkedHashMap<>();

        for (int i = 0; i < size; i++) {
            int entityId = buf.readVarInt();
            int time = buf.readVarInt();

            if (player.level().getEntity(entityId) instanceof LivingEntity living) {
                map.put(living, time);
            }
        }
        return map;
    }

    public MassPuppeteeringMenu(int containerId, Inventory playerInventory, Map<LivingEntity, Integer> validTargets) {
        super(ModMenuTypes.MASS_PUPPETEERING_MENU.get(), containerId);

        validTargets.forEach((entity, time) -> {
            this.targets.add(new PuppetTarget(entity, time));
        });
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public List<PuppetTarget> getTargets() {
        return targets;
    }

}