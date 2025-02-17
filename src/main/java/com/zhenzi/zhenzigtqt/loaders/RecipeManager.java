package com.zhenzi.zhenzigtqt.loaders;

import com.zhenzi.zhenzigtqt.loaders.recipes.AssemblerRecipeLoader;

public class RecipeManager {
    public RecipeManager() {
    }

    public static void init() {
        AssemblerRecipeLoader.init();
    }
}
