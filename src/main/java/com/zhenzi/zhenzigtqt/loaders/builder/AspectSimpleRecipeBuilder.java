package com.zhenzi.zhenzigtqt.loaders.builder;

import com.zhenzi.zhenzigtqt.loaders.AspectRecipe;
import com.zhenzi.zhenzigtqt.loaders.AspectRecipeBuilder;
import com.zhenzi.zhenzigtqt.loaders.AspectRecipeMap;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;

public class AspectSimpleRecipeBuilder extends AspectRecipeBuilder<AspectSimpleRecipeBuilder> {
    public AspectSimpleRecipeBuilder() {
    }

    public AspectSimpleRecipeBuilder(AspectRecipe recipe, AspectRecipeMap<AspectSimpleRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
    }

    public AspectSimpleRecipeBuilder(AspectRecipeBuilder<AspectSimpleRecipeBuilder> recipeBuilder) {
        super(recipeBuilder);
    }

    public AspectSimpleRecipeBuilder copy() {
        return new AspectSimpleRecipeBuilder(this);
    }
}
