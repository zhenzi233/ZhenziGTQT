package com.zhenzi.zhenzigtqt.integration;

import com.zhenzi.zhenzigtqt.integration.jei.IGuiAspectStackGroup;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IRecipeLayout;

public interface IZZRecipeLayout extends IRecipeLayout {
    IGuiAspectStackGroup getAspectStacks();
}
