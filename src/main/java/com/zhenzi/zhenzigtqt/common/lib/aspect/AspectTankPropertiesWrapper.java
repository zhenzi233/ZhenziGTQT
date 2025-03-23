package com.zhenzi.zhenzigtqt.common.lib.aspect;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import javax.annotation.Nullable;

public class AspectTankPropertiesWrapper implements IAspectTankProperties
{
    protected final AspectTank tank;

    public AspectTankPropertiesWrapper(AspectTank tank)
    {
        this.tank = tank;
    }
    @Nullable
    @Override
    public AspectStack getContents() {
        AspectStack contents = tank.getAspectStack();
        return contents == null ? null : contents.copy();
    }

    @Override
    public int getCapacity() {
        return tank.getCapacity();
    }

    @Override
    public boolean canFill() {
        return tank.canFill();
    }

    @Override
    public boolean canDrain() {
        return tank.canDrain();
    }

    @Override
    public boolean canFillAspectType(AspectStack aspectStack) {
        return tank.canFillFluidType(aspectStack);
    }

    @Override
    public boolean canDrainAspectType(AspectStack aspectStack) {
        return tank.canDrainFluidType(aspectStack);
    }
}
