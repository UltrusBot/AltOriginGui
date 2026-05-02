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
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


import java.util.List;

@Mixin(ChooseOriginScreen.class)
public abstract class ChoseOriginScreenMixin extends OriginDisplayScreen {



    @Shadow public abstract Origin getCurrentOrigin();

    @Shadow
    private int optionCount;
    @Shadow
    @Final
    private int layerIndex;
    @Shadow
    @Final
    private List<OriginLayer> layers;
    @Shadow
    private int originIndex;
    @Shadow
    @Final
    private List<Origin> origins;
    private static final Identifier ORIGINS_CHOICES = Identifier.of(AltOriginGuiMod.MOD_ID, "textures/gui/origin_choices.png");
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

    @Inject(method = "init", at = @At(value = "TAIL"))
    protected void changeGuiPosition(CallbackInfo ci) {
        this.calculatedTop = (this.height - CHOICES_HEIGHT) / 2;
        this.calculatedLeft = (this.width - (CHOICES_WIDTH + 10 + WINDOW_WIDTH)) / 2;

        this.guiTop = (this.height - WINDOW_HEIGHT) / 2;
        this.guiLeft = calculatedLeft + CHOICES_WIDTH + 10;
        this.pages = (int)Math.ceil((float) optionCount / COUNT_PER_PAGE);
        int x = 0;
        int y = 0;
        for (int i = 0; i < Math.min(optionCount, 35); i++) {
            if (x > 6) {
                x = 0;
                y++;
            }
            int actualX = (12 + (x * (ORIGIN_ICON_SIZE + 2))) + calculatedLeft;
            int actualY = (10 + (y * (ORIGIN_ICON_SIZE + 4))) + calculatedTop;
            int finalI = i;
            addDrawableChild(ButtonWidget.builder(Text.of(""), b -> {
                int index = finalI + (currentPage * COUNT_PER_PAGE);
                if (index > optionCount - 1) {
                    return;
                }
                originIndex = index;
                Origin newOrigin = getCurrentOrigin();
                showOrigin(newOrigin, layers.get(layerIndex));
            }).position(actualX, actualY).size(26, 26).build());
            x++;
        }

        if(optionCount > COUNT_PER_PAGE) {
            addDrawableChild(ButtonWidget.builder(Text.of("<"), b -> {
                currentPage = (currentPage - 1);
                if(currentPage < 0) {
                    currentPage = pages - 1;
                }
            }).position(calculatedLeft, guiTop + WINDOW_HEIGHT + 5).size(20, 20).build());
            addDrawableChild(ButtonWidget.builder(Text.of(">"), b -> {
                currentPage = (currentPage + 1) % (pages);
            }).position(calculatedLeft + CHOICES_WIDTH - 20, guiTop + WINDOW_HEIGHT + 5).size(20, 20).build());
        }
    }

    @WrapWithCondition(
            method = "init",
            at = @At(value = "INVOKE", target = "Lio/github/apace100/origins/screen/ChooseOriginScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;", ordinal = 1))
    public <T extends Element & Drawable & Selectable> boolean disableFirstArrowButton(ChooseOriginScreen screen, T element) {
        return false;
    }

    @WrapWithCondition(
            method = "init",
            at = @At(value = "INVOKE", target = "Lio/github/apace100/origins/screen/ChooseOriginScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;", ordinal = 2))
    public <T extends Element & Drawable & Selectable> boolean disableSecondArrowButton(ChooseOriginScreen screen, T element) {
        return false;
    }

    @Inject(method = "render", at = @At("TAIL"))
    void addRendering(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        renderOriginChoicesBox(context, mouseX, mouseY, delta);
        tickTime += delta;
    }


    @Unique
    public void renderOriginChoicesBox(DrawContext context, int mouseX, int mouseY, float delta) {
//        RenderSystem.setShaderTexture(0, ORIGINS_CHOICES);
        context.drawTexture(ORIGINS_CHOICES, calculatedLeft, calculatedTop, 0, 0, CHOICES_WIDTH, CHOICES_HEIGHT);
        int x = 0;
        int y = 0;
        for (int i = (currentPage * COUNT_PER_PAGE); i < Math.min((currentPage + 1) * COUNT_PER_PAGE, optionCount); i++) {
            if (x > 6) {
                x = 0;
                y++;
            }
            int actualX = (12 + (x * (ORIGIN_ICON_SIZE + 2))) + calculatedLeft;
            int actualY = (10 + (y * (ORIGIN_ICON_SIZE + 4))) + calculatedTop;
            if (origins.get(i) == Origin.RANDOM) {
                // This is the random origin
                boolean selected = this.getCurrentOrigin().getId().equals(Origins.identifier("random"));
                renderRandomOrigin(context, mouseX, mouseY, delta, actualX, actualY, selected);
            } else {
                Origin origin = origins.get(i);
                boolean selected = origin.getId().equals(this.getCurrentOrigin().getId());
                renderOriginWidget(context, mouseX, mouseY, delta, actualX, actualY, selected, origin);
                context.drawItem(origin.getDisplayItem(), actualX + 5, actualY + 5);
            }

            x++;
        }
        context.drawCenteredTextWithShadow(this.textRenderer, Text.of((currentPage + 1) + "/" + (pages)).asOrderedText(), calculatedLeft + (CHOICES_WIDTH / 2), guiTop + WINDOW_HEIGHT + 5 + this.textRenderer.fontHeight/2, 0xFFFFFF);

    }

    @Unique
    public void renderOriginWidget(DrawContext context, int mouseX, int mouseY, float delta, int x, int y, boolean selected, Origin origin) {
        RenderSystem.setShaderTexture(0, ORIGINS_CHOICES);
        int u = selected ? 26 : 0;
        boolean mouseHovering = mouseX >= x && mouseY >= y && mouseX < x + 26 && mouseY < y + 26;
        boolean guiSelected = (getFocused() instanceof ButtonWidget buttonWidget && buttonWidget.getX() == x && buttonWidget.getY() == y) || mouseHovering;
        if (guiSelected) {
                u += 52;
        }
        context.drawTexture(ORIGINS_CHOICES, x, y, 230, u, 26, 26);
        var impact = origin.getImpact();
        switch(impact.name()) {
            case "NONE" -> context.drawTexture(ORIGINS_CHOICES, x, y, 224, guiSelected ? 112 : 104, 8, 8);
            case "LOW" -> context.drawTexture(ORIGINS_CHOICES, x, y, 232, guiSelected ? 112 : 104, 8, 8);
            case "MEDIUM" -> context.drawTexture(ORIGINS_CHOICES, x, y, 240, guiSelected ? 112 : 104, 8, 8);
            case "HIGH" -> context.drawTexture(ORIGINS_CHOICES, x, y, 248, guiSelected ? 112 : 104, 8, 8);
            case "VERY_HIGH" -> context.drawTexture(ORIGINS_CHOICES, x, y, 248, guiSelected ? 144 : 136, 8, 8);
            default -> context.drawTexture(ORIGINS_CHOICES, x, y, 240, guiSelected ? 144 : 136, 8, 8);
        }
        if (mouseHovering) {
            Text text = getCurrentLayer().getName().copy().append(": ").append(origin.getName());
            context.drawTooltip(this.textRenderer, text, mouseX, mouseY);
        }
    }
    public void renderRandomOrigin(DrawContext context, int mouseX, int mouseY, float delta, int x, int y, boolean selected) {
        int u = selected ? 26 : 0;
        boolean mouseHovering = mouseX >= x && mouseY >= y && mouseX < x + 26 && mouseY < y + 26;
        boolean guiSelected = (getFocused() instanceof ButtonWidget buttonWidget && buttonWidget.getX() == x && buttonWidget.getY() == y) || mouseHovering;
        if (guiSelected) {
            u += 52;
        }
        context.drawTexture(ORIGINS_CHOICES, x, y, 230, u, 26, 26);
        context.drawTexture(ORIGINS_CHOICES, x + 6, y + 5, 243, 120, 13, 16);
        int impact = (int) (tickTime / 15.0) % 4;
        context.drawTexture(ORIGINS_CHOICES, x, y, 224 + (impact * 8), guiSelected ? 112 : 104, 8, 8);

    }
}
