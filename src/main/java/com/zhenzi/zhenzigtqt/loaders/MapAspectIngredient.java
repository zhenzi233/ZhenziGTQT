package com.zhenzi.zhenzigtqt.loaders;

import com.zhenzi.zhenzigtqt.common.lib.aspect.AspectStack;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.recipes.map.AbstractMapIngredient;
import gregtech.api.recipes.map.MapFluidIngredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import thaumcraft.api.aspects.Aspect;

import java.util.Objects;

public class MapAspectIngredient extends AbstractMapIngredient {
    public final Aspect aspect;
    public final NBTTagCompound tag;

    public MapAspectIngredient(AspectRecipeInput fluidInput) {
        AspectStack fluidStack = fluidInput.getInputAspectStack();
        this.aspect = fluidStack.getAspect();
        this.tag = fluidStack.tag;
    }

    public MapAspectIngredient(AspectStack fluidStack) {
        this.aspect = fluidStack.getAspect();
        this.tag = fluidStack.tag;
    }

    protected int hash() {
        int hash = 31 + this.aspect.getTag().hashCode();
        return this.tag != null ? 31 * hash + this.tag.hashCode() : hash;
    }

    public boolean equals(Object o) {
        if (super.equals(o)) {
            MapAspectIngredient other = (MapAspectIngredient)o;
            if (this.aspect.getTag().equals(other.aspect.getTag())) {
                return Objects.equals(this.tag, other.tag);
            }
        }

        return false;
    }

    public String toString() {
        return "MapAspectIngredient{{aspect=" + this.aspect.getTag() + "} {tag=" + this.tag + "}";
    }
}
