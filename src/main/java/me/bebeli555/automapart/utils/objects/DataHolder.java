package me.bebeli555.automapart.utils.objects;

import me.bebeli555.automapart.settings.Settings;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DataHolder {
    public String name;
    public List<String[]> data = new ArrayList<>();

    public DataHolder(String name) {
        this.name = name;

        //Retrieve the data from the file
        File file = new File(Settings.path + "/data/" + name);
        if (file.exists()) {
            try {
                Scanner scanner = new Scanner(file);
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.matches(".*(?<!\\\\), .*")) {
                        String[] split = line.split("(?<!\\\\), ");
                        for (int i = 0; i < split.length; i++) {
                            split[i] = split[i].replace("\\,", ",");
                        }

                        data.add(split);
                    } else {
                        data.add(new String[]{line.replace("\\,", ",")});
                    }
                }

                scanner.close();
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Sierra failed to load DataHolder: " + name);
            }
        }
    }

    /**
     * Writes the current data variable to the file
     */
    public void updateToFile() {
        try {
            File file = new File(Settings.path + "/data/" + name);
            file.delete();
            file.createNewFile();

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
            for (String[] split : data) {
                String output = "";
                for (String s : split) {
                    output += s.replace(",", "\\,") + ", ";
                }

                if (output.length() > 2) {
                    output = output.substring(0, output.length() - 2);
                }

                bw.write(output);
                bw.newLine();
            }

            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Sierra failed to save DataHolder: " + name);
        }
    }
}
