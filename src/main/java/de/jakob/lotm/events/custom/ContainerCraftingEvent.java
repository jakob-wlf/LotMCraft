package de.jakob.lotm.events.custom;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.Event;

public class ContainerCraftingEvent extends Event {
    private final Entity entity;
    private final AbstractContainerMenu container;
    private final Item item;

    public ContainerCraftingEvent(Entity entity, AbstractContainerMenu container, Item item){
        this.entity = entity;
        this.container = container;
        this.item = item;
    }

    public Entity getEntity(){
        return entity;
    }

    public AbstractContainerMenu getContainer(){
        return container;
    }

    public Item getItem() {
        return item;
    }
}
