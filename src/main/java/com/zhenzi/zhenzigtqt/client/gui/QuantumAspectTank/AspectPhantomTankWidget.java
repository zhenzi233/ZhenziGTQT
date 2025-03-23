package com.zhenzi.zhenzigtqt.client.gui.QuantumAspectTank;

import com.google.common.collect.Lists;
import com.zhenzi.zhenzigtqt.common.lib.aspect.*;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.fluids.GTFluid;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.ingredient.IGhostIngredientTarget;
import gregtech.api.gui.widgets.PhantomTankWidget;
import gregtech.api.util.GTUtility;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.client.utils.RenderUtil;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;

import java.awt.*;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AspectPhantomTankWidget extends AspectTankWidget implements IGhostIngredientTarget {
    private final Supplier<AspectStack> phantomFluidGetter;
    private final Consumer<AspectStack> phantomFluidSetter;
    protected @Nullable AspectStack lastPhantomStack;

    public AspectPhantomTankWidget(IAspectTank fluidTank, int x, int y, int width, int height, Supplier<AspectStack> phantomFluidGetter, Consumer<AspectStack> phantomFluidSetter) {
        super(fluidTank, x, y, width, height);
        this.phantomFluidGetter = phantomFluidGetter;
        this.phantomFluidSetter = phantomFluidSetter;
        this.setLastPhantomStack(this.phantomFluidGetter.get());
    }

    protected void setLastPhantomStack(AspectStack fluid) {
        if (fluid != null) {
            this.lastPhantomStack = fluid.copy();
            this.lastPhantomStack.amount = 1;
        } else {
            this.lastPhantomStack = null;
        }

    }

    public List<IGhostIngredientHandler.Target<?>> getPhantomTargets(Object ingredient) {
        if (this.lastFluidInTank == null && GTUtility.getFluidFromContainer(ingredient) != null) {
            final Rectangle rectangle = this.toRectangleBox();
            return Lists.newArrayList(new IGhostIngredientHandler.Target[]{new IGhostIngredientHandler.Target<Object>() {
                public @NotNull Rectangle getArea() {
                    return rectangle;
                }

                public void accept(@NotNull Object ingredient) {
                    AspectStack stack = AspectUtil.getFluidFromContainer(ingredient);
                    if (stack != null) {
                        NBTTagCompound compound = stack.writeToNBT(new NBTTagCompound());
                        AspectPhantomTankWidget.this.writeClientAction(GregtechDataCodes.LOAD_PHANTOM_FLUID_STACK_FROM_NBT, (buf) -> {
                            buf.writeCompoundTag(compound);
                        });
                    }

                    AspectPhantomTankWidget.this.phantomFluidSetter.accept(stack);
                }
            }});
        } else {
            return Collections.emptyList();
        }
    }

    public void handleClientAction(int id, PacketBuffer buf) {
        if (id == GregtechDataCodes.SET_PHANTOM_FLUID) {
            ItemStack stack = this.gui.entityPlayer.inventory.getItemStack().copy();
            if (stack.isEmpty()) {
                this.phantomFluidSetter.accept(null);
            } else {
                stack.setCount(1);
                IAspectHandlerItem fluidHandler = stack.getCapability(CapabilityAspectHandler.ASPECT_HANDLER_ITEM_CAPABILITY, (EnumFacing)null);
                if (fluidHandler != null) {
                    this.phantomFluidSetter.accept(fluidHandler.drain(Integer.MAX_VALUE, false));
                }
            }
        } else if (id == GregtechDataCodes.LOAD_PHANTOM_FLUID_STACK_FROM_NBT) {
            AspectStack stack;
            try {
                stack = AspectStack.loadAspectStackFromNBT(buf.readCompoundTag());
            } catch (IOException var5) {
                throw new RuntimeException(var5);
            }

            this.phantomFluidSetter.accept(stack);
        } else {
            super.handleClientAction(id, buf);
        }

    }

    public Object getIngredientOverMouse(int mouseX, int mouseY) {
        if (this.isMouseOverElement(mouseX, mouseY)) {
            return this.lastFluidInTank == null ? this.phantomFluidGetter.get() : this.lastFluidInTank;
        } else {
            return null;
        }
    }

    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (this.isMouseOverElement(mouseX, mouseY)) {
            this.writeClientAction(GregtechDataCodes.SET_PHANTOM_FLUID, (buf) -> {
            });
            return true;
        } else {
            return false;
        }
    }

    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        if (this.lastFluidInTank != null) {
            super.drawInBackground(mouseX, mouseY, partialTicks, context);
        } else {
            Position pos = this.getPosition();
            Size size = this.getSize();
            AspectStack fluid = this.phantomFluidGetter.get();
            if (fluid != null && !this.gui.isJEIHandled) {
                GlStateManager.disableBlend();
                super.drawAspectForGui(fluid, pos.x + this.fluidRenderOffset, pos.y + this.fluidRenderOffset, size.width - this.fluidRenderOffset, size.height - this.fluidRenderOffset);
                GlStateManager.enableBlend();
            }

        }
    }

    public void drawInForeground(int mouseX, int mouseY) {
        if (this.lastFluidInTank != null) {
            super.drawInForeground(mouseX, mouseY);
        }
    }

    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        AspectStack stack = this.phantomFluidGetter.get();
        if (stack == null) {
            if (this.lastPhantomStack != null) {
                this.setLastPhantomStack(null);
                this.writeUpdateInfo(GregtechDataCodes.REMOVE_PHANTOM_FLUID_TYPE, (buf) -> {
                });
            }
        } else if (this.lastPhantomStack == null || !stack.isAspectEqual(this.lastPhantomStack)) {
            this.setLastPhantomStack(stack);
            NBTTagCompound stackTag = stack.writeToNBT(new NBTTagCompound());
            this.writeUpdateInfo(GregtechDataCodes.CHANGE_PHANTOM_FLUID, (buf) -> {
                buf.writeCompoundTag(stackTag);
            });
        }

    }

    public void readUpdateInfo(int id, PacketBuffer buf) {
        if (id == GregtechDataCodes.REMOVE_PHANTOM_FLUID_TYPE) {
            this.phantomFluidSetter.accept(null);
        } else if (id == GregtechDataCodes.CHANGE_PHANTOM_FLUID) {
            NBTTagCompound stackTag;
            try {
                stackTag = buf.readCompoundTag();
            } catch (IOException var5) {
                return;
            }

            this.phantomFluidSetter.accept(AspectStack.loadAspectStackFromNBT(stackTag));
        } else {
            super.readUpdateInfo(id, buf);
        }

    }

    public String getFluidLocalizedName() {
        if (this.lastFluidInTank != null) {
            return this.lastFluidInTank.getLocalizedName();
        } else {
            AspectStack fluid = this.phantomFluidGetter.get();
            return fluid == null ? "" : fluid.getLocalizedName();
        }
    }

    public @Nullable TextComponentTranslation getFluidTextComponent() {
        if (this.lastFluidInTank != null) {
            Aspect var2 = this.lastFluidInTank.getAspect();
//            if (var2 instanceof GTFluid.GTMaterialFluid) {
//                GTFluid.GTMaterialFluid materialFluid = (GTFluid.GTMaterialFluid)var2;
//                return materialFluid.toTextComponentTranslation();
//            }
        }

        AspectStack stack = this.phantomFluidGetter.get();
        if (stack == null) {
            return null;
        } else {
            return new TextComponentTranslation(stack.getUnlocalizedName());
        }
    }
}
