package com.zhenzi.zhenzigtqt.integration.jei;

import com.google.common.collect.ImmutableList;
import com.zhenzi.zhenzigtqt.common.lib.aspect.AspectStack;
import com.zhenzi.zhenzigtqt.common.metatileentity.AspectMetaTileEntity;
import com.zhenzi.zhenzigtqt.common.metatileentity.SimpleMachineAspectMetaTileEntity;
import com.zhenzi.zhenzigtqt.common.metatileentity.ZhenziGTQTMetaTileEntity;
import com.zhenzi.zhenzigtqt.integration.jei.recipes.ZZRecipeCategory;
import com.zhenzi.zhenzigtqt.integration.jei.recipes.ZZRecipeMapCategory;
import com.zhenzi.zhenzigtqt.integration.jei.recipes.ZZRecipeWrapper;
import com.zhenzi.zhenzigtqt.loaders.*;
import gregtech.api.GregTechAPI;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SteamMetaTileEntity;
import gregtech.api.modules.GregTechModule;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.category.GTRecipeCategory;
import gregtech.api.recipes.machines.IScannerRecipeMap;
import gregtech.common.gui.widget.craftingstation.CraftingSlotWidget;
import gregtech.integration.IntegrationSubmodule;
import gregtech.integration.jei.recipe.GTRecipeWrapper;
import gregtech.integration.jei.recipe.IntCircuitCategory;
import gregtech.integration.jei.recipe.IntCircuitRecipeWrapper;
import gregtech.integration.jei.utils.ModularUIGuiHandler;
import gregtech.integration.jei.utils.MultiblockInfoRecipeFocusShower;
import mezz.jei.Internal;
import mezz.jei.api.*;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.ingredients.IngredientRegistry;
import mezz.jei.input.IShowsRecipeFocuses;
import mezz.jei.input.InputHandler;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@GregTechModule(
        moduleID = "jei_integration",
        containerID = "zhenzigtqt",
        modDependencies = {"jei"},
        name = "GregTech JEI Integration",
        description = "JustEnoughItems Integration Module"
)
@JEIPlugin
public class ZZJustEnoughItemsModule extends IntegrationSubmodule implements IModPlugin {
    public static IIngredientRegistry ingredientRegistry;
    public static IJeiRuntime jeiRuntime;
    public static IGuiHelper guiHelper;

    public ZZJustEnoughItemsModule() {
    }

    public void loadComplete(FMLLoadCompleteEvent event) {
        if (event.getSide() == Side.CLIENT) {
            this.setupInputHandler();
        }

    }

    public void onRuntimeAvailable(@NotNull IJeiRuntime jeiRuntime) {
        gregtech.integration.jei.JustEnoughItemsModule.jeiRuntime = jeiRuntime;
    }

    public void registerItemSubtypes(@NotNull ISubtypeRegistry subtypeRegistry) {

    }

    public void registerCategories(@NotNull IRecipeCategoryRegistration registry) {
        guiHelper = registry.getJeiHelpers().getGuiHelper();
        List<AspectRecipeMap<?>> iterator = AspectRecipeMap.getRecipeMapsA();

        for (AspectRecipeMap<?> aspectRecipeMap : iterator)
        {
            for (Map.Entry<ZZRecipeCategory, List<AspectRecipe>> entry : aspectRecipeMap.getRecipesByCategoryA().entrySet())
            {
                registry.addRecipeCategories(new ZZRecipeMapCategory(aspectRecipeMap, entry.getKey(), registry.getJeiHelpers().getGuiHelper()));
            }
        }
    }

    public void registerIngredients(IModIngredientRegistration registry) {
//        registry.register(AspectTypes.ASPECT, AspectStack.getAllAspects(), new AspectStackHelper(), new AspectStackRenderer());
    }

    public void register(IModRegistry registry) {
        IJeiHelpers jeiHelpers = registry.getJeiHelpers();
//        registry.addRecipes(IntCircuitRecipeWrapper.create(), IntCircuitCategory.UID);
//        MultiblockInfoCategory.registerRecipes(registry);
//        registry.addRecipeRegistryPlugin(new FacadeRegistryPlugin());
        ModularUIGuiHandler modularUIGuiHandler = new ModularUIGuiHandler(jeiHelpers.recipeTransferHandlerHelper());
        modularUIGuiHandler.setValidHandlers((widget) -> {
            return !(widget instanceof CraftingSlotWidget);
        });
//        modularUIGuiHandler.blacklistCategory(new String[]{IntCircuitCategory.UID, "gregtech:material_tree", "jei.information", "minecraft.fuel"});
        registry.getRecipeTransferRegistry().addRecipeTransferHandler(modularUIGuiHandler, "universal recipe transfer handler");
        registry.addAdvancedGuiHandlers(new IAdvancedGuiHandler[]{modularUIGuiHandler});
        registry.addGhostIngredientHandler(modularUIGuiHandler.getGuiContainerClass(), modularUIGuiHandler);
        ModularUIGuiHandler craftingStationGuiHandler = new ModularUIGuiHandler(jeiHelpers.recipeTransferHandlerHelper());
        registry.getRecipeTransferRegistry().addRecipeTransferHandler(craftingStationGuiHandler, "minecraft.crafting");
        List<AspectRecipeMap<?>> var5 = AspectRecipeMap.getRecipeMapsA();

//        registry.addIngredientInfo();
        for (AspectRecipeMap<?> aspectRecipeMap : var5)
        {
            for (Map.Entry<ZZRecipeCategory, List<AspectRecipe>> entry : aspectRecipeMap.getRecipesByCategoryA().entrySet())
            {
                for (AspectRecipe recipe : entry.getValue())
                {
                    Collection<ZZRecipeWrapper> collection = ImmutableList.of(new ZZRecipeWrapper(aspectRecipeMap, recipe));
                    registry.addRecipes(collection, entry.getKey().getUniqueID());
                }
            }
        }
        this.registerRecipeMapCatalyst(registry, ZZRecipeMaps.ASPECT_GENERATOR_RECIPES, ZhenziGTQTMetaTileEntity.ASPECT_GENERATOR[0]);

        for (SimpleMachineAspectMetaTileEntity metaTileEntity : ZhenziGTQTMetaTileEntity.CLATHRATE_ESSENCE_FORMER)
        {
            this.registerRecipeMapCatalyst(registry, ZZRecipeMaps.CLATHRATE_ESSENCE_FORMER_RECIPES, metaTileEntity);
        }
    }

    private void setupInputHandler() {
        try {
            Field inputHandlerField = Internal.class.getDeclaredField("inputHandler");
            inputHandlerField.setAccessible(true);
            InputHandler inputHandler = (InputHandler)inputHandlerField.get((Object)null);
            List<IShowsRecipeFocuses> showsRecipeFocuses = (List) ObfuscationReflectionHelper.getPrivateValue(InputHandler.class, inputHandler, "showsRecipeFocuses");
            showsRecipeFocuses.add(new MultiblockInfoRecipeFocusShower());
        } catch (IllegalArgumentException | IllegalAccessException | SecurityException | NoSuchFieldException var4) {
            this.getLogger().error("Could not reflect JEI Internal inputHandler", var4);
        }

    }

    private void registerRecipeMapCatalyst(IModRegistry registry, AspectRecipeMap<?> recipeMap, MetaTileEntity metaTileEntity) {
        Iterator var4 = recipeMap.getRecipesByCategoryA().keySet().iterator();

        ZZRecipeCategory category;
        ZZRecipeMapCategory jeiCategory;
        while(var4.hasNext()) {
            category = (ZZRecipeCategory)var4.next();
            jeiCategory = ZZRecipeMapCategory.getCategoryFor(category);
            if (jeiCategory != null) {
                registry.addRecipeCatalyst(metaTileEntity.getStackForm(), jeiCategory.getUid());
            }
        }

        if (recipeMap.getSmallRecipeMap() != null) {
            registry.addRecipeCatalyst(metaTileEntity.getStackForm(), "zhenzigtqt:" + recipeMap.getSmallRecipeMap().unlocalizedName);
        } else {
            var4 = recipeMap.getRecipesByCategoryA().keySet().iterator();

            while(var4.hasNext()) {
                category = (ZZRecipeCategory)var4.next();
                jeiCategory = ZZRecipeMapCategory.getCategoryFor(category);
                if (jeiCategory != null && !(metaTileEntity instanceof SteamMetaTileEntity)) {
                    Object icon = category.getJEIIcon();
                    if (icon instanceof TextureArea) {
                        TextureArea textureArea = (TextureArea)icon;
                        icon = guiHelper.drawableBuilder(textureArea.imageLocation, 0, 0, 18, 18).setTextureSize(18, 18).build();
                    } else if (icon == null) {
                        icon = metaTileEntity.getStackForm();
                    }

                    jeiCategory.setIcon(icon);
                }
            }

        }
    }
}
