package me.bebeli555.automapart.utils.objects;

import java.util.ArrayList;
import java.util.List;

public class ClientStatus {
    public static List<String> status = new ArrayList<>();

    public static void setStatus(String... s) {
        status = List.of(s);
    }

    public static void clearStatus() {
        status.clear();
    }
}
