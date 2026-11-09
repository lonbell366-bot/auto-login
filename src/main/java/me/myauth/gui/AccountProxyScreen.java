package me.myauth.gui;

import me.myauth.MyAuthClient;
import me.myauth.core.AuthHelper;
import me.myauth.core.ProxyConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class AccountProxyScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget tokenField;
    private TextFieldWidget proxyHostField;
    private TextFieldWidget proxyPortField;
    private TextFieldWidget proxyUserField;
    private TextFieldWidget proxyPassField;
    private String statusText = "§7Nhập Access Token để đăng nhập hoặc cấu hình Proxy";

    public AccountProxyScreen(Screen parent) {
        super(Text.literal("Quản lý Token & Proxy"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;

        this.tokenField = new TextFieldWidget(this.textRenderer, centerX - 150, 35, 220, 20, Text.literal("Access Token"));
        this.tokenField.setMaxLength(4096);
        this.tokenField.setPlaceholder(Text.literal("Dán Access Token vào đây..."));
        this.addDrawableChild(this.tokenField);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Đăng nhập"), btn -> {
            AuthHelper.loginWithToken(this.tokenField.getText(), msg -> {
                this.statusText = msg;
                this.clearAndInit();
            });
        }).dimensions(centerX + 75, 35, 75, 20).build());

        int accY = 75;
        for (int i = 0; i < Math.min(4, MyAuthClient.savedAccounts.size()); i++) {
            MyAuthClient.SavedAccount acc = MyAuthClient.savedAccounts.get(i);
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Dùng: " + acc.username()), btn -> {
                AuthHelper.loginWithToken(acc.accessToken(), msg -> this.statusText = msg);
            }).dimensions(centerX - 150, accY, 240, 20).build());

            int index = i;
            this.addDrawableChild(ButtonWidget.builder(Text.literal("§cXóa"), btn -> {
                MyAuthClient.savedAccounts.remove(index);
                MyAuthClient.saveAccounts();
                this.clearAndInit();
            }).dimensions(centerX + 95, accY, 55, 20).build());
            accY += 24;
        }

        int proxyY = 180;
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Proxy: " + MyAuthClient.proxyConfig.type.name()),
                btn -> {
                    ProxyConfig.Type[] types = ProxyConfig.Type.values();
                    MyAuthClient.proxyConfig.type = types[(MyAuthClient.proxyConfig.type.ordinal() + 1) % types.length];
                    btn.setMessage(Text.literal("Proxy: " + MyAuthClient.proxyConfig.type.name()));
                }
        ).dimensions(centerX - 150, proxyY, 95, 20).build());

        this.proxyHostField = new TextFieldWidget(this.textRenderer, centerX - 50, proxyY, 130, 20, Text.literal("IP/Host"));
        this.proxyHostField.setPlaceholder(Text.literal("IP Proxy"));
        this.proxyHostField.setText(MyAuthClient.proxyConfig.host);
        this.addDrawableChild(this.proxyHostField);

        this.proxyPortField = new TextFieldWidget(this.textRenderer, centerX + 85, proxyY, 65, 20, Text.literal("Port"));
        this.proxyPortField.setPlaceholder(Text.literal("Port"));
        this.proxyPortField.setText(String.valueOf(MyAuthClient.proxyConfig.port));
        this.addDrawableChild(this.proxyPortField);

        this.proxyUserField = new TextFieldWidget(this.textRenderer, centerX - 150, proxyY + 25, 145, 20, Text.literal("User"));
        this.proxyUserField.setPlaceholder(Text.literal("Proxy User (nếu có)"));
        this.proxyUserField.setText(MyAuthClient.proxyConfig.username);
        this.addDrawableChild(this.proxyUserField);

        this.proxyPassField = new TextFieldWidget(this.textRenderer, centerX + 5, proxyY + 25, 145, 20, Text.literal("Pass"));
        this.proxyPassField.setPlaceholder(Text.literal("Proxy Pass (nếu có)"));
        this.proxyPassField.setText(MyAuthClient.proxyConfig.password);
        this.addDrawableChild(this.proxyPassField);

        int utilY = proxyY + 55;
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("HoverLoot: " + (MyAuthClient.hoverLootEnabled ? "§aBẬT" : "§cTẮT")),
                btn -> {
                    MyAuthClient.hoverLootEnabled = !MyAuthClient.hoverLootEnabled;
                    btn.setMessage(Text.literal("HoverLoot: " + (MyAuthClient.hoverLootEnabled ? "§aBẬT" : "§cTẮT")));
                }
        ).dimensions(centerX - 150, utilY, 95, 20).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Giữ Chuột: " + (MyAuthClient.noCursorResetEnabled ? "§aBẬT" : "§cTẮT")),
                btn -> {
                    MyAuthClient.noCursorResetEnabled = !MyAuthClient.noCursorResetEnabled;
                    btn.setMessage(Text.literal("Giữ Chuột: " + (MyAuthClient.noCursorResetEnabled ? "§aBẬT" : "§cTẮT")));
                }
        ).dimensions(centerX - 48, utilY, 96, 20).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("AutoRecon: " + (MyAuthClient.autoReconnectEnabled ? "§aBẬT" : "§cTẮT")),
                btn -> {
                    MyAuthClient.autoReconnectEnabled = !MyAuthClient.autoReconnectEnabled;
                    btn.setMessage(Text.literal("AutoRecon: " + (MyAuthClient.autoReconnectEnabled ? "§aBẬT" : "§cTẮT")));
                }
        ).dimensions(centerX + 55, utilY, 95, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Lưu & Quay lại"), btn -> {
            saveProxyFields();
            this.client.setScreen(this.parent);
        }).dimensions(centerX - 100, this.height - 28, 200, 20).build());
    }

    private void saveProxyFields() {
        MyAuthClient.proxyConfig.host = this.proxyHostField.getText().trim();
        try {
            MyAuthClient.proxyConfig.port = Integer.parseInt(this.proxyPortField.getText().trim());
        } catch (NumberFormatException ignored) {}
        MyAuthClient.proxyConfig.username = this.proxyUserField.getText().trim();
        MyAuthClient.proxyConfig.password = this.proxyPassField.getText().trim();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int centerX = this.width / 2;
        String currentAcc = this.client.getSession().getUsername();
        context.drawCenteredTextWithShadow(this.textRenderer, "§lQUẢN LÝ TOKEN & PROXY - Đang dùng: §a" + currentAcc, centerX, 12, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, this.statusText, centerX, 60, 0xFFFFFF);
    }
}
