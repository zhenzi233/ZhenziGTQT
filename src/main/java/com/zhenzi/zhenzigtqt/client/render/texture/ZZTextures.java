package com.zhenzi.zhenzigtqt.client.render.texture;

import codechicken.lib.texture.TextureUtils;
import com.zhenzi.zhenzigtqt.ZhenziGtqt;
import gregtech.api.GTValues;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.util.GTLog;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import net.minecraft.client.renderer.texture.TextureMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ZZTextures {
    public static final List<TextureUtils.IIconRegister> iconRegisters = new ArrayList();
    public static final SimpleOverlayRenderer QUANTUM_ASPECT_TANK_OVERLAY = new SimpleOverlayRenderer("overlay/machine/overlay_q_aspect_tank");
    public static final SimpleOverlayRenderer PIPE_ASPECT_OUT_OVERLAY  = new SimpleOverlayRenderer("overlay/machine/overlay_pipe_aspect_out");
    public static final SimpleOverlayRenderer ASPECT_OUTPUT_OVERLAY = new SimpleOverlayRenderer("overlay/machine/overlay_aspect_out");
//    public static final SimpleOverlayRenderer ASPECT_SLOT = new SimpleOverlayRenderer("overlay/machine/aspect_slot");
    public static final TextureArea ASPECT_SLOT = TextureArea.fullImage("textures/gui/widget/aspect_slot.png");
    public static final SimpleSidedCubeRenderer[] APSECT_CASINGS;

    static {
        APSECT_CASINGS = new SimpleSidedCubeRenderer[GTValues.V.length];

        for(int i = 0; i < APSECT_CASINGS.length; ++i) {
            String voltageName = GTValues.VN[i].toLowerCase();
            APSECT_CASINGS[i] = new SimpleSidedCubeRenderer("casings/voltage_aspect/" + voltageName);
        }
    }
    public static void register(TextureMap textureMap) {
        ZhenziGtqt.LOGGER.info("Loading meta tile entity texture sprites...");

        QUANTUM_ASPECT_TANK_OVERLAY.registerIcons(textureMap);
        PIPE_ASPECT_OUT_OVERLAY.registerIcons(textureMap);
        ASPECT_OUTPUT_OVERLAY.registerIcons(textureMap);

//        for (TextureUtils.IIconRegister iconRegister : iconRegisters) {
//            iconRegister.registerIcons(textureMap);
//        }
    }
}
