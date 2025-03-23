package com.zhenzi.zhenzigtqt.integration.jei;

import com.zhenzi.zhenzigtqt.common.lib.aspect.AspectStack;
import mezz.jei.api.gui.IGuiIngredientGroup;
import mezz.jei.api.gui.ITooltipCallback;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

public interface IGuiAspectStackGroup extends IGuiIngredientGroup<AspectStack> {
    void init(int var1, boolean var2, int var3, int var4);

    void set(int var1, @Nullable AspectStack var2);

    void addTooltipCallback(ITooltipCallback<AspectStack> var1);
}
