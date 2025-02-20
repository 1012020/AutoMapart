package me.bebeli555.automapart.settings;

import me.bebeli555.automapart.Mod;

public class SettingValue extends Mod {
    public Object value;
    public double minValue, maxValue, step;

    public SettingValue(Object value, double minValue, double maxValue, double step) {
        this.value = value;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.step = step;
    }

    public SettingValue(Object value, double minValue, double maxValue) {
        this(value, minValue, maxValue, 1);
    }
}
