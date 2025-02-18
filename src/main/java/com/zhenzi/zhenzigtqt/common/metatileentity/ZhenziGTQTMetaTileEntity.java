package com.zhenzi.zhenzigtqt.common.metatileentity;

import com.zhenzi.zhenzigtqt.ZhenziGtqt;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.util.ResourceLocation;

import static gregtech.common.metatileentities.MetaTileEntities.registerMetaTileEntity;

public class ZhenziGTQTMetaTileEntity {
    public static ResourceLocation gtqtcoreId(String id) {
        return new ResourceLocation(ZhenziGtqt.MODID, id);
    }

    public static MetaTileEntity[] MULTI_QUANTUM_TANK = new MetaTileEntity[6];

    public static void initialization() {
        MULTI_QUANTUM_TANK[0] = new MetaTileEntityMultiQuantumTank(gtqtcoreId("multi_quantum_tank_lv"), 1, 4000000 / 4, 4);
        MULTI_QUANTUM_TANK[1] = new MetaTileEntityMultiQuantumTank(gtqtcoreId("multi_quantum_tank_mv"), 2, 8000000 / 4, 4);
        MULTI_QUANTUM_TANK[2] = new MetaTileEntityMultiQuantumTank(gtqtcoreId("multi_quantum_tank_hv"), 3, 16000000 / 4, 4);
        MULTI_QUANTUM_TANK[3] = new MetaTileEntityMultiQuantumTank(gtqtcoreId("multi_quantum_tank_ev"), 4, 32000000 / 4, 4);
        MULTI_QUANTUM_TANK[4] = new MetaTileEntityMultiQuantumTank(gtqtcoreId("multi_quantum_tank_iv"), 5, 64000000 / 4, 4);

        MULTI_QUANTUM_TANK[5] = new MetaTileEntityMultiQuantumTank(gtqtcoreId("multi_quantum_tank_zpm"), 6, 4000000 / 4, 9);
        registerMetaTileEntity(30001, MULTI_QUANTUM_TANK[0]);
        registerMetaTileEntity(30002, MULTI_QUANTUM_TANK[1]);
        registerMetaTileEntity(30003, MULTI_QUANTUM_TANK[2]);
        registerMetaTileEntity(30004, MULTI_QUANTUM_TANK[3]);
        registerMetaTileEntity(30005, MULTI_QUANTUM_TANK[4]);
        registerMetaTileEntity(30006, MULTI_QUANTUM_TANK[5]);
    }
}
