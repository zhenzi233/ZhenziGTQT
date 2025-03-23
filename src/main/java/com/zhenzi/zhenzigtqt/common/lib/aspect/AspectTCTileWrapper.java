//package com.zhenzi.zhenzigtqt.common.lib.aspect;
//
//import net.minecraft.item.Item;
//import net.minecraft.item.ItemStack;
//import net.minecraft.nbt.NBTTagCompound;
//import net.minecraft.nbt.NBTTagList;
//import net.minecraft.tileentity.TileEntity;
//import net.minecraft.util.EnumFacing;
//import net.minecraftforge.common.capabilities.Capability;
//import net.minecraftforge.common.capabilities.ICapabilityProvider;
//import org.jetbrains.annotations.NotNull;
//import thaumcraft.api.aspects.Aspect;
//import thaumcraft.api.aspects.AspectList;
//import thaumcraft.api.blocks.BlocksTC;
//import thaumcraft.api.items.ItemsTC;
//import thaumcraft.common.blocks.essentia.BlockJarItem;
//import thaumcraft.common.items.consumables.ItemPhial;
//import thaumcraft.common.tiles.essentia.TileSmelter;
//
//import javax.annotation.Nonnull;
//import javax.annotation.Nullable;
//import java.util.ArrayList;
//import java.util.List;
//
//public class AspectTCTileWrapper implements IAspectHandler, ICapabilityProvider {
//    @Nonnull
//    protected TileEntity container;
//
//    public AspectTCTileWrapper(@Nonnull TileEntity container)
//    {
//        this.container = container;
//    }
//
//    public @NotNull TileEntity getContainer()
//    {
//        return container;
//    }
//
//    public List<AspectStack> getAspectStacks()
//    {
//        List<AspectStack> list = new ArrayList<>();
//        if (container instanceof TileSmelter)
//        {
//            AspectList aspectList = ((TileSmelter) container).aspects;
//            if (aspectList.size() != 0)
//            {
//                for (Aspect aspect : aspectList.getAspects())
//                {
//                    AspectStack aspectStack = new AspectStack(aspect, aspectList.getAmount(aspect));
//                    list.add(aspectStack);
//                }
//            }
//        }
//        return list;
//    }
////    @Nullable
////    public AspectStack getAspectStack()
////    {
//////        Item item = container.getItem();
////        if (container instanceof TileSmelter)
////        {
////            AspectList aspectList = ((TileSmelter) container).aspects;
////            if (aspectList.size() != 0)
////            {
////                aspectList.
////                NBTTagList nbta = nbtTagCompound.getTagList("Aspects", 10);
////                NBTTagCompound a = null;
////                a = (NBTTagCompound) nbta.get(0);
////                int amount = a.getInteger("amount");
////                String key = a.getString("key");
////                Aspect aspect = Aspect.getAspect(key);
////                return new AspectStack(aspect, amount);
////            }
////        }
////        else if (item instanceof BlockJarItem)
////        {
////            NBTTagCompound nbtTagCompound = container.getTagCompound();
////            if (nbtTagCompound != null)
////            {
////                NBTTagList nbta = nbtTagCompound.getTagList("Aspects", 10);
////                NBTTagCompound a = null;
////                a = (NBTTagCompound) nbta.get(0);
////                int amount = a.getInteger("amount");
////                String key = a.getString("key");
////                Aspect aspect = Aspect.getAspect(key);
////                return new AspectStack(aspect, amount);
////            }
////        }
////        return null;
////    }
//
////    protected void setAspect(@Nullable AspectStack fluidStack)
////    {
////        if (fluidStack == null)
////        {
////            if (container.getItem() instanceof ItemPhial)
////            {
////                container = new ItemStack(ItemsTC.phial, 1);
////            }   else if (container.getItem() instanceof BlockJarItem)
////            {
////                container = new ItemStack(BlocksTC.jarNormal, 1);
////            }
////        }   else
////        {
////            if (container.getItem() instanceof ItemPhial)
////            {
////                container = ItemPhial.makePhial(fluidStack.getAspect(), 10);
////            }   else if (container.getItem() instanceof BlockJarItem)
////            {
////                ItemStack newItem = container;
////                ((BlockJarItem) newItem.getItem()).setAspects(newItem, (new AspectList()).add(fluidStack.getAspect(), 250));
////                container = new ItemStack(BlocksTC.jarNormal, 1);
////            }
////        }
////    }
////
////    @Override
////    public IAspectTankProperties[] getTankProperties()
////    {
////        if (container.getItem() instanceof ItemPhial)
////        {
////            return new AspectTankProperties[] { new AspectTankProperties(getAspectStack(), 10) };
////        }   else
////        if (container.getItem() instanceof BlockJarItem)
////        {
////            return new AspectTankProperties[] { new AspectTankProperties(getAspectStack(), 250) };
////        }
////        return null;
////    }
////
////    @Override
////    public int fill(AspectStack resource, boolean doFill)
////    {
////        if (container.getItem() instanceof ItemPhial)
////        {
////            if (container.getCount() != 1 || resource == null || resource.amount < 10 || getAspectStack() != null)
////            {
////                return 0;
////            }
////        }
////        if (container.getItem() instanceof BlockJarItem)
////        {
////            if (container.getCount() != 1 || resource == null || resource.amount < 250 || getAspectStack() != null)
////            {
////                return 0;
////            }
////        }
////
////        if (doFill)
////        {
////            setAspect(resource);
////        }
////
////        if (container.getItem() instanceof ItemPhial) return 10;
////        if (container.getItem() instanceof BlockJarItem) return 250;
////        return 1;
////    }
////
////    @Nullable
////    @Override
////    public AspectStack drain(AspectStack resource, boolean doDrain)
////    {
////        if (container.getItem() instanceof ItemPhial)
////        {
////            if (container.getCount() != 1 || resource == null || resource.amount < 10)
////            {
////                return null;
////            }
////        }
////        if (container.getItem() instanceof BlockJarItem)
////        {
////            if (container.getCount() != 1 || resource == null || resource.amount < 250)
////            {
////                return null;
////            }
////        }
////
////        AspectStack fluidStack = getAspectStack();
////        if (fluidStack != null && fluidStack.isAspectEqual(resource))
////        {
////            if (doDrain)
////            {
////                setAspect(null);
////            }
////            return fluidStack;
////        }
////
////        return null;
////    }
////
////    @Nullable
////    @Override
////    public AspectStack drain(int maxDrain, boolean doDrain)
////    {
////        if (container.getItem() instanceof ItemPhial)
////        {
////            if (container.getCount() != 1 || maxDrain < 10)
////            {
////                return null;
////            }
////        }
////        if (container.getItem() instanceof BlockJarItem)
////        {
////            if (container.getCount() != 1 || maxDrain < 250)
////            {
////                return null;
////            }
////        }
////
////        AspectStack fluidStack = getAspectStack();
////        if (fluidStack != null)
////        {
////            if (doDrain)
////            {
////                setAspect(null);
////            }
////            return fluidStack;
////        }
////
////        return null;
////    }
////
////    @Override
////    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
////    {
////        return capability == CapabilityAspectHandler.ASPECT_HANDLER_ITEM_CAPABILITY;
////    }
////
////    @Override
////    @Nullable
////    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
////    {
////        if (capability == CapabilityAspectHandler.ASPECT_HANDLER_ITEM_CAPABILITY)
////        {
////            return CapabilityAspectHandler.ASPECT_HANDLER_ITEM_CAPABILITY.cast(this);
////        }
////        return null;
////    }
//}
