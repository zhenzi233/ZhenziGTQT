package com.zhenzi.zhenzigtqt.integration.jei.recipes;

import com.buuz135.thaumicjei.ThaumcraftJEIPlugin;
import com.zhenzi.zhenzigtqt.client.gui.QuantumAspectTank.AspectTankWidget;
import com.zhenzi.zhenzigtqt.common.lib.aspect.AspectStack;
import com.zhenzi.zhenzigtqt.integration.jei.AspectTypes;
import com.zhenzi.zhenzigtqt.loaders.AspectRecipe;
import com.zhenzi.zhenzigtqt.loaders.AspectRecipeInput;
import com.zhenzi.zhenzigtqt.loaders.AspectRecipeMap;
import com.zhenzi.zhenzigtqt.loaders.ChancedAspectOutput;
import gregtech.api.GTValues;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IDataItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.chance.boost.BoostableChanceEntry;
import gregtech.api.recipes.chance.output.ChancedOutputLogic;
import gregtech.api.recipes.chance.output.impl.ChancedFluidOutput;
import gregtech.api.recipes.chance.output.impl.ChancedItemOutput;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.recipes.machines.IResearchRecipeMap;
import gregtech.api.recipes.machines.IScannerRecipeMap;
import gregtech.api.recipes.recipeproperties.ComputationProperty;
import gregtech.api.recipes.recipeproperties.RecipeProperty;
import gregtech.api.recipes.recipeproperties.ScanProperty;
import gregtech.api.recipes.recipeproperties.TotalComputationProperty;
import gregtech.api.util.AssemblyLineManager;
import gregtech.api.util.ClipboardUtil;
import gregtech.api.util.GTUtility;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.utils.TooltipHelper;
import gregtech.integration.RecipeCompatUtil;
import gregtech.integration.jei.recipe.GTRecipeWrapper;
import gregtech.integration.jei.utils.AdvancedRecipeWrapper;
import gregtech.integration.jei.utils.JeiButton;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.AspectList;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

public class ZZRecipeWrapper extends AdvancedRecipeWrapper {
    private static final int LINE_HEIGHT = 10;
    private final AspectRecipeMap<?> recipeMap;
    private final AspectRecipe recipe;
    private final List<AspectRecipeInput> sortedInputs;
    private final List<AspectRecipeInput> sortedFluidInputs;
    private final List<AspectRecipeInput> sortedAspectInputs;

    public ZZRecipeWrapper(AspectRecipeMap<?> recipeMap, AspectRecipe recipe) {
        this.recipeMap = recipeMap;
        this.recipe = recipe;
        this.sortedInputs = new ArrayList(recipe.getInputs());
        this.sortedInputs.sort(AspectRecipeInput.RECIPE_INPUT_COMPARATOR);
        this.sortedFluidInputs = new ArrayList(recipe.getFluidInputs());
        this.sortedFluidInputs.sort(AspectRecipeInput.RECIPE_INPUT_COMPARATOR);
        this.sortedAspectInputs = new ArrayList<>(recipe.getAspectInputs());
        this.sortedAspectInputs.sort(AspectRecipeInput.RECIPE_INPUT_COMPARATOR);
    }

    public Recipe getRecipe() {
        return this.recipe;
    }

    public void getIngredients(@NotNull IIngredients ingredients) {
        ArrayList list;
        Iterator var3;
        AspectRecipeInput input;
        AspectRecipeInput inputA;
        if (!this.sortedInputs.isEmpty()) {
            list = new ArrayList();
            var3 = this.sortedInputs.iterator();

            while(var3.hasNext()) {
                input = (AspectRecipeInput)var3.next();
                List<ItemStack> stacks = new ArrayList();
                ItemStack[] var6 = input.getInputStacks();
                int var7 = var6.length;

                for(int var8 = 0; var8 < var7; ++var8) {
                    ItemStack stack = var6[var8];
                    stacks.add(stack.copy());
                }

                list.add(stacks);
            }

            ingredients.setInputLists(VanillaTypes.ITEM, list);
        }

        if (!this.sortedFluidInputs.isEmpty()) {
            list = new ArrayList();
            var3 = this.sortedFluidInputs.iterator();

            while(var3.hasNext()) {
                input = (AspectRecipeInput)var3.next();
                list.add(input.getInputFluidStack());
            }

            ingredients.setInputs(VanillaTypes.FLUID, list);
        }

        if (!this.sortedAspectInputs.isEmpty()) {
            List<AspectList> list1 = new ArrayList();
            var3 = this.sortedAspectInputs.iterator();

            while(var3.hasNext()) {
                inputA = (AspectRecipeInput) var3.next();
                AspectStack aspectStack = inputA.getInputAspectStack();
                List<AspectStack> aspectStackList = new ArrayList<>();
                aspectStackList.add(aspectStack);

                list1.add(AspectStack.stacksToAspectList(aspectStackList));
            }

            ingredients.setInputs(ThaumcraftJEIPlugin.ASPECT_LIST, list1);
        }

        List recipeOutputs;
        ArrayList scannerPossibilities;
        if (!this.recipe.getOutputs().isEmpty() || !this.recipe.getChancedOutputs().getChancedEntries().isEmpty()) {
            recipeOutputs = (List)this.recipe.getOutputs().stream().map(ItemStack::copy).collect(Collectors.toList());
            scannerPossibilities = null;
            Iterator var15;
            if (this.recipeMap instanceof IScannerRecipeMap) {
                scannerPossibilities = new ArrayList();
                String researchId = null;
                var15 = this.recipe.getOutputs().iterator();

                while(var15.hasNext()) {
                    ItemStack stack = (ItemStack)var15.next();
                    researchId = AssemblyLineManager.readResearchId(stack);
                    if (researchId != null) {
                        break;
                    }
                }

                if (researchId != null) {
                    Collection<Recipe> possibleRecipes = ((IResearchRecipeMap)RecipeMaps.ASSEMBLY_LINE_RECIPES).getDataStickEntry(researchId);
                    if (possibleRecipes != null) {
                        Iterator var21 = possibleRecipes.iterator();

                        while(var21.hasNext()) {
                            Recipe r = (Recipe)var21.next();
                            ItemStack researchItem = (ItemStack)r.getOutputs().get(0);
                            researchItem = researchItem.copy();
                            researchItem.setCount(1);
                            boolean didMatch = false;
                            Iterator var10 = scannerPossibilities.iterator();

                            while(var10.hasNext()) {
                                ItemStack stack = (ItemStack)var10.next();
                                if (ItemStack.areItemStacksEqual(stack, researchItem)) {
                                    didMatch = true;
                                    break;
                                }
                            }

                            if (!didMatch) {
                                scannerPossibilities.add(researchItem);
                            }
                        }
                    }

                    scannerPossibilities.add((ItemStack)recipeOutputs.get(0));
                }
            }

            List<ChancedItemOutput> chancedOutputs = new ArrayList(this.recipe.getChancedOutputs().getChancedEntries());
            var15 = chancedOutputs.iterator();

            while(var15.hasNext()) {
                ChancedItemOutput chancedEntry = (ChancedItemOutput)var15.next();
                recipeOutputs.add((ItemStack)chancedEntry.getIngredient());
            }

            if (scannerPossibilities != null && !scannerPossibilities.isEmpty()) {
                ingredients.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(scannerPossibilities));
            } else {
                ingredients.setOutputs(VanillaTypes.ITEM, recipeOutputs);
            }
        }

        if (!this.recipe.getFluidOutputs().isEmpty() || !this.recipe.getChancedFluidOutputs().getChancedEntries().isEmpty()) {
            recipeOutputs = (List)this.recipe.getFluidOutputs().stream().map(FluidStack::copy).collect(Collectors.toList());
            scannerPossibilities = new ArrayList(this.recipe.getChancedFluidOutputs().getChancedEntries());
            Iterator var18 = scannerPossibilities.iterator();

            while(var18.hasNext()) {
                ChancedFluidOutput chancedEntry = (ChancedFluidOutput)var18.next();
                recipeOutputs.add((FluidStack)chancedEntry.getIngredient());
            }

            ingredients.setOutputs(VanillaTypes.FLUID, recipeOutputs);
        }

        if (!this.recipe.getAspectOutputs().isEmpty() || !this.recipe.getChancedAspectOutputs().getChancedEntries().isEmpty()) {

            List<AspectList> list1 = new ArrayList();
            List<AspectStack> recipeOutputAs = (List)this.recipe.getAspectOutputs().stream().map(AspectStack::copy).collect(Collectors.toList());
            scannerPossibilities = new ArrayList(this.recipe.getChancedAspectOutputs().getChancedEntries());
            Iterator var18 = scannerPossibilities.iterator();

            while(var18.hasNext()) {
                ChancedAspectOutput chancedEntry = (ChancedAspectOutput)var18.next();
                recipeOutputAs.add((AspectStack)chancedEntry.getIngredient());
            }

            list1.add(AspectStack.stacksToAspectList(recipeOutputAs));

            ingredients.setOutputs(ThaumcraftJEIPlugin.ASPECT_LIST, list1);
        }
    }

    public void addItemTooltip(int slotIndex, boolean input, Object ingredient, List<String> tooltip) {
        boolean notConsumed = input && this.isNotConsumedItem(slotIndex);
        BoostableChanceEntry<?> entry = null;
        if (!input && !this.recipe.getChancedOutputs().getChancedEntries().isEmpty()) {
            int outputIndex = slotIndex - this.recipeMap.getMaxInputs();
            if (outputIndex >= this.recipe.getOutputs().size()) {
                entry = (BoostableChanceEntry)this.recipe.getChancedOutputs().getChancedEntries().get(outputIndex - this.recipe.getOutputs().size());
            }
        }

        this.addIngredientTooltips(tooltip, notConsumed, input, entry, this.recipe.getChancedOutputs().getChancedOutputLogic());
        this.addIngredientTooltips(tooltip, notConsumed, input, ingredient, (Object)null);
    }

    public void addFluidTooltip(int slotIndex, boolean input, Object ingredient, List<String> tooltip) {
        FluidStack fluidStack = (FluidStack)ingredient;
        TankWidget.addIngotMolFluidTooltip(fluidStack, tooltip);
        boolean notConsumed = input && this.isNotConsumedFluid(slotIndex);
        BoostableChanceEntry<?> entry = null;
        if (!this.recipe.getChancedFluidOutputs().getChancedEntries().isEmpty()) {
            int outputIndex = slotIndex - this.recipeMap.getMaxFluidInputs();
            if (outputIndex >= this.recipe.getFluidOutputs().size()) {
                entry = (BoostableChanceEntry)this.recipe.getChancedFluidOutputs().getChancedEntries().get(outputIndex - this.recipe.getFluidOutputs().size());
            }
        }

        this.addIngredientTooltips(tooltip, notConsumed, input, entry, this.recipe.getChancedFluidOutputs().getChancedOutputLogic());
        this.addIngredientTooltips(tooltip, notConsumed, input, ingredient, (Object)null);
    }

    public void addAspectTooltip(int slotIndex, boolean input, Object ingredient, List<String> tooltip) {
        AspectList fluidStackList = (AspectList)ingredient;
        for (AspectStack fluidStack : AspectStack.aspectListToStacks(fluidStackList))
        {
            AspectTankWidget.addIngotMolFluidTooltip(fluidStack, tooltip);
            boolean notConsumed = input && this.isNotConsumedFluid(slotIndex);
            BoostableChanceEntry<?> entry = null;
            if (!this.recipe.getChancedAspectOutputs().getChancedEntries().isEmpty()) {
                int outputIndex = slotIndex - this.recipeMap.getMaxAspectInputs();
                if (outputIndex >= this.recipe.getAspectOutputs().size()) {
                    entry = (BoostableChanceEntry)this.recipe.getChancedAspectOutputs().getChancedEntries().get(outputIndex - this.recipe.getAspectOutputs().size());
                }
            }

            this.addIngredientTooltips(tooltip, notConsumed, input, entry, this.recipe.getChancedAspectOutputs().getChancedOutputLogic());
            this.addIngredientTooltips(tooltip, notConsumed, input, ingredient, (Object)null);
        }
    }

    public void addIngredientTooltips(@NotNull Collection<String> tooltip, boolean notConsumed, boolean input, @Nullable Object ingredient, @Nullable Object ingredient2) {
        if (ingredient2 instanceof ChancedOutputLogic) {
            ChancedOutputLogic logic = (ChancedOutputLogic)ingredient2;
            if (ingredient instanceof BoostableChanceEntry) {
                BoostableChanceEntry<?> entry = (BoostableChanceEntry)ingredient;
                double chance = (double)entry.getChance() / 100.0;
                double boost = (double)entry.getChanceBoost() / 100.0;
                if (logic != ChancedOutputLogic.NONE && logic != ChancedOutputLogic.OR) {
                    tooltip.add(TooltipHelper.BLINKING_CYAN + I18n.format("gregtech.recipe.chance_logic", new Object[]{chance, boost, I18n.format(logic.getTranslationKey(), new Object[0])}));
                } else {
                    tooltip.add(TooltipHelper.BLINKING_CYAN + I18n.format("gregtech.recipe.chance", new Object[]{chance, boost}));
                }
            }
        } else if (notConsumed) {
            tooltip.add(TooltipHelper.BLINKING_CYAN + I18n.format("gregtech.recipe.not_consumed", new Object[0]));
        }
    }

    public void drawInfo(@NotNull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        super.drawInfo(minecraft, recipeWidth, recipeHeight, mouseX, mouseY);
        Set<RecipeProperty<?>> properties = this.recipe.getPropertyTypes();
        boolean drawTotalEU = properties.isEmpty() || properties.stream().noneMatch(RecipeProperty::hideTotalEU);
        boolean drawEUt = properties.isEmpty() || properties.stream().noneMatch(RecipeProperty::hideEUt);
        boolean drawDuration = properties.isEmpty() || properties.stream().noneMatch(RecipeProperty::hideDuration);
        int defaultLines = 0;
        if (drawTotalEU) {
            ++defaultLines;
        }

        if (drawEUt) {
            ++defaultLines;
        }

        if (drawDuration) {
            ++defaultLines;
        }

        int yPosition = recipeHeight - ((this.recipe.getUnhiddenPropertyCount() + defaultLines) * 10 - 3);
        if (drawTotalEU) {
            long eu = Math.abs((long)this.recipe.getEUt()) * (long)this.recipe.getDuration();
            if (this.recipe.hasProperty(TotalComputationProperty.getInstance()) && this.recipe.hasProperty(ComputationProperty.getInstance())) {
                int minimumCWUt = (Integer)this.recipe.getProperty(ComputationProperty.getInstance(), 1);
                minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.max_eu", new Object[]{eu / (long)minimumCWUt}), 0, yPosition, 1118481);
            } else {
                minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.total", new Object[]{eu}), 0, yPosition, 1118481);
            }
        }

        FontRenderer var10000;
        String var10001;
        if (drawEUt) {
            var10000 = minecraft.fontRenderer;
            var10001 = I18n.format(this.recipe.getEUt() >= 0 ? "gregtech.recipe.eu" : "gregtech.recipe.eu_inverted", new Object[]{Math.abs(this.recipe.getEUt()), GTValues.VN[GTUtility.getTierByVoltage((long)this.recipe.getEUt())]});
            yPosition += 10;
            var10000.drawString(var10001, 0, yPosition, 1118481);
        }

        if (drawDuration) {
            var10000 = minecraft.fontRenderer;
            var10001 = I18n.format("gregtech.recipe.duration", new Object[]{TextFormattingUtil.formatNumbers((double)this.recipe.getDuration() / 20.0)});
            yPosition += 10;
            var10000.drawString(var10001, 0, yPosition, 1118481);
        }

        Iterator var16 = this.recipe.getPropertyValues().iterator();

        while(var16.hasNext()) {
            Map.Entry<RecipeProperty<?>, Object> propertyEntry = (Map.Entry)var16.next();
            if (!((RecipeProperty)propertyEntry.getKey()).isHidden()) {
                RecipeProperty<?> property = (RecipeProperty)propertyEntry.getKey();
                Object value = propertyEntry.getValue();
                property.drawInfo(minecraft, 0, yPosition += property.getInfoHeight(value), 1118481, value, mouseX, mouseY);
            }
        }

    }

    public @NotNull List<String> getTooltipStrings(int mouseX, int mouseY) {
        List<String> tooltips = new ArrayList();
        Iterator var4 = this.recipe.getPropertyValues().iterator();

        while(var4.hasNext()) {
            Map.Entry<RecipeProperty<?>, Object> entry = (Map.Entry)var4.next();
            if (!((RecipeProperty)entry.getKey()).isHidden()) {
                RecipeProperty<?> property = (RecipeProperty)entry.getKey();
                Object value = entry.getValue();
                property.getTooltipStrings(tooltips, mouseX, mouseY, value);
            }
        }

        return tooltips;
    }

    public void initExtras() {
        if (RecipeCompatUtil.isTweakerLoaded()) {
            BooleanSupplier creativePlayerCtPredicate = () -> {
                return Minecraft.getMinecraft().player != null && Minecraft.getMinecraft().player.isCreative();
            };
            this.buttons.add((new JeiButton(166.0F, 2.0F, 10, 10)).setTextures(new IGuiTexture[]{GuiTextures.BUTTON_CLEAR_GRID}).setTooltipBuilder((lines) -> {
                lines.add("Copies a " + RecipeCompatUtil.getTweakerName() + " script, to remove this recipe, to the clipboard");
            }).setClickAction((minecraft, mouseX, mouseY, mouseButton) -> {
                String recipeLine = RecipeCompatUtil.getRecipeRemoveLine(this.recipeMap, this.recipe);
                String output = RecipeCompatUtil.getFirstOutputString(this.recipe);
                if (!output.isEmpty()) {
                    output = "// " + output + "\n";
                }

                String copyString = output + recipeLine + "\n";
                ClipboardUtil.copyToClipboard(copyString);
                Minecraft.getMinecraft().player.sendMessage(new TextComponentString("Copied [§6" + recipeLine + "§r] to the clipboard"));
                return true;
            }).setActiveSupplier(creativePlayerCtPredicate));
        }
    }

    public ChancedItemOutput getOutputChance(int slot) {
        return slot < this.recipe.getChancedOutputs().getChancedEntries().size() && slot >= 0 ? (ChancedItemOutput)this.recipe.getChancedOutputs().getChancedEntries().get(slot) : null;
    }

    public ChancedOutputLogic getChancedOutputLogic() {
        return this.recipe.getChancedOutputs().getChancedOutputLogic();
    }

    public ChancedFluidOutput getFluidOutputChance(int slot) {
        return slot < this.recipe.getChancedFluidOutputs().getChancedEntries().size() && slot >= 0 ? (ChancedFluidOutput)this.recipe.getChancedFluidOutputs().getChancedEntries().get(slot) : null;
    }

    public ChancedOutputLogic getChancedFluidOutputLogic() {
        return this.recipe.getChancedFluidOutputs().getChancedOutputLogic();
    }

    public boolean isNotConsumedItem(int slot) {
        return slot < this.sortedInputs.size() && ((GTRecipeInput)this.sortedInputs.get(slot)).isNonConsumable();
    }

    public boolean isNotConsumedFluid(int slot) {
        return slot < this.sortedFluidInputs.size() && ((GTRecipeInput)this.sortedFluidInputs.get(slot)).isNonConsumable();
    }
}

