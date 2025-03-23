package com.zhenzi.zhenzigtqt.loaders;

import com.zhenzi.zhenzigtqt.common.metatileentity.AspectMetaTileEntity;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.ParallelLogicType;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.recipeproperties.IRecipePropertyStorage;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class AspectFuelRecipeLogic extends AspectRecipeLogicEnergy{
    public AspectFuelRecipeLogic(AspectMetaTileEntity tileEntity, AspectRecipeMap<?> recipeMap, Supplier<IEnergyContainer> energyContainer) {
        super(tileEntity, recipeMap, energyContainer);
    }

    public @NotNull ParallelLogicType getParallelLogicType() {
        return ParallelLogicType.MULTIPLY;
    }

    public boolean consumesEnergy() {
        return false;
    }

    protected boolean hasEnoughPower(@NotNull int[] resultOverclock) {
        return true;
    }

    protected void modifyOverclockPost(int[] overclockResults, @NotNull IRecipePropertyStorage storage) {
        super.modifyOverclockPost(overclockResults, storage);
        overclockResults[0] = -overclockResults[0];
    }

    public int getParallelLimit() {
        return Integer.MAX_VALUE;
    }

    public boolean isAllowOverclocking() {
        return false;
    }
}
