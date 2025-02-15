package me.bebeli555.automapart.gui.windows.windows.pickers;

import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.gui.Gui;
import me.bebeli555.automapart.gui.GuiNode;
import me.bebeli555.automapart.gui.windows.TitledWindow;
import me.bebeli555.automapart.gui.windows.components.ButtonComponent;
import me.bebeli555.automapart.gui.windows.components.TextFieldComponent;
import me.bebeli555.automapart.mods.ClientSettings;
import me.bebeli555.automapart.settings.Mode;
import me.bebeli555.automapart.settings.Setting;
import me.bebeli555.automapart.settings.Settings;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PickerWindow extends TitledWindow {
    public List<PickerItem> list = new ArrayList<>();
    public List<PickerItem> selected = new ArrayList<>();
    public int gap;
    public int scroll;
    public int startY = 45;
    public boolean onlyShowSelected;
    public boolean allowOnlyOneSelected;
    public boolean noWindowComponentRendering;
    public boolean noScroll;
    public boolean noSelectAllListener;
    public int lastMouseButtonClick;
    public Setting currentSetting;
    public List<Setting.ValueChangedListener> valueChangedListeners = new ArrayList<>();
    public PickerItem lastItemChanged;
    public List<ItemFilter> itemFilters = new ArrayList<>();

    public TextFieldComponent search = new TextFieldComponent(this, "Search", 100, 15);
    public ButtonComponent showSelected = new ButtonComponent(this, "Show selected", 15);
    public ButtonComponent selectAll = new ButtonComponent(this, "Select all");
    public ButtonComponent deselectAll = new ButtonComponent(this, "Deselect all");

    public PickerWindow(String name, int gap) {
        super(name, 200, 300);
        this.gap = gap;
        this.scrollAmount = gap;

        search.addClickListener(() -> scroll = 0);
        showSelected.addClickListener(() -> {
            scroll = 0;
            onlyShowSelected = !onlyShowSelected;
            showSelected.customDown = onlyShowSelected;

            search.text = "";
        });

        selectAll.addClickListener(() -> {
            if (noSelectAllListener) {
                return;
            }

            selected = list.stream().filter(this::testItemFilter).collect(Collectors.toList());
            scroll = 0;
            updateSettings();
        });

        deselectAll.addClickListener(() -> {
            selected.clear();
            scroll = 0;
            updateSettings();
        });
    }

    public PickerWindow(String name) {
        this(name, 20);
    }

    public void add(PickerItem item) {
        if (list.stream().noneMatch(i -> i.name.equals(item.name))) {
            list.add(item);
        }
    }

    public void onEnabled(Setting setting) {}
    public void enable(Setting setting, int x, int y) {
        this.currentSetting = setting;

        //Set saved values
        if (setting.mode == Mode.PICKER) {
            selected.clear();
            selected.addAll(getSelectedFromSetting(currentSetting));
        }

        onEnabled(setting);
        enable(x, y);
    }

    public void resetToDefaults(Setting setting) {
        selected.clear();
        selected.addAll(getSelectedFromSetting(setting));
        updateSettings();
    }

    public List<PickerItem> getSelectedFromSetting(Setting setting) {
        if (setting == null) {
            return new ArrayList<>();
        }

        String value = setting.string();
        List<PickerItem> list = new ArrayList<>();

        if (value.length() > 0) {
            if (value.contains("$")) {
                for (String splitValue : value.split("\\$")) {
                    PickerItem item = getPickerItemFromName(splitValue);
                    if (item != null) list.add(item);
                }
            } else {
                PickerItem item = getPickerItemFromName(value);
                if (item != null) list.add(item);
            }
        }

        return list;
    }

    public void addItemSelectedListener(Setting.ValueChangedListener listener) {
        this.valueChangedListeners.add(listener);
    }

    public void addItemSelectedListener(Runnable runnable, Mod mod) {
        this.valueChangedListeners.add(new Setting.ValueChangedListener(mod, mod != null){
            public void valueChanged() {
                runnable.run();
            }
        });
    }

    public PickerItem getPickerItemFromName(String name) {
        for (PickerItem item : list) {
            if (item.name.equals(name)) {
                return item;
            }
        }

        return null;
    }

    public void onRender(DrawContext context) {}

    @Override
    public void onDraw(DrawContext context, int mouseX, int mouseY, int lastMouseX, int lastMouseY) {
        //Render items
        int i = 0;
        for (PickerItem item : list) {
            if (!item.visible) {
                continue;
            }

            if (!item.name.toLowerCase().replace(" ", "").contains(search.text.toLowerCase().replace(" ", ""))) {
                continue;
            }

            if (onlyShowSelected && !selected.contains(item)) {
                continue;
            }

            if (!testItemFilter(item)) {
                continue;
            }

            int y = startY + (i * gap) + scroll;
            i++;

            if (y < startY) {
                continue;
            } else if (y >= this.height - 10) {
                break;
            }

            //Render texture, class overrides this
            item.renderItem.render(context, this.x, this.y + y);

            item.renderedX = this.x + 35;
            item.renderedY = this.y + y;
            item.renderedX2 = item.renderedX + Gui.fontRenderer.getWidth(context.getMatrices(), item.name) + 3;
            item.renderedY2 = item.renderedY + Gui.fontRenderer.getHeight(context.getMatrices()) + 2;

            //Draw background if selected
            if (selected.contains(item)) {
                Gui.drawRect(context.getMatrices(), item.renderedX, item.renderedY, item.renderedX2, item.renderedY2, ClientSettings.titledWindowOtherPickerSelected.asInt());
            }

            //Draw name
            Gui.fontRenderer.drawString(context.getMatrices(), item.name, item.renderedX + 1, item.renderedY + 2.5f, ClientSettings.titledWindowText.asInt());

            //Draw description if hovering over
            if (item.description != null && !item.description.isEmpty() && isInsideBounds(mouseX, mouseY, item)) {
                int drawX = item.renderedX2 + 5;
                int drawY = item.renderedY + 2;

                Gui.drawRect(context.getMatrices(), drawX - 1, drawY - 2, drawX + Gui.fontRenderer.getWidth(context.getMatrices(), item.description) + 2, drawY + Gui.fontRenderer.getHeight(context.getMatrices()), ClientSettings.descriptionBackground.asInt());
                Gui.fontRenderer.drawString(context.getMatrices(), item.description, drawX, drawY, ClientSettings.descriptionText.asInt());
            }
        }

        onRender(context);
        if (!noWindowComponentRendering) {
            search.width = allowOnlyOneSelected ? 150 : 100;
            search.render(context, 6, 15);

            if (!allowOnlyOneSelected) {
                showSelected.customHeight = 13;
                showSelected.render(context, 142, 16);

                selectAll.customWidth = 51;
                deselectAll.customWidth = 51;

                selectAll.render(context, 142, 35);
                deselectAll.render(context, 142, 47);
            }
        }
    }

    public boolean testItemFilter(PickerItem item) {
        if (currentSetting != null) {
            ItemFilter filter = null;
            for (ItemFilter filt : itemFilters) {
                if (filt.setting == currentSetting) {
                    filter = filt;
                }
            }

            if (filter != null) {
                return filter.filter.test(item);
            }
        }

        return true;
    }

    @Override
    public void onClick(int mouseX, int mouseY, int button) {
        this.lastMouseButtonClick = button;
        for (PickerItem item : onlyShowSelected ? selected : list) {
            if (!item.visible) {
                continue;
            }

            if (!item.name.toLowerCase().replace(" ", "").contains(search.text.toLowerCase().replace(" ", ""))) {
                continue;
            }

            if (isInsideBounds(mouseX, mouseY, item)) {
                if (allowOnlyOneSelected) {
                    selected.clear();
                }

                if (!selected.contains(item)) {
                    selected.add(item);
                } else {
                    selected.remove(item);
                }

                lastItemChanged = item;
                updateSettings();
                return;
            }
        }
    }

    public void updateSettings() {
        //Set the setting and node
        if (allowOnlyOneSelected) {
            setSetting(selected.get(0).name);
        } else {
            String value = "";
            for (PickerItem pickerItem : selected) {
                value += pickerItem.name + "$";
            }

            if (value.endsWith("$")) {
                value = value.substring(0, value.length() - 1);
            }

            setSetting(value);
        }

        for (Setting.ValueChangedListener listener : valueChangedListeners) {
            listener.valueChanged();
            listener.valueChanged(lastItemChanged);
        }
    }

    public boolean isInsideBounds(int mouseX, int mouseY, PickerItem item) {
        boolean isInsideBounds;
        if (item.renderedY2 >= item.renderedY) {
            isInsideBounds = mouseX >= item.renderedX && mouseX <= item.renderedX2 && mouseY >= item.renderedY && mouseY <= item.renderedY2;
        } else {
            isInsideBounds = mouseX >= item.renderedX && mouseX <= item.renderedX2 && mouseY >= item.renderedY2 && mouseY <= item.renderedY;
        }

        return isInsideBounds;
    }

    private void setSetting(String value) {
        if (currentSetting == null || currentSetting.id == null) {
            return;
        }

        GuiNode node = Settings.getGuiNodeFromId(currentSetting.id);
        if (node != null) {
            node.stringValue = value;
            node.setSetting();
        } else {
            currentSetting.setValue(value);
        }
    }

    @Override
    public void onOutsideClick() {
        disable();
    }

    @Override
    public void onEnabled() {
        scroll = 0;
    }

    @Override
    public void onScroll(boolean up, int multiplier) {
        if (this.noScroll) {
            return;
        }

        if (isHoveringOver(lastMouseX, lastMouseY)) {
            scroll += up ? multiplier : -multiplier;
        }
    }

    public static class PickerItem {
        public String name;
        public RenderItem renderItem;
        public String description;
        public Object customObject, customObject2;
        public int renderedX, renderedY, renderedX2, renderedY2;
        public boolean visible = true;

        public PickerItem(String name, Object renderObject, String description, Object customObject, Object customObject2) {
            this.name = name;
            this.description = description;
            this.renderItem = new RenderItem(renderObject);
            this.customObject = customObject;
            this.customObject2 = customObject2;
        }

        public PickerItem(String name, Object renderObject, String description, Object customObject) {
            this(name, renderObject, description, customObject, null);
        }

        public PickerItem(String name, Object renderObject, String description) {
            this(name, renderObject, description, null);
        }

        public PickerItem(String name, Object renderObject) {
            this(name, renderObject, "");
        }
    }

    public record ItemFilter(Setting setting, Predicate<PickerItem> filter) {}
}
