package de.jakob.lotm.abilities.mother.handler;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityDimensions;

public class HybridMobData {
    private final ResourceLocation modelEntityType;
    private final EntityDimensions dimensions;

    public HybridMobData(ResourceLocation modelEntityType, EntityDimensions dimensions) {
        this.modelEntityType = modelEntityType;
        this.dimensions = dimensions;
    }

    public ResourceLocation getModelEntityType() {
        return modelEntityType;
    }

    public EntityDimensions getDimensions() {
        return dimensions;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("ModelEntityType", modelEntityType.toString());
        tag.putFloat("Width", dimensions.width());
        tag.putFloat("Height", dimensions.height());
        tag.putBoolean("Fixed", dimensions.fixed());
        return tag;
    }

    public static HybridMobData load(CompoundTag tag) {
        ResourceLocation modelType = ResourceLocation.parse(tag.getString("ModelEntityType"));
        float width = tag.getFloat("Width");
        float height = tag.getFloat("Height");
        boolean fixed = tag.getBoolean("Fixed");
        
        EntityDimensions dimensions = fixed ? 
            EntityDimensions.fixed(width, height) : 
            EntityDimensions.scalable(width, height);
            
        return new HybridMobData(modelType, dimensions);
    }
}