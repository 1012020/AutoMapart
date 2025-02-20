package me.bebeli555.automapart.gui.windows.windows.other;

import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.gui.Gui;
import me.bebeli555.automapart.gui.windows.TitledWindow;
import me.bebeli555.automapart.mods.ClientSettings;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.util.Arrays;
import java.util.Collections;

public class TipsWindow extends TitledWindow {
    public static TipsWindow INSTANCE;
    public int scroll;

    public String[] tips = {
            "Middle-click to reset setting values to their default",
            "Press space on mode setting to open selector with all the values",
            "Check all available commands in " + Mod.NAME + " with " + ClientSettings.prefix.string() + "help command",
            "If you have issue with the client don't hesitate to contact the dev",
            "Middle-click to reset windows or mod groups to their default pos",
            "Right click to set setting values through your keyboard"
    };

    public TipsWindow() {
        super("Tips", 125, 125);

        INSTANCE = this;
        this.closeOnGuiClose = false;
        this.loadDefaults = true;
        this.enableX = 736;
        this.enableY = 234;
        this.scrollAmount = 5;

        Collections.shuffle(Arrays.asList(tips));
    }

    @Override
    public void onDraw(DrawContext context, int mouseX, int mouseY, int lastMouseX, int lastMouseY) {
        MatrixStack stack = context.getMatrices();
        int index = 0;

        for (String tipArray : tips) {
            for (String tip : getStringListThatFitsIntoWidth(stack, tipArray, this.width)) {
                int yPos = this.y + (index * 10) + scroll;
                index++;

                if (yPos < this.y + 15) {
                    continue;
                } else if (yPos > this.y + this.height - 10) {
                    continue;
                }

                Gui.fontRenderer.drawString(stack, tip, this.x + 5, yPos, ClientSettings.titledWindowText.asInt());
            }

            index++;
        }
    }

    @Override
    public void onScroll(boolean up, int multiplier) {
        if (!isHoveringOver(lastMouseX, lastMouseY)) {
            return;
        }

        scroll += up ? multiplier : -multiplier;
    }
}
