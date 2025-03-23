package com.zhenzi.zhenzigtqt.common.lib.aspect;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;

public interface IAspectHandler {
    IAspectTankProperties[] getTankProperties();
    int fill(AspectStack resource, boolean doFill);
    @Nullable
    AspectStack drain(AspectStack resource, boolean doDrain);
    @Nullable
    AspectStack drain(int maxDrain, boolean doDrain);
}
