package com.zhenzi.zhenzigtqt.common.lib.aspect;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidActionResult;

import javax.annotation.Nonnull;

public class AspectActionResult {
    public static final AspectActionResult FAILURE = new AspectActionResult(false, ItemStack.EMPTY);

    public final boolean success;
    @Nonnull
    public final ItemStack result;

    public AspectActionResult(@Nonnull ItemStack result)
    {
        this(true, result);
    }

    private AspectActionResult(boolean success, @Nonnull ItemStack result)
    {
        this.success = success;
        this.result = result;
    }

    public boolean isSuccess()
    {
        return success;
    }

    @Nonnull
    public ItemStack getResult()
    {
        return result;
    }
}
