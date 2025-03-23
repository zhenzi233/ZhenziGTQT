package com.zhenzi.zhenzigtqt.common.lib.aspect;

import com.google.common.collect.Lists;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class AspectHandlerProxy implements IAspectHandler {
    public IAspectHandler input;
    public IAspectHandler output;
    private IAspectTankProperties[] properties;

    public AspectHandlerProxy(IAspectHandler input, IAspectHandler output) {
        this.reinitializeHandler(input, output);
    }

    public void reinitializeHandler(IAspectHandler input, IAspectHandler output) {
        this.input = input;
        this.output = output;
        List<IAspectTankProperties> tanks = Lists.newArrayList();
        Collections.addAll(tanks, input.getTankProperties());
        Collections.addAll(tanks, output.getTankProperties());
        this.properties = tanks.toArray(new IAspectTankProperties[0]);
    }

    public IAspectTankProperties[] getTankProperties() {
        return this.properties;
    }

    public int fill(AspectStack resource, boolean doFill) {
        return this.input.fill(resource, doFill);
    }

    public @Nullable AspectStack drain(AspectStack resource, boolean doDrain) {
        return this.output.drain(resource, doDrain);
    }

    public @Nullable AspectStack drain(int maxDrain, boolean doDrain) {
        return this.output.drain(maxDrain, doDrain);
    }
}
