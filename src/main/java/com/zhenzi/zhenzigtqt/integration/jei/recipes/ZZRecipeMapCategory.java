package com.zhenzi.zhenzigtqt.integration.jei.recipes;

import com.buuz135.thaumicjei.ThaumcraftJEIPlugin;
import com.buuz135.thaumicjei.ingredient.AspectIngredientRender;
import com.google.common.collect.UnmodifiableIterator;
import com.zhenzi.zhenzigtqt.client.gui.QuantumAspectTank.AspectTankWidget;
import com.zhenzi.zhenzigtqt.common.lib.aspect.AspectStack;
import com.zhenzi.zhenzigtqt.common.lib.aspect.AspectTank;
import com.zhenzi.zhenzigtqt.common.lib.aspect.AspectTankList;
import com.zhenzi.zhenzigtqt.integration.IZZRecipeLayout;
import com.zhenzi.zhenzigtqt.integration.jei.AspectTypes;
import com.zhenzi.zhenzigtqt.integration.jei.IGuiAspectStackGroup;
import com.zhenzi.zhenzigtqt.loaders.AspectRecipeMap;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.BlankUIHolder;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.util.LocalizationUtils;
import gregtech.integration.jei.JustEnoughItemsModule;
import gregtech.integration.jei.utils.render.FluidStackTextRenderer;
import gregtech.integration.jei.utils.render.ItemStackTextRenderer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.*;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.AspectList;

import java.util.*;

public class ZZRecipeMapCategory implements IRecipeCategory<ZZRecipeWrapper> {
    private final AspectRecipeMap<?> recipeMap;
    private final ZZRecipeCategory category;
    private final ModularUI modularUI;
    private final ItemStackHandler importItems;
    private final ItemStackHandler exportItems;
    private final FluidTankList importFluids;
    private final FluidTankList exportFluids;
    private final AspectTankList importAspects;
    private final AspectTankList exportAspects;
    private final IDrawable backgroundDrawable;
    private Object iconIngredient;
    private IDrawable icon;
    private static final Map<ZZRecipeCategory, ZZRecipeMapCategory> gtCategories = new Object2ObjectOpenHashMap();
    private static final Map<AspectRecipeMap<?>, List<ZZRecipeMapCategory>> recipeMapCategories = new Object2ObjectOpenHashMap();

    public ZZRecipeMapCategory(@NotNull AspectRecipeMap<?> recipeMap, @NotNull ZZRecipeCategory category, IGuiHelper guiHelper) {
        this.recipeMap = recipeMap;
        this.category = category;
        FluidTank[] importFluidTanks = new FluidTank[recipeMap.getMaxFluidInputs()];
        AspectTank[] importAspectTanks = new AspectTank[recipeMap.getMaxAspectInputs()];

        for(int i = 0; i < importFluidTanks.length; ++i) {
            importFluidTanks[i] = new FluidTank(16000);
        }
        for(int i = 0; i < importAspectTanks.length; ++i) {
            importAspectTanks[i] = new AspectTank(1000);
        }

        FluidTank[] exportFluidTanks = new FluidTank[recipeMap.getMaxFluidOutputs()];
        AspectTank[] exportAspectTanks = new AspectTank[recipeMap.getMaxAspectOutputs()];

        for(int i = 0; i < exportFluidTanks.length; ++i) {
            exportFluidTanks[i] = new FluidTank(16000);
        }
        for(int i = 0; i < exportAspectTanks.length; ++i) {
            exportAspectTanks[i] = new AspectTank(1000);
        }

        this.modularUI = recipeMap.createJeiUITemplate(
                this.importItems = new ItemStackHandler(recipeMap.getMaxInputs() + (recipeMap == RecipeMaps.ASSEMBLY_LINE_RECIPES ? 1 : 0)),
                this.exportItems = new ItemStackHandler(recipeMap.getMaxOutputs()),
                this.importFluids = new FluidTankList(false, importFluidTanks),
                this.exportFluids = new FluidTankList(false, exportFluidTanks),
                        this.importAspects = new AspectTankList(false, importAspectTanks),
                        this.exportAspects = new AspectTankList(false, exportAspectTanks),
                0)
                .build(new BlankUIHolder(), Minecraft.getMinecraft().player);
        this.modularUI.initWidgets();
        this.backgroundDrawable = guiHelper.createBlankDrawable(this.modularUI.getWidth(), this.modularUI.getHeight() * 2 / 3 + recipeMap.getPropertyHeightShift());
        gtCategories.put(category, this);
        recipeMapCategories.compute(recipeMap, (k, v) -> {
            if (v == null) {
                v = new ArrayList();
            }

            ((List)v).add(this);
            return (List)v;
        });
    }

    public @NotNull String getUid() {
        return this.category.getUniqueID();
    }

    public @NotNull String getTitle() {
        return LocalizationUtils.format(this.category.getTranslationKey(), new Object[0]);
    }

    public @Nullable IDrawable getIcon() {
        if (this.icon != null) {
            return this.icon;
        } else {
            Object var2 = this.iconIngredient;
            if (var2 instanceof IDrawable) {
                IDrawable drawable = (IDrawable)var2;
                return this.icon = drawable;
            } else {
                return this.iconIngredient != null ? (this.icon = JustEnoughItemsModule.guiHelper.createDrawableIngredient(this.iconIngredient)) : null;
            }
        }
    }

    public void setIcon(Object icon) {
        if (this.iconIngredient == null) {
            this.iconIngredient = icon;
        }

    }

    public @NotNull String getModName() {
        return "gregtech";
    }

    public @NotNull IDrawable getBackground() {
        return this.backgroundDrawable;
    }

    public void setRecipe(IRecipeLayout recipeLayout, @NotNull ZZRecipeWrapper recipeWrapper, @NotNull IIngredients ingredients) {
        IGuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();
        IGuiFluidStackGroup fluidStackGroup = recipeLayout.getFluidStacks();
        IGuiIngredientGroup<AspectList> aspectStackGroup = recipeLayout.getIngredientsGroup(ThaumcraftJEIPlugin.ASPECT_LIST);
        UnmodifiableIterator var6 = this.modularUI.guiWidgets.values().iterator();

        while (var6.hasNext()) {
            Widget uiWidget = (Widget) var6.next();
            if (uiWidget instanceof SlotWidget) {
                SlotWidget slotWidget = (SlotWidget) uiWidget;
                if (slotWidget.getHandle() instanceof SlotItemHandler) {
                    SlotItemHandler handle = (SlotItemHandler) slotWidget.getHandle();
                    if (handle.getItemHandler() == this.importItems) {
                        itemStackGroup.init(handle.getSlotIndex(), true, new ItemStackTextRenderer(recipeWrapper.isNotConsumedItem(handle.getSlotIndex())), slotWidget.getPosition().x + 1, slotWidget.getPosition().y + 1, slotWidget.getSize().width - 2, slotWidget.getSize().height - 2, 0, 0);
                    } else if (handle.getItemHandler() == this.exportItems) {
                        itemStackGroup.init(this.importItems.getSlots() + handle.getSlotIndex(), false,
                                new ItemStackTextRenderer(recipeWrapper.getOutputChance(handle.getSlotIndex() - recipeWrapper.getRecipe().getOutputs().size()), recipeWrapper.getChancedOutputLogic()), slotWidget.getPosition().x + 1, slotWidget.getPosition().y + 1, slotWidget.getSize().width - 2, slotWidget.getSize().height - 2, 0, 0);
                    }
                }
            } else if (uiWidget instanceof TankWidget) {
                TankWidget tankWidget = (TankWidget) uiWidget;
                List inputsList;
                int fluidAmount;
                int exportIndex;
                if (this.importFluids.getFluidTanks().contains(tankWidget.fluidTank)) {
                    exportIndex = this.importFluids.getFluidTanks().indexOf(tankWidget.fluidTank);
                    inputsList = ingredients.getInputs(VanillaTypes.FLUID);
                    fluidAmount = 0;
                    if (inputsList.size() > exportIndex && !((List) inputsList.get(exportIndex)).isEmpty()) {
                        fluidAmount = ((FluidStack) ((List) inputsList.get(exportIndex)).get(0)).amount;
                    }

                    fluidStackGroup.init(exportIndex, true,
                            (new FluidStackTextRenderer(fluidAmount,
                                    false,
                                    tankWidget.getSize().width - 2 * tankWidget.fluidRenderOffset,
                                    tankWidget.getSize().height - 2 * tankWidget.fluidRenderOffset,
                                    (IDrawable) null)).setNotConsumed(recipeWrapper.isNotConsumedFluid(exportIndex)),
                            tankWidget.getPosition().x + tankWidget.fluidRenderOffset,
                            tankWidget.getPosition().y + tankWidget.fluidRenderOffset,
                            tankWidget.getSize().width - 2 * tankWidget.fluidRenderOffset,
                            tankWidget.getSize().height - 2 * tankWidget.fluidRenderOffset,
                            0, 0);
                } else if (this.exportFluids.getFluidTanks().contains(tankWidget.fluidTank)) {
                    exportIndex = this.exportFluids.getFluidTanks().indexOf(tankWidget.fluidTank);
                    inputsList = ingredients.getOutputs(VanillaTypes.FLUID);
                    fluidAmount = 0;
                    if (inputsList.size() > exportIndex && !((List) inputsList.get(exportIndex)).isEmpty()) {
                        fluidAmount = ((FluidStack) ((List) inputsList.get(exportIndex)).get(0)).amount;
                    }

                    fluidStackGroup.init(this.importFluids.getFluidTanks().size() + exportIndex,
                            false,
                            new FluidStackTextRenderer(fluidAmount,
                                    false,
                                    tankWidget.getSize().width - 2 * tankWidget.fluidRenderOffset,
                                    tankWidget.getSize().height - 2 * tankWidget.fluidRenderOffset,
                                    (IDrawable) null,
                                    recipeWrapper.getFluidOutputChance(exportIndex - recipeWrapper.getRecipe().getFluidOutputs().size()),
                                    recipeWrapper.getChancedFluidOutputLogic()),
                            tankWidget.getPosition().x + tankWidget.fluidRenderOffset,
                            tankWidget.getPosition().y + tankWidget.fluidRenderOffset,
                            tankWidget.getSize().width - 2 * tankWidget.fluidRenderOffset,
                            tankWidget.getSize().height - 2 * tankWidget.fluidRenderOffset,
                            0, 0);
                }
            } else if (uiWidget instanceof AspectTankWidget)
            {
                AspectTankWidget tankWidget = (AspectTankWidget) uiWidget;
                List<List<AspectList>> aspectLists = null;
                int aspectAmount;
                int exportIndex;
                if (this.importAspects.getFluidTanks().contains(tankWidget.aspectTank))
                {
                    exportIndex = this.importAspects.getFluidTanks().indexOf(tankWidget.aspectTank);
                    aspectLists = ingredients.getInputs(ThaumcraftJEIPlugin.ASPECT_LIST);
                    aspectAmount = 0;
                    if (aspectLists.size() > exportIndex && !((List) aspectLists.get(exportIndex)).isEmpty()) {
                        List<AspectList> aspectLists1 = aspectLists.get(exportIndex);
                        AspectList aspectList = aspectLists1.get(0);
//                        aspectAmount = ((FluidStack) ((List) aspectLists.get(exportIndex)).get(0)).amount;
                        aspectStackGroup.init(exportIndex, true, new AspectIngredientRender(), tankWidget.getPosition().x + tankWidget.fluidRenderOffset,
                                tankWidget.getPosition().y + tankWidget.fluidRenderOffset,
                                tankWidget.getSize().width - 2 * tankWidget.fluidRenderOffset,
                                tankWidget.getSize().height - 2 * tankWidget.fluidRenderOffset,
                                0, 0);
                        aspectStackGroup.set(exportIndex, aspectList);
                    }


                } else if (this.importAspects.getFluidTanks().contains(tankWidget.aspectTank)) {
                    exportIndex = this.importAspects.getFluidTanks().indexOf(tankWidget.aspectTank);
                    aspectLists = ingredients.getOutputs(ThaumcraftJEIPlugin.ASPECT_LIST);
                    aspectAmount = 0;
                    if (aspectLists.size() > exportIndex && !((List) aspectLists.get(exportIndex)).isEmpty()) {
                        List<AspectList> aspectLists1 = aspectLists.get(exportIndex);
                        AspectList aspectList = aspectLists1.get(0);
//                        aspectAmount = ((FluidStack) ((List) aspectLists.get(exportIndex)).get(0)).amount;
                        aspectStackGroup.init(this.importAspects.getFluidTanks().size() + exportIndex, true, new AspectIngredientRender(), tankWidget.getPosition().x + tankWidget.fluidRenderOffset,
                                tankWidget.getPosition().y + tankWidget.fluidRenderOffset,
                                tankWidget.getSize().width - 2 * tankWidget.fluidRenderOffset,
                                tankWidget.getSize().height - 2 * tankWidget.fluidRenderOffset,
                                0, 0);
                        aspectStackGroup.set(this.importAspects.getFluidTanks().size() + exportIndex, aspectList);
                    }
                }
            }
//            else if (uiWidget instanceof AspectTankWidget) {
//                AspectTankWidget tankWidget = (AspectTankWidget) uiWidget;
//                List inputsList;
//                int fluidAmount;
//                int exportIndex;
//                if (this.importAspects.getFluidTanks().contains(tankWidget.aspectTank)) {
////                    exportIndex = this.importAspects.getFluidTanks().indexOf(tankWidget.aspectTank);
////                    inputsList = ingredients.getInputs(ThaumcraftJEIPlugin.ASPECT_LIST);
////                    fluidAmount = 0;
////                    if (inputsList.size() > exportIndex && !((List) inputsList.get(exportIndex)).isEmpty()) {
////                        fluidAmount = ((AspectStack) ((List) inputsList.get(exportIndex)).get(0)).amount;
////                    }
//
////                    aspectStackGroup.init(exportIndex, true,
////                            (new AspectStackTextRenderer(fluidAmount, false, tankWidget.getSize().width - 2 * tankWidget.fluidRenderOffset, tankWidget.getSize().height - 2 * tankWidget.fluidRenderOffset, (IDrawable) null)).setNotConsumed(recipeWrapper.isNotConsumedFluid(exportIndex)), tankWidget.getPosition().x + tankWidget.fluidRenderOffset, tankWidget.getPosition().y + tankWidget.fluidRenderOffset, tankWidget.getSize().width - 2 * tankWidget.fluidRenderOffset, tankWidget.getSize().height - 2 * tankWidget.fluidRenderOffset, 0, 0);
//                } else if (this.exportAspects.getFluidTanks().contains(tankWidget.aspectTank)) {
//                    exportIndex = this.exportAspects.getFluidTanks().indexOf(tankWidget.aspectTank);
//                    inputsList = ingredients.getOutputs(ThaumcraftJEIPlugin.ASPECT_LIST);
//                    fluidAmount = 0;
//                    if (inputsList.size() > exportIndex && !((List) inputsList.get(exportIndex)).isEmpty()) {
//                        fluidAmount = ((FluidStack) ((List) inputsList.get(exportIndex)).get(0)).amount;
//                    }
//
//                    aspectStackGroup.init(this.importAspects.getFluidTanks().size() + exportIndex, false,
//                            new AspectStackTextRenderer(fluidAmount, false, tankWidget.getSize().width - 2 * tankWidget.fluidRenderOffset, tankWidget.getSize().height - 2 * tankWidget.fluidRenderOffset, (IDrawable) null, recipeWrapper.getFluidOutputChance(exportIndex - recipeWrapper.getRecipe().getFluidOutputs().size()), recipeWrapper.getChancedFluidOutputLogic()), tankWidget.getPosition().x + tankWidget.fluidRenderOffset, tankWidget.getPosition().y + tankWidget.fluidRenderOffset, tankWidget.getSize().width - 2 * tankWidget.fluidRenderOffset, tankWidget.getSize().height - 2 * tankWidget.fluidRenderOffset, 0, 0);
//                }
//            }

//        if (ConfigHolder.machines.enableResearch && this.recipeMap == RecipeMaps.ASSEMBLY_LINE_RECIPES) {
//            ResearchPropertyData data = (ResearchPropertyData)recipeWrapper.getRecipe().getProperty(ResearchProperty.getInstance(), (Object)null);
//            if (data != null) {
//                List<ItemStack> dataItems = new ArrayList();
//                Iterator var15 = data.iterator();
//
//                while(var15.hasNext()) {
//                    ResearchPropertyData.ResearchEntry entry = (ResearchPropertyData.ResearchEntry)var15.next();
//                    ItemStack dataStick = entry.getDataItem().copy();
//                    AssemblyLineManager.writeResearchToNBT(GTUtility.getOrCreateNbtCompound(dataStick), entry.getResearchId());
//                    dataItems.add(dataStick);
//                }
//
//                itemStackGroup.set(16, dataItems);
//            }
//        }

            Objects.requireNonNull(recipeWrapper);
            itemStackGroup.addTooltipCallback(recipeWrapper::addItemTooltip);
            Objects.requireNonNull(recipeWrapper);
            fluidStackGroup.addTooltipCallback(recipeWrapper::addFluidTooltip);
            Objects.requireNonNull(recipeWrapper);
            aspectStackGroup.addTooltipCallback(recipeWrapper::addAspectTooltip);
            itemStackGroup.set(ingredients);
            fluidStackGroup.set(ingredients);
            aspectStackGroup.set(ingredients);
        }
    }

    public void drawExtras(Minecraft mc) {
        UnmodifiableIterator var2 = this.modularUI.guiWidgets.values().iterator();

        while(var2.hasNext()) {
            Widget widget = (Widget)var2.next();
            if (widget instanceof ProgressWidget) {
                widget.detectAndSendChanges();
            }

            widget.drawInBackground(0, 0, mc.getRenderPartialTicks(), new IRenderContext() {
            });
            widget.drawInForeground(0, 0);
        }

    }

        public static @Nullable ZZRecipeMapCategory getCategoryFor (@NotNull ZZRecipeCategory category){
            return (ZZRecipeMapCategory) gtCategories.get(category);
        }

        public static @Nullable Collection<ZZRecipeMapCategory> getCategoriesFor (@NotNull AspectRecipeMap<?> recipeMap)
        {
            return (Collection) recipeMapCategories.get(recipeMap);
        }
    }
