package com.zhenzi.zhenzigtqt.common.lib.aspect;

import com.zhenzi.zhenzigtqt.loaders.AbstractAspectRecipeLogic;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.capability.impl.EUToFEProvider;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import thaumcraft.api.items.ItemsTC;

public class CapabilityAspectHandler {
    @CapabilityInject(IAspectHandler.class)
    public static Capability<IAspectHandler> ASPECT_HANDLER_CAPABILITY = null;
    @CapabilityInject(IAspectHandlerItem.class)
    public static Capability<IAspectHandlerItem> ASPECT_HANDLER_ITEM_CAPABILITY = null;

    @CapabilityInject(AbstractAspectRecipeLogic.class)
    public static Capability<AbstractAspectRecipeLogic> CAPABILITY_ASPECT_RECIPE_LOGIC = null;

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IAspectHandler.class, new CapabilityAspectHandler.DefaultAspectHandlerStorage<>(), () -> new AspectTank(250));

        CapabilityManager.INSTANCE.register(IAspectHandlerItem.class, new CapabilityAspectHandler.DefaultAspectHandlerStorage<>(), () -> new AspectHandlerItemStack(new ItemStack(ItemsTC.phial), 10));
    }

    private static class DefaultAspectHandlerStorage<T extends IAspectHandler> implements Capability.IStorage<T> {
        @Override
        public NBTBase writeNBT(Capability<T> capability, T instance, EnumFacing side)
        {
            if (!(instance instanceof IAspectTank))
                throw new RuntimeException("IAspectTank instance does not implement IAspectTank");
            NBTTagCompound nbt = new NBTTagCompound();
            IAspectTank tank = (IAspectTank) instance;
            AspectStack aspectStack = tank.getAspectStack();
            if (aspectStack != null)
            {
                aspectStack.writeToNBT(nbt);
            }
            else
            {
                nbt.setString("Empty", "");
            }
            nbt.setInteger("Capacity", tank.getCapacity());
            return nbt;
        }

        @Override
        public void readNBT(Capability<T> capability, T instance, EnumFacing side, NBTBase nbt)
        {
            if (!(instance instanceof AspectTank))
                throw new RuntimeException("IFluidHandler instance is not instance of FluidTank");
            NBTTagCompound tags = (NBTTagCompound) nbt;
            AspectTank tank = (AspectTank) instance;
            tank.setCapacity(tags.getInteger("Capacity"));
            tank.readFromNBT(tags);
        }
    }
}
