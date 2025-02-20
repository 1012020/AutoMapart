package me.bebeli555.automapart.settings;

import java.util.ArrayList;
import java.util.List;

public class SettingList {
    public List<Setting> list = new ArrayList<>();

    public void add(Setting setting) {
        list.add(setting);
    }

    public void add(Setting... settings) {
        for (Setting setting : settings) {
            add(setting);
        }
    }

    public Setting get(int index) {
        return list.get(index);
    }

    public Setting get(String id) {
        return list.stream().filter(s -> s.id.equals(id)).findFirst().orElse(null);
    }
}
