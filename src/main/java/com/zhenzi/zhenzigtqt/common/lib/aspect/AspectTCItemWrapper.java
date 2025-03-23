package com.zhenzi.zhenzigtqt.common.lib.aspect;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBucketMilk;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.Packet;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.common.blocks.essentia.BlockJarItem;
import thaumcraft.common.items.consumables.ItemPhial;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AspectTCItemWrapper implements IAspectHandlerItem, ICapabilityProvider {
    @Nonnull
    protected ItemStack container;

    public AspectTCItemWrapper(@Nonnull ItemStack container)
    {
        this.container = container;
    }

    @Nonnull
    @Override
    public ItemStack getContainer()
    {
        return container;
    }

    @Nullable
    public AspectStack getAspectStack()
    {
        Item item = container.getItem();
        if (item instanceof ItemPhial)
        {
            NBTTagCompound nbtTagCompound = container.getTagCompound();
            if (nbtTagCompound != null)
            {
                NBTTagList nbta = nbtTagCompound.getTagList("Aspects", 10);
                NBTTagCompound a = null;
                a = (NBTTagCompound) nbta.get(0);
                int amount = a.getInteger("amount");
                String key = a.getString("key");
                Aspect aspect = Aspect.getAspect(key);
                return new AspectStack(aspect, amount);
            }
        }
        else if (item instanceof BlockJarItem)
        {
            NBTTagCompound nbtTagCompound = container.getTagCompound();
            if (nbtTagCompound != null)
            {
                NBTTagList nbta = nbtTagCompound.getTagList("Aspects", 10);
                NBTTagCompound a = null;
                a = (NBTTagCompound) nbta.get(0);
                int amount = a.getInteger("amount");
                String key = a.getString("key");
                Aspect aspect = Aspect.getAspect(key);
                return new AspectStack(aspect, amount);
            }
        }
        return null;
    }

    protected void setAspect(@Nullable AspectStack fluidStack)
    {
        if (fluidStack == null)
        {
            if (container.getItem() instanceof ItemPhial)
            {
                container = new ItemStack(ItemsTC.phial, 1);
            }   else if (container.getItem() instanceof BlockJarItem)
            {
                container = new ItemStack(BlocksTC.jarNormal, 1);
            }
        }   else
        {
            if (container.getItem() instanceof ItemPhial)
            {
                container = ItemPhial.makePhial(fluidStack.getAspect(), 10);
            }   else if (container.getItem() instanceof BlockJarItem)
            {
                ItemStack newItem = container;
                ((BlockJarItem) newItem.getItem()).setAspects(newItem, (new AspectList()).add(fluidStack.getAspect(), Math.min(fluidStack.amount, 250)));
                container = newItem;
            }
        }
    }

    @Override
    public IAspectTankProperties[] getTankProperties()
    {
        if (container.getItem() instanceof ItemPhial)
        {
            return new AspectTankProperties[] { new AspectTankProperties(getAspectStack(), 10) };
        }   else
        if (container.getItem() instanceof BlockJarItem)
        {
            return new AspectTankProperties[] { new AspectTankProperties(getAspectStack(), 250) };
        }
        return null;
    }

    @Override
    public int fill(AspectStack resource, boolean doFill)
    {
        if (container.getItem() instanceof ItemPhial)
        {
            if (container.getCount() != 1 || resource == null || resource.amount < 10 || getAspectStack() != null)
            {
                return 0;
            }
        }
        if (container.getItem() instanceof BlockJarItem)
        {
            if (container.getCount() != 1 || resource == null || resource.amount == 0 || getAspectStack() != null)
            {
                return 0;
            }
        }

        if (doFill)
        {
            setAspect(resource);
        }

        if (container.getItem() instanceof ItemPhial) return 10;
        if (container.getItem() instanceof BlockJarItem) return Math.min(resource.amount, 250);
        return 1;
    }

    @Nullable
    @Override
    public AspectStack drain(AspectStack resource, boolean doDrain)
    {
        if (container.getItem() instanceof ItemPhial)
        {
            if (container.getCount() != 1 || resource == null || resource.amount < 10)
            {
                return null;
            }
        }
        if (container.getItem() instanceof BlockJarItem)
        {
            if (container.getCount() != 1 || resource == null || resource.amount == 0)
            {
                return null;
            }
        }

        AspectStack fluidStack = getAspectStack();
        if (fluidStack != null && fluidStack.isAspectEqual(resource))
        {
            if (doDrain)
            {
                setAspect(null);
            }
            return fluidStack;
        }

        return null;
    }

    @Nullable
    @Override
    public AspectStack drain(int maxDrain, boolean doDrain)
    {
        if (container.getItem() instanceof ItemPhial)
        {
            if (container.getCount() != 1 || maxDrain < 10)
            {
                return null;
            }
        }
        if (container.getItem() instanceof BlockJarItem)
        {
            if (container.getCount() != 1 || maxDrain == 0)
            {
                return null;
            }
        }

        AspectStack fluidStack = getAspectStack();
        if (fluidStack != null)
        {
            if (doDrain)
            {
                setAspect(null);
            }
            return fluidStack;
        }

        return null;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
    {
        return capability == CapabilityAspectHandler.ASPECT_HANDLER_ITEM_CAPABILITY;
    }

    @Override
    @Nullable
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
    {
        if (capability == CapabilityAspectHandler.ASPECT_HANDLER_ITEM_CAPABILITY)
        {
            return CapabilityAspectHandler.ASPECT_HANDLER_ITEM_CAPABILITY.cast(this);
        }
        return null;
    }
}
