package com.zhenzi.zhenzigtqt.common.lib.aspect;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;

import javax.annotation.Nullable;

public interface IAspectTank {
    @Nullable
    AspectStack getAspectStack();

    int getAspectAmount();

    int getCapacity();

    AspectTankInfo getInfo();

    int fill(AspectStack resource, boolean doFill);

    @Nullable
    AspectStack drain(int maxDrain, boolean doDrain);
}
