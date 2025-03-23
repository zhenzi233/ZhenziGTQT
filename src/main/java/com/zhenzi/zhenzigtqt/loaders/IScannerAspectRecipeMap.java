package com.zhenzi.zhenzigtqt.loaders;

import com.zhenzi.zhenzigtqt.common.lib.aspect.AspectStack;
import gregtech.api.recipes.Recipe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public interface IScannerAspectRecipeMap {
    default @NotNull List<AspectRecipe> getRepresentativeRecipes() {
        return Collections.emptyList();
    }

    public interface ICustomScannerLogic {
        @Nullable AspectRecipe createCustomRecipe(long var1,
                                                  List<ItemStack> var3,
                                                  List<FluidStack> var4,
                                                  List<AspectStack> var6,
                                                  boolean var5);

        default @Nullable List<AspectRecipe> getRepresentativeRecipes() {
            return null;
        }
    }
}
