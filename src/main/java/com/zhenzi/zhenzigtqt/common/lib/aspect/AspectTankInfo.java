package com.zhenzi.zhenzigtqt.common.lib.aspect;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import javax.annotation.Nullable;

public class AspectTankInfo {
    @Nullable
    public final AspectStack aspectStack;
    public final int capacity;

    public AspectTankInfo(@Nullable AspectStack aspectStack, int capacity)
    {
        this.aspectStack = aspectStack;
        this.capacity = capacity;
    }

    public AspectTankInfo(IAspectTank tank)
    {
        this.aspectStack = tank.getAspectStack();
        this.capacity = tank.getCapacity();
    }
}
