package com.zhenzi.zhenzigtqt.integration.jei;

import com.zhenzi.zhenzigtqt.common.lib.aspect.AspectStack;
import mezz.jei.api.recipe.IIngredientType;
import net.minecraft.item.ItemStack;

public class AspectTypes {
    public static final IIngredientType<AspectStack> ASPECT = () -> {
        return AspectStack.class;
    };
    private AspectTypes() {
    }
}
