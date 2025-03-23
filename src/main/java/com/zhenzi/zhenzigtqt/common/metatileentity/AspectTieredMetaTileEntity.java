package com.zhenzi.zhenzigtqt.common.metatileentity;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.zhenzi.zhenzigtqt.client.render.texture.ZZTextures;
import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.EnergyContainerHandler;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import gregtech.common.ConfigHolder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AspectTieredMetaTileEntity extends AspectMetaTileEntity implements EnergyContainerHandler.IEnergyChangeListener, ITieredMetaTileEntity {
    private final int tier;
    protected IEnergyContainer energyContainer;

    public AspectTieredMetaTileEntity(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId);
        this.tier = tier;
        this.reinitializeEnergyContainer();
    }

    protected void reinitializeEnergyContainer() {
        long tierVoltage = GTValues.V[this.tier];
        if (this.isEnergyEmitter()) {
            this.energyContainer = EnergyContainerHandler.emitterContainer(this, tierVoltage * 64L, tierVoltage, this.getMaxInputOutputAmperage());
        } else {
            this.energyContainer = EnergyContainerHandler.receiverContainer(this, tierVoltage * 64L, tierVoltage, this.getMaxInputOutputAmperage());
        }

    }

    public void onEnergyChanged(IEnergyContainer container, boolean isInitialChange) {
    }

    @SideOnly(Side.CLIENT)
    protected SimpleSidedCubeRenderer getBaseRenderer() {
        return ZZTextures.APSECT_CASINGS[this.tier];
    }

    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        if (ConfigHolder.machines.doTerrainExplosion && this.getIsWeatherOrTerrainResistant()) {
            tooltip.add(I18n.format("gregtech.universal.tooltip.terrain_resist"));
        }

    }

    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(this.getBaseRenderer().getParticleSprite(), this.getPaintingColorForRendering());
    }

    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        IVertexOperation[] colouredPipeline = (IVertexOperation[]) ArrayUtils.add(pipeline, new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(this.getPaintingColorForRendering())));
        this.getBaseRenderer().render(renderState, translation, colouredPipeline);
    }

    public void update() {
        super.update();
        this.checkWeatherOrTerrainExplosion((float)this.tier, (double)(this.tier * 10), this.energyContainer);
    }

    public int getTier() {
        return this.tier;
    }

    protected long getMaxInputOutputAmperage() {
        return 1L;
    }

    protected boolean isEnergyEmitter() {
        return false;
    }
}
