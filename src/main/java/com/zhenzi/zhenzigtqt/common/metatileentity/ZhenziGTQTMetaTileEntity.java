package com.zhenzi.zhenzigtqt.common.metatileentity;

import com.zhenzi.zhenzigtqt.ZhenziGtqt;
import com.zhenzi.zhenzigtqt.loaders.AspectRecipeMap;
import com.zhenzi.zhenzigtqt.loaders.ZZRecipeMaps;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SimpleGeneratorMetaTileEntity;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.util.ResourceLocation;

import java.util.function.BiFunction;
import java.util.function.Function;

import static com.zhenzi.zhenzigtqt.common.metatileentity.AspectMetaTileEntity.registerMetaTileEntityA;
import static gregtech.common.metatileentities.MetaTileEntities.*;
import static gregtech.common.metatileentities.MetaTileEntities.registerMetaTileEntity;

public class ZhenziGTQTMetaTileEntity {
    public static ResourceLocation gtqtcoreId(String id) {
        return new ResourceLocation(ZhenziGtqt.MODID, id);
    }

    public static MetaTileEntity[] MULTI_QUANTUM_TANK = new MetaTileEntity[10];
    public static MetaTileEntity[] ASPECT_TANK = new MetaTileEntity[10];

    public static MetaTileEntity[] ASPECT_TANK_C = new MetaTileEntity[10];
    public static AspectMetaTileEntity[] ASPECT_GENERATOR = new AspectMetaTileEntity[10];
    public static SimpleMachineAspectMetaTileEntity[] CLATHRATE_ESSENCE_FORMER = new SimpleMachineAspectMetaTileEntity[GTValues.V.length - 1];

    public static void initialization() {
        MULTI_QUANTUM_TANK[0] = new MetaTileEntityMultiQuantumTank(gtqtcoreId("multi_quantum_tank_lv"), 1, 4000000 / 4, 4);
        MULTI_QUANTUM_TANK[1] = new MetaTileEntityMultiQuantumTank(gtqtcoreId("multi_quantum_tank_mv"), 2, 8000000 / 4, 4);
        MULTI_QUANTUM_TANK[2] = new MetaTileEntityMultiQuantumTank(gtqtcoreId("multi_quantum_tank_hv"), 3, 16000000 / 4, 4);

        MULTI_QUANTUM_TANK[3] = new MetaTileEntityMultiQuantumTank(gtqtcoreId("multi_quantum_tank_ev"), 4, 36000000 / 9, 9);
        MULTI_QUANTUM_TANK[4] = new MetaTileEntityMultiQuantumTank(gtqtcoreId("multi_quantum_tank_iv"), 5, 72000000 / 9, 9);

        MULTI_QUANTUM_TANK[5] = new MetaTileEntityMultiQuantumTank(gtqtcoreId("multi_quantum_tank_iv1"), 5, 64000000 / 16, 16);
        MULTI_QUANTUM_TANK[6] = new MetaTileEntityMultiQuantumTank(gtqtcoreId("multi_quantum_tank_luv"), 6, 128000000 / 16, 16);
        MULTI_QUANTUM_TANK[7] = new MetaTileEntityMultiQuantumTank(gtqtcoreId("multi_quantum_tank_zpm"), 7, 256000000 / 16, 16);

        MULTI_QUANTUM_TANK[8] = new MetaTileEntityMultiQuantumTank(gtqtcoreId("multi_quantum_tank_uv"), 8, 400000000 / 25, 25);
        MULTI_QUANTUM_TANK[9] = new MetaTileEntityMultiQuantumTank(gtqtcoreId("multi_quantum_tank_uhv"), 9, 2000000000 / 25, 25);

//        ASPECT_TANK[0] = new MetaTileEntityAspectTank(gtqtcoreId("aspect_tank_lv"), 1, 10000);
//        ASPECT_TANK[1] = new MetaTileEntityAspectTank(gtqtcoreId("aspect_tank_mv"), 2, 20000);
//        ASPECT_TANK[2] = new MetaTileEntityAspectTank(gtqtcoreId("aspect_tank_hv"), 3, 40000);
//        ASPECT_TANK[3] = new MetaTileEntityAspectTank(gtqtcoreId("aspect_tank_ev"), 4, 80000);
//        ASPECT_TANK[4] = new MetaTileEntityAspectTank(gtqtcoreId("aspect_tank_iv"), 5, 160000);
//        ASPECT_TANK[5] = new MetaTileEntityAspectTank(gtqtcoreId("aspect_tank_luv"), 6, 320000);
//        ASPECT_TANK[6] = new MetaTileEntityAspectTank(gtqtcoreId("aspect_tank_zpm"), 7, 640000);
//        ASPECT_TANK[7] = new MetaTileEntityAspectTank(gtqtcoreId("aspect_tank_uv"), 8, 1280000);
//        ASPECT_TANK[8] = new MetaTileEntityAspectTank(gtqtcoreId("aspect_tank_uhv"), 9, 2560000);

        ASPECT_TANK_C[0] = new CMetaTileEntityAspectTank(gtqtcoreId("aspect_tank_lv"), 1, 10000);
        ASPECT_TANK_C[1] = new CMetaTileEntityAspectTank(gtqtcoreId("aspect_tank_mv"), 1, 20000);
        ASPECT_TANK_C[2] = new CMetaTileEntityAspectTank(gtqtcoreId("aspect_tank_hv"), 1, 40000);
        ASPECT_TANK_C[3] = new CMetaTileEntityAspectTank(gtqtcoreId("aspect_tank_ev"), 1, 80000);
        ASPECT_TANK_C[4] = new CMetaTileEntityAspectTank(gtqtcoreId("aspect_tank_iv"), 1, 160000);
        ASPECT_TANK_C[5] = new CMetaTileEntityAspectTank(gtqtcoreId("aspect_tank_luv"), 1, 320000);
        ASPECT_TANK_C[6] = new CMetaTileEntityAspectTank(gtqtcoreId("aspect_tank_zpm"), 1, 640000);
        ASPECT_TANK_C[7] = new CMetaTileEntityAspectTank(gtqtcoreId("aspect_tank_uv"), 1, 1280000);
        ASPECT_TANK_C[8] = new CMetaTileEntityAspectTank(gtqtcoreId("aspect_tank_uhv"), 1, 2560000);

        ASPECT_GENERATOR[0] = new MetaTileEntityAspectGenerator(gtqtcoreId("aspect_generator"), ZZRecipeMaps.ASPECT_GENERATOR_RECIPES, Textures.COMBUSTION_GENERATOR_OVERLAY, 1, GTUtility.genericGeneratorTankSizeFunction);
//        ASPECT_GENERATOR[1] = new SimpleGeneratorMetaTileEntity(gtqtcoreId("aspect_generator"), ZZRecipeMaps.ASPECT_GENERATOR_RECIPES, Textures.COMBUSTION_GENERATOR_OVERLAY, 1, GTUtility.genericGeneratorTankSizeFunction);

//        CLATHRATE_ESSENCE_FORMER = new SimpleMachineAspectMetaTileEntity[GTValues.V.length - 1];
        CLATHRATE_ESSENCE_FORMER[0] = new SimpleMachineAspectMetaTileEntity(gtqtcoreId("clatherate_essence_former.lv"), ZZRecipeMaps.CLATHRATE_ESSENCE_FORMER_RECIPES, Textures.SIFTER_OVERLAY, 1, true);
        CLATHRATE_ESSENCE_FORMER[1] = new SimpleMachineAspectMetaTileEntity(gtqtcoreId("clatherate_essence_former.hv"), ZZRecipeMaps.CLATHRATE_ESSENCE_FORMER_RECIPES, Textures.SIFTER_OVERLAY, 3, true);
        CLATHRATE_ESSENCE_FORMER[2] = new SimpleMachineAspectMetaTileEntity(gtqtcoreId("clatherate_essence_former.ev"), ZZRecipeMaps.CLATHRATE_ESSENCE_FORMER_RECIPES, Textures.SIFTER_OVERLAY, 4, true);


        registerMetaTileEntity(30001, MULTI_QUANTUM_TANK[0]);
        registerMetaTileEntity(30002, MULTI_QUANTUM_TANK[1]);
        registerMetaTileEntity(30003, MULTI_QUANTUM_TANK[2]);
        registerMetaTileEntity(30004, MULTI_QUANTUM_TANK[3]);
        registerMetaTileEntity(30005, MULTI_QUANTUM_TANK[4]);
        registerMetaTileEntity(30006, MULTI_QUANTUM_TANK[5]);
        registerMetaTileEntity(30007, MULTI_QUANTUM_TANK[6]);
        registerMetaTileEntity(30008, MULTI_QUANTUM_TANK[7]);
        registerMetaTileEntity(30009, MULTI_QUANTUM_TANK[8]);
        registerMetaTileEntity(30010, MULTI_QUANTUM_TANK[9]);
        registerMetaTileEntity(30011, ASPECT_TANK_C[0]);
        registerMetaTileEntity(30012, ASPECT_TANK_C[1]);
        registerMetaTileEntity(30013, ASPECT_TANK_C[2]);
        registerMetaTileEntity(30014, ASPECT_TANK_C[3]);
        registerMetaTileEntity(30015, ASPECT_TANK_C[4]);
        registerMetaTileEntity(30016, ASPECT_TANK_C[5]);
        registerMetaTileEntity(30017, ASPECT_TANK_C[6]);
        registerMetaTileEntity(30018, ASPECT_TANK_C[7]);
        registerMetaTileEntity(30019, ASPECT_TANK_C[8]);
//        registerMetaTileEntity(30020, ASPECT_TANK_C[0]);
        registerMetaTileEntity(30021, ASPECT_GENERATOR[0]);

        registerMetaTileEntity(30025, CLATHRATE_ESSENCE_FORMER[0]);
        registerMetaTileEntity(30026, CLATHRATE_ESSENCE_FORMER[1]);
        registerMetaTileEntity(30027, CLATHRATE_ESSENCE_FORMER[2]);
//        registerSimpleMetaTileEntity(CLATHRATE_ESSENCE_FORMER, 30025, "clatherate_essence_former", ZZRecipeMaps.CLATHRATE_ESSENCE_FORMER_RECIPES, Textures.SIFTER_OVERLAY, true);
    }

//    private static void registerSimpleMetaTileEntity(SimpleMachineAspectMetaTileEntity[] machines, int startId, String name, AspectRecipeMap<?> map, ICubeRenderer texture, boolean hasFrontFacing, Function<Integer, Integer> tankScalingFunction) {
//        registerSimpleMetaTileEntity(machines, startId, name, map, texture, hasFrontFacing, GTUtility::gregtechId, tankScalingFunction);
//    }
//
//    private static void registerSimpleMetaTileEntity(SimpleMachineAspectMetaTileEntity[] machines, int startId, String name, AspectRecipeMap<?> map, ICubeRenderer texture, boolean hasFrontFacing) {
//        registerSimpleMetaTileEntity(machines, startId, name, map, texture, hasFrontFacing, GTUtility.defaultTankSizeFunction);
//    }
//
//    public static void registerSimpleMetaTileEntity(SimpleMachineAspectMetaTileEntity[] machines, int startId, String name, AspectRecipeMap<?> map, ICubeRenderer texture, boolean hasFrontFacing, Function<String, ResourceLocation> resourceId, Function<Integer, Integer> tankScalingFunction) {
//        for (int i = 0; i < GTValues.V.length; i++)
//        {
//            registerMetaTileEntities(machines, startId, name);
//        }
//    }
//
//    public static void registerMetaTileEntities(AspectMetaTileEntity[] machines, int startId, String name) {
//        for(int i = 0; i < machines.length - 1; ++i) {
//            if (i <= 4 || getMidTier(name)) {
//                if (i > 7 && !getHighTier(name)) {
//                    break;
//                }
//
//                String voltageName = GTValues.VN[i + 1].toLowerCase();
//                registerMetaTileEntity(startId + i, machines[i + 1]);
////                machines[i + 1] = registerMetaTileEntity(startId, )
////                machines[i + 1] = registerMetaTileEntity(startId + i, (AspectMetaTileEntity)mteCreator.apply(i + 1, voltageName));
//            }
//        }
//
//    }
//
//    public static void registerMetaTileEntities(AspectMetaTileEntity[] machines, int startId, String name, BiFunction<Integer, String, AspectMetaTileEntity> mteCreator) {
//        for(int i = 0; i < machines.length - 1; ++i) {
//            if (i <= 4 || getMidTier(name)) {
//                if (i > 7 && !getHighTier(name)) {
//                    break;
//                }
//
//                String voltageName = GTValues.VN[i + 1].toLowerCase();
//                machines[i + 1] = registerMetaTileEntity(startId + i, (AspectMetaTileEntity)mteCreator.apply(i + 1, voltageName));
//            }
//        }
//
//    }


}
