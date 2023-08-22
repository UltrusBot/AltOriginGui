package me.ultrusmods.altorigingui.gui;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Objects;

import static me.ultrusmods.altorigingui.gui.CustomOriginScreen.BUTTONS;

public class OriginButtonWidget extends TexturedButtonWidget {
    protected int selectedU;
    protected int selectedV;

    protected SelectedCriteria selectedCriteria;
    protected OriginLayer currentLayer;
    Origin origin;
    TextRenderer textRenderer;
    public OriginButtonWidget(int x, int y, int width, int height, int u, int v, int selectedU, int selectedV, int hoveredVOffset, Identifier texture, PressAction pressAction, SelectedCriteria selectedCriteria, OriginLayer currentLayer, TextRenderer textRenderer) {
        super(x, y, width, height, u, v, hoveredVOffset, texture, pressAction);
        this.selectedU = selectedU;
        this.selectedV = selectedV;
        this.selectedCriteria = selectedCriteria;
        this.currentLayer = currentLayer;
        this.textRenderer = textRenderer;
    }

    public void setOrigin(Origin origin) {
        this.origin = origin;
        this.active = this.origin != null;
    }

    public Origin getOrigin() {
        return origin;
    }

    @Override
    public void drawWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        if (origin == null) {
            return;
        }
        int currentU = this.u;
        int currentV = this.v;
        if (this.selectedCriteria.isSelected()) {
            currentU = this.selectedU;
            currentV = this.selectedV;
        }
        boolean mouseHovering = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + 26 && mouseY < getY() + 26;
        if (mouseHovering) {
            Text text = Text.translatable(currentLayer.getTranslationKey()).append(": ").append(origin.getName());
            graphics.drawTooltip(textRenderer, text, mouseX, mouseY);
        }
        this.drawTexture(graphics, this.texture, this.getX(), this.getY(), currentU, currentV, this.hoveredVOffset, this.width, this.height, this.textureWidth, this.textureHeight);
        if (!Objects.equals(this.origin.getIdentifier(), Origins.identifier("random"))) {
            graphics.drawItem(this.origin.getDisplayItem(), this.getX() + 5, this.getY() + 5);
            var impact = origin.getImpact();
            graphics.drawTexture(BUTTONS, getX(), getY(), impact.ordinal() * 8, isHoveredOrFocused() ? 248 : 240, 8, 8);
        }
    }

    @FunctionalInterface
    public interface SelectedCriteria {
        boolean isSelected();
    }
}
