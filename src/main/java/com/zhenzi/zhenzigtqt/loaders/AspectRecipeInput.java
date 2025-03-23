package com.zhenzi.zhenzigtqt.loaders;

import com.zhenzi.zhenzigtqt.common.lib.aspect.AspectStack;
import gregtech.api.recipes.ingredients.GTRecipeFluidInput;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;

import java.util.Objects;

public class AspectRecipeInput extends GTRecipeInput {
    private final AspectStack inputStack;

    public AspectRecipeInput(Aspect aspect, int amount)
    {
        this(new AspectStack(aspect, amount), amount);
    }

    public AspectRecipeInput(AspectStack inputStack) {
        this.inputStack = inputStack;
        this.amount = inputStack.amount;
    }

    public AspectRecipeInput(AspectStack inputStack, int amount) {
        this.inputStack = inputStack.copy();
        this.inputStack.amount = amount;
        this.amount = amount;
    }

    protected AspectRecipeInput copy() {
        AspectRecipeInput copy = new AspectRecipeInput(this.inputStack, this.amount);
        copy.isConsumable = this.isConsumable;
        copy.nbtMatcher = this.nbtMatcher;
        copy.nbtCondition = this.nbtCondition;
        return copy;
    }

    public AspectRecipeInput copyWithAmount(int amount) {
        AspectRecipeInput copy = new AspectRecipeInput(this.inputStack, amount);
        copy.isConsumable = this.isConsumable;
        copy.nbtMatcher = this.nbtMatcher;
        copy.nbtCondition = this.nbtCondition;
        return copy;
    }

    public AspectStack getInputAspectStack() {
        return this.inputStack;
    }

    public boolean acceptsAspect(@Nullable AspectStack input) {
        if (input != null && input.amount != 0) {
            if (!areAspectsEqual(this.inputStack, input)) {
                return false;
            } else {
                return this.nbtMatcher == null ? AspectStack.areAspectStackTagsEqual(this.inputStack, input) : this.nbtMatcher.evaluate(input.tag, this.nbtCondition);
            }
        } else {
            return false;
        }
    }

    protected int computeHash() {
        return Objects.hash(this.inputStack.getAspect().getTag(), this.amount, this.nbtMatcher, this.nbtCondition, this.nbtMatcher == null ? this.inputStack.tag : 0);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof AspectRecipeInput)) {
            return false;
        } else {
            AspectRecipeInput other = (AspectRecipeInput)obj;
            if (this.amount == other.amount && this.isConsumable == other.isConsumable) {
                if (!Objects.equals(this.nbtMatcher, other.nbtMatcher)) {
                    return false;
                } else if (!Objects.equals(this.nbtCondition, other.nbtCondition)) {
                    return false;
                } else {
                    return areAspectsEqual(this.inputStack, other.inputStack) && (this.nbtMatcher != null || AspectStack.areAspectStackTagsEqual(this.inputStack, other.inputStack));
                }
            } else {
                return false;
            }
        }
    }

    @Override
    public boolean equalIgnoreAmount(GTRecipeInput input) {
        if (this == input) {
            return true;
        } else if (!(input instanceof AspectRecipeInput)) {
            return false;
        } else {
            AspectRecipeInput other = (AspectRecipeInput)input;
            if (!Objects.equals(this.nbtMatcher, other.nbtMatcher)) {
                return false;
            } else if (!Objects.equals(this.nbtCondition, other.nbtCondition)) {
                return false;
            } else {
                return areAspectsEqual(this.inputStack, other.inputStack) && (this.nbtMatcher != null || AspectStack.areAspectStackTagsEqual(this.inputStack, other.inputStack));
            }
        }
    }

    public String toString() {
        return this.amount + "x" + this.inputStack.getUnlocalizedName();
    }

    private static boolean areAspectsEqual(AspectStack fluid1, AspectStack fluid2) {
        return fluid1.getAspect().getTag().equals(fluid2.getAspect().getTag());
    }
}
