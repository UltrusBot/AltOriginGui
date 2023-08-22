package me.ultrusmods.altorigingui.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.screen.ChooseOriginScreen;
import io.github.apace100.origins.screen.OriginDisplayScreen;
import me.ultrusmods.altorigingui.AltOriginGuiMod;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ChooseOriginScreen.class)
public abstract class ChoseOriginScreenMixin extends OriginDisplayScreen {

    @Shadow @Final private List<Origin> originSelection;
    @Shadow private int currentOrigin;

    @Shadow protected abstract Origin getCurrentOriginInternal();

    @Shadow @Final private ArrayList<OriginLayer> layerList;
    @Shadow private int currentLayerIndex;
    @Shadow private Origin randomOrigin;
    @Shadow private int maxSelection;
    private static final Identifier ORIGINS_CHOICES = new Identifier(AltOriginGuiMod.MOD_ID, "textures/gui/origin_choices.png");
    private static final int CHOICES_WIDTH = 219;
    private static final int CHOICES_HEIGHT = 182;

    private static final int ORIGIN_ICON_SIZE = 26;

    private int calculatedTop;
    private int calculatedLeft;

    private int currentPage = 0;
    private static final int COUNT_PER_PAGE = 35;
    private int pages;
    private float tickTime = 0.0F;


    public ChoseOriginScreenMixin(Text title, boolean showDirtBackground) {
        super(title, showDirtBackground);
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lio/github/apace100/origins/screen/OriginDisplayScreen;init()V", shift = At.Shift.AFTER))
    protected void changeGuiPosition(CallbackInfo ci) {
        this.calculatedTop = (this.height - CHOICES_HEIGHT) / 2;
        this.calculatedLeft = (this.width - (CHOICES_WIDTH + 10 + windowWidth)) / 2;

        this.guiTop = (this.height - windowHeight) / 2;
        this.guiLeft = calculatedLeft + CHOICES_WIDTH + 10;
        this.pages = (int)Math.ceil((float) maxSelection / COUNT_PER_PAGE);
        int x = 0;
        int y = 0;
        for (int i = 0; i < Math.min(maxSelection, 35); i++) {
            if (x > 6) {
                x = 0;
                y++;
            }
            int actualX = (12 + (x * (ORIGIN_ICON_SIZE + 2))) + calculatedLeft;
            int actualY = (10 + (y * (ORIGIN_ICON_SIZE + 4))) + calculatedTop;
            int finalI = i;
            addDrawableChild(ButtonWidget.builder(Text.of(""), b -> {
                int index = finalI + (currentPage * COUNT_PER_PAGE);
                if (index > maxSelection - 1) {
                    return;
                }
                currentOrigin = index;
                Origin newOrigin = getCurrentOriginInternal();
                showOrigin(newOrigin, layerList.get(currentLayerIndex), newOrigin == randomOrigin);
            }).positionAndSize(actualX, actualY, 26, 26).build());
            x++;
        }

        if(maxSelection > COUNT_PER_PAGE) {
            addDrawableChild(ButtonWidget.builder(Text.of("<"), b -> {
                currentPage = (currentPage - 1);
                if(currentPage < 0) {
                    currentPage = pages - 1;
                }
            }).positionAndSize(calculatedLeft, guiTop + windowHeight + 5, 20, 20).build());
            addDrawableChild(ButtonWidget.builder(Text.of(">"), b -> {
                currentPage = (currentPage + 1) % (pages);
            }).positionAndSize(calculatedLeft + CHOICES_WIDTH - 20, guiTop + windowHeight + 5, 20, 20).build());
        }
    }

    @WrapWithCondition(
            method = "init",
            at = @At(value = "INVOKE", target = "Lio/github/apace100/origins/screen/ChooseOriginScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;", ordinal = 0))
    public <T extends Element & Drawable & Selectable> boolean disableFirstArrowButton(ChooseOriginScreen screen, T element) {
        return false;
    }

    @WrapWithCondition(
            method = "init",
            at = @At(value = "INVOKE", target = "Lio/github/apace100/origins/screen/ChooseOriginScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;", ordinal = 1))
    public <T extends Element & Drawable & Selectable> boolean disableSecondArrowButton(ChooseOriginScreen screen, T element) {
        return false;
    }

    @Inject(method = "render", at = @At("TAIL"))
    void addRendering(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        renderOriginChoicesBox(matrices, mouseX, mouseY, delta);
        tickTime += delta;
    }


    @Unique
    public void renderOriginChoicesBox(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        RenderSystem.setShaderTexture(0, ORIGINS_CHOICES);
        drawTexture(matrices, calculatedLeft, calculatedTop, 0, 0, CHOICES_WIDTH, CHOICES_HEIGHT);
        int x = 0;
        int y = 0;
        for (int i = (currentPage * COUNT_PER_PAGE); i < Math.min((currentPage + 1) * COUNT_PER_PAGE, maxSelection); i++) {
            if (x > 6) {
                x = 0;
                y++;
            }
            int actualX = (12 + (x * (ORIGIN_ICON_SIZE + 2))) + calculatedLeft;
            int actualY = (10 + (y * (ORIGIN_ICON_SIZE + 4))) + calculatedTop;
            if (i >= originSelection.size()) {
                // This is the random origin
                boolean selected = this.getCurrentOrigin().getIdentifier().equals(Origins.identifier("random"));
                renderRandomOrigin(matrices, mouseX, mouseY, delta, actualX, actualY, selected);
            } else {
                Origin origin = originSelection.get(i);
                boolean selected = origin.getIdentifier().equals(this.getCurrentOrigin().getIdentifier());
                renderOriginWidget(matrices, mouseX, mouseY, delta, actualX, actualY, selected, origin);
                this.itemRenderer.renderItemInGui(matrices, origin.getDisplayItem(), actualX + 5, actualY + 5);
            }

            x++;
        }
        drawCenteredTextWithShadow(matrices, this.textRenderer, Text.of((currentPage + 1) + "/" + (pages)).asOrderedText(), calculatedLeft + (CHOICES_WIDTH / 2), guiTop + windowHeight + 5 + this.textRenderer.fontHeight/2, 0xFFFFFF);

    }

    public void renderOriginWidget(MatrixStack matrices, int mouseX, int mouseY, float delta, int x, int y, boolean selected, Origin origin) {
        RenderSystem.setShaderTexture(0, ORIGINS_CHOICES);
        int u = selected ? 26 : 0;
        boolean mouseHovering = mouseX >= x && mouseY >= y && mouseX < x + 26 && mouseY < y + 26;
        boolean guiSelected = (getFocused() instanceof ButtonWidget buttonWidget && buttonWidget.getX() == x && buttonWidget.getY() == y) || mouseHovering;
        if (guiSelected) {
                u += 52;
        }
        drawTexture(matrices, x, y, 230, u, 26, 26);
        var impact = origin.getImpact();
        drawTexture(matrices, x, y, 224 + (impact.ordinal() * 8), guiSelected ? 112 : 104, 8, 8);
//        switch(impact) {
//            case LOW -> drawTexture(matrices, x, y, 232, 104, 8, 8);
//            case MEDIUM -> drawTexture(matrices, x, y, 240, 104, 8, 8);
//            case HIGH -> drawTexture(matrices, x, y, 248, 104, 8, 8);
//            default -> drawTexture(matrices, x, y, 224, 104, 8, 8);
//        }
        if (mouseHovering) {
            Text text = Text.translatable(getCurrentLayer().getTranslationKey()).append(": ").append(origin.getName());
            renderTooltip(matrices, text, mouseX, mouseY);
        }
    }
    public void renderRandomOrigin(MatrixStack matrices, int mouseX, int mouseY, float delta, int x, int y, boolean selected) {
        RenderSystem.setShaderTexture(0, ORIGINS_CHOICES);
        int u = selected ? 26 : 0;
        boolean mouseHovering = mouseX >= x && mouseY >= y && mouseX < x + 26 && mouseY < y + 26;
        boolean guiSelected = (getFocused() instanceof ButtonWidget buttonWidget && buttonWidget.getX() == x && buttonWidget.getY() == y) || mouseHovering;
        if (guiSelected) {
            u += 52;
        }
        drawTexture(matrices, x, y, 230, u, 26, 26);
        drawTexture(matrices, x + 6, y + 5, 243, 120, 13, 16);
        int impact = (int) (tickTime / 15.0) % 4;
        drawTexture(matrices, x, y, 224 + (impact * 8), guiSelected ? 112 : 104, 8, 8);

    }
}
