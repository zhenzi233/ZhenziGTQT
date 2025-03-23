package com.zhenzi.zhenzigtqt.common.lib.aspect;

import com.google.common.collect.Lists;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.fluids.GTFluid;
import gregtech.api.recipes.FluidKey;
import gregtech.api.util.OverlayedFluidHandler;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.aspects.Aspect;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static net.minecraftforge.fluids.FluidUtil.getFluidHandler;

public class AspectUtil {
    private static final Map<Aspect, List<Supplier<List<String>>>> tooltips = new HashMap();
    @Nullable
    public static AspectStack getFluidContained(@Nonnull ItemStack container)
    {
        if (!container.isEmpty())
        {
            container = ItemHandlerHelper.copyStackWithSize(container, 1);

            IAspectHandlerItem aspectHandler = getAspectHandler(container);
            if (aspectHandler != null)
            {
                return aspectHandler.drain(Integer.MAX_VALUE, false);
            }
        }
        return null;
    }

    public static List<AspectStack> copyAspectList(List<AspectStack> fluidStacks) {
        AspectStack[] stacks = new AspectStack[fluidStacks.size()];

        for(int i = 0; i < fluidStacks.size(); ++i) {
            stacks[i] = ((AspectStack)fluidStacks.get(i)).copy();
        }

        return Lists.newArrayList(stacks);
    }

    public static List<AspectStack> aspectHandlerToList(IMultipleAspectTankHandler fluidInputs) {
        final List<IMultipleAspectTankHandler.MultiAspectTankEntry> backedList = fluidInputs.getFluidTanks();
        return new AbstractList<AspectStack>() {
            public AspectStack set(int index, AspectStack element) {
                IAspectTank fluidTank = ((IMultipleAspectTankHandler.MultiAspectTankEntry)backedList.get(index)).getDelegate();
                AspectStack oldStack = fluidTank.getAspectStack();
                if (fluidTank instanceof AspectStack) {
                    ((AspectTank)fluidTank).setAspectStack(element);
                }

                return oldStack;
            }

            public AspectStack get(int index) {
                return ((IMultipleAspectTankHandler.MultiAspectTankEntry)backedList.get(index)).getAspectStack();
            }

            public int size() {
                return backedList.size();
            }
        };
    }

    public static @org.jetbrains.annotations.Nullable AspectStack getFluidFromContainer(Object ingredient) {
        if (ingredient instanceof AspectStack) {
            return (AspectStack)ingredient;
        } else {
            if (ingredient instanceof ItemStack) {
                ItemStack itemStack = (ItemStack)ingredient;
                IAspectHandlerItem fluidHandler = itemStack.getCapability(CapabilityAspectHandler.ASPECT_HANDLER_ITEM_CAPABILITY, (EnumFacing)null);
                if (fluidHandler != null) {
                    return fluidHandler.drain(Integer.MAX_VALUE, false);
                }
            }

            return null;
        }
    }

    @Contract("null -> null")
    public static TextComponentTranslation getFluidTranslation(@org.jetbrains.annotations.Nullable AspectStack stack) {
        if (stack == null) {
            return null;
        } else {
            Aspect var2 = stack.getAspect();
            return new TextComponentTranslation(stack.getUnlocalizedName());
        }
    }

    @Nullable
    public static IAspectHandlerItem getAspectHandler(@Nonnull ItemStack itemStack)
    {
        if (itemStack.hasCapability(CapabilityAspectHandler.ASPECT_HANDLER_ITEM_CAPABILITY, null))
        {
            return itemStack.getCapability(CapabilityAspectHandler.ASPECT_HANDLER_ITEM_CAPABILITY, null);
        }
        else
        {
            return null;
        }
    }

    @Nullable
    private static AspectStack tryAspectTransfer_Internal(IAspectHandler fluidDestination, IAspectHandler fluidSource, AspectStack drainable, boolean doTransfer)
    {
        int fillableAmount = fluidDestination.fill(drainable, false);
        if (fillableAmount > 0)
        {
            if (doTransfer)
            {
                AspectStack drained = fluidSource.drain(fillableAmount, true);
                if (drained != null)
                {
                    drained.amount = fluidDestination.fill(drained, true);
                    return drained;
                }
            }
            else
            {
                drainable.amount = fillableAmount;
                return drainable;
            }
        }
        return null;
    }

    @Nullable
    public static AspectStack tryAspectTransfer(IAspectHandler fluidDestination, IAspectHandler fluidSource, AspectStack resource, boolean doTransfer)
    {
        AspectStack drainable = fluidSource.drain(resource, false);
        if (drainable != null && drainable.amount > 0 && resource.isAspectEqual(drainable))
        {
            return tryAspectTransfer_Internal(fluidDestination, fluidSource, drainable, doTransfer);
        }
        return null;
    }

    @Nullable
    public static AspectStack tryAspectTransfer(IAspectHandler fluidDestination, IAspectHandler fluidSource, int maxAmount, boolean doTransfer)
    {
        AspectStack drainable = fluidSource.drain(maxAmount, false);
        if (drainable != null && drainable.amount > 0)
        {
            return tryAspectTransfer_Internal(fluidDestination, fluidSource, drainable, doTransfer);
        }
        return null;
    }

    @Nonnull
    public static AspectActionResult tryFillContainer(@Nonnull ItemStack container, IAspectHandler fluidSource, int maxAmount, @Nullable EntityPlayer player, boolean doFill)
    {
        ItemStack containerCopy = ItemHandlerHelper.copyStackWithSize(container, 1); // do not modify the input
        IAspectHandlerItem containerFluidHandler = getAspectHandler(containerCopy);
        if (containerFluidHandler != null)
        {
            AspectStack simulatedTransfer = tryAspectTransfer(containerFluidHandler, fluidSource, maxAmount, false);
            if (simulatedTransfer != null)
            {
                if (doFill)
                {
                    tryAspectTransfer(containerFluidHandler, fluidSource, maxAmount, true);
                }
                else
                {
                    containerFluidHandler.fill(simulatedTransfer, true);
                }

                ItemStack resultContainer = containerFluidHandler.getContainer();
                return new AspectActionResult(resultContainer);
            }
        }
        return AspectActionResult.FAILURE;
    }

    @Nonnull
    public static AspectActionResult tryEmptyContainer(@Nonnull ItemStack container, IAspectHandler fluidDestination, int maxAmount, @Nullable EntityPlayer player, boolean doDrain)
    {
        ItemStack containerCopy = ItemHandlerHelper.copyStackWithSize(container, 1); // do not modify the input
        IAspectHandlerItem containerFluidHandler = getAspectHandler(containerCopy);
        if (containerFluidHandler != null)
        {
            if (doDrain)
            {
                AspectStack transfer = tryAspectTransfer(fluidDestination, containerFluidHandler, maxAmount, true);
                if (transfer != null)
                {
                    ItemStack resultContainer = containerFluidHandler.getContainer();
                    return new AspectActionResult(resultContainer);
                }
            }
            else
            {
                AspectStack simulatedTransfer = tryAspectTransfer(fluidDestination, containerFluidHandler, maxAmount, false);
                if (simulatedTransfer != null)
                {
                    containerFluidHandler.drain(simulatedTransfer, true);
                    ItemStack resultContainer = containerFluidHandler.getContainer();
                    return new AspectActionResult(resultContainer);
                }
            }
        }
        return AspectActionResult.FAILURE;
    }

    public static int transferAspects(@NotNull IAspectHandler sourceHandler, @NotNull IAspectHandler destHandler) {
        return transferAspects(sourceHandler, destHandler, Integer.MAX_VALUE, (fluidStack) -> {
            return true;
        });
    }

    public static int transferAspects(@NotNull IAspectHandler sourceHandler, @NotNull IAspectHandler destHandler, int transferLimit, @NotNull Predicate<AspectStack> fluidFilter) {
        int fluidLeftToTransfer = transferLimit;
        IAspectTankProperties[] var5 = sourceHandler.getTankProperties();
        int var6 = var5.length;

        for(int var7 = 0; var7 < var6; ++var7) {
            IAspectTankProperties tankProperties = var5[var7];
            AspectStack currentFluid = tankProperties.getContents();
            if (currentFluid != null && currentFluid.amount != 0 && fluidFilter.test(currentFluid)) {
                currentFluid.amount = fluidLeftToTransfer;
                AspectStack fluidStack = sourceHandler.drain(currentFluid, false);
                if (fluidStack != null && fluidStack.amount != 0) {
                    int canInsertAmount = destHandler.fill(fluidStack, false);
                    if (canInsertAmount > 0) {
                        fluidStack.amount = canInsertAmount;
                        fluidStack = sourceHandler.drain(fluidStack, true);
                        if (fluidStack != null && fluidStack.amount > 0) {
                            destHandler.fill(fluidStack, true);
                            fluidLeftToTransfer -= fluidStack.amount;
                            if (fluidLeftToTransfer == 0) {
                                break;
                            }
                        }
                    }
                }
            }
        }

        return transferLimit - fluidLeftToTransfer;
    }

    public static boolean addAspectsToAspectHandler(IMultipleAspectTankHandler fluidHandler, boolean simulate, List<AspectStack> fluidStacks) {
        if (simulate) {
            OverlayedAspectHandler overlayedFluidHandler = new OverlayedAspectHandler(fluidHandler);
            Iterator var8 = fluidStacks.iterator();

            AspectStack fluidStack;
            int inserted;
            do {
                if (!var8.hasNext()) {
                    return true;
                }

                fluidStack = (AspectStack)var8.next();
                inserted = overlayedFluidHandler.insertFluid(fluidStack, fluidStack.amount);
            } while(inserted == fluidStack.amount);

            return false;
        } else {

            for (AspectStack stack : fluidStacks) {
                AspectStack fluidStack = stack;
                fluidHandler.fill(fluidStack, true);
            }

            return true;
        }
    }

    public static Map<AspectKey, Integer> fromFluidHandler(IAspectHandler fluidInputs) {
        Object2IntMap<AspectKey> map = new Object2IntLinkedOpenHashMap();

        for(int i = 0; i < fluidInputs.getTankProperties().length; ++i) {
            AspectStack fluidStack = fluidInputs.getTankProperties()[i].getContents();
            if (fluidStack != null && fluidStack.amount > 0) {
                AspectKey key = new AspectKey(fluidStack);
                map.put(key, map.getInt(key) + fluidStack.amount);
            }
        }

        return map;
    }

//    public static List<String> getAspectTooltip(Aspect aspect) {
//        if (aspect == null) {
//            return null;
//        } else {
//            List<Supplier<List<String>>> list = (List)tooltips.get(aspect);
//            if (list == null) {
//                return Collections.emptyList();
//            } else {
//                List<String> tooltip = new ArrayList();
//                Iterator var3 = list.iterator();
//
//                while(var3.hasNext()) {
//                    Supplier<List<String>> supplier = (Supplier)var3.next();
//                    tooltip.addAll((Collection)supplier.get());
//                }
//
//                return tooltip;
//            }
//        }
//    }
}
