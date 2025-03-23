package com.zhenzi.zhenzigtqt.common.lib.aspect;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.FMLLog;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

import javax.annotation.Nullable;
import java.util.*;

public class AspectStack {
    public int amount;
    public NBTTagCompound tag;
    public Aspect aspect;

    public AspectStack(Aspect aspect, int amount)
    {
        if (aspect == null)
        {
            FMLLog.bigWarning("Null aspect supplied to aspctstack. Did you try and create a stack for an unregistered aspect?");
            throw new IllegalArgumentException("Cannot create a aspctstack from a null aspect");
        }
        this.aspect = aspect;
        this.amount = amount;
    }

    public static List<AspectStack> aspectListToStacks (AspectList aspectList)
    {
        List<AspectStack> list = new ArrayList<>();
        for (Map.Entry<Aspect, Integer> entry : aspectList.aspects.entrySet())
        {
            list.add(new AspectStack(entry.getKey(), entry.getValue()));
        }
        return list;
    }

    public static AspectList stacksToAspectList(List<AspectStack> aspectStacks)
    {
        AspectList aspectList = new AspectList();
        for (AspectStack aspectStack : aspectStacks)
        {
            aspectList.add(aspectStack.getAspect(), aspectStack.amount);
        }
        return aspectList;
    }

    public static Collection<AspectStack> getAllAspects()
    {
        Collection<AspectStack> collection = new ArrayList<>();
        for (Map.Entry<String, Aspect> aspect : Aspect.aspects.entrySet())
        {
            collection.add(new AspectStack(aspect.getValue(), 10));
        }
        return collection;
    }

    public AspectStack(AspectStack stack, int amount)
    {
        this(stack.getAspect(), amount);
    }

    public static AspectStack loadAspectStackFromNBT(NBTTagCompound nbt)
    {
        if (nbt == null)
        {
            return null;
        }
        if (!nbt.hasKey("AspectName", Constants.NBT.TAG_STRING))
        {
            return null;
        }

        String aspectName = nbt.getString("AspectName");
        if (Aspect.getAspect(aspectName) == null)
        {
            return null;
        }

        return new AspectStack(Aspect.getAspect(aspectName), nbt.getInteger("Amount"));
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        nbt.setString("AspectName", getAspect().getTag());
        nbt.setInteger("Amount", amount);
//        if (tag != null)
//        {
//            nbt.setTag("Tag", tag);
//        }
        return nbt;
    }

    public final Aspect getAspect()
    {
        return this.aspect;
    }

    public String getLocalizedName()
    {
        return this.getAspect().getLocalizedDescription();
    }

    public String getUnlocalizedName()
    {
        return this.getAspect().getName();
    }

    public AspectStack copy()
    {
        return new AspectStack(getAspect(), amount);
    }

    public boolean isAspectEqual(@Nullable AspectStack other)
    {
        return other != null && Objects.equals(getAspect().getTag(), other.getAspect().getTag());
    }

    public boolean containsAspect(@Nullable AspectStack other)
    {
        return isAspectEqual(other) && amount >= other.amount;
    }

    public boolean isAspectStackIdentical(AspectStack other)
    {
        return isAspectEqual(other) && amount == other.amount;
    }

    public boolean isAspectEqual(ItemStack other)
    {
        if (other == null)
        {
            return false;
        }

        return isAspectEqual(AspectUtil.getFluidContained(other));
    }

    public static boolean areAspectStackTagsEqual(@Nullable AspectStack stack1, @Nullable AspectStack stack2)
    {
        return stack1 == null && stack2 == null || (stack1 != null && stack2 != null && stack1.isAspectEqual(stack2));
    }

    @Override
    public final boolean equals(Object o)
    {
        if (!(o instanceof AspectStack))
        {
            return false;
        }

        return isAspectEqual((AspectStack) o);
    }
}
