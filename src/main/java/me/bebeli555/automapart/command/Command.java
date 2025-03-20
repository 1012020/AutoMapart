package me.bebeli555.automapart.command;

import me.bebeli555.automapart.command.commands.*;
import me.bebeli555.automapart.mods.ClientSettings;
import me.bebeli555.automapart.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class Command extends Utils {
    public static List<Command> list = new ArrayList<>();

    public String name;
    public String description;
    public Object[] parameters;
    public boolean newLine;
    public static CommandParameter active;

    public Command(String name, String description, Object... parameters) {
        this.name = name;
        this.description = description;
        this.parameters = parameters;
        list.add(this);
    }

    public Command() {}
    public void onCommand(String[] parameters) throws Exception {}

    public String getUsageHint(boolean showString) {
        String usage = showString ? ClientSettings.prefix.string() + name + " " : "";

        for (Object param : parameters) {
            if (param instanceof CommandParameter.CommandParameterGroup parameter) {
                usage += "{";
                for (CommandParameter parameter1 : parameter.parameters) {
                    usage += parameter1.getUsageHint();
                    usage += " ";
                }

                usage = usage.substring(0, usage.length() - 1);
                usage += "}, ";
            } else if (param instanceof CommandParameter parameter) {
                usage += parameter.getUsageHint();
                usage += " ";
            }
        }

        if (usage.endsWith(", ")) {
            usage = usage.substring(0, usage.length() - 2);
        }

        return usage;
    }

    public String getUsageHint() {
        return getUsageHint(true);
    }

    public static String getCommandNotFoundHint() {
        return "Command not found, check " + HelpCommand.INSTANCE.getUsageHint();
    }

    public static Command getCommand(String name) {
        for (Command command : list) {
            if (command.name.equalsIgnoreCase(name)) {
                return command;
            }
        }

        return null;
    }

    public String getStringValue(Object[] object) {
        String value = "";
        for (Object o : object) {
            value += getStringValue(o) + " ";
        }

        if (value.length() > 0) {
            value = value.substring(0, value.length() - 1);
        }

        return value;
    }

    private String getStringValue(Object object) {
        String value = String.valueOf(object);
        if (value.endsWith(".0")) {
            value = value.replace(".0", "");
        }

        return value;
    }

    public double getDouble(Object object) {
        try {
            return (double)object;
        } catch (Exception e) {
            return Double.parseDouble(String.valueOf(object));
        }
    }

    /**
     * Gets the parameters and also the parameters inside the groups
     */
    public List<CommandParameter> getAllParams() {
        List<CommandParameter> list = new ArrayList<>();
        for (Object object : parameters) {
            if (object instanceof CommandParameter parameter) {
                list.add(parameter);
            } else if (object instanceof CommandParameter.CommandParameterGroup param) {
                list.addAll(param.parameters);
            }
        }

        return list;
    }

    /**
     * Gets the current completions for the given command
     * This code goes beyond human intelligence so pray we don't need to change it anymore
     */
    public static List<CommandCompletion> getCompletions(String command) {
        String real = command;

        List<CommandCompletion> list = new ArrayList<>();
        int prefixLength = ClientSettings.prefix.string().length();
        command = command.substring(prefixLength).toLowerCase();

        int spaces = command.replaceAll("[^ ]", "").length() - 1;
        if (!command.contains(" ")) {
            for (Command cmd : Command.list) {
                if (cmd.name.startsWith(command) && !cmd.name.equals(command)) {
                    list.add(new CommandCompletion(cmd.name, prefixLength));
                }
            }
        } else {
            Command cmd = getCommand(command.split(" ")[0]);
            if (cmd != null) {
                //First get all standalone parameters
                for (CommandParameter parameter : cmd.getAllParams()) {
                    String[] split = real.split(" ");
                    String last = split[split.length - 1].toLowerCase();
                    if (real.replaceAll("[^ ]", "").length() == split.length) {
                        last = "";
                    }

                    int pos = real.length() - last.length();
                    if (parameter.index == spaces) {
                        //Don't add this parameter if it's a param and the parent is a group
                        CommandParameter parent = null;
                        if (split.length > 2) {
                            for (CommandParameter parameter1 : cmd.getAllParams()) {
                                String splitName = split[1];
                                List<String> names = new ArrayList<>();
                                names.add(parameter1.name);
                                names.addAll(List.of(parameter1.name.split("/")));

                                for (String name : names) {
                                    if (name.equals(splitName)) {
                                        parent = parameter1;
                                        break;
                                    }
                                }
                            }
                        }

                        if (parent != null) {
                            if (parent.insideGroup != parameter.insideGroup) {
                                continue;
                            }
                        }

                        if (parameter.value == null || parameter.value.isEmpty()) {
                            if (parameter.runnable != null) {
                                parameter.writtenCommand = command;
                                active = parameter;
                                parameter.runnable.run();
                            }

                            boolean added = false;
                            if (parameter.completions != null && !parameter.completions.isEmpty()) {
                                for (String s : parameter.completions) {
                                    if (s.toLowerCase().startsWith(last)) {
                                        list.add(new CommandCompletion(s, pos));
                                        added = true;
                                    }
                                }
                            }

                            if (added) {
                                continue;
                            }
                        }

                        if (parameter.nameStartsWith(last)) {
                            if (parameter.name.contains("/")) {
                                for (String name : parameter.name.split("/")) {
                                    if (name.startsWith(last)) {
                                        list.add(new CommandCompletion(name, pos));
                                    }
                                }
                            } else {
                                list.add(new CommandCompletion(parameter.name, pos));
                            }
                        }
                    } else if (parameter.index == spaces - 1 && parameter.value != null && !parameter.value.isEmpty()) {
                        if (parameter.name.contains("/")) {
                            boolean found = false;
                            for (String s : parameter.name.split("/")) {
                                if (command.contains(" " + s + " ")) {
                                    found = true;
                                    break;
                                }
                            }

                            if (!found) {
                                continue;
                            }
                        } else if (!command.contains(" " + parameter.name + " ")) {
                            continue;
                        }

                        if (parameter.runnable != null) {
                            parameter.writtenCommand = command;
                            active = parameter;
                            parameter.runnable.run();
                        }

                        if (parameter.completions != null && !parameter.completions.isEmpty()) {
                            for (String s : parameter.completions) {
                                if (s.toLowerCase().startsWith(last)) {
                                    list.add(new CommandCompletion(s, pos));
                                }
                            }
                        } else if (parameter.value.startsWith(last)) {
                            list.add(new CommandCompletion(parameter.value, pos));
                        }
                    }
                }
            }
        }

        if (list.isEmpty()) {
            list.add(new CommandCompletion("", 0));
        }

        return list;
    }

    public static void initCommands() {
        new GuiCommand();
        new HelpCommand();
        new ConfigCommand();
        new SetCommand();
        new YawCommand();
        new PitchCommand();
        new NoPauseCommand();
        new ClearCommand();
        new SetBlockCommand();
        new ChangeNameCommand();
        new DropCommand();
        new ChatCommand();
        new DisconnectCommand();
        new DevCommand();
        new OpenFolderCommand();
        new LastDeathCommand();
        new SeenCommand();
        new ToggleCommand();
        new StopWatchCommand();
        new ClipCommand();
        new BindingsCommand();
        new SetWeatherCommand();
        new SetTimeCommand();
    }

    public record CommandCompletion(String command, int start) {}
}
