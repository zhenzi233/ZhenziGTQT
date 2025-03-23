package com.zhenzi.zhenzigtqt.loaders.recipes;

import com.zhenzi.zhenzigtqt.common.lib.aspect.AspectStack;
import com.zhenzi.zhenzigtqt.loaders.AspectRecipeInput;
import com.zhenzi.zhenzigtqt.loaders.ZZRecipeMaps;
import gregtech.api.GTValues;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import thaumcraft.api.aspects.Aspect;
import thelm.packagedthaumic.item.ItemClathrateEssence;

public class ClatherateEssenceFormerRecipeLoader {
    public ClatherateEssenceFormerRecipeLoader() {
    }
    public static void init() {
        for (Aspect aspect : Aspect.getCompoundAspects())
        {
            createAspectRecipe(aspect);
        }
        for (Aspect aspect : Aspect.getPrimalAspects())
        {
            createAspectRecipe(aspect);
        }
    }

    public static void createAspectRecipe(Aspect aspect)
    {
        ItemStack clathrateA = ItemClathrateEssence.makeClathrate(aspect, 1);
        ItemStack clathrateB = ItemClathrateEssence.makeClathrate(aspect, 1);
        ItemStack clathrateC = ItemClathrateEssence.makeClathrate(aspect, 1);
        clathrateA.setCount(64);
        clathrateB.setCount(31);
        clathrateC.setCount(1);
        ZZRecipeMaps.CLATHRATE_ESSENCE_FORMER_RECIPES
                .recipeBuilder()
                .duration(100)
                .EUt(120)
                .aspectInputs(new AspectRecipeInput(aspect, 100))
                .outputs(clathrateA, clathrateB)
                .chancedOutput(clathrateC, 8000, 1000)
                .chancedOutput(clathrateC, 6000, 1000)
                .chancedOutput(clathrateC, 4000, 1000)
                .chancedOutput(clathrateC, 2000, 1000)
                .buildAndRegister();
    }
}
