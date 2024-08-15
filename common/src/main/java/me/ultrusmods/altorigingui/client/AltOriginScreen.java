package me.ultrusmods.altorigingui.client;

import com.mojang.blaze3d.systems.RenderSystem;
import me.ultrusmods.altorigingui.Constants;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public interface AltOriginScreen {
    ResourceLocation ORIGINS_CHOICES = new ResourceLocation(Constants.MOD_ID, "textures/gui/origin_choices.png");




//    String getLayerTranslationKey();

    static void renderImpactIcon(GuiGraphics context, int x, int y, String impact, boolean guiSelected) {
        switch(impact) {
            case "NONE" -> context.blit(ORIGINS_CHOICES, x, y, 224, guiSelected ? 112 : 104, 8, 8);
            case "LOW" -> context.blit(ORIGINS_CHOICES, x, y, 232, guiSelected ? 112 : 104, 8, 8);
            case "MEDIUM" -> context.blit(ORIGINS_CHOICES, x, y, 240, guiSelected ? 112 : 104, 8, 8);
            case "HIGH" -> context.blit(ORIGINS_CHOICES, x, y, 248, guiSelected ? 112 : 104, 8, 8);
            case "VERY_HIGH" -> context.blit(ORIGINS_CHOICES, x, y, 248, guiSelected ? 144 : 136, 8, 8); // Extra Origins Impact
            default -> context.blit(ORIGINS_CHOICES, x, y, 240, guiSelected ? 144 : 136, 8, 8);
        }
    }
    default void renderRandomOrigin(GuiGraphics context, int mouseX, int mouseY, float delta, int x, int y, boolean selected) {
        int u = selected ? 26 : 0;
        boolean guiSelected = isIconHighlighted(mouseX, mouseY, x, y);
        if (guiSelected) {
            u += 52;
        }
        context.blit(ORIGINS_CHOICES, x, y, 230, u, 26, 26);
        context.blit(ORIGINS_CHOICES, x + 6, y + 5, 243, 120, 13, 16);
        int impact = (int) (getTickTime() / 15.0) % 4;
        context.blit(ORIGINS_CHOICES, x, y, 224 + (impact * 8), guiSelected ? 112 : 104, 8, 8);
    }


    default void renderOriginWidget(GuiGraphics context, int mouseX, int mouseY, float delta, int x, int y, boolean selected, String impact, MutableComponent originName) {
        RenderSystem.setShaderTexture(0, ORIGINS_CHOICES);
        int u = selected ? 26 : 0;
        boolean mouseHovering = isMouseHoveringIcon(mouseX, mouseY, x, y);
        boolean guiSelected = isIconHighlighted(mouseX, mouseY, x, y);
        if (guiSelected) {
            u += 52;
        }
        context.blit(ORIGINS_CHOICES, x, y, 230, u, 26, 26);
        AltOriginScreen.renderImpactIcon(context, x, y, impact, guiSelected);
        if (mouseHovering) {
            Component text = getCurrentLayerTranslationKey().append(": ").append(originName);
            context.renderTooltip(getScreenFont(), text, mouseX, mouseY);
        }
    }

    MutableComponent getCurrentLayerTranslationKey();

    boolean isIconHighlighted(int mouseX, int mouseY, int x, int y);

    boolean isOriginSelected(int index);
    boolean isRandomOriginSelected();

    Font getScreenFont();

    default boolean isMouseHoveringIcon(int mouseX, int mouseY, int x, int y) {
        return mouseX >= x && mouseY >= y && mouseX < x + 26 && mouseY < y + 26;
    }

    float getTickTime();
}
