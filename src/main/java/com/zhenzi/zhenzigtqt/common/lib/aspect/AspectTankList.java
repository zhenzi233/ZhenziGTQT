package com.zhenzi.zhenzigtqt.common.lib.aspect;

import gregtech.api.capability.IMultipleTankHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AspectTankList implements IMultipleAspectTankHandler, INBTSerializable<NBTTagCompound> {
    private final IMultipleAspectTankHandler.MultiAspectTankEntry[] aspectTanks;
    private final boolean allowSameAspectFill;

    public AspectTankList(boolean allowSameAspectFill, IAspectTank... aspectTanks) {
        ArrayList<IMultipleAspectTankHandler.MultiAspectTankEntry> list = new ArrayList();
        IAspectTank[] tanks = aspectTanks;
        int fluidTankSize = aspectTanks.length;

        for(int i = 0; i < fluidTankSize; ++i) {
            IAspectTank tank = tanks[i];
            list.add(this.wrapIntoEntry(tank));
        }

        this.aspectTanks = list.toArray(new IMultipleAspectTankHandler.MultiAspectTankEntry[0]);
        this.allowSameAspectFill = allowSameAspectFill;
    }

    public AspectTankList(boolean allowSameAspectFill, @NotNull List<? extends IAspectTank> aspectTanks) {
        ArrayList<IMultipleAspectTankHandler.MultiAspectTankEntry> list = new ArrayList();
        Iterator var4 = aspectTanks.iterator();

        while(var4.hasNext()) {
            IAspectTank tank = (IAspectTank)var4.next();
            list.add(this.wrapIntoEntry(tank));
        }

        this.aspectTanks = list.toArray(new IMultipleAspectTankHandler.MultiAspectTankEntry[0]);
        this.allowSameAspectFill = allowSameAspectFill;
    }

    public AspectTankList(boolean allowSameAspectFill, @NotNull IMultipleAspectTankHandler parent, IAspectTank... additionalTanks) {
        ArrayList<IMultipleAspectTankHandler.MultiAspectTankEntry> list = new ArrayList(parent.getFluidTanks());
        IAspectTank[] var5 = additionalTanks;
        int var6 = additionalTanks.length;

        for(int var7 = 0; var7 < var6; ++var7) {
            IAspectTank tank = var5[var7];
            list.add(this.wrapIntoEntry(tank));
        }

        this.aspectTanks = list.toArray(new IMultipleAspectTankHandler.MultiAspectTankEntry[0]);
        this.allowSameAspectFill = allowSameAspectFill;
    }

    private IMultipleAspectTankHandler.MultiAspectTankEntry wrapIntoEntry(IAspectTank tank) {
        IMultipleAspectTankHandler.MultiAspectTankEntry var10000;
        if (tank instanceof IMultipleAspectTankHandler.MultiAspectTankEntry) {
            IMultipleAspectTankHandler.MultiAspectTankEntry entry = (IMultipleAspectTankHandler.MultiAspectTankEntry)tank;
            var10000 = entry;
        } else {
            var10000 = new IMultipleAspectTankHandler.MultiAspectTankEntry(this, tank);
        }

        return var10000;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound fluidInventory = new NBTTagCompound();
        NBTTagList tanks = new NBTTagList();

        for(int i = 0; i < this.getTanks(); ++i) {
            tanks.appendTag(this.aspectTanks[i].trySerialize());
        }

        fluidInventory.setTag("AspectTanks", tanks);
        return fluidInventory;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        NBTTagList tanks = nbt.getTagList("AspectTanks", 10);

        for(int i = 0; i < Math.min(this.aspectTanks.length, tanks.tagCount()); ++i) {
            this.aspectTanks[i].tryDeserialize(tanks.getCompoundTagAt(i));
        }
    }

    @Override
    public String toString() {
        return this.toString(false);
    }

    public String toString(boolean lineBreak) {
        StringBuilder stb = (new StringBuilder("AspectTankList[")).append(this.aspectTanks.length).append(";");

        for(int i = 0; i < this.aspectTanks.length; ++i) {
            if (i != 0) {
                stb.append(',');
            }

            stb.append(lineBreak ? "\n  " : " ");
            AspectStack fluid = this.aspectTanks[i].getAspectStack();
            if (fluid != null && fluid.amount != 0) {
                stb.append(fluid.getAspect().getName()).append(' ').append(fluid.amount).append(" / ").append(this.aspectTanks[i].getCapacity());
            } else {
                stb.append("None 0 / ").append(this.aspectTanks[i].getCapacity());
            }
        }

        if (lineBreak) {
            stb.append('\n');
        }

        return stb.append(']').toString();
    }

    @Override
    public IAspectTankProperties[] getTankProperties() {
        ArrayList<IAspectTankProperties> propertiesList = new ArrayList();
        IMultipleAspectTankHandler.MultiAspectTankEntry[] var2 = this.aspectTanks;
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            IMultipleAspectTankHandler.MultiAspectTankEntry fluidTank = var2[var4];
            Collections.addAll(propertiesList, fluidTank.getTankProperties());
        }

        return propertiesList.toArray(new IAspectTankProperties[0]);
    }

    @Override
    public int fill(AspectStack resource, boolean doFill) {
        if (resource != null && resource.amount > 0) {
            int totalInserted = 0;
            boolean inputFluidCopied = false;
            boolean distinctSlotVisited = false;
            IMultipleAspectTankHandler.MultiAspectTankEntry[] fluidTanks = this.aspectTanks.clone();
            Arrays.sort(fluidTanks, IMultipleAspectTankHandler.ENTRY_COMPARATOR);
            IMultipleAspectTankHandler.MultiAspectTankEntry[] var7 = fluidTanks;
            int var8 = fluidTanks.length;

            int var9;
            IMultipleAspectTankHandler.MultiAspectTankEntry tank;
            for(var9 = 0; var9 < var8; ++var9) {
                tank = var7[var9];
                if (resource.isAspectEqual(tank.getAspectStack())) {
                    int inserted = tank.fill(resource, doFill);
                    if (inserted > 0) {
                        totalInserted += inserted;
                        if (resource.amount - inserted <= 0) {
                            return totalInserted;
                        }

                        if (!inputFluidCopied) {
                            inputFluidCopied = true;
                            resource = resource.copy();
                        }

                        resource.amount -= inserted;
                    }

                    if (!tank.allowSameFluidFill()) {
                        distinctSlotVisited = true;
                    }
                }
            }

            var7 = fluidTanks;
            var8 = fluidTanks.length;

            for(var9 = 0; var9 < var8; ++var9) {
                tank = var7[var9];
                boolean usesDistinctFluidFill = tank.allowSameFluidFill();
                if ((usesDistinctFluidFill || !distinctSlotVisited) && tank.getAspectAmount() == 0) {
                    int inserted = tank.fill(resource, doFill);
                    if (inserted > 0) {
                        totalInserted += inserted;
                        if (resource.amount - inserted <= 0) {
                            return totalInserted;
                        }

                        if (!inputFluidCopied) {
                            inputFluidCopied = true;
                            resource = resource.copy();
                        }

                        resource.amount -= inserted;
                        if (!usesDistinctFluidFill) {
                            distinctSlotVisited = true;
                        }
                    }
                }
            }

            return totalInserted;
        } else {
            return 0;
        }
    }

    @Nullable
    @Override
    public AspectStack drain(AspectStack resource, boolean doDrain) {
        if (resource != null && resource.amount > 0) {
            int amountLeft = resource.amount;
            AspectStack totalDrained = null;
            IMultipleAspectTankHandler.MultiAspectTankEntry[] var5 = this.aspectTanks;
            int var6 = var5.length;

            for(int var7 = 0; var7 < var6; ++var7) {
                IAspectTank handler = var5[var7];
                if (resource.isAspectEqual(handler.getAspectStack())) {
                    AspectStack drain = handler.drain(amountLeft, doDrain);
                    if (drain != null) {
                        if (totalDrained == null) {
                            totalDrained = drain;
                        } else {
                            totalDrained.amount += drain.amount;
                        }

                        amountLeft -= drain.amount;
                        if (amountLeft <= 0) {
                            return totalDrained;
                        }
                    }
                }
            }

            return totalDrained;
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public AspectStack drain(int maxDrain, boolean doDrain) {
        if (maxDrain <= 0) {
            return null;
        } else {
            AspectStack totalDrained = null;
            IMultipleAspectTankHandler.MultiAspectTankEntry[] var4 = this.aspectTanks;
            int var5 = var4.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                IAspectTank handler = var4[var6];
                if (totalDrained == null) {
                    totalDrained = handler.drain(maxDrain, doDrain);
                    if (totalDrained != null) {
                        maxDrain -= totalDrained.amount;
                    }
                } else {
                    if (!totalDrained.isAspectEqual(handler.getAspectStack())) {
                        continue;
                    }

                    AspectStack drain = handler.drain(maxDrain, doDrain);
                    if (drain != null) {
                        totalDrained.amount += drain.amount;
                        maxDrain -= drain.amount;
                    }
                }

                if (maxDrain <= 0) {
                    return totalDrained;
                }
            }

            return totalDrained;
        }
    }

    @Override
    public @NotNull List<MultiAspectTankEntry> getFluidTanks() {
        return Collections.unmodifiableList(Arrays.asList(this.aspectTanks));
    }

    @Override
    public int getTanks() {
        return this.aspectTanks.length;
    }

    @Override
    public @NotNull MultiAspectTankEntry getTankAt(int index) {
        return this.aspectTanks[index];
    }

    @Override
    public boolean allowSameFluidFill() {
        return this.allowSameAspectFill;
    }
}
