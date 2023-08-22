package me.ultrusmods.altorigingui.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.apace100.origins.screen.OriginDisplayScreen;
import me.ultrusmods.altorigingui.gui.CustomOriginScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OriginDisplayScreen.class)
public class OriginDisplayScreenMixin extends Screen {

    protected OriginDisplayScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "renderOriginWindow", at = @At("HEAD"))
    void addCustomWindow(GuiGraphics context, int mouseX, int mouseY, CallbackInfo ci) {
        if (this instanceof CustomOriginScreen customOriginScreen) {
            RenderSystem.setShaderTexture(0, CustomOriginScreen.ORIGINS_CHOICES);
            context.drawTexture(CustomOriginScreen.ORIGINS_CHOICES, customOriginScreen.getCalculatedLeft(), customOriginScreen.getCalculatedTop(), 0, 0, CustomOriginScreen.CHOICES_WIDTH, CustomOriginScreen.CHOICES_HEIGHT);
        }
    }
}
