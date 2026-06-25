package de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.implementations;

import de.jakob.lotm.beyonders.abilities.visionary.prophecy.TokenStream;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.ActionContextBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.ActionContextEnum;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.UUID;

public class ActionPositionContext extends ActionContextBase {
    public Vec3 pos;
    public String dimension;

    public static String NBT_DIMENSION = "dimension";

    public ActionPositionContext(UUID id, Vec3 pos){
        super(id);
        this.pos = pos;
        dimension = "";
    }

    public ActionPositionContext(UUID id){
        super(id);
        this.pos = null;
        dimension = "";
    }

    @Override
    public ActionContextEnum getType() {
        return ActionContextEnum.POSITION;
    }

    @Override
    public ActionContextBase fillFromStream(TokenStream stream) {
        stream.next();

        try{
            int x = Integer.parseInt(Objects.requireNonNull(stream.peek()));
            stream.next();
            int y = Integer.parseInt(Objects.requireNonNull(stream.peek()));
            stream.next();
            int z = Integer.parseInt(Objects.requireNonNull(stream.peek()));

            pos = new Vec3(x, y, z);
        }catch (NumberFormatException e){
            pos = Vec3.ZERO;
        }

        stream.next();
        if(!stream.match("then") || !stream.match("and")){
            dimension = stream.peek();
        }

        if(dimension == null) dimension = "";

        return this;
    }

    @Override
    public CompoundTag toNBT(HolderLookup.Provider provider) {
        var tag = super.toNBT(provider);

        tag.putDouble("x", pos.x);
        tag.putDouble("y", pos.y);
        tag.putDouble("z", pos.z);

        tag.putString(NBT_DIMENSION, dimension);

        return tag;
    }

    public static ActionPositionContext load(CompoundTag tag, UUID id, HolderLookup.Provider provider) {
        var context = new ActionPositionContext(id, new Vec3(
                tag.getDouble("x"),
                tag.getDouble("y"),
                tag.getDouble("z")
        ));

        context.dimension = tag.getString(NBT_DIMENSION);

        return context;
    }

}
