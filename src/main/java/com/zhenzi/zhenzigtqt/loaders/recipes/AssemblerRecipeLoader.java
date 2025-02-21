package com.zhenzi.zhenzigtqt.loaders.recipes;

import com.zhenzi.zhenzigtqt.common.metatileentity.ZhenziGTQTMetaTileEntity;
import gregtech.api.GTValues;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.builders.AssemblerRecipeBuilder;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockHermeticCasing;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
import net.minecraft.item.ItemStack;


public class AssemblerRecipeLoader {
    public AssemblerRecipeLoader() {
    }
    public static void init() {
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(MetaBlocks.HERMETIC_CASING.getItemVariant(BlockHermeticCasing.HermeticCasingsType.HERMETIC_LV).getItem(), 4, GTValues.LV - 1)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.LV, 4)
                .input(MetaItems.ELECTRIC_PUMP_LV)
                .output(ZhenziGTQTMetaTileEntity.MULTI_QUANTUM_TANK[0], 1)
                .duration(200)
                .EUt(120)
                .buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(MetaBlocks.HERMETIC_CASING.getItemVariant(BlockHermeticCasing.HermeticCasingsType.HERMETIC_MV).getItem(), 4, GTValues.MV - 1)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.MV, 4)
                .input(MetaItems.ELECTRIC_PUMP_MV)
                .output(ZhenziGTQTMetaTileEntity.MULTI_QUANTUM_TANK[1], 1)
                .duration(200)
                .EUt(120)
                .buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(MetaBlocks.HERMETIC_CASING.getItemVariant(BlockHermeticCasing.HermeticCasingsType.HERMETIC_HV).getItem(), 4, GTValues.HV - 1)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.HV, 4)
                .input(MetaItems.ELECTRIC_PUMP_HV)
                .output(ZhenziGTQTMetaTileEntity.MULTI_QUANTUM_TANK[2], 1)
                .duration(200)
                .EUt(120)
                .buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(MetaBlocks.HERMETIC_CASING.getItemVariant(BlockHermeticCasing.HermeticCasingsType.HERMETIC_EV).getItem(), 9, GTValues.EV - 1)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.EV, 4)
                .input(MetaItems.ELECTRIC_PUMP_EV)
                .output(ZhenziGTQTMetaTileEntity.MULTI_QUANTUM_TANK[3], 1)
                .duration(200)
                .EUt(120)
                .buildAndRegister();
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(MetaBlocks.HERMETIC_CASING.getItemVariant(BlockHermeticCasing.HermeticCasingsType.HERMETIC_IV).getItem(), 9, GTValues.IV - 1)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.IV, 4)
                .input(MetaItems.ELECTRIC_PUMP_IV)
                .output(ZhenziGTQTMetaTileEntity.MULTI_QUANTUM_TANK[4], 1)
                .duration(200)
                .EUt(120)
                .buildAndRegister();
    }
}
