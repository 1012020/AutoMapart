package me.bebeli555.automapart.gui.windows.windows.other;

import me.bebeli555.automapart.gui.windows.TitledWindow;
import net.minecraft.client.gui.DrawContext;
import me.bebeli555.automapart.gui.windows.components.ButtonComponent;

public class ContactsWindow extends TitledWindow {
    public static ContactsWindow INSTANCE;

    public ButtonComponent developer = new ButtonComponent(this, "Developer: bebeli555", true);
    public ButtonComponent discord = new ButtonComponent(this, "Discord: ADD LINK", true);
    public ButtonComponent website = new ButtonComponent(this, "Website: ADD LINK", true);

    public ContactsWindow() {
        super("Contacts", 100, 50);
        this.closeOnGuiClose = false;
        this.loadDefaults = true;
        this.enableX = 736;
        this.enableY = 12;

        INSTANCE = this;
    }

    @Override
    public void onDraw(DrawContext context, int mouseX, int mouseY, int lastMouseX, int lastMouseY) {
        int size = 12;

        developer.render(context, 15);
        discord.render(context, 15 + size);
        website.render(context, 15 + size * 2);
    }
}
