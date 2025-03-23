package com.zhenzi.zhenzigtqt.common.lib.aspect;

import gregtech.api.capability.IFilter;
import gregtech.api.capability.IFilteredFluidContainer;
import gregtech.api.capability.IMultipleTankHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public interface IMultipleAspectTankHandler extends IAspectHandler, Iterable<IMultipleAspectTankHandler.MultiAspectTankEntry>
{
    Comparator<MultiAspectTankEntry> ENTRY_COMPARATOR = (o1, o2) -> {
        boolean empty1 = o1.getAspectAmount() <= 0;
        boolean empty2 = o2.getAspectAmount() <= 0;
        if (empty1 != empty2) {
            return empty1 ? 1 : -1;
        } else {
            IFilter<AspectStack> filter1 = o1.getFilter();
            IFilter<AspectStack> filter2 = o2.getFilter();
            if (filter1 == null) {
                return filter2 == null ? 0 : 1;
            } else {
                return filter2 == null ? -1 : IFilter.FILTER_COMPARATOR.compare(filter1, filter2);
            }
        }
    };

    @NotNull List<MultiAspectTankEntry> getFluidTanks();

    int getTanks();

    @NotNull MultiAspectTankEntry getTankAt(int var1);

    boolean allowSameFluidFill();

    default int getIndexOfFluid(@Nullable AspectStack aspectStack) {
        List<MultiAspectTankEntry> fluidTanks = this.getFluidTanks();

        for(int i = 0; i < fluidTanks.size(); ++i) {
            AspectStack tankStack = ((MultiAspectTankEntry)fluidTanks.get(i)).getAspectStack();
            if (aspectStack == tankStack || tankStack != null && tankStack.isAspectEqual(aspectStack)) {
                return i;
            }
        }

        return -1;
    }

    default Iterator<MultiAspectTankEntry> iterator() {
        return this.getFluidTanks().iterator();
    }

    public static final class MultiAspectTankEntry implements IAspectTank, IAspectHandler, IFilteredAspectContainer {
        private final IMultipleAspectTankHandler tank;
        private final IAspectTank delegate;

        public MultiAspectTankEntry(@NotNull IMultipleAspectTankHandler tank, @NotNull IAspectTank delegate) {
            this.tank = tank;
            this.delegate = delegate;
        }

        public @NotNull IMultipleAspectTankHandler getTank() {
            return this.tank;
        }

        public @NotNull IAspectTank getDelegate() {
            return this.delegate;
        }

        public boolean allowSameFluidFill() {
            return this.tank.allowSameFluidFill();
        }

        public @Nullable IFilter<AspectStack> getFilter() {
            IAspectTank delegate = this.delegate;
            IFilter filter;
            if (delegate instanceof IFilteredAspectContainer) {
                IFilteredAspectContainer filtered = (IFilteredAspectContainer)delegate;
                filter = filtered.getFilter();
            } else {
                filter = null;
            }

            return filter;
        }

        public @NotNull IAspectTankProperties[] getTankProperties() {
            IAspectTank delegate = this.delegate;
            IAspectTankProperties[] aspectTankProperties;
            if (delegate instanceof IAspectHandler) {
                IAspectHandler aspectHandler = (IAspectHandler)delegate;
                aspectTankProperties = aspectHandler.getTankProperties();
            } else {
                aspectTankProperties = new IAspectTankProperties[]{new IMultipleAspectTankHandler.MultiAspectTankEntry.FallbackAspectTankProperty()};
            }

            return aspectTankProperties;
        }

        public NBTTagCompound trySerialize() {
            IAspectTank delegate = this.delegate;
            if (delegate instanceof AspectTank) {
                AspectTank aspectTank = (AspectTank)delegate;
                return aspectTank.writeToNBT(new NBTTagCompound());
            } else {
                delegate = this.delegate;
                if (delegate instanceof INBTSerializable) {
                    INBTSerializable serializable = (INBTSerializable)delegate;

                    try {
                        return (NBTTagCompound)serializable.serializeNBT();
                    } catch (ClassCastException var4) {
                    }
                }

                return new NBTTagCompound();
            }
        }

        public void tryDeserialize(NBTTagCompound tag) {
            IAspectTank delegate = this.delegate;
            if (delegate instanceof AspectTank) {
                AspectTank aspectTank = (AspectTank)delegate;
                aspectTank.readFromNBT(tag);
            } else {
                delegate = this.delegate;
                if (delegate instanceof INBTSerializable) {
                    INBTSerializable serializable = (INBTSerializable)delegate;

                    try {
                        serializable.deserializeNBT(tag);
                    } catch (ClassCastException var5) {
                    }
                }
            }

        }

        public @Nullable AspectStack getAspectStack() {
            return this.delegate.getAspectStack();
        }

        @Override
        public int getAspectAmount() {
            return this.delegate.getAspectAmount();
        }

        public int getCapacity() {
            return this.delegate.getCapacity();
        }

        public AspectTankInfo getInfo() {
            return this.delegate.getInfo();
        }

        public int fill(AspectStack resource, boolean doFill) {
            return this.delegate.fill(resource, doFill);
        }

        public @Nullable AspectStack drain(AspectStack resource, boolean doDrain) {
            if (resource != null && resource.amount > 0) {
                IAspectTank delegate = this.delegate;
                if (delegate instanceof IAspectHandler) {
                    IAspectHandler aspectHandler = (IAspectHandler)delegate;
                    return aspectHandler.drain(resource, doDrain);
                } else {
                    AspectStack aspectStack = this.delegate.getAspectStack();
                    return aspectStack != null && aspectStack.isAspectEqual(resource) ? this.drain(resource.amount, doDrain) : null;
                }
            } else {
                return null;
            }
        }

        public @Nullable AspectStack drain(int maxDrain, boolean doDrain) {
            return this.delegate.drain(maxDrain, doDrain);
        }

        public int hashCode() {
            return this.delegate.hashCode();
        }

        public boolean equals(Object obj) {
            return this == obj || this.delegate.equals(obj);
        }

        public String toString() {
            return this.delegate.toString();
        }

        private final class FallbackAspectTankProperty implements IAspectTankProperties {
            private FallbackAspectTankProperty() {
            }

            public @Nullable AspectStack getContents() {
                return IMultipleAspectTankHandler.MultiAspectTankEntry.this.delegate.getAspectStack();
            }

            public int getCapacity() {
                return IMultipleAspectTankHandler.MultiAspectTankEntry.this.delegate.getCapacity();
            }

            public boolean canFill() {
                return true;
            }

            public boolean canDrain() {
                return true;
            }

            public boolean canFillAspectType(AspectStack fluidStack) {
                IFilter<AspectStack> filter = IMultipleAspectTankHandler.MultiAspectTankEntry.this.getFilter();
                return filter == null || filter.test(fluidStack);
            }

            public boolean canDrainAspectType(AspectStack fluidStack) {
                return true;
            }
        }
    }
}
