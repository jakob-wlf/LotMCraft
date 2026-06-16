package de.jakob.lotm.abilities.visionary.prophecy.actions;

import de.jakob.lotm.abilities.visionary.prophecy.actions.context.ActionContextBase;
import de.jakob.lotm.abilities.visionary.prophecy.actions.implementations.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.level.Level;

import java.sql.Time;
import java.util.UUID;

public abstract class ActionBase {
    public static final String CONTEXT = "action_contex";

    protected final ActionContextBase context;

    public ActionBase(ActionContextBase context){
        this.context = context;
    }

    abstract public ActionsEnum getType();

    public CompoundTag toNBT(HolderLookup.Provider provider){
        CompoundTag tag = new CompoundTag();
        tag.put(CONTEXT, context.toNBT(provider));
        return tag;
    }

    public abstract int getRequiredSeq();

    abstract public void action(Level level, LivingEntity entity, UUID casterId);

    public static ActionBase load(ActionsEnum type, CompoundTag tag, HolderLookup.Provider provider) {
        return switch (type) {
            case DROP_ITEM -> DropItemAction.load(tag, provider);
            case TELEPORT -> TeleportAction.load(tag, provider);
            case DIGESTION -> DigestionAction.load(tag, provider);
            case HEALTH -> HealthAction.load(tag, provider);
            case SANITY -> SanityAction.load(tag, provider);
            case CALAMITY -> CalamityAction.load(tag, provider);
            case STUN -> StunAction.load(tag, provider);
            case SKILL -> UseSkillAction.load(tag, provider);
            case CONFUSION -> ConfusionAction.load(tag, provider);
            case SEAL -> SealAction.load(tag, provider);
            case UNSEAL -> UnSealAction.load(tag, provider);
            case SPAWN -> SpawnAction.load(tag, provider);
            case SAY -> SayAction.load(tag, provider);
            case WEATHER -> WeatherAction.load(tag, provider);
            case TIME -> TimeAction.load(tag, provider);
            case WHISPERS -> WhispersAction.load(tag, provider);
            case SUICIDE -> SuicideAction.load(tag, provider);
            case PLAGUE -> PlagueAction.load(tag, provider);
            case SLEEP -> SleepAction.load(tag, provider);
            case DOUBLE -> DoubleAction.load(tag, provider);
            case SPIRITUALITY -> SpiritualityAction.load(tag, provider);
            case PLAYER -> PlayerAction.load(tag,provider);
            case EMPTY -> EmptyAction.load(tag, provider);
        };
    }

    public static ActionBase create(ActionsEnum type, ActionContextBase context){
        return switch (type){
            case DROP_ITEM -> new DropItemAction(context);
            case TELEPORT -> new TeleportAction(context);
            case DIGESTION -> new DigestionAction(context);
            case HEALTH -> new HealthAction(context);
            case SANITY -> new SanityAction(context);
            case CALAMITY -> new CalamityAction(context);
            case STUN -> new StunAction(context);
            case SKILL -> new UseSkillAction(context);
            case CONFUSION -> new ConfusionAction(context);
            case SEAL -> new SealAction(context);
            case UNSEAL -> new UnSealAction(context);
            case SPAWN -> new SpawnAction(context);
            case SAY -> new SayAction(context);
            case WEATHER -> new WeatherAction(context);
            case TIME -> new TimeAction(context);
            case WHISPERS -> new WhispersAction(context);
            case SUICIDE -> new SuicideAction(context);
            case PLAGUE -> new PlagueAction(context);
            case SLEEP -> new SleepAction(context);
            case DOUBLE -> new DoubleAction(context);
            case SPIRITUALITY -> new SpiritualityAction(context);
            case PLAYER -> new PlayerAction(context);
            case EMPTY -> new EmptyAction(context);
        };
    }
}
