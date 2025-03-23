package com.zhenzi.zhenzigtqt.common.lib.aspect;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import thaumcraft.api.aspects.Aspect;

import javax.annotation.Nullable;

public class AspectTank implements IAspectTank, IAspectHandler{
    @Nullable
    protected AspectStack aspectStack;
    protected int capacity;
    protected TileEntity tile;
    protected boolean canFill = true;
    protected boolean canDrain = true;
    protected IAspectTankProperties[] tankProperties;

    public AspectTank(int capacity)
    {
        this(null, capacity);
    }

    public AspectTank(@Nullable AspectStack aspectStack, int capacity)
    {
        this.aspectStack = aspectStack;
        this.capacity = capacity;
    }

    public AspectTank(Aspect aspect, int amount, int capacity)
    {
        this(new AspectStack(aspect, amount), capacity);
    }

    public AspectTank readFromNBT(NBTTagCompound nbt)
    {
        if (!nbt.hasKey("Empty"))
        {
            AspectStack aspectStack = AspectStack.loadAspectStackFromNBT(nbt);
            setAspectStack(aspectStack);
        }
        else
        {
            setAspectStack(null);
        }
        return this;
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        if (aspectStack != null)
        {
            aspectStack.writeToNBT(nbt);
        }
        else
        {
            nbt.setString("Empty", "");
        }
        return nbt;
    }

    @Override
    public IAspectTankProperties[] getTankProperties() {
        if (this.tankProperties == null)
        {
            this.tankProperties = new IAspectTankProperties[] { new AspectTankPropertiesWrapper(this) };
        }
        return this.tankProperties;
    }

    @Nullable
    @Override
    public AspectStack getAspectStack() {
        return this.aspectStack;
    }

    public void setAspectStack(@Nullable AspectStack as)
    {
        this.aspectStack = as;
    }

    @Override
    public int getAspectAmount() {
        if (this.aspectStack == null)
        {
            return 0;
        }
        return this.aspectStack.amount;
    }

    @Override
    public int getCapacity() {
        return this.capacity;
    }

    public void setCapacity(int capacity)
    {
        this.capacity = capacity;
    }

    public void setTileEntity(TileEntity tile)
    {
        this.tile = tile;
    }

    @Override
    public AspectTankInfo getInfo() {
        return new AspectTankInfo(this);
    }

    @Override
    public int fill(AspectStack resource, boolean doFill) {
        if (!canFillFluidType(resource))
        {
            return 0;
        }

        return this.fillInternal(resource, doFill);
    }

    public int fillInternal(AspectStack resource, boolean doFill)
    {
        if (resource == null || resource.amount <= 0)
        {
            return 0;
        }

        if (!doFill)
        {
            if (this.aspectStack == null)
            {
                return Math.min(this.capacity, resource.amount);
            }

            if (!this.aspectStack.isAspectEqual(resource))
            {
                return 0;
            }

            return Math.min(this.capacity - this.aspectStack.amount, resource.amount);
        }

        if (this.aspectStack == null)
        {
            this.aspectStack = new AspectStack(resource, Math.min(capacity, resource.amount));

            onContentsChanged();

            if (this.tile != null)
            {
                AspectEvent.fireEvent(new AspectEvent.AspectFillingEvent(this.aspectStack, tile.getWorld(), tile.getPos(), this, this.aspectStack.amount));
            }
            return this.aspectStack.amount;
        }

        if (!this.aspectStack.isAspectEqual(resource))
        {
            return 0;
        }
        int filled = this.capacity - this.aspectStack.amount;

        if (resource.amount < filled)
        {
            this.aspectStack.amount += resource.amount;
            filled = resource.amount;
        }
        else
        {
            this.aspectStack.amount = this.capacity;
        }

        onContentsChanged();

        if (tile != null)
        {
            AspectEvent.fireEvent(new AspectEvent.AspectFillingEvent(this.aspectStack, this.tile.getWorld(), this.tile.getPos(), this, filled));
        }
        return filled;
    }

    @Nullable
    @Override
    public AspectStack drain(int maxDrain, boolean doDrain) {
        if (!canDrainFluidType(this.aspectStack))
        {
            return null;
        }
        return this.drainInternal(maxDrain, doDrain);
    }

    @Nullable
    @Override
    public AspectStack drain(AspectStack resource, boolean doDrain) {
        if (!canDrainFluidType(getAspectStack()))
        {
            return null;
        }
        return this.drainInternal(resource, doDrain);
    }

    @Nullable
    public AspectStack drainInternal(AspectStack resource, boolean doDrain)
    {
        if (resource == null || !resource.isAspectEqual(getAspectStack()))
        {
            return null;
        }
        return drainInternal(resource.amount, doDrain);
    }

    @Nullable
    public AspectStack drainInternal(int maxDrain, boolean doDrain)
    {
        if (this.aspectStack == null || maxDrain <= 0)
        {
            return null;
        }

        int drained = maxDrain;
        if (this.aspectStack.amount < drained)
        {
            drained = this.aspectStack.amount;
        }

        AspectStack stack = new AspectStack(this.aspectStack, drained);
        if (doDrain)
        {
            this.aspectStack.amount -= drained;
            if (this.aspectStack.amount <= 0)
            {
                this.aspectStack = null;
            }

            onContentsChanged();

            if (tile != null)
            {
                AspectEvent.fireEvent(new AspectEvent.AspectDrainingEvent(this.aspectStack, tile.getWorld(), tile.getPos(), this, drained));
            }
        }
        return stack;
    }

    public boolean canFill()
    {
        return this.canFill;
    }

    public boolean canDrain()
    {
        return this.canDrain;
    }

    public void setCanFill(boolean canFill)
    {
        this.canFill = canFill;
    }
    public void setCanDrain(boolean canDrain)
    {
        this.canDrain = canDrain;
    }
    public boolean canFillFluidType(AspectStack aspectStack)
    {
        return canFill();
    }
    public boolean canDrainFluidType(@Nullable AspectStack fluid)
    {
        return fluid != null && canDrain();
    }
    protected void onContentsChanged()
    {

    }
}
