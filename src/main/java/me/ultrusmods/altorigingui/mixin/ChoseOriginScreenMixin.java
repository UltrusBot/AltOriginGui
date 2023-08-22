package me.ultrusmods.altorigingui.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.screen.ChooseOriginScreen;
import io.github.apace100.origins.screen.OriginDisplayScreen;
import me.ultrusmods.altorigingui.gui.CustomOriginScreen;
import me.ultrusmods.altorigingui.gui.OriginButtonWidget;
import me.ultrusmods.altorigingui.gui.SortMode;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mixin(ChooseOriginScreen.class)
public abstract class ChoseOriginScreenMixin extends OriginDisplayScreen implements CustomOriginScreen {

    @Mutable
    @Shadow @Final private List<Origin> originSelection;
    @Shadow private int currentOrigin;
    private TextFieldWidget searchField;

    @Shadow protected abstract Origin getCurrentOriginInternal();

    @Shadow @Final private ArrayList<OriginLayer> layerList;
    @Shadow private int currentLayerIndex;
    @Shadow private Origin randomOrigin;
    @Shadow private int maxSelection;

    @Shadow protected abstract void initRandomOrigin();

    @Shadow public abstract void render(GuiGraphics par1, int par2, int par3, float par4);

    private static final int ORIGIN_ICON_SIZE = 26;

    private int calculatedTop;
    private int calculatedLeft;

    private int currentPage = 0;
    private int pages;
    private float tickTime = 0.0F;

    private SortMode currentSortMode = SortMode.IMPACT;

    private ArrayList<OriginButtonWidget> originButtons = new ArrayList<>();

    @Unique private List<Origin> filteredOrigins = new ArrayList<>();



    public ChoseOriginScreenMixin(Text title, boolean showDirtBackground) {
        super(title, showDirtBackground);
    }


    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lio/github/apace100/origins/screen/OriginDisplayScreen;init()V", shift = At.Shift.AFTER))
    protected void changeGuiPosition(CallbackInfo ci) {
        filteredOrigins = new ArrayList<>(originSelection);
        if (this.randomOrigin == null) { // Do this so the random origin isn't null, and can be safely passed to buttons.
            initRandomOrigin();
        }
        this.calculatedTop = (this.height - CHOICES_HEIGHT) / 2;
        this.calculatedLeft = (this.width - (CHOICES_WIDTH + 10 + windowWidth)) / 2;

        this.guiTop = (this.height - windowHeight) / 2;
        this.guiLeft = calculatedLeft + CHOICES_WIDTH + 10;
        this.pages = Math.round((float) maxSelection / COUNT_PER_PAGE);
        int x = 0;
        int y = 0;
        originButtons = new ArrayList<>();
        for (int i = 0; i < Math.min(maxSelection, 35); i++) {
            if (x > 6) {
                x = 0;
                y++;
            }
            int actualX = (12 + (x * (ORIGIN_ICON_SIZE + 2))) + calculatedLeft;
            int actualY = (30 + (y * (ORIGIN_ICON_SIZE + 1))) + calculatedTop;
            int finalI = i;
            var button = addDrawableChild(new OriginButtonWidget(actualX, actualY, 26, 26, 0, 0, 26, 0, 26, BUTTONS, b -> {
                int index = finalI + (currentPage * COUNT_PER_PAGE);
                if (index > maxSelection - 1) {
                    return;
                }
                currentOrigin = index;
                Origin newOrigin = getCurrentOriginInternal();
                showOrigin(newOrigin, layerList.get(currentLayerIndex), newOrigin == randomOrigin);
            }, () -> {
                int index = finalI + (currentPage * COUNT_PER_PAGE);
                return currentOrigin == index;
            }, getCurrentLayer(), this.textRenderer));
            originButtons.add(button);
            x++;
        }
        updateButtonOrigins();

        if(maxSelection > COUNT_PER_PAGE) {
            addDrawableChild(ButtonWidget.builder(Text.of("<"), b -> {
                currentPage = (currentPage - 1);
                if(currentPage < 0) {
                    currentPage = pages;
                }
                updateButtonOrigins();
            }).positionAndSize(calculatedLeft, guiTop + windowHeight + 5, 20, 20).build());
            addDrawableChild(ButtonWidget.builder(Text.of(">"), b -> {
                currentPage = (currentPage + 1) % (pages + 1);
                updateButtonOrigins();
            }).positionAndSize(calculatedLeft + CHOICES_WIDTH - 20, guiTop + windowHeight + 5, 20, 20).build());
        }
        var sortButton = addDrawableChild(new TexturedButtonWidget(calculatedLeft + CHOICES_WIDTH - 35, calculatedTop + 9, 26, 13, 52, 0, BUTTONS, buttonWidget -> {
            currentSortMode = SortMode.values()[(currentSortMode.ordinal() + 1) % SortMode.values().length];
            filteredOrigins = currentSortMode.sort(filteredOrigins);
            buttonWidget.setTooltip(Tooltip.create(Text.translatable(SORTED_BY).append(": ").append(Text.translatable(currentSortMode.getTranslationKey()))));
            updateButtonOrigins();
        }));
        sortButton.setTooltip(Tooltip.create(Text.translatable(SORTED_BY).append(": ").append(Text.translatable(currentSortMode.getTranslationKey()))));
        this.searchField = new TextFieldWidget(textRenderer, calculatedLeft + 13, calculatedTop + 12, 192, 10, this.searchField, Text.of("Search..."));
        this.searchField.setMaxLength(50);
        this.searchField.setVisible(true);
        this.searchField.setEditableColor(0xc8c8c8);
        this.searchField.setHint(Text.of("Search..."));
        this.searchField.setDrawsBackground(false);
        this.searchField.setChangedListener(s -> {
            if(s.isEmpty()) {
                filteredOrigins = new ArrayList<>(originSelection);
            } else {
                filteredOrigins = originSelection.stream().filter(o -> o.getIdentifier().toString().toLowerCase().contains(s.toLowerCase())).collect(Collectors.toList());
            }
            updateButtonOrigins();
        });
        this.addDrawableChild(this.searchField);
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
    void renderingAtTail(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        // Rendering that has to be done after the buttons
        tickTime += delta;
        context.drawCenteredShadowedText(this.textRenderer, Text.of((currentPage + 1) + "/" + (pages + 1)).asOrderedText(), calculatedLeft + (CHOICES_WIDTH / 2), guiTop + windowHeight + 5 + this.textRenderer.fontHeight/2, 0xFFFFFF);
        for (OriginButtonWidget buttonWidget : originButtons) {
            Origin origin = buttonWidget.getOrigin();
            if (origin != null && buttonWidget.getOrigin().equals(randomOrigin)) {
                renderRandomOrigin(context, mouseX, mouseY, delta, buttonWidget.getX(), buttonWidget.getY(), buttonWidget.isHoveredOrFocused());
                break;
            }
        }
    }
    public void updateButtonOrigins() {
        int buttonIndex = 0;
        for (int i = (currentPage * COUNT_PER_PAGE); i < Math.min(Math.min((currentPage + 1) * COUNT_PER_PAGE, maxSelection), filteredOrigins.size()); i++) {
            Origin origin = i == maxSelection - 1 ? randomOrigin : filteredOrigins.get(i);
            OriginButtonWidget button = originButtons.get(buttonIndex);
            button.setOrigin(origin);
            buttonIndex++;
        }
        // Set the remaining buttons to null
        for (int i = buttonIndex; i < originButtons.size(); i++) {
            originButtons.get(i).setOrigin(null);
        }
    }
    public void renderRandomOrigin(GuiGraphics context, int mouseX, int mouseY, float delta, int x, int y, boolean selected) {
        RenderSystem.setShaderTexture(0, ORIGINS_CHOICES);
        boolean mouseHovering = mouseX >= x && mouseY >= y && mouseX < x + 26 && mouseY < y + 26;
        boolean guiSelected = (getFocused() instanceof ButtonWidget buttonWidget && buttonWidget.getX() == x && buttonWidget.getY() == y) || mouseHovering;
        context.drawTexture(ORIGINS_CHOICES, x + 6, y + 5, 243, 120, 13, 16);
        int impact = (int) (tickTime / 15.0) % 4;
        context.drawTexture(ORIGINS_CHOICES, x, y, 224 + (impact * 8), guiSelected ? 112 : 104, 8, 8);
    }


    @Override
    public int getCalculatedTop() {
        return calculatedTop;
    }

    @Override
    public int getCalculatedLeft() {
        return calculatedLeft;
    }

    @Override
    public int getCurrentPage() {
        return currentPage;
    }

    @Override
    public int getPages() {
        return pages;
    }

}
