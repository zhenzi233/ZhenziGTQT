package com.zhenzi.zhenzigtqt.common.lib.aspect;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemAspectContainer extends Item {
    protected final int capacity;
    public ItemAspectContainer(int capacity)
    {
        this.capacity = capacity;
    }

    @Override
    public ICapabilityProvider initCapabilities(@Nonnull ItemStack stack, @Nullable NBTTagCompound nbt)
    {
        return new AspectHandlerItemStack(stack, capacity);
    }
}
