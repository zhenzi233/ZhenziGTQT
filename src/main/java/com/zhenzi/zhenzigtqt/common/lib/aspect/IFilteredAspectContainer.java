package com.zhenzi.zhenzigtqt.common.lib.aspect;

import gregtech.api.capability.IFilter;
import gregtech.api.capability.IFilteredFluidContainer;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.Comparator;

public interface IFilteredAspectContainer {
    Comparator<IFilteredAspectContainer> COMPARATOR = Comparator.nullsLast(Comparator.comparing(IFilteredAspectContainer::getFilter, IFilter.FILTER_COMPARATOR));

    @Nullable
    IFilter<AspectStack> getFilter();
}
