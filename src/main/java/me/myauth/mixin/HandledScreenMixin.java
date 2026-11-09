package me.myauth.mixin;

import me.myauth.MyAuthClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> {
    @Shadow @Nullable protected Slot focusedSlot;
    @Shadow @Final protected T handler;

    @Unique private int lastHoveredSlotId = -1;

    @Inject(method = "init", at = @At("TAIL"))
    private void restoreCursorPosition(CallbackInfo ci) {
        if (MyAuthClient.noCursorResetEnabled && MyAuthClient.lastMouseX >= 0 && MyAuthClient.lastMouseY >= 0) {
            long window = MinecraftClient.getInstance().getWindow().getHandle();
            GLFW.glfwSetCursorPos(window, MyAuthClient.lastMouseX, MyAuthClient.lastMouseY);
        }
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void saveCursorPosition(CallbackInfo ci) {
        if (MyAuthClient.noCursorResetEnabled) {
            MinecraftClient client = MinecraftClient.getInstance();
            MyAuthClient.lastMouseX = client.mouse.getX();
            MyAuthClient.lastMouseY = client.mouse.getY();
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void handleHoverLoot(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!MyAuthClient.hoverLootEnabled || this.focusedSlot == null || !this.focusedSlot.hasStack()) {
            this.lastHoveredSlotId = -1;
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        long window = client.getWindow().getHandle();
        boolean leftMouseDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;

        if (leftMouseDown && Screen.hasShiftDown()) {
            if (this.focusedSlot.id != this.lastHoveredSlotId && client.interactionManager != null) {
                this.lastHoveredSlotId = this.focusedSlot.id;
                client.interactionManager.clickSlot(
                        this.handler.syncId,
                        this.focusedSlot.id,
                        0,
                        SlotActionType.QUICK_MOVE,
                        client.player
                );
            }
        } else {
            this.lastHoveredSlotId = -1;
        }
    }
}
