package com.zhenzi.zhenzigtqt.client;

import com.zhenzi.zhenzigtqt.client.gui.QuantumAspectTank.AspectImage;
import com.zhenzi.zhenzigtqt.client.render.texture.ZZTextures;
import com.zhenzi.zhenzigtqt.common.CommonProxy;
import com.zhenzi.zhenzigtqt.common.block.ZhenziGTQTMetaBlocks;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
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

    @SubscribeEvent
    public static void textureStitchPre(TextureStitchEvent.Pre event) {
        TextureMap map = event.getMap();
//        GTFluidRegistration.INSTANCE.registerSprites(map);
//        PipeRenderer.initializeRestrictor(map);
        ZZTextures.register(map);
//        CableRenderer.INSTANCE.registerIcons(map);
//        FluidPipeRenderer.INSTANCE.registerIcons(map);
//        ItemPipeRenderer.INSTANCE.registerIcons(map);
//        OpticalPipeRenderer.INSTANCE.registerIcons(map);
//        LaserPipeRenderer.INSTANCE.registerIcons(map);
    }

    public void init() {
        AspectImage.create();
        super.init();
    }
}
