package me.myauth.mixin;

import io.netty.channel.Channel;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import me.myauth.MyAuthClient;
import me.myauth.core.ProxyConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetSocketAddress;

@Mixin(targets = "net.minecraft.network.ClientConnection$1")
public class NettyProxyMixin {
    @Inject(method = "initChannel(Lio/netty/channel/Channel;)V", at = @At("HEAD"))
    private void injectProxyHandler(Channel channel, CallbackInfo ci) {
        ProxyConfig cfg = MyAuthClient.proxyConfig;
        if (!cfg.isActive()) return;

        InetSocketAddress addr = new InetSocketAddress(cfg.host, cfg.port);
        boolean hasAuth = !cfg.username.isEmpty();

        if (cfg.type == ProxyConfig.Type.SOCKS5) {
            channel.pipeline().addFirst("myauth_socks5",
                    hasAuth ? new Socks5ProxyHandler(addr, cfg.username, cfg.password) : new Socks5ProxyHandler(addr));
        } else if (cfg.type == ProxyConfig.Type.HTTP) {
            channel.pipeline().addFirst("myauth_http",
                    hasAuth ? new HttpProxyHandler(addr, cfg.username, cfg.password) : new HttpProxyHandler(addr));
        }
    }
}
