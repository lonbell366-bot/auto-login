package me.myauth.mixin;

import me.myauth.MyAuthClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisconnectedScreen.class)
public abstract class DisconnectedScreenMixin extends Screen {
    @Shadow @Final private Screen parent;
    @Unique private int ticksRemaining = 100;
    @Unique private ButtonWidget reconnectBtn;

    protected DisconnectedScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addReconnectButtons(CallbackInfo ci) {
        if (MyAuthClient.lastServerInfo == null || MyAuthClient.lastServerAddress == null) return;
        this.ticksRemaining = MyAuthClient.autoReconnectDelaySeconds * 20;

        this.reconnectBtn = this.addDrawableChild(ButtonWidget.builder(
                getReconnectText(),
                btn -> triggerReconnect()
        ).dimensions(this.width / 2 - 100, this.height - 30, 200, 20).build());
    }

    @Override
    public void tick() {
        super.tick();
        if (!MyAuthClient.autoReconnectEnabled || this.reconnectBtn == null || MyAuthClient.lastServerInfo == null) return;

        if (this.ticksRemaining > 0) {
            this.ticksRemaining--;
            this.reconnectBtn.setMessage(getReconnectText());
            if (this.ticksRemaining == 0) {
                triggerReconnect();
            }
        }
    }

    @Unique
    private Text getReconnectText() {
        if (!MyAuthClient.autoReconnectEnabled) {
            return Text.literal("Kết nối lại ngay");
        }
        int secs = (int) Math.ceil(this.ticksRemaining / 20.0);
        return Text.literal("Tự động kết nối lại sau " + secs + "s...");
    }

    @Unique
    private void triggerReconnect() {
        if (this.client != null && MyAuthClient.lastServerInfo != null && MyAuthClient.lastServerAddress != null) {
            ConnectScreen.connect(this.parent, this.client, MyAuthClient.lastServerAddress, MyAuthClient.lastServerInfo, false, null);
        }
    }
}
