package com.zhenzi.zhenzigtqt.common.lib.aspect;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AspectHandlerItemStack implements IAspectHandlerItem, ICapabilityProvider {
    public static final String ASPECT_NBT_KEY = "Aspect";

    @Nonnull
    protected ItemStack container;
    protected int capacity;
    public AspectHandlerItemStack(@Nonnull ItemStack container, int capacity)
    {
        this.container = container;
        this.capacity = capacity;
    }

    @Nullable
    public AspectStack getAspectStack()
    {
        NBTTagCompound tagCompound = container.getTagCompound();
        if (tagCompound == null || !tagCompound.hasKey(ASPECT_NBT_KEY))
        {
            return null;
        }
        return AspectStack.loadAspectStackFromNBT(tagCompound.getCompoundTag(ASPECT_NBT_KEY));
    }

    protected void setFluid(AspectStack aspectStack)
    {
        if (!container.hasTagCompound())
        {
            container.setTagCompound(new NBTTagCompound());
        }

        NBTTagCompound fluidTag = new NBTTagCompound();
        aspectStack.writeToNBT(fluidTag);
        container.getTagCompound().setTag(ASPECT_NBT_KEY, fluidTag);
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityAspectHandler.ASPECT_HANDLER_ITEM_CAPABILITY;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityAspectHandler.ASPECT_HANDLER_ITEM_CAPABILITY ? (T) this : null;
    }

    @Override
    public IAspectTankProperties[] getTankProperties() {
        return new AspectTankProperties[] { new AspectTankProperties(this.getAspectStack(), capacity) };
    }

    @Override
    public int fill(AspectStack resource, boolean doFill) {
        if (this.container.getCount() != 1 || resource == null || resource.amount <= 0 || !this.canFillFluidType(resource))
        {
            return 0;
        }

        AspectStack contained = this.getAspectStack();
        if (contained == null)
        {
            int fillAmount = Math.min(capacity, resource.amount);

            if (doFill)
            {
                AspectStack filled = resource.copy();
                filled.amount = fillAmount;
                this.setFluid(filled);
            }

            return fillAmount;
        }
        else
        {
            if (contained.isAspectEqual(resource))
            {
                int fillAmount = Math.min(capacity - contained.amount, resource.amount);

                if (doFill && fillAmount > 0) {
                    contained.amount += fillAmount;
                    this.setFluid(contained);
                }

                return fillAmount;
            }

            return 0;
        }
    }

    @Nullable
    @Override
    public AspectStack drain(AspectStack resource, boolean doDrain) {
        if (this.container.getCount() != 1 || resource == null || resource.amount <= 0 || !resource.isAspectEqual(this.getAspectStack()))
        {
            return null;
        }
        return drain(resource.amount, doDrain);
    }

    @Nullable
    @Override
    public AspectStack drain(int maxDrain, boolean doDrain) {
        if (this.container.getCount() != 1 || maxDrain <= 0)
        {
            return null;
        }

        AspectStack contained = this.getAspectStack();
        if (contained == null || contained.amount <= 0 || !this.canDrainFluidType(contained))
        {
            return null;
        }

        final int drainAmount = Math.min(contained.amount, maxDrain);

        AspectStack drained = contained.copy();
        drained.amount = drainAmount;

        if (doDrain)
        {
            contained.amount -= drainAmount;
            if (contained.amount == 0)
            {
                this.setContainerToEmpty();
            }
            else
            {
                this.setFluid(contained);
            }
        }

        return drained;
    }

    public boolean canFillFluidType(AspectStack aspectStack)
    {
        return true;
    }

    public boolean canDrainFluidType(AspectStack aspectStack)
    {
        return true;
    }
    protected void setContainerToEmpty()
    {
        container.getTagCompound().removeTag(ASPECT_NBT_KEY);
    }

    @Nonnull
    @Override
    public ItemStack getContainer() {
        return this.container;
    }
}
