package com.zhenzi.zhenzigtqt.common.lib.aspect;

import gregtech.api.recipes.FluidKey;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import thaumcraft.api.aspects.Aspect;

import java.util.Objects;

public class AspectKey {
    public final String fluid;
    public NBTTagCompound tag;
    private final int amount;

    public AspectKey(AspectStack fluidStack) {
        this.fluid = fluidStack.getAspect().getTag();
        this.tag = fluidStack.tag;
        this.amount = fluidStack.amount;
    }

    public AspectKey copy() {
        return new AspectKey(new AspectStack(this.getFluid(), this.amount));
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof AspectKey)) {
            return false;
        } else {
            AspectKey fluidKey = (AspectKey)o;
            if (!Objects.equals(this.fluid, fluidKey.fluid)) {
                return false;
            } else if (this.tag == null && fluidKey.tag != null) {
                return false;
            } else {
                return this.tag == null || this.tag.equals(fluidKey.tag);
            }
        }
    }

    public int hashCode() {
        int hash = 0;
        hash += Objects.hash(this.fluid);
        if (this.tag != null && !this.tag.isEmpty()) {
            hash += this.tag.hashCode();
        }

        return hash;
    }

    public String toString() {
        return "AspectKey{aspect=" + this.fluid + ", tag=" + this.tag + '}';
    }

    public Aspect getFluid() {
        return Aspect.getAspect(this.fluid);
    }
}
