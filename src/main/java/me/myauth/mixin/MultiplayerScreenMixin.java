package me.myauth.mixin;

import me.myauth.MyAuthClient;
import me.myauth.gui.AccountProxyScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiplayerScreen.class)
public abstract class MultiplayerScreenMixin extends Screen {
    protected MultiplayerScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addAuthButton(CallbackInfo ci) {
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Token & Proxy"),
                btn -> this.client.setScreen(new AccountProxyScreen(this))
        ).dimensions(8, 6, 95, 20).build());
    }

    @Inject(method = "connect(Lnet/minecraft/client/network/ServerInfo;)V", at = @At("HEAD"))
    private void captureServer(ServerInfo entry, CallbackInfo ci) {
        MyAuthClient.lastServerInfo = entry;
        MyAuthClient.lastServerAddress = ServerAddress.parse(entry.address);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderAccountStatus(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        String user = this.client.getSession().getUsername();
        String proxyStatus = MyAuthClient.proxyConfig.isActive()
                ? "§a" + MyAuthClient.proxyConfig.type + " (" + MyAuthClient.proxyConfig.host + ")"
                : "§7Tắt";
        context.drawTextWithShadow(this.textRenderer, "§7Tài khoản: §a" + user + " §8| §7Proxy: " + proxyStatus, 110, 12, 0xFFFFFF);
    }
}
