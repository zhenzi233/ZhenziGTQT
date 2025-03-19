package com.zhenzi.zhenzigtqt.client.gui.QuantumAspectTank;

import gregtech.api.gui.resources.TextureArea;
import net.minecraft.util.ResourceLocation;
import thaumcraft.api.aspects.Aspect;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class AspectImage {
    private final int color;
    private final TextureArea area;
    private final String name;
    public static LinkedHashMap<String, AspectImage> aspectImages = new LinkedHashMap();
    public AspectImage(Aspect aspect)
    {
        Color co = new Color(0);
        if (aspect != null)
        {
            this.color = aspect.getColor();
            this.area = new TextureArea(aspect.getImage(), 0, 0, 1.0, 1.0);
            this.name = aspect.getTag();
        }   else
        {
            this.color = co.getRGB();
            this.area = new TextureArea(new ResourceLocation("gregtech", "textures/blocks/overlay/machine/overlay_aspect_out.png"), 0, 0, 1.0, 1.0);
            this.name = "empty";
        }
    }

    public static void create()
    {
        for (Map.Entry<String, Aspect> entry : Aspect.aspects.entrySet())
        {
            AspectImage image = new AspectImage(entry.getValue());
            aspectImages.put(entry.getValue().getTag(), image);
        }
    }

    public static AspectImage EMPTY = new AspectImage(null);

    public int getColor()
    {
        return this.color;
    }

    public TextureArea getArea()
    {
        return this.area;
    }

    public String getTag()
    {
        return this.name;
    }
}
