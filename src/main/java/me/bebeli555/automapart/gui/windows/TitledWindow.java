package me.bebeli555.automapart.gui.windows;

import me.bebeli555.automapart.events.game.KeyInputEvent;
import me.bebeli555.automapart.gui.Gui;
import me.bebeli555.automapart.gui.windows.components.WindowComponent;
import me.bebeli555.automapart.gui.windows.windows.games.minesweeper.MineSweeperGameWindow;
import me.bebeli555.automapart.gui.windows.windows.games.snake.SnakeGameWindow;
import me.bebeli555.automapart.gui.windows.windows.games.tetris.TetrisGameWindow;
import me.bebeli555.automapart.gui.windows.windows.other.*;
import me.bebeli555.automapart.gui.windows.windows.pickers.pickers.*;
import me.bebeli555.automapart.gui.windows.windows.tools.ReflectorTool;
import me.bebeli555.automapart.gui.windows.windows.tools.TextureEditorTool;
import me.bebeli555.automapart.mods.ClientSettings;
import me.bebeli555.automapart.utils.globalsettings.GlobalBorderSettings;
import me.bebeli555.automapart.utils.Utils;
import me.bebeli555.automapart.utils.input.Mouse;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.List;

public class TitledWindow extends Utils {
    public static List<TitledWindow> list = new ArrayList<>();

    public String title;
    public int x, y;
    public int width, height;
    public boolean toggled;
    public boolean dontRender;
    public boolean dragging, clickedDrag;
    public int enableX, enableY;
    public boolean closeOnGuiClose = true;
    public boolean loadDefaults;
    public int rainbowI;
    public List<WindowComponent> windowComponents = new ArrayList<>();
    public int scrollAmount = 15;
    public static int mouseX, mouseY, lastMouseX, lastMouseY;
    public List<WindowClosedListener> closedListeners = new ArrayList<>();

    public TitledWindow(String title, int width, int height) {
        this.title = title;
        this.width = width;
        this.height = height;
        list.add(this);
    }

    public void onGuiClose() {}
    public void onClick(int mouseX, int mouseY, int button) {}
    public void onDraw(DrawContext context, int mouseX, int mouseY, int lastMouseX, int lastMouseY) {}
    public void onRender(float partialTicks) {}
    public void onOutsideClick() {}
    public void onEnabled() {}
    public void onScroll(boolean up, int scroll) {}
    public void onKey(KeyInputEvent e) {}
    public void onChar(char chr) {}

    public void enable(int x, int y) {
        setToTop();
        if (this.loadDefaults) {
            this.x = enableX;
            this.y = enableY;
        } else {
            this.x = x;
            this.y = y;
        }

        this.toggled = true;
        onEnabled();
    }

    public void onDisabled() {}
    public void disable() {
        this.toggled = false;

        List<WindowClosedListener> copy = new ArrayList<>(closedListeners);
        for (WindowClosedListener listener : copy) {
            if (listener.closed()) {
                closedListeners.remove(listener);
            }
        }

        onDisabled();
    }

    public void addCloseListener(WindowClosedListener listener) {
        this.closedListeners.add(listener);
    }

    public boolean isToggled() {
        return this.toggled;
    }

    public boolean isHoveringOver(int mouseX, int mouseY) {
        if (dontRender) {
            return false;
        }

        return x < mouseX && x + width > mouseX && y < mouseY && y + height > mouseY;
    }

    public void drawWindow(DrawContext context, int mouseX, int mouseY, int lastMouseX, int lastMouseY) {
        if (!isToggled() || dontRender) {
            return;
        }

        MatrixStack stack = context.getMatrices();

        TitledWindow.mouseX = mouseX;
        TitledWindow.mouseY = mouseY;
        TitledWindow.lastMouseX = lastMouseX;
        TitledWindow.lastMouseY = lastMouseY;

        //Draw container background
        Gui.drawRect(stack, x, y, x + width, y + height, ClientSettings.titledWindowBackground.asInt());

        //Draw border
        if (ClientSettings.titledWindowBorder.bool()) {
            int border = getBorderColor();
            GlobalBorderSettings.render(stack, x, y, x + width, y + height, border, ClientSettings.titledWindowBorderSize.asDouble());
        }

        //Draw title
        Gui.fontRenderer.drawString(stack, title, x + (float)width / 2 - (float)Gui.fontRenderer.getWidth(stack, title) / 2, y + 3, ClientSettings.titledWindowTitleText.asInt());

        //Drag window
        if (!Mouse.isButtonDown(0)) {
            clickedDrag = false;
        }

        if (((mouseY >= y && mouseY <= y + 10 && mouseX >= x && mouseX <= x + width) || dragging) && Mouse.isButtonDown(0) && clickedDrag) {
            this.x += mouseX - lastMouseX;
            this.y += mouseY - lastMouseY;
            dragging = true;
        } else {
            dragging = false;
        }

        onDraw(context, mouseX, mouseY, lastMouseX, lastMouseY);
    }

    public int getBorderColor() {
        int border = ClientSettings.titledWindowBorderColor.asInt();
        if (ClientSettings.titledWindowBorderRainbow.bool()) {
            border = Gui.gui.rainbow.getRainbowColorAt(rainbowI);
        }

        return border;
    }

    /**
     * Sets this as the top window
     */
    public void setToTop() {
        list.remove(this);
        list.add(0, this);
    }

    public void onClickParent(int mouseX, int mouseY, int mouseButton) {
        setToTop();
        onClick(mouseX, mouseY, mouseButton);

        for (WindowComponent component : windowComponents) {
            component.onComponentClick(mouseX, mouseY, mouseButton);
        }

        if (mouseY >= y && mouseY <= y + 10 && mouseX >= x && mouseX <= x + width) {
            if (mouseButton == 2) {
                if (this.loadDefaults) {
                    this.x = enableX;
                    this.y = enableY;
                    return;
                }
            }

            clickedDrag = true;
        }
    }

    public void onWindowChar(char chr) {
        onChar(chr);

        for (WindowComponent component : windowComponents) {
            component.onComponentChar(chr);
        }
    }

    public void onWindowKey(KeyInputEvent event) {
        onKey(event);

        for (WindowComponent component : windowComponents) {
            component.onComponentKey(event);
        }
    }

    public static void initWindows() {
        try {
            //Other
            new ColorPickerWindow();
            new TextSetterWindow();
            new SnakeGameWindow();
            new MineSweeperGameWindow();
            new TetrisGameWindow();
            new ContactsWindow();
            new ChangeLogWindow();
            new TipsWindow();
            new PositionPickerWindow();
            new ImageEditorWindow();
            new TextArrayWindow();

            //Tools
            new ReflectorTool();
            new TextureEditorTool();

            //Pickers
            new ItemPicker();
            new BlockPicker();
            new ParticlePicker();
            new EffectPicker();
            new EnchantmentPicker();
            new PotionPicker();
            new SoundPicker();
            new SettingModePicker();
            new EntityTypePicker();
            new EntityPicker();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class WindowClosedListener {
        public boolean closed() {return false;}
    }
}
