package com.zhenzi.zhenzigtqt.common.lib.aspect;

import net.minecraftforge.fluids.FluidStack;
import thaumcraft.api.aspects.Aspect;

import javax.annotation.Nullable;

public interface IAspectTankProperties {
    @Nullable
    AspectStack getContents();
    int getCapacity();
    boolean canFill();
    boolean canDrain();
    boolean canFillAspectType(AspectStack aspectStack);
    boolean canDrainAspectType(AspectStack aspectStack);
}
