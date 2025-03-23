//package com.zhenzi.zhenzigtqt.mixin;
//
//import com.drppp.drtech.api.capability.impl.BatteryEnergyContainerHandler;
//import gregtech.api.capability.IElectricItem;
//import gregtech.api.capability.IEnergyContainer;
//import gregtech.api.metatileentity.MTETrait;
//import gregtech.api.metatileentity.MetaTileEntity;
//import gregtech.api.util.GTUtility;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//
//import java.util.List;
//
//@Mixin(BatteryEnergyContainerHandler.class)
//public abstract class MixinBatteryEnergyContainerHandler extends MTETrait implements IEnergyContainer {
//
//    @Shadow
//    private List<IElectricItem> batteries;
//    @Shadow
//    private final long maxInputVoltage;
//
//    public MixinBatteryEnergyContainerHandler(MetaTileEntity tileEntity, long maxCapacity, long maxInputVoltage, long maxInputAmperage, long maxOutputVoltage, long maxOutputAmperage, long maxInputVoltage1) {
//        super(tileEntity);
//        this.maxInputVoltage = maxInputVoltage1;
//    }
//
//    @Inject(method = "changeBatteryEnergy", at = @At("HEAD"))
//    public long changeBatteryEnergy(long energyToAdd) {
//        long leftenergy = energyToAdd;
//        if (this.batteries.size() == 0) {
//            return energyToAdd;
//        } else {
//            int count = this.batteries.size();
//            long average_energy = energyToAdd / (long)count;
//
//            for(int i = 0; i < count; ++i) {
//                IElectricItem battery = (IElectricItem)this.batteries.get(i);
//                long charged;
//                if (average_energy > 0L) {
//                    charged = battery.charge(average_energy, GTUtility.getTierByVoltage(this.maxInputVoltage), false, false);
//                    leftenergy += charged;
//                } else {
//                    charged = battery.charge(Math.abs(average_energy), GTUtility.getTierByVoltage(this.maxInputVoltage), false, false);
//                    leftenergy -= charged;
//                }
//            }
//
//            return leftenergy;
//        }
//    }
//
//}
