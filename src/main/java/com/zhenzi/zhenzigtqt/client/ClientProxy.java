package com.zhenzi.zhenzigtqt.client;

import com.zhenzi.zhenzigtqt.common.CommonProxy;
import com.zhenzi.zhenzigtqt.common.block.ZhenziGTQTMetaBlocks;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber({Side.CLIENT})
public class ClientProxy extends CommonProxy {

    public ClientProxy() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ZhenziGTQTMetaBlocks.registerItemModels();
    }

    public void init() {
        super.init();
    }
}
