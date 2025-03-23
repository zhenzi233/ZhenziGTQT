package com.zhenzi.zhenzigtqt.loaders;

import gregtech.api.recipes.RecipeMap;

public interface IMultipleAspectRecipeMap {
    AspectRecipeMap<?>[] getAvailableRecipeMaps();

    AspectRecipeMap<?> getCurrentRecipeMap();

    int getRecipeMapIndex();

    void setRecipeMapIndex(int var1);
}
