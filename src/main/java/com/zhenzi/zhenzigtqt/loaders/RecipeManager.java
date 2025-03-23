package com.zhenzi.zhenzigtqt.loaders;

import com.zhenzi.zhenzigtqt.loaders.recipes.AspectGeneratorRecipeLoader;
import com.zhenzi.zhenzigtqt.loaders.recipes.AssemblerRecipeLoader;
import com.zhenzi.zhenzigtqt.loaders.recipes.ClatherateEssenceFormerRecipeLoader;

public class RecipeManager {
    public RecipeManager() {
    }

    public static void init() {
        AssemblerRecipeLoader.init();
        AspectGeneratorRecipeLoader.init();
        ClatherateEssenceFormerRecipeLoader.init();
    }
}
