package com.zhenzi.zhenzigtqt.loaders;

import com.zhenzi.zhenzigtqt.common.metatileentity.AspectMetaTileEntity;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.RecipeMap;

import java.util.function.Supplier;

public class AspectRecipeLogicEnergy extends AbstractAspectRecipeLogic {
    protected final Supplier<IEnergyContainer> energyContainer;

    public AspectRecipeLogicEnergy(AspectMetaTileEntity tileEntity, AspectRecipeMap<?> recipeMap, Supplier<IEnergyContainer> energyContainer) {
        super(tileEntity, recipeMap);
        this.energyContainer = energyContainer;
        this.setMaximumOverclockVoltage(this.getMaxVoltage());
    }

    protected long getEnergyInputPerSecond() {
        return ((IEnergyContainer)this.energyContainer.get()).getInputPerSec();
    }

    protected long getEnergyStored() {
        return ((IEnergyContainer)this.energyContainer.get()).getEnergyStored();
    }

    protected long getEnergyCapacity() {
        return ((IEnergyContainer)this.energyContainer.get()).getEnergyCapacity();
    }

    protected boolean drawEnergy(int recipeEUt, boolean simulate) {
        long resultEnergy = this.getEnergyStored() - (long)recipeEUt;
        if (resultEnergy >= 0L && resultEnergy <= this.getEnergyCapacity()) {
            if (!simulate) {
                ((IEnergyContainer)this.energyContainer.get()).changeEnergy((long)(-recipeEUt));
            }

            return true;
        } else {
            return false;
        }
    }

    public long getMaxVoltage() {
        return Math.max(((IEnergyContainer)this.energyContainer.get()).getInputVoltage(), ((IEnergyContainer)this.energyContainer.get()).getOutputVoltage());
    }
}
