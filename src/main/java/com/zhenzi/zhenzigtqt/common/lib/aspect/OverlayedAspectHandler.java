package com.zhenzi.zhenzigtqt.common.lib.aspect;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.util.OverlayedFluidHandler;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class OverlayedAspectHandler {
    private final List<OverlayedAspectHandler.OverlayedTank> overlayedTanks = new ArrayList();

    public OverlayedAspectHandler(@NotNull IMultipleAspectTankHandler tank) {
        IMultipleAspectTankHandler.MultiAspectTankEntry[] entries = tank.getFluidTanks().toArray(new IMultipleAspectTankHandler.MultiAspectTankEntry[0]);
        Arrays.sort(entries, IMultipleAspectTankHandler.ENTRY_COMPARATOR);
        IMultipleAspectTankHandler.MultiAspectTankEntry[] var3 = entries;
        int var4 = entries.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            IMultipleAspectTankHandler.MultiAspectTankEntry fluidTank = var3[var5];
            IAspectTankProperties[] var7 = fluidTank.getTankProperties();
            int var8 = var7.length;

            for(int var9 = 0; var9 < var8; ++var9) {
                IAspectTankProperties property = var7[var9];
                this.overlayedTanks.add(new OverlayedAspectHandler.OverlayedTank(property, fluidTank.allowSameFluidFill()));
            }
        }

    }

    public void reset() {

        for (OverlayedTank tank : this.overlayedTanks) {
            tank.reset();
        }

    }

    public int insertFluid(@NotNull AspectStack fluid, int amountToInsert) {
        if (amountToInsert <= 0) {
            return 0;
        } else {
            int totalInserted = 0;
            boolean distinctFillPerformed = false;
            Iterator var5 = this.overlayedTanks.iterator();

            OverlayedAspectHandler.OverlayedTank overlayedTank;
            int inserted;
            while(var5.hasNext()) {
                overlayedTank = (OverlayedAspectHandler.OverlayedTank)var5.next();
                if (fluid.isAspectEqual(overlayedTank.fluid)) {
                    inserted = overlayedTank.tryInsert(fluid, amountToInsert);
                    if (inserted > 0) {
                        totalInserted += inserted;
                        amountToInsert -= inserted;
                        if (amountToInsert <= 0) {
                            return totalInserted;
                        }
                    }

                    if (!overlayedTank.allowSameFluidFill) {
                        distinctFillPerformed = true;
                    }
                }
            }

            var5 = this.overlayedTanks.iterator();

            while(true) {
                do {
                    if (!var5.hasNext()) {
                        return totalInserted;
                    }

                    overlayedTank = (OverlayedAspectHandler.OverlayedTank)var5.next();
                } while(distinctFillPerformed && !overlayedTank.allowSameFluidFill);

                if (overlayedTank.isEmpty() && overlayedTank.property.canFillAspectType(fluid)) {
                    inserted = overlayedTank.tryInsert(fluid, amountToInsert);
                    if (inserted > 0) {
                        totalInserted += inserted;
                        amountToInsert -= inserted;
                        if (amountToInsert <= 0) {
                            return totalInserted;
                        }

                        if (!overlayedTank.allowSameFluidFill) {
                            distinctFillPerformed = true;
                        }
                    }
                }
            }
        }
    }

    public String toString() {
        return this.toString(false);
    }

    public String toString(boolean lineBreak) {
        StringBuilder stb = (new StringBuilder("OverlayedAspectHandler[")).append(this.overlayedTanks.size()).append(";");
        if (lineBreak) {
            stb.append("\n  ");
        }

        for(int i = 0; i < this.overlayedTanks.size(); ++i) {
            if (i != 0) {
                stb.append(',');
            }

            if (lineBreak) {
                stb.append("\n  ");
            }

            OverlayedAspectHandler.OverlayedTank overlayedTank = (OverlayedAspectHandler.OverlayedTank)this.overlayedTanks.get(i);
            AspectStack fluid = overlayedTank.fluid;
            if (fluid != null && fluid.amount != 0) {
                stb.append(fluid.getAspect().getTag()).append(' ').append(fluid.amount).append(" / ").append(overlayedTank.property.getCapacity());
            } else {
                stb.append("None 0 / ").append(overlayedTank.property.getCapacity());
            }
        }

        if (lineBreak) {
            stb.append('\n');
        }

        return stb.append(']').toString();
    }

    private static class OverlayedTank {
        private final IAspectTankProperties property;
        private final boolean allowSameFluidFill;
        private @Nullable AspectStack fluid;

        OverlayedTank(@NotNull IAspectTankProperties property, boolean allowSameFluidFill) {
            this.property = property;
            this.allowSameFluidFill = allowSameFluidFill;
            this.reset();
        }

        public boolean isEmpty() {
            return this.fluid == null || this.fluid.amount <= 0;
        }

        public int tryInsert(@NotNull AspectStack fluid, int amount) {
            if (this.fluid == null) {
                this.fluid = fluid.copy();
                return this.fluid.amount = Math.min(this.property.getCapacity(), amount);
            } else {
                int maxInsert = Math.min(this.property.getCapacity() - this.fluid.amount, amount);
                if (maxInsert > 0) {
                    AspectStack var10000 = this.fluid;
                    var10000.amount += maxInsert;
                    return maxInsert;
                } else {
                    return 0;
                }
            }
        }

        public void reset() {
            AspectStack fluid = this.property.getContents();
            this.fluid = fluid != null ? fluid.copy() : null;
        }
    }
}
