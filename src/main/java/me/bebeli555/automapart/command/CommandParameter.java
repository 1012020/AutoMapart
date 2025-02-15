package me.bebeli555.automapart.command;

import java.util.ArrayList;
import java.util.List;

public class CommandParameter {
    public String name, value;
    public String writtenCommand;
    public List<String> completions;
    public Runnable runnable;
    public int index;
    public CommandParameterGroup insideGroup;

    public CommandParameter(int index, String name, String value, Runnable runnable) {
        this.index = index;
        this.name = name;
        this.value = value;
        this.runnable = runnable;
    }

    public CommandParameter(int index, String name, String value) {
        this(index, name, value, null);
    }

    public CommandParameter(int index, String name) {
        this(index, name, null, null);
    }

    public CommandParameter(int index, String name, Runnable runnable) {
        this(index, name, null, runnable);
    }

    public String getUsageHint() {
        String hint = "[" + name;

        if (value != null && !value.isEmpty()) {
            hint += " (" + value + ")";
        }

        hint += "]";
        return hint;
    }

    public boolean nameStartsWith(String s) {
        if (name.contains("/")) {
            for (String split : name.split("/")) {
                if (split.startsWith(s)) {
                    return true;
                }
            }

            return false;
        } else {
            return name.startsWith(s);
        }
    }

    public void setCompletions(List<String> completions) {
        this.completions = completions;
    }

    public static class CommandParameterGroup {
        public List<CommandParameter> parameters = new ArrayList<>();

        public CommandParameterGroup(CommandParameter... parameters) {
            for (CommandParameter parameter : parameters) {
                parameter.insideGroup = this;
                this.parameters.add(parameter);
            }
        }
    }
}
