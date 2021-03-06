package net.blay09.mods.excompressum.client.gui;

import com.google.common.collect.Lists;
import net.blay09.mods.excompressum.ExCompressum;
import net.blay09.mods.excompressum.container.ContainerAutoCompressor;
import net.blay09.mods.excompressum.tile.TileAutoCompressor;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class GuiAutoCompressor extends GuiContainer {

    private static final ResourceLocation texture = new ResourceLocation(ExCompressum.MOD_ID, "textures/gui/autoCompressor.png");
    private TileAutoCompressor tileEntity;

    public GuiAutoCompressor(InventoryPlayer inventoryPlayer, TileAutoCompressor tileEntity) {
        super(new ContainerAutoCompressor(inventoryPlayer, tileEntity));
        this.tileEntity = tileEntity;
        xSize = 176;
        ySize = 166;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1f, 1f, 1f, 1f);
        mc.getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        if (tileEntity.isProcessing()) {
            drawTexturedModalRect(guiLeft + 69, guiTop + 9, 176, 0, (int) (tileEntity.getProgress() * 15f), 15);
            drawTexturedModalRect(guiLeft + 69, guiTop + 36, 176, 0, (int) (tileEntity.getProgress() * 15f), 15);
            drawTexturedModalRect(guiLeft + 69, guiTop + 63, 176, 0, (int) (tileEntity.getProgress() * 15f), 15);
        }
        float energyPercentage = tileEntity.getEnergyPercentage();
        drawTexturedModalRect(guiLeft + 152, guiTop + 8 + (70 - (int) (energyPercentage * 70)), 176 + 15, 0, 16, (int) (energyPercentage * 70));
    }

    private static final List<String> tmpLines = Lists.newArrayList();
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        if (mouseX >= guiLeft + 152 && mouseX <= guiLeft + 167 && mouseY >= guiTop + 8 && mouseY <= guiTop + 77) {
            tmpLines.clear();
            tmpLines.add(tileEntity.getEnergyStored(null) + " RF");
            tmpLines.add(I18n.format("tooltip.excompressum:consumingEnergy", tileEntity.getEffectiveEnergy()));
            drawHoveringText(tmpLines, mouseX - guiLeft, mouseY - guiTop);
        }
    }

}
