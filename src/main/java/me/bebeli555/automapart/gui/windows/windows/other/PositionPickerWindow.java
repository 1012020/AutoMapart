package me.bebeli555.automapart.gui.windows.windows.other;

import me.bebeli555.automapart.gui.windows.TitledWindow;
import me.bebeli555.automapart.gui.windows.components.ButtonComponent;
import me.bebeli555.automapart.gui.windows.components.TextFieldComponent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Vec3d;

public class PositionPickerWindow extends TitledWindow {
    public static PositionPickerWindow INSTANCE;

    public TextFieldComponent xField = new TextFieldComponent(this, "X", 100, 11, false, true);
    public TextFieldComponent yField = new TextFieldComponent(this, "Y", 100, 11, false, true);
    public TextFieldComponent zField = new TextFieldComponent(this, "Z", 100, 11, false, true);

    public ButtonComponent fromPlayerButton = new ButtonComponent(this, "From player", 10, 43, false);
    public ButtonComponent setPositionButton = new ButtonComponent(this, "Set position", 10, 43, false);

    public PositionPickerWindow() {
        super("PositionPicker", 120, 90);
        INSTANCE = this;

        fromPlayerButton.addClickListener(() -> {
            xField.text = "" + mc.getCameraEntity().getBlockPos().getX();
            yField.text = "" + mc.getCameraEntity().getBlockPos().getY();
            zField.text = "" + mc.getCameraEntity().getBlockPos().getZ();
        });
    }

    @Override
    public void onOutsideClick() {
        this.disable();
    }

    @Override
    public void onDraw(DrawContext context, int mouseX, int mouseY, int lastMouseX, int lastMouseY) {
        xField.render(context, 5, 20);
        yField.render(context, 5, 20 + 15);
        zField.render(context, 5, 20 + 15 * 2);

        fromPlayerButton.render(context, 13, 72);
        setPositionButton.render(context, 65, 72);
    }

    public void setPosition(Vec3d vec) {
        xField.text = "" + vec.x;
        yField.text = "" + vec.y;
        zField.text = "" + vec.z;
    }

    public Vec3d getPosition() {
        return new Vec3d(Double.parseDouble(xField.text), Double.parseDouble(yField.text), Double.parseDouble(zField.text));
    }
}
