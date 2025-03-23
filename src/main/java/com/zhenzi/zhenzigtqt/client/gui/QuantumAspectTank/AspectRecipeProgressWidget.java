package com.zhenzi.zhenzigtqt.client.gui.QuantumAspectTank;

import com.zhenzi.zhenzigtqt.integration.jei.recipes.ZZRecipeMapCategory;
import com.zhenzi.zhenzigtqt.loaders.AspectRecipeMap;
import gregtech.api.GregTechAPI;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.gui.widgets.RecipeProgressWidget;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.integration.IntegrationModule;
import gregtech.integration.jei.JustEnoughItemsModule;
import gregtech.integration.jei.recipe.RecipeMapCategory;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.DoubleSupplier;

public class AspectRecipeProgressWidget extends ProgressWidget {
    private final AspectRecipeMap<?> recipeMap;

    public AspectRecipeProgressWidget(DoubleSupplier progressSupplier, int x, int y, int width, int height, AspectRecipeMap<?> recipeMap) {
        super(progressSupplier, x, y, width, height);
        this.recipeMap = recipeMap;
        this.setHoverTextConsumer((list) -> {
            list.add(new TextComponentTranslation("gui.widget.recipeProgressWidget.default_tooltip", new Object[0]));
        });
    }

    public AspectRecipeProgressWidget(DoubleSupplier progressSupplier, int x, int y, int width, int height, TextureArea fullImage, ProgressWidget.MoveType moveType, AspectRecipeMap<?> recipeMap) {
        super(progressSupplier, x, y, width, height, fullImage, moveType);
        this.recipeMap = recipeMap;
        this.setHoverTextConsumer((list) -> {
            list.add(new TextComponentTranslation("gui.widget.recipeProgressWidget.default_tooltip", new Object[0]));
        });
    }

    public AspectRecipeProgressWidget(int ticksPerCycle, int x, int y, int width, int height, TextureArea fullImage, ProgressWidget.MoveType moveType, AspectRecipeMap<?> recipeMap) {
        super(ticksPerCycle, x, y, width, height, fullImage, moveType);
        this.recipeMap = recipeMap;
        this.setHoverTextConsumer((list) -> {
            list.add(new TextComponentTranslation("gui.widget.recipeProgressWidget.default_tooltip", new Object[0]));
        });
    }

    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (!GregTechAPI.moduleManager.isModuleEnabled("jei_integration")) {
            return false;
        } else {
            if (this.isMouseOverElement(mouseX, mouseY)) {
                Collection<ZZRecipeMapCategory> categories = ZZRecipeMapCategory.getCategoriesFor(this.recipeMap);
                if (categories != null && !categories.isEmpty()) {
                    List<String> categoryID = new ArrayList();
                        for (ZZRecipeMapCategory aspectRecipeMap : categories) {
                            ZZRecipeMapCategory category = (ZZRecipeMapCategory) aspectRecipeMap;
                            categoryID.add(category.getUid());
                        }
                    if (JustEnoughItemsModule.jeiRuntime == null) {
                        IntegrationModule.logger.error("GTCEu JEI integration has crashed, this is not a good thing");
                        return false;
                    }

                    JustEnoughItemsModule.jeiRuntime.getRecipesGui().showCategories(categoryID);
                    return true;
                }
            }

            return false;
        }
    }
}
