package me.bebeli555.automapart.gui.windows.windows.tools;

import me.bebeli555.automapart.gui.Gui;
import me.bebeli555.automapart.gui.GuiNode;
import me.bebeli555.automapart.gui.windows.TitledWindow;
import me.bebeli555.automapart.gui.windows.components.ButtonComponent;
import me.bebeli555.automapart.gui.windows.components.TextFieldComponent;
import me.bebeli555.automapart.gui.windows.components.WindowComponent;
import me.bebeli555.automapart.gui.windows.windows.pickers.PickerWindow;
import me.bebeli555.automapart.mods.ClientSettings;
import me.bebeli555.automapart.settings.Setting;
import me.bebeli555.automapart.settings.Settings;
import me.bebeli555.automapart.utils.ClientUtils;
import me.bebeli555.automapart.utils.objects.Timer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Formatting;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ReflectorTool extends TitledWindow {
    public static ReflectorTool INSTANCE;

    public PickerWindow classPicker = new PickerWindow("ClassPicker");
    public ButtonComponent returnComponent = new ButtonComponent(this, "Return");

    public Set<Class<?>> classList = new HashSet<>();
    public Map<String, Class<?>> classHash = new HashMap<>();

    public List<WindowComponent> fieldComponents = new ArrayList<>();
    public String selectedPath = "";
    public int scroll;

    public boolean loading;
    public String loadingDots = ".";
    public Timer loadingDotTimer = new Timer();

    public MatrixStack lastStack;

    public ReflectorTool() {
        super("Reflector", 125, 200);
        INSTANCE = this;

        returnComponent.addClickListener(() -> {
            selectedPath = getLastSplit(selectedPath, ".")[0];
            width = 125;
            dontRender = true;
            classPicker.enable(INSTANCE.x, INSTANCE.y);
            classPicker.valueChangedListeners.get(0).valueChanged();
        });

        classPicker.showSelected.name = "Back";
        classPicker.showSelected.addClickListener(() -> {
            classPicker.showSelected.down = false;
            classPicker.showSelected.customDown = false;

            selectedPath = getLastSplit(selectedPath, ".")[0];
            classPicker.valueChangedListeners.get(0).valueChanged();
        });

        classPicker.addCloseListener(new WindowClosedListener(){
            public boolean closed() {
                if (INSTANCE.dontRender) {
                    GuiNode node = Settings.getGuiNodeFromId(ClientSettings.reflectorTool.id);
                    node.toggled = false;
                    node.setSetting();
                }

                return false;
            }
        });

        classPicker.addItemSelectedListener(new Setting.ValueChangedListener(){
            public void valueChanged() {
                try {
                    if (!classPicker.selected.isEmpty()) {
                        selectedPath += (selectedPath.isEmpty() ? "" : ".") + classPicker.selected.get(0).name;
                    }

                    classPicker.title = "ClassPicker: " + selectedPath;

                    classPicker.scroll = 0;
                    classPicker.list.clear();
                    classPicker.selected.clear();
                    classPicker.onlyShowSelected = false;

                    //Class selected
                    if (isClass(selectedPath)) {
                        INSTANCE.windowComponents.removeAll(fieldComponents);
                        fieldComponents.clear();

                        List<FieldOrMethod> list = new ArrayList<>();
                        for (Field field : classList.stream().filter(c -> c.getName().equals(selectedPath)).toList().get(0).getFields()) {
                            list.add(new FieldOrMethod(field));
                        }

                        for (Method method : classList.stream().filter(c -> c.getName().equals(selectedPath)).toList().get(0).getMethods()) {
                            list.add(new FieldOrMethod(method));
                        }

                        for (FieldOrMethod reflected : list) {
                            boolean isStatic = Modifier.isStatic(reflected.getModifiers());
                            boolean allowChange = isStatic && !Modifier.isFinal(reflected.getModifiers()) && (
                                reflected.getType() == int.class ||
                                reflected.getType() == double.class ||
                                reflected.getType() == float.class ||
                                reflected.getType() == String.class ||
                                reflected.getType() == boolean.class
                            );

                            TextFieldComponent component = new TextFieldComponent(INSTANCE, reflected.getName() + " (" + reflected.getType().getSimpleName() + ")", 100, 11, !allowChange);

                            if (reflected.isMethod()) {
                                String params = "";
                                for (Parameter parameter : reflected.method.getParameters()) {
                                    params += parameter.getType().getSimpleName() + ", ";
                                }

                                if (params.endsWith(", ")) {
                                    params = params.substring(0, params.length() - 2);
                                }

                                component.name = component.name.replace(" (", "(" + params + ") (");
                            }

                            int componentWidth = Gui.fontRenderer.getWidth(lastStack, component.name + ":") + 25;
                            if (!component.onlyRenderText) {
                                componentWidth += component.width;
                            }

                            if (componentWidth > INSTANCE.width) {
                                INSTANCE.width = componentWidth;
                            }

                            if (isStatic && reflected.isField()) {
                                reflected.setAccessible();
                                component.text = "" + reflected.get();
                            }

                            component.addClickListener(() -> {
                                if (reflected.isMethod()) {
                                    return;
                                }

                                Object value = null;
                                try {
                                    if (reflected.getType() == String.class) {
                                        value = component.text;
                                    } else if (reflected.getType() == int.class) {
                                        value = Integer.parseInt(component.text);
                                    } else if (reflected.getType() == double.class) {
                                        value = Double.parseDouble(component.text);
                                    } else if (reflected.getType() == float.class) {
                                        value = Float.parseFloat(component.text);
                                    } else if (reflected.getType() == boolean.class) {
                                        value = Boolean.parseBoolean(component.text);
                                    }
                                } catch (Exception ignored) {
                                    //Mod.sendMessage("Error parsing " + reflected.getType().getSimpleName() + " from " + reflected.getName(), true, "Reflector");
                                }

                                if (value != null) {
                                    try {
                                        reflected.set(value);
                                    } catch (Exception ignored) {}
                                }
                            });

                            if (reflected.isMethod()) {
                                component.onlyRenderText = true;
                            }

                            //Create invoke button for method
                            if (reflected.isMethod() && reflected.method.getParameters().length == 0 && isStatic) {
                                ButtonComponent button = new ButtonComponent(INSTANCE, "Invoke");
                                button.customObject = Gui.fontRenderer.getWidth(lastStack, component.name) + 11;
                                fieldComponents.add(button);

                                button.addClickListener(() -> {
                                    try {
                                        reflected.invoke();
                                    } catch (Exception ignored) {}
                                });
                            }

                            fieldComponents.add(component);
                        }

                        INSTANCE.x = classPicker.x;
                        INSTANCE.y = classPicker.y;

                        dontRender = false;
                        classPicker.disable();
                        return;
                    }

                    for (Class<?> clazz : classList) {
                        if (clazz.getName().startsWith(selectedPath)) {
                            try {
                                String name = clazz.getName().substring(selectedPath.isEmpty() ? 0 : selectedPath.length() + 1);
                                if (name.contains(".")) {
                                    name = name.split("\\.")[0];
                                }

                                Item item = clazz.getName().contains("net.minecraft") ? Items.GRASS_BLOCK : Items.PAPER;
                                if (isClass(selectedPath + "." + name)) {
                                    item = Items.COMMAND_BLOCK;
                                }

                                classPicker.add(new PickerWindow.PickerItem(name, item));
                            } catch (IndexOutOfBoundsException ignored) {}
                        }
                    }
                } catch (Exception e) {
                    classPicker.disable();
                    disable();

                    e.printStackTrace();
                    ClientUtils.sendMessage("Closed reflector due to exception", Formatting.RED, "Reflector");
                }
            }
        });
    }

    @Override
    public void onEnabled() {
        this.width = 125;

        selectedPath = "";
        this.dontRender = false;
        this.closeOnGuiClose = false;

        new Thread(() -> {
            loading = true;

            loadClasses();
            classPicker.valueChangedListeners.get(0).valueChanged();
            classPicker.enable(INSTANCE.x, INSTANCE.y);

            this.dontRender = true;
            loading = false;
        }).start();
    }

    @Override
    public void onDraw(DrawContext context, int mouseX, int mouseY, int lastMouseX, int lastMouseY) {
        lastStack = context.getMatrices();

        if (loading) {
            if (loadingDots.length() > 3) {
                loadingDots = ".";
            }

            String[] content = {"Loading classes" + loadingDots, "Takes up to 30 seconds", "Be patient! :)"};
            for (int i = 0; i < content.length; i++) {
                String s = content[i];
                Gui.fontRenderer.drawString(context.getMatrices(), s, this.x + (float)this.width / 2 - (float)Gui.fontRenderer.getWidth(context.getMatrices(), s) / 2, this.y + 25 + (i * 10), ClientSettings.titledWindowText.asInt());
            }

            if (loadingDotTimer.hasPassed(1000)) {
                loadingDotTimer.reset();
                loadingDots += ".";
            }
        } else {
            returnComponent.customWidth = Gui.fontRenderer.getWidth(context.getMatrices(), "Show selected");
            returnComponent.render(context, this.width - 57, 3);

            String[] split = selectedPath.split("\\.");
            this.title = "Reflector: " + split[split.length - 1];

            int index = 0;
            for (WindowComponent component : fieldComponents) {
                int y = index * 15 + scroll + 15;
                index++;

                if (y < 15 || y > this.height - 15) {
                    continue;
                }

                if (component instanceof TextFieldComponent textField) {
                    textField.render(context, 5, y);
                } else if (component instanceof ButtonComponent button) {
                    index--;
                    button.render(context, (int)button.customObject, y + 1);
                }
            }
        }
    }

    @Override
    public void onScroll(boolean up, int multiplier) {
        if (isHoveringOver(lastMouseX, lastMouseY)) {
            scroll += up ? multiplier : -multiplier;
        }
    }

    public void loadClasses() {
        ConfigurationBuilder configuration = new ConfigurationBuilder().forPackages("me", "net", "org", "com", "meteordevelopment", "baritone", "io").addScanners(new SubTypesScanner(false));
        Reflections reflections = new Reflections(configuration);

        this.classList = new HashSet<>(reflections.getSubTypesOf(Object.class));
        this.classHash = this.classList.stream().collect(Collectors.toMap(Class::getName, Function.identity(), (a, b) -> b, HashMap::new));
    }

    public boolean isClass(String path) {
        return this.classHash.get(path) != null;
    }

    public static class FieldOrMethod {
        public Field field;
        public Method method;

        public FieldOrMethod(Field field) {
            this.field = field;
        }

        public FieldOrMethod(Method method) {
            this.method = method;
        }

        public Class<?> getType() {
            return field != null ? field.getType() : method.getReturnType();
        }

        public String getName() {
            return field != null ? field.getName() : method.getName();
        }

        public int getModifiers() {
            return field != null ? field.getModifiers() : method.getModifiers();
        }

        public Object get() throws Exception {
            return field.get(null);
        }

        public Object invoke() throws Exception {
            return method.invoke(null);
        }

        public void setAccessible() {
            if (field != null) {
                field.setAccessible(true);
            } else {
                method.setAccessible(true);
            }
        }

        public void set(Object value) throws Exception {
            if (field != null) {
                field.set(null, value);
            }
        }

        public boolean isField() {
            return field != null;
        }

        public boolean isMethod() {
            return method != null;
        }
    }
}
