package me.bebeli555.automapart.gui;

import me.bebeli555.automapart.gui.windows.TitledWindow;
import me.bebeli555.automapart.mods.ClientSettings;

import java.util.*;

public enum Group {
	CLIENT("Client", 549,26, "Client settings such as prefix, GUI, HUD etc"),
	WORLD("World", 458,26, "Some other modules that like do something");

	public String name;
	public double x, y, renderX, renderY, renderX2, renderY2;
	public double defaultX, defaultY;
	public boolean extended = true;
	public String[] description;
	Group(String name, int x, int y, String... description) {
		this.name = name;
		this.x = x;
		this.y = y;
		this.defaultX = x;
		this.defaultY = y;
		this.description = description;
	}

	public static List<Group> valuesSortedByX() {
		List<Group> list = Arrays.asList(values());
		list.sort(Comparator.comparingDouble(g -> g.x));
		Collections.reverse(list);
		return list;
	}

	public static List<GroupWindow> valuesSortedByXGroupWindows() {
		List<GroupWindow> list = new ArrayList<>();
		for (Group group : values()) {
			list.add(new GroupWindow(group));
		}

		if (ClientSettings.titledWindowBorderRainbow.bool()) {
			for (TitledWindow window : TitledWindow.list) {
				if (window.isToggled()) {
					list.add(new GroupWindow(window));
				}
			}
		}

		list.sort(Comparator.comparingDouble(GroupWindow::getX));
		Collections.reverse(list);
		return list;
	}

	public static class GroupWindow {
		public Group group;
		public TitledWindow window;

		public GroupWindow(Group group) {
			this.group = group;
		}

		public GroupWindow(TitledWindow window) {
			this.window = window;
		}

		public double getX() {
			if (group == null) {
				return window.x;
			} else {
				return group.x;
			}
		}
	}
}
