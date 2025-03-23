package com.zhenzi.zhenzigtqt.loaders;

import com.zhenzi.zhenzigtqt.loaders.builder.AspectGeneratorRecipeBuilder;
import com.zhenzi.zhenzigtqt.loaders.builder.AspectSimpleRecipeBuilder;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.core.sound.GTSoundEvents;
import net.minecraft.init.SoundEvents;

public class ZZRecipeMaps {
    public static final AspectRecipeMap<AspectGeneratorRecipeBuilder> ASPECT_GENERATOR_RECIPES;
    public static final AspectRecipeMap<AspectSimpleRecipeBuilder> CLATHRATE_ESSENCE_FORMER_RECIPES;
    static {
        ASPECT_GENERATOR_RECIPES = new AspectRecipeMap("aspect_generator",
                0,
                0,
                0,
                0,
                1,
                0,
                new AspectGeneratorRecipeBuilder(),
                false)
                .setSlotOverlay(false, false, true, true, GuiTextures.FURNACE_OVERLAY_2)
                .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, ProgressWidget.MoveType.HORIZONTAL)
                .setSound(GTSoundEvents.COMBUSTION).allowEmptyOutput();

        CLATHRATE_ESSENCE_FORMER_RECIPES = (new AspectRecipeMap("clatherate_essence_former",
                0,
                6,
                0,
                0,
                1,
                0,
                new AspectSimpleRecipeBuilder(),
                false))
                .setProgressBar(GuiTextures.PROGRESS_BAR_SIFT, ProgressWidget.MoveType.VERTICAL_DOWNWARDS)
                .setSound(SoundEvents.BLOCK_SAND_PLACE);
    }


}
