package com.zhenzi.zhenzigtqt.common.lib.aspect;

import gregtech.api.gui.widgets.SlotWidget;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandlerModifiable;

public class AspectContainerSlotWidget extends SlotWidget {
    private final boolean requireFilledContainer;

    public AspectContainerSlotWidget(IItemHandlerModifiable itemHandler, int slotIndex, int xPosition, int yPosition, boolean requireFilledContainer) {
        super(itemHandler, slotIndex, xPosition, yPosition, true, true);
        this.requireFilledContainer = requireFilledContainer;
    }

    public boolean canPutStack(ItemStack stack) {
        IAspectHandlerItem fluidHandlerItem = AspectUtil.getAspectHandler(stack);
        return fluidHandlerItem != null && (!this.requireFilledContainer || fluidHandlerItem.getTankProperties()[0].getContents() != null);
    }
}
