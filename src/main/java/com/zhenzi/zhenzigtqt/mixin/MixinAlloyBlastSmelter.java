//package com.zhenzi.zhenzigtqt.mixin;
//
//import gregtech.api.metatileentity.MetaTileEntity;
//import gregtech.api.metatileentity.multiblock.MultiblockAbility;
//import gregtech.api.pattern.BlockPattern;
//import gregtech.api.pattern.FactoryBlockPattern;
//import gregtech.api.pattern.TraceabilityPredicate;
//import gregtech.api.recipes.RecipeMap;
//import keqing.pollution.api.metatileentity.POMultiblockAbility;
//import keqing.pollution.api.metatileentity.PORecipeMapMultiblockController;
//import keqing.pollution.api.predicate.TiredTraceabilityPredicate;
//import keqing.pollution.common.block.PollutionMetaBlocks;
//import keqing.pollution.common.block.metablocks.POMagicBlock;
//import keqing.pollution.common.metatileentity.multiblock.MetaTileEntityMagicAlloyBlastSmelter;
//import net.minecraft.block.state.IBlockState;
//import net.minecraft.util.ResourceLocation;
//import org.spongepowered.asm.mixin.Mixin;
//
//@Mixin(MetaTileEntityMagicAlloyBlastSmelter.class)
//public abstract class MixinAlloyBlastSmelter extends PORecipeMapMultiblockController {
//
//    public MixinAlloyBlastSmelter(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap) {
//        super(metaTileEntityId, recipeMap);
//    }
//
//    @Override
//    protected BlockPattern createStructurePattern() {
//        return FactoryBlockPattern.start()
//                .aisle(new String[]{"#XXX#", "#CCC#", "#GGG#", "#CCC#", "#XXX#"})
//                .aisle(new String[]{"XXXXX", "CAAAC", "GAAAG", "CAAAC", "XXXXX"})
//                .aisle(new String[]{"XXXXX", "CAAAC", "GAAAG", "CAAAC", "XXMXX"})
//                .aisle(new String[]{"XXXXX", "CAAAC", "GAAAG", "CAAAC", "XXXXX"})
//                .aisle(new String[]{"#FSX#", "#CCC#", "#GGG#", "#CCC#", "#XXX#"})
//                .where('S', this.selfPredicate())
//                .where('X', states(new IBlockState[]{getCasingState1()}).setMinGlobalLimited(9).or(this.autoAbilities(true, true, true, true, true, true, false)))
//                .where('M', abilities(new MultiblockAbility[]{MultiblockAbility.MUFFLER_HATCH}))
//                .where('F', abilities(new MultiblockAbility[]{POMultiblockAbility.VIS_HATCH}).setMaxGlobalLimited(1).setPreviewCount(1))
//                .where('C', (TraceabilityPredicate) TiredTraceabilityPredicate.CP_COIL_CASING.get())
//                .where('G', states(new IBlockState[]{getCasingState2()}))
//                .where('#', any())
//                .where('A', air())
//                .build();
//    }
//
//    private static IBlockState getCasingState1() {
//        return PollutionMetaBlocks.MAGIC_BLOCK.getState(POMagicBlock.MagicBlockType.SPELL_PRISM_HOT);
//    }
//
//    private static IBlockState getCasingState2() {
//        return PollutionMetaBlocks.MAGIC_BLOCK.getState(POMagicBlock.MagicBlockType.ALLOY_BLAST_CASING);
//    }
//}
