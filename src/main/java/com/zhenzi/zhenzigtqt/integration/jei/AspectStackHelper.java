package com.zhenzi.zhenzigtqt.integration.jei;

import com.google.common.base.MoreObjects;
import com.zhenzi.zhenzigtqt.common.lib.aspect.AspectStack;
import mezz.jei.api.ingredients.IIngredientHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;

import java.util.Iterator;

public class AspectStackHelper implements IIngredientHelper<AspectStack> {
    public AspectStackHelper() {
    }
    @Nullable
    @Override
    public AspectStack getMatch(Iterable<AspectStack> iterable, AspectStack aspectStack) {
        Iterator var3 = iterable.iterator();

        AspectStack fluidStack;
        do {
            if (!var3.hasNext()) {
                return null;
            }

            fluidStack = (AspectStack)var3.next();
        } while(aspectStack.getAspect() != fluidStack.getAspect());

        return fluidStack;
    }

    @Override
    public String getDisplayName(AspectStack aspectStack) {
        return aspectStack.getLocalizedName();
    }

    @Override
    public String getUniqueId(AspectStack ingredient) {
        return ingredient.tag != null ? "aspect:" + ingredient.getAspect().getTag() + ":" + ingredient.tag : "aspect:" + ingredient.getAspect().getName();

    }

    @Override
    public String getWildcardId(AspectStack ingredient) {
        return this.getUniqueId(ingredient);
    }

    @Override
    public String getModId(AspectStack ingredient) {
        Aspect aspect = ingredient.getAspect();
        if (aspect == null) {
            return "";
        } else {
            ResourceLocation fluidResourceName = aspect.getImage();
            return fluidResourceName.getNamespace();
        }
    }

    @Override
    public String getResourceId(AspectStack aspectStack) {
        Aspect aspect = aspectStack.getAspect();
        if (aspect == null) {
            return "";
        } else {
            ResourceLocation fluidResourceName = aspect.getImage();
            return fluidResourceName.getPath();
        }
    }

    @Override
    public AspectStack copyIngredient(AspectStack aspectStack) {
        return aspectStack.copy();
    }

    @Override
    public String getErrorInfo(@Nullable AspectStack ingredient) {
        if (ingredient == null) {
            return "null";
        } else {
            MoreObjects.ToStringHelper toStringHelper = MoreObjects.toStringHelper(FluidStack.class);
            Aspect fluid = ingredient.getAspect();
            if (fluid != null) {
                toStringHelper.add("Aspect", fluid.getTag());
            } else {
                toStringHelper.add("Aspect", "null");
            }

            toStringHelper.add("Amount", ingredient.amount);
            if (ingredient.tag != null) {
                toStringHelper.add("Tag", ingredient.tag);
            }

            return toStringHelper.toString();
        }
    }
}
