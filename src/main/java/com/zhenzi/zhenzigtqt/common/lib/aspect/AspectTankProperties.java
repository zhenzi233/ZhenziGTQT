package com.zhenzi.zhenzigtqt.common.lib.aspect;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;

public class AspectTankProperties implements IAspectTankProperties {
    @Nullable
    private final AspectStack contents;
    private final int capacity;
    private final boolean canFill;
    private final boolean canDrain;

    public AspectTankProperties(@Nullable AspectStack contents, int capacity)
    {
        this(contents, capacity, true, true);
    }

    public AspectTankProperties(@Nullable AspectStack contents, int capacity, boolean canFill, boolean canDrain)
    {
        this.contents = contents;
        this.capacity = capacity;
        this.canFill = canFill;
        this.canDrain = canDrain;
    }
    @Nullable
    @Override
    public AspectStack getContents() {
        return contents == null ? null : contents.copy();
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public boolean canFill() {
        return canFill;
    }

    @Override
    public boolean canDrain() {
        return canDrain;
    }

    @Override
    public boolean canFillAspectType(AspectStack aspectStack) {
        return canFill;
    }

    @Override
    public boolean canDrainAspectType(AspectStack aspectStack) {
        return canDrain;
    }
}
