package com.zhenzi.zhenzigtqt.loaders.builder;

import com.zhenzi.zhenzigtqt.loaders.AspectRecipe;
import com.zhenzi.zhenzigtqt.loaders.AspectRecipeBuilder;
import com.zhenzi.zhenzigtqt.loaders.AspectRecipeMap;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.AssemblerRecipeBuilder;
import gregtech.api.recipes.builders.FuelRecipeBuilder;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;

public class AspectGeneratorRecipeBuilder extends AspectRecipeBuilder<AspectGeneratorRecipeBuilder> {
    public AspectGeneratorRecipeBuilder() {
    }
    public AspectGeneratorRecipeBuilder(AspectRecipe recipe, AspectRecipeMap<AspectGeneratorRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
    }

    public AspectGeneratorRecipeBuilder(AspectGeneratorRecipeBuilder recipeBuilder) {
        super(recipeBuilder);
    }

    public AspectGeneratorRecipeBuilder copy() {
        return new AspectGeneratorRecipeBuilder(this);
    }
}
