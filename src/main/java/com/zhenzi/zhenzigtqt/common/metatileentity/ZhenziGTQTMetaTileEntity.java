package com.zhenzi.zhenzigtqt.common.metatileentity;

import com.zhenzi.zhenzigtqt.ZhenziGtqt;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.util.ResourceLocation;

import static gregtech.common.metatileentities.MetaTileEntities.registerMetaTileEntity;

public class ZhenziGTQTMetaTileEntity {
    public static ResourceLocation gtqtcoreId(String id) {
        return new ResourceLocation(ZhenziGtqt.MODID, id);
    }

    public static MetaTileEntity[] MULTI_QUANTUM_TANK = new MetaTileEntity[10];
    public static MetaTileEntity[] ASPECT_TANK = new MetaTileEntity[10];

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

        ASPECT_TANK[0] = new MetaTileEntityAspectTank(gtqtcoreId("aspect_tank_lv"), 1, 10000);
        ASPECT_TANK[1] = new MetaTileEntityAspectTank(gtqtcoreId("aspect_tank_mv"), 2, 20000);
        ASPECT_TANK[2] = new MetaTileEntityAspectTank(gtqtcoreId("aspect_tank_hv"), 3, 40000);
        ASPECT_TANK[3] = new MetaTileEntityAspectTank(gtqtcoreId("aspect_tank_ev"), 4, 80000);
        ASPECT_TANK[4] = new MetaTileEntityAspectTank(gtqtcoreId("aspect_tank_iv"), 5, 160000);
        ASPECT_TANK[5] = new MetaTileEntityAspectTank(gtqtcoreId("aspect_tank_luv"), 6, 320000);
        ASPECT_TANK[6] = new MetaTileEntityAspectTank(gtqtcoreId("aspect_tank_zpm"), 7, 640000);
        ASPECT_TANK[7] = new MetaTileEntityAspectTank(gtqtcoreId("aspect_tank_uv"), 8, 1280000);
        ASPECT_TANK[8] = new MetaTileEntityAspectTank(gtqtcoreId("aspect_tank_uhv"), 9, 2560000);

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
        registerMetaTileEntity(30011, ASPECT_TANK[0]);
        registerMetaTileEntity(30012, ASPECT_TANK[1]);
        registerMetaTileEntity(30013, ASPECT_TANK[2]);
        registerMetaTileEntity(30014, ASPECT_TANK[3]);
        registerMetaTileEntity(30015, ASPECT_TANK[4]);
        registerMetaTileEntity(30016, ASPECT_TANK[5]);
        registerMetaTileEntity(30017, ASPECT_TANK[6]);
        registerMetaTileEntity(30018, ASPECT_TANK[7]);
        registerMetaTileEntity(30019, ASPECT_TANK[8]);

    }


}
