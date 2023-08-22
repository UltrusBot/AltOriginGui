package me.ultrusmods.altorigingui.gui;

import me.ultrusmods.altorigingui.AltOriginGuiMod;
import net.minecraft.util.Identifier;

public interface CustomOriginScreen {
    Identifier ORIGINS_CHOICES = new Identifier(AltOriginGuiMod.MOD_ID, "textures/gui/origin_choices.png");
    Identifier BUTTONS = new Identifier(AltOriginGuiMod.MOD_ID, "textures/gui/origin_choices_buttons.png");
    String SORTED_BY = "altorigingui.text.sorted_by";

    int CHOICES_WIDTH = 219;
    int CHOICES_HEIGHT = 182;
    int COUNT_PER_PAGE = 35;

    int getCalculatedTop();
    int getCalculatedLeft();
    int getCurrentPage();
    int getPages();


}
