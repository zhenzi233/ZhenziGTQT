package com.zhenzi.zhenzigtqt.loaders;

import com.zhenzi.zhenzigtqt.common.lib.aspect.IMultipleAspectTankHandler;
import com.zhenzi.zhenzigtqt.common.metatileentity.AspectMetaTileEntity;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.metatileentity.IVoidable;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.ParallelLogicType;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.logic.ParallelLogic;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IParallelableAspectRecipeLogic {
    default void applyParallelBonus(@NotNull AspectRecipeBuilder<?> builder) {
    }

    default AspectRecipeBuilder<?> findMultipliedParallelRecipe(@NotNull AspectRecipeMap<?> recipeMap, @NotNull AspectRecipe currentRecipe,
                                                                @NotNull IItemHandlerModifiable inputs,
                                                                @NotNull IMultipleTankHandler fluidInputs,
                                                                @NotNull IMultipleAspectTankHandler aspectInputs,
                                                                @NotNull IItemHandlerModifiable outputs,
                                                                @NotNull IMultipleTankHandler fluidOutputs,
                                                                @NotNull IMultipleAspectTankHandler aspectOutputs,
                                                                int parallelLimit, long maxVoltage, @NotNull IVoidableAspect voidable) {
        return ParallelLogicAspect.doParallelRecipes(currentRecipe, recipeMap, inputs, fluidInputs, aspectInputs, outputs, fluidOutputs, aspectOutputs, parallelLimit, maxVoltage, voidable);
    }

    default AspectRecipeBuilder<?> findAppendedParallelItemRecipe(@NotNull AspectRecipeMap<?> recipeMap,
                                                                  @NotNull IItemHandlerModifiable inputs,
                                                                  @NotNull IItemHandlerModifiable outputs,
                                                                  int parallelLimit, long maxVoltage, @NotNull IVoidable voidable) {
        return ParallelLogicAspect.appendItemRecipes(recipeMap, inputs, outputs, parallelLimit, maxVoltage, voidable);
    }

    default AspectRecipe findParallelRecipe(@NotNull AspectRecipe currentRecipe, @NotNull IItemHandlerModifiable inputs,
                                            @NotNull IMultipleTankHandler fluidInputs,
                                            @NotNull IMultipleAspectTankHandler aspectInputs,
                                            @NotNull IItemHandlerModifiable outputs,
                                            @NotNull IMultipleTankHandler fluidOutputs,
                                            @NotNull IMultipleAspectTankHandler aspectOutputs,
                                            long maxVoltage, int parallelLimit) {
        if (parallelLimit > 1 && this.getRecipeMap() != null) {
            AspectRecipeBuilder var10000;
            switch (this.getParallelLogicType()) {
                case MULTIPLY:
                    var10000 = this.findMultipliedParallelRecipe(this.getRecipeMap(), currentRecipe, inputs, fluidInputs,aspectInputs, outputs, fluidOutputs,aspectOutputs, parallelLimit, maxVoltage, this.getMetaTileEntity());
                    break;
                case APPEND_ITEMS:
                    var10000 = this.findAppendedParallelItemRecipe(this.getRecipeMap(), inputs, outputs, parallelLimit, maxVoltage, this.getMetaTileEntity());
                    break;
                default:
                    throw new IncompatibleClassChangeError();
            }

            AspectRecipeBuilder<?> parallelBuilder = var10000;
            if (parallelBuilder == null) {
                this.invalidateInputs();
                return null;
            } else if (parallelBuilder.getParallel() == 0) {
                this.invalidateOutputs();
                return null;
            } else {
                this.setParallelRecipesPerformed(parallelBuilder.getParallel());
                this.applyParallelBonus(parallelBuilder);
                return (AspectRecipe)parallelBuilder.buildA().getResult();
            }
        } else {
            return currentRecipe;
        }
    }

    @NotNull AspectMetaTileEntity getMetaTileEntity();

    @Nullable AspectRecipeMap<?> getRecipeMap();

    @NotNull ParallelLogicType getParallelLogicType();

    void setParallelRecipesPerformed(int var1);

    void invalidateInputs();

    void invalidateOutputs();
}
