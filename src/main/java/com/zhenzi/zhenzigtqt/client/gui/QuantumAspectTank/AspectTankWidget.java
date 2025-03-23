package com.zhenzi.zhenzigtqt.client.gui.QuantumAspectTank;

import com.zhenzi.zhenzigtqt.common.lib.aspect.*;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.ingredient.IIngredientSlot;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.util.*;
import gregtech.client.utils.TooltipHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AspectTankWidget extends Widget implements IIngredientSlot {
    public final IAspectTank aspectTank;
    public int fluidRenderOffset = 1;
    private boolean hideTooltip;
    private boolean alwaysShowFull;
    private boolean drawHoveringText;
    private boolean allowClickFilling;
    private boolean allowClickEmptying;
    private IGuiTexture[] backgroundTexture;
    private IGuiTexture overlayTexture;
    protected AspectStack lastFluidInTank;
    private int lastTankCapacity;
    protected boolean isClient;

    private boolean enableColor;

    public AspectTankWidget(IAspectTank aspectTank, int x, int y, int width, int height) {
        super(new Position(x, y), new Size(width, height));
        this.aspectTank = aspectTank;
        this.drawHoveringText = true;
    }

    public AspectTankWidget setClient() {
        this.isClient = true;
        this.lastFluidInTank = this.aspectTank != null ? (this.aspectTank.getAspectStack() != null ? this.aspectTank.getAspectStack().copy() : null) : null;
        this.lastTankCapacity = this.aspectTank != null ? this.aspectTank.getCapacity() : 0;
        return this;
    }

    public AspectTankWidget setHideTooltip(boolean hideTooltip) {
        this.hideTooltip = hideTooltip;
        return this;
    }

    public AspectTankWidget setDrawHoveringText(boolean drawHoveringText) {
        this.drawHoveringText = drawHoveringText;
        return this;
    }

    public AspectTankWidget setAlwaysShowFull(boolean alwaysShowFull) {
        this.alwaysShowFull = alwaysShowFull;
        return this;
    }

    public AspectTankWidget setBackgroundTexture(IGuiTexture... backgroundTexture) {
        this.backgroundTexture = backgroundTexture;
        return this;
    }

    public AspectTankWidget setOverlayTexture(IGuiTexture overlayTexture) {
        this.overlayTexture = overlayTexture;
        return this;
    }

    public AspectTankWidget setFluidRenderOffset(int fluidRenderOffset) {
        this.fluidRenderOffset = fluidRenderOffset;
        return this;
    }

    public AspectTankWidget setEnableColor(boolean flag)
    {
        this.enableColor = flag;
        return this;
    }

    public AspectTankWidget setContainerClicking(boolean allowClickContainerFilling, boolean allowClickContainerEmptying) {
        if (!(this.aspectTank instanceof IAspectHandler)) {
            throw new IllegalStateException("Container IO is only supported for fluid tanks that implement IFluidHandler");
        } else {
            this.allowClickFilling = allowClickContainerFilling;
            this.allowClickEmptying = allowClickContainerEmptying;
            return this;
        }
    }

    public Object getIngredientOverMouse(int mouseX, int mouseY) {
        return this.isMouseOverElement(mouseX, mouseY) ? this.lastFluidInTank : null;
    }

    public String getFormattedFluidAmount() {
        return String.format("%,d", this.lastFluidInTank == null ? 0 : this.lastFluidInTank.amount);
    }

    public String getFluidLocalizedName() {
        return this.lastFluidInTank == null ? "" : this.lastFluidInTank.getLocalizedName();
    }

    public @Nullable TextComponentTranslation getFluidTextComponent() {
        if (this.lastFluidInTank == null) {
            return null;
        } else {
            Aspect var2 = this.lastFluidInTank.getAspect();
            return new TextComponentTranslation(this.lastFluidInTank.getUnlocalizedName());
        }
    }

    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        Position pos = this.getPosition();
        Size size = this.getSize();
        int drawAmount;
//        if (this.backgroundTexture != null) {
//            IGuiTexture[] var7 = this.backgroundTexture;
//            drawAmount = var7.length;
//
//            for(int var9 = 0; var9 < drawAmount; ++var9) {
//                IGuiTexture textureArea = var7[var9];
//                textureArea.draw((double)pos.x, (double)pos.y, 32, 32);
//            }
//        }

        if (this.lastFluidInTank != null && !this.gui.isJEIHandled) {
            GlStateManager.disableBlend();
            AspectStack stackToDraw = this.lastFluidInTank;
            drawAmount = this.alwaysShowFull ? this.lastFluidInTank.amount : this.lastTankCapacity;
            if (this.alwaysShowFull && this.lastFluidInTank.amount == 0) {
                stackToDraw = this.lastFluidInTank.copy();
                stackToDraw.amount = 1;
                drawAmount = 1;
            }

            this.drawAspectForGui(stackToDraw, pos.x + this.fluidRenderOffset, pos.y + this.fluidRenderOffset, size.width - this.fluidRenderOffset, size.height - this.fluidRenderOffset);
            if (this.alwaysShowFull && !this.hideTooltip && this.drawHoveringText) {
                GlStateManager.pushMatrix();
                GlStateManager.scale(0.5, 0.5, 1.0);
                String s = TextFormattingUtil.formatLongToCompactString((long)this.lastFluidInTank.amount, 4) + "L";
                FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
                fontRenderer.drawStringWithShadow(s, ((float)pos.x + (float)size.width / 3.0F) * 2.0F - (float)fontRenderer.getStringWidth(s) + 21.0F, ((float)pos.y + (float)size.height / 3.0F + 6.0F) * 2.0F, 16777215);
                GlStateManager.popMatrix();
            }

            GlStateManager.enableBlend();
        }

//        if (this.overlayTexture != null) {
//            this.overlayTexture.draw((double)pos.x, (double)pos.y, size.width, size.height);
//        }   else
//        {
//            TextureArea gui = new TextureArea(new ResourceLocation("thaumcraft", "textures/gui/gui_researchbook_overlay.png"), 0, 0, 1, 1);
            Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("thaumcraft", "textures/gui/gui_researchbook_overlay.png"));
            GL11.glEnable(3042);
//            gui.draw(pos.x, pos.y, 512, 512);
            Gui.drawModalRectWithCustomSizedTexture(pos.x - (size.width - 3) / 2, pos.y - (size.height-3) / 2, 40.0F, 6.0F, 32, 32, 512.0F, 512.0F);
//            Gui.drawModalRectWithCustomSizedTexture(16, 6, 199.0F, 168.0F, 26, 26, 512.0F, 512.0F);
            GL11.glDisable(3042);
//        }

    }

    public void drawAspectForGui(AspectStack aspectStack, int startX, int startY, int widthT, int heightT)
    {
        if (aspectStack != null)
        {
            IGuiTexture area = new TextureArea(aspectStack.getAspect().getImage(), 0, 0, 1.0, 1.0);
            if (super.isVisible()) {

                GL11.glPushMatrix();
                Minecraft.getMinecraft().renderEngine.bindTexture(aspectStack.getAspect().getImage());
                GL11.glEnable(3042);
                Color c = new Color(aspectStack.getAspect().getColor());
                GL11.glColor4f((float)c.getRed() / 255.0F, (float)c.getGreen() / 255.0F, (float)c.getBlue() / 255.0F, 1.0F);
                Gui.drawModalRectWithCustomSizedTexture(startX, startY, 0.0F, 0.0F, 16, 16, 16.0F, 16.0F);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                GL11.glScaled(0.5, 0.5, 0.5);

                GL11.glPopMatrix();
            }
        }
    }

    public void drawInForeground(int mouseX, int mouseY) {
        if (!this.hideTooltip && !this.gui.isJEIHandled && this.isMouseOverElement(mouseX, mouseY)) {
            java.util.List<String> tooltips = new ArrayList();
            if (this.lastFluidInTank == null) {
                tooltips.add(LocalizationUtils.format("gregtech.fluid.empty"));
                tooltips.add(LocalizationUtils.format("gregtech.fluid.amount", 0, this.lastTankCapacity));
            } else {
                Aspect aspect = this.lastFluidInTank.getAspect();
                tooltips.add(aspect.getLocalizedDescription());
                tooltips.add(LocalizationUtils.format("gregtech.fluid.amount", this.lastFluidInTank.amount, this.lastTankCapacity));
//                List<String> formula = FluidTooltipUtil.getFluidTooltip(this.lastFluidInTank);
//                if (formula != null) {
//                    for (String s : formula) {
//                        if (!s.isEmpty()) {
//                            tooltips.add(s);
//                        }
//                    }
//                }

                addIngotMolFluidTooltip(this.lastFluidInTank, tooltips);
            }

            if (this.allowClickEmptying && this.allowClickFilling) {
                tooltips.add("");
                tooltips.add(LocalizationUtils.format("gregtech.fluid.click_combined"));
            } else if (this.allowClickFilling) {
                tooltips.add("");
                tooltips.add(LocalizationUtils.format("gregtech.fluid.click_to_fill"));
            } else if (this.allowClickEmptying) {
                tooltips.add("");
                tooltips.add(LocalizationUtils.format("gregtech.fluid.click_to_empty"));
            }

            this.drawHoveringText(ItemStack.EMPTY, tooltips, 300, mouseX, mouseY);
            GlStateManager.color(1.0F, 1.0F, 1.0F);
        }

    }

    public void updateScreen() {
        if (this.isClient) {
            AspectStack aspectStack = this.aspectTank.getAspectStack();
            if (this.aspectTank.getCapacity() != this.lastTankCapacity) {
                this.lastTankCapacity = this.aspectTank.getCapacity();
            }

            if (aspectStack == null && this.lastFluidInTank != null) {
                this.lastFluidInTank = null;
            } else if (aspectStack != null) {
                if (!aspectStack.isAspectEqual(this.lastFluidInTank)) {
                    this.lastFluidInTank = aspectStack.copy();
                } else if (aspectStack.amount != this.lastFluidInTank.amount) {
                    this.lastFluidInTank.amount = aspectStack.amount;
                }
            }
        }

    }

    public void detectAndSendChanges() {
        AspectStack aspectStack = this.aspectTank.getAspectStack();
        if (this.aspectTank.getCapacity() != this.lastTankCapacity) {
            this.lastTankCapacity = this.aspectTank.getCapacity();
            this.writeUpdateInfo(0, (buffer) -> {
                buffer.writeVarInt(this.lastTankCapacity);
            });
        }

        if (aspectStack == null && this.lastFluidInTank != null) {
            this.lastFluidInTank = null;
            this.writeUpdateInfo(1, (buffer) -> {
            });
        } else if (aspectStack != null) {
            if (!aspectStack.isAspectEqual(this.lastFluidInTank)) {
                this.lastFluidInTank = aspectStack.copy();
                NBTTagCompound fluidStackTag = aspectStack.writeToNBT(new NBTTagCompound());
                this.writeUpdateInfo(2, (buffer) -> {
                    buffer.writeCompoundTag(fluidStackTag);
                });
            } else if (aspectStack.amount != this.lastFluidInTank.amount) {
                this.lastFluidInTank.amount = aspectStack.amount;
                this.writeUpdateInfo(3, (buffer) -> {
                    buffer.writeVarInt(this.lastFluidInTank.amount);
                });
            }
        }

    }

    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == 0) {
            this.lastTankCapacity = buffer.readVarInt();
        } else if (id == 1) {
            this.lastFluidInTank = null;
        } else if (id == 2) {
            NBTTagCompound fluidStackTag;
            try {
                fluidStackTag = buffer.readCompoundTag();
            } catch (IOException var5) {
                return;
            }

            this.lastFluidInTank = AspectStack.loadAspectStackFromNBT(fluidStackTag);
        } else if (id == 3 && this.lastFluidInTank != null) {
            this.lastFluidInTank.amount = buffer.readVarInt();
        }

    }

    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        if (id == 1) {
            ItemStack clickResult = this.tryClickContainer(buffer.readBoolean());
            if (clickResult != ItemStack.EMPTY) {
                ((EntityPlayerMP)this.gui.entityPlayer).updateHeldItem();
            }
        }

    }

    private ItemStack tryClickContainer(boolean tryFillAll) {
        EntityPlayer player = this.gui.entityPlayer;
        ItemStack currentStack = player.inventory.getItemStack();
        if (currentStack != ItemStack.EMPTY && currentStack.getCount() != 0) {
            ItemStack heldItemSizedOne = currentStack.copy();
            heldItemSizedOne.setCount(1);
            IAspectHandlerItem fluidHandlerItem = heldItemSizedOne.getCapability(CapabilityAspectHandler.ASPECT_HANDLER_ITEM_CAPABILITY, null);
            if (fluidHandlerItem == null) {
                return ItemStack.EMPTY;
            } else {
                AspectStack tankFluid = this.aspectTank.getAspectStack();
                AspectStack heldFluid = fluidHandlerItem.drain(Integer.MAX_VALUE, false);
                if (heldFluid != null && heldFluid.amount <= 0) {
                    heldFluid = null;
                }

                if (tankFluid == null) {
                    if (!this.allowClickEmptying) {
                        return ItemStack.EMPTY;
                    } else {
                        return heldFluid == null ? ItemStack.EMPTY : this.fillTankFromStack(heldFluid, tryFillAll);
                    }
                } else if (heldFluid != null && this.aspectTank.getAspectAmount() < this.aspectTank.getCapacity()) {
                    if (this.allowClickEmptying) {
                        return this.fillTankFromStack(heldFluid, tryFillAll);
                    } else {
                        return !this.allowClickFilling ? ItemStack.EMPTY : this.drainTankFromStack(tryFillAll);
                    }
                } else {
                    return !this.allowClickFilling ? ItemStack.EMPTY : this.drainTankFromStack(tryFillAll);
                }
            }
        } else {
            return ItemStack.EMPTY;
        }
    }

    private ItemStack fillTankFromStack(@NotNull AspectStack heldFluid, boolean tryFillAll) {
        EntityPlayer player = this.gui.entityPlayer;
        ItemStack heldItem = player.inventory.getItemStack();
        if (heldItem != ItemStack.EMPTY && heldItem.getCount() != 0) {
            ItemStack heldItemSizedOne = heldItem.copy();
            heldItemSizedOne.setCount(1);
            AspectStack currentFluid = this.aspectTank.getAspectStack();
            if (currentFluid != null && !currentFluid.isAspectEqual(heldFluid)) {
                return ItemStack.EMPTY;
            } else {
                int freeSpace = this.aspectTank.getCapacity() - this.aspectTank.getAspectAmount();
                if (freeSpace <= 0) {
                    return ItemStack.EMPTY;
                } else {
                    ItemStack itemStackEmptied = ItemStack.EMPTY;
                    int fluidAmountTaken = 0;
                    IAspectHandlerItem fluidHandler = heldItemSizedOne.getCapability(CapabilityAspectHandler.ASPECT_HANDLER_ITEM_CAPABILITY, (EnumFacing)null);
                    if (fluidHandler == null) {
                        return ItemStack.EMPTY;
                    } else {
                        AspectStack drained = fluidHandler.drain(freeSpace, true);
                        if (drained != null && drained.amount > 0) {
                            itemStackEmptied = fluidHandler.getContainer();
                            fluidAmountTaken = drained.amount;
                        }

                        if (itemStackEmptied == ItemStack.EMPTY) {
                            return ItemStack.EMPTY;
                        } else {
                            int additional = tryFillAll ? Math.min(freeSpace / fluidAmountTaken, heldItem.getCount()) : 1;
                            AspectStack copiedFluidStack = heldFluid.copy();
                            copiedFluidStack.amount = fluidAmountTaken * additional;
                            this.aspectTank.fill(copiedFluidStack, true);
                            itemStackEmptied.setCount(additional);
                            this.replaceCursorItemStack(itemStackEmptied);
                            this.playSound(heldFluid, true);
                            return itemStackEmptied;
                        }
                    }
                }
            }
        } else {
            return ItemStack.EMPTY;
        }
    }

    private ItemStack drainTankFromStack(boolean tryFillAll) {
        EntityPlayer player = this.gui.entityPlayer;
        ItemStack heldItem = player.inventory.getItemStack();
        if (heldItem != ItemStack.EMPTY && heldItem.getCount() != 0) {
            ItemStack heldItemSizedOne = heldItem.copy();
            heldItemSizedOne.setCount(1);
            AspectStack currentFluid = this.aspectTank.getAspectStack();
            if (currentFluid == null) {
                return ItemStack.EMPTY;
            } else {
                currentFluid = currentFluid.copy();
                int originalFluidAmount = this.aspectTank.getAspectAmount();
                IAspectHandlerItem handler = heldItemSizedOne.getCapability(CapabilityAspectHandler.ASPECT_HANDLER_ITEM_CAPABILITY, (EnumFacing)null);
                if (handler == null) {
                    return ItemStack.EMPTY;
                } else {
                    ItemStack filledContainer = this.fillFluidContainer(currentFluid, heldItemSizedOne);
                    if (filledContainer != ItemStack.EMPTY) {
                        int filledAmount = originalFluidAmount - currentFluid.amount;
                        if (filledAmount <= 0) {
                            return ItemStack.EMPTY;
                        }

                        this.aspectTank.drain(filledAmount, true);
                        if (tryFillAll) {
                            int additional = Math.min(heldItem.getCount() - 1, currentFluid.amount / filledAmount);
                            this.aspectTank.drain(filledAmount * additional, true);
                            filledContainer.grow(additional);
                        }

                        this.replaceCursorItemStack(filledContainer);
                        this.playSound(currentFluid, false);
                    }

                    return filledContainer;
                }
            }
        } else {
            return ItemStack.EMPTY;
        }
    }

    private ItemStack fillFluidContainer(AspectStack aspectStack, ItemStack itemStack) {
        IAspectHandlerItem fluidHandlerItem = itemStack.getCapability(CapabilityAspectHandler.ASPECT_HANDLER_ITEM_CAPABILITY, (EnumFacing)null);
        if (fluidHandlerItem == null) {
            return ItemStack.EMPTY;
        } else {
            int filledAmount = fluidHandlerItem.fill(aspectStack, true);
            if (filledAmount > 0) {
                aspectStack.amount -= filledAmount;
                return fluidHandlerItem.getContainer();
            } else {
                return ItemStack.EMPTY;
            }
        }
    }

    private void replaceCursorItemStack(ItemStack resultStack) {
        EntityPlayer player = this.gui.entityPlayer;
        int resultStackSize = resultStack.getMaxStackSize();

        while(resultStack.getCount() > resultStackSize) {
            player.inventory.getItemStack().shrink(resultStackSize);
            addItemToPlayerInventory(player, resultStack.splitStack(resultStackSize));
        }

        if (player.inventory.getItemStack().getCount() == resultStack.getCount()) {
            player.inventory.setItemStack(resultStack);
        } else {
            ItemStack heldStack = player.inventory.getItemStack();
            heldStack.shrink(resultStack.getCount());
            player.inventory.setItemStack(heldStack);
            addItemToPlayerInventory(player, resultStack);
        }

    }

    private static void addItemToPlayerInventory(EntityPlayer player, ItemStack stack) {
        if (stack != null) {
            if (!player.inventory.addItemStackToInventory(stack) && !player.world.isRemote) {
                EntityItem dropItem = player.entityDropItem(stack, 0.0F);
                if (dropItem != null) {
                    dropItem.setPickupDelay(0);
                }
            }

        }
    }

    private void playSound(AspectStack aspectStack, boolean fill) {
    }

    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (this.isMouseOverElement(mouseX, mouseY)) {
            ItemStack currentStack = this.gui.entityPlayer.inventory.getItemStack();
            if ((this.allowClickEmptying || this.allowClickFilling) && currentStack.hasCapability(CapabilityAspectHandler.ASPECT_HANDLER_ITEM_CAPABILITY, (EnumFacing)null)) {
                this.writeClientAction(1, (writer) -> {
                    writer.writeBoolean(button == 0);
                });
                playButtonClickSound();
                return true;
            }
        }

        return false;
    }

    public static void addIngotMolFluidTooltip(AspectStack aspectStack, List<String> tooltip) {
        if (TooltipHelper.isShiftDown() && aspectStack.amount > 250) {
            int numJars = aspectStack.amount / 250;
            int extra = aspectStack.amount % 250;
            String fluidAmount = String.format(" %,d L = %,d * %d L", aspectStack.amount, numJars, 250);
            if (extra != 0) {
                fluidAmount = fluidAmount + String.format(" + %d L", extra);
            }

            tooltip.add(TextFormatting.GRAY + LocalizationUtils.format("gregtech.gui.amount_raw", new Object[0]) + fluidAmount);
        }

    }
}
