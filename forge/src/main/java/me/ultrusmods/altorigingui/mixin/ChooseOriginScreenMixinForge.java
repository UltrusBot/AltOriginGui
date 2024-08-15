package me.ultrusmods.altorigingui.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.apace100.origins.screen.ChooseOriginScreen;
import io.github.apace100.origins.screen.OriginDisplayScreen;
import io.github.edwinmindcraft.origins.api.origin.Origin;
import io.github.edwinmindcraft.origins.api.origin.OriginLayer;
import me.ultrusmods.altorigingui.Constants;
import me.ultrusmods.altorigingui.client.AltOriginScreen;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ChooseOriginScreen.class)
public abstract class ChooseOriginScreenMixinForge extends OriginDisplayScreen implements AltOriginScreen {


    @Shadow private int maxSelection;
    @Shadow private int currentOrigin;

    @Shadow protected abstract Holder<Origin> getCurrentOriginInternal();

    @Shadow @Final private List<Holder<OriginLayer>> layerList;
    @Shadow @Final private int currentLayerIndex;
    @Shadow private Origin randomOrigin;
    @Shadow @Final private List<Holder<Origin>> originSelection;
    private static final ResourceLocation ORIGINS_CHOICES = new ResourceLocation(Constants.MOD_ID, "textures/gui/origin_choices.png");
    private static final int CHOICES_WIDTH = 219;
    private static final int CHOICES_HEIGHT = 182;

    private static final int ORIGIN_ICON_SIZE = 26;

    private int calculatedTop;
    private int calculatedLeft;

    private int currentPage = 0;
    private static final int COUNT_PER_PAGE = 35;
    private int pages;
    private float tickTime = 0.0F;


    public ChooseOriginScreenMixinForge(Component title, boolean showDirtBackground) {
        super(title, showDirtBackground);
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lio/github/apace100/origins/screen/ChooseOriginScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;", shift = At.Shift.BEFORE))
    protected void changeGuiSize(CallbackInfo ci) {
        this.guiTop = (this.height - windowHeight) / 2;
        this.guiLeft = calculatedLeft + CHOICES_WIDTH + 10;
    }


    @Inject(method = "init", at = @At(value = "FIELD", target = "Lio/github/apace100/origins/screen/ChooseOriginScreen;guiTop:I", shift = At.Shift.AFTER, opcode = Opcodes.PUTFIELD))
    protected void changeGuiPosition(CallbackInfo ci) {
        this.calculatedTop = (this.height - CHOICES_HEIGHT) / 2;
        this.calculatedLeft = (this.width - (CHOICES_WIDTH + 10 + windowWidth)) / 2;
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
            addRenderableWidget(Button.builder(Component.nullToEmpty(""), b -> {
                int index = finalI + (currentPage * COUNT_PER_PAGE);
                if (index > maxSelection - 1) {
                    return;
                }
                currentOrigin = index;
                var newOrigin = getCurrentOriginInternal();
                showOrigin(newOrigin, layerList.get(currentLayerIndex), newOrigin == randomOrigin);
            }).bounds(actualX, actualY, 26, 26).build());
            x++;
        }

        if(maxSelection > COUNT_PER_PAGE) {
            addRenderableWidget(Button.builder(Component.nullToEmpty("<"), b -> {
                currentPage = (currentPage - 1);
                if(currentPage < 0) {
                    currentPage = pages - 1;
                }
            }).bounds(calculatedLeft, guiTop + windowHeight + 5, 20, 20).build());
            addRenderableWidget(Button.builder(Component.nullToEmpty(">"), b -> {
                currentPage = (currentPage + 1) % (pages);
            }).bounds(calculatedLeft + CHOICES_WIDTH - 20, guiTop + windowHeight + 5, 20, 20).build());
        }
    }

    @WrapWithCondition(
            method = "init",
            at = @At(value = "INVOKE", target = "Lio/github/apace100/origins/screen/ChooseOriginScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;", ordinal = 0))
    public <T extends GuiEventListener & Renderable & NarratableEntry> boolean disableFirstArrowButton(ChooseOriginScreen screen, T element) {
        return false;
    }

    @WrapWithCondition(
            method = "init",
            at = @At(value = "INVOKE", target = "Lio/github/apace100/origins/screen/ChooseOriginScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;", ordinal = 1))
    public <T extends GuiEventListener & Renderable & NarratableEntry> boolean disableSecondArrowButton(ChooseOriginScreen screen, T element) {
        return false;
    }

    @Inject(method = "render", at = @At("TAIL"))
    void addRendering(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        renderOriginChoicesBox(context, mouseX, mouseY, delta);
        tickTime += delta;
    }


    @Unique
    public void renderOriginChoicesBox(GuiGraphics context, int mouseX, int mouseY, float delta) {
//        RenderSystem.setShaderTexture(0, ORIGINS_CHOICES);
        context.blit(ORIGINS_CHOICES, calculatedLeft, calculatedTop, 0, 0, CHOICES_WIDTH, CHOICES_HEIGHT);
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
                boolean selected = this.getCurrentOrigin().get().equals(randomOrigin);
                renderRandomOrigin(context, mouseX, mouseY, delta, actualX, actualY, selected);
            } else {
                var origin = originSelection.get(i).get();
                boolean selected = origin.equals(this.getCurrentOrigin().get());
                renderOriginWidget(context, mouseX, mouseY, delta, actualX, actualY, selected, origin.getImpact().name(), origin.getName().copy());
                context.renderItem(origin.getIcon(), actualX + 5, actualY + 5);
            }

            x++;
        }
        context.drawCenteredString(this.font, Component.nullToEmpty((currentPage + 1) + "/" + (pages)).getVisualOrderText(), calculatedLeft + (CHOICES_WIDTH / 2), guiTop + windowHeight + 5 + this.font.lineHeight/2, 0xFFFFFF);

    }

    @Override
    public boolean isIconHighlighted(int mouseX, int mouseY, int x, int y) {
        boolean mouseHovering = mouseX >= x && mouseY >= y && mouseX < x + 26 && mouseY < y + 26;
        return (getFocused() instanceof Button buttonWidget && buttonWidget.getX() == x && buttonWidget.getY() == y) || mouseHovering;
    }

    @Override
    public MutableComponent getCurrentLayerTranslationKey() {
        return getCurrentLayer().get().name().copy();
    }

    @Override
    public Font getScreenFont() {
        return this.font;
    }

    @Override
    public boolean isOriginSelected(int index) {
        return index == currentOrigin;
    }

    @Override
    public boolean isRandomOriginSelected() {
        return this.getCurrentOrigin().get().equals(randomOrigin);
    }

    @Override
    public float getTickTime() {
        return tickTime;
    }
}
