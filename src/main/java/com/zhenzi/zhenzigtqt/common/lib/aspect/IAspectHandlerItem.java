package com.zhenzi.zhenzigtqt.common.lib.aspect;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public interface IAspectHandlerItem extends IAspectHandler{
    @Nonnull
    ItemStack getContainer();
}
