package me.bebeli555.automapart.gui.windows.windows.other;

import me.bebeli555.automapart.gui.windows.TitledWindow;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import me.bebeli555.automapart.gui.Gui;
import me.bebeli555.automapart.mods.ClientSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChangeLogWindow extends TitledWindow {
    public static ChangeLogWindow INSTANCE;
    public int scroll;

    String[] changelog = {
            "v1.0-beta: 2023-1-1",
            "- Released!",
            "",
            "v1.0-test",
            "- Testing changelog",
    };

    public ChangeLogWindow() {
        super("Changelog", 125, 165);
        INSTANCE = this;

        this.closeOnGuiClose = false;
        this.loadDefaults = true;
        this.enableX = 736;
        this.enableY = 65;
    }

    @Override
    public void onDraw(DrawContext context, int mouseX, int mouseY, int lastMouseX, int lastMouseY) {
        MatrixStack stack = context.getMatrices();
        List<String> list = new ArrayList<>(Arrays.asList(changelog));

        int index = 0;
        for (String instance : list) {
            for (String s : getStringListThatFitsIntoWidth(stack, instance, this.width)) {
                int yPos = this.y + (index * 10) + scroll + 15;
                index++;

                if (yPos < this.y + 15) {
                    continue;
                } else if (yPos > this.y + this.height - 10) {
                    continue;
                }

                Gui.fontRenderer.drawString(stack, s, this.x + 5, yPos, ClientSettings.titledWindowText.asInt());
            }
        }
    }

    @Override
    public void onScroll(boolean up, int multiplier) {
        if (isHoveringOver(lastMouseX, lastMouseY)) {
            scroll += up ? multiplier : -multiplier;
        }
    }
}
