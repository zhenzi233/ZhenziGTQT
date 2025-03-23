package com.zhenzi.zhenzigtqt.loaders.recipes;

import com.zhenzi.zhenzigtqt.common.lib.aspect.AspectStack;
import com.zhenzi.zhenzigtqt.loaders.ZZRecipeMaps;
import gregtech.api.GTValues;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.builders.FuelRecipeBuilder;
import gregtech.api.unification.material.Materials;
import net.minecraftforge.fluids.FluidStack;
import thaumcraft.api.aspects.Aspect;

public class AspectGeneratorRecipeLoader {
    public AspectGeneratorRecipeLoader() {
    }
    public static void init() {
        ZZRecipeMaps.ASPECT_GENERATOR_RECIPES
                .recipeBuilder()
                .aspectInputs(new AspectStack(Aspect.AIR, 1))
                .duration(10)
                .EUt((int) GTValues.V[1])
                .buildAndRegister();

    }
}
