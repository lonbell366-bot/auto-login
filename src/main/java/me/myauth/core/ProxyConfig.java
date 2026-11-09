package me.myauth.core;

public class ProxyConfig {
    public enum Type { NONE, SOCKS5, HTTP }

    public Type type = Type.NONE;
    public String host = "";
    public int port = 1080;
    public String username = "";
    public String password = "";

    public boolean isActive() {
        return type != Type.NONE && !host.isBlank() && port > 0 && port <= 65535;
    }
}
