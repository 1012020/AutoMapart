package me.bebeli555.automapart.gui.windows.windows.tools;

import me.bebeli555.automapart.gui.windows.TitledWindow;
import me.bebeli555.automapart.gui.windows.windows.other.ImageEditorWindow;
import me.bebeli555.automapart.gui.windows.windows.pickers.PickerWindow;
import me.bebeli555.automapart.settings.Setting;
import me.bebeli555.automapart.settings.Settings;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.item.Items;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TextureEditorTool extends TitledWindow {
    public static TextureEditorTool INSTANCE;

    public static List<EditedTexture> editedTextures = new ArrayList<>();

    public PickerWindow texturePicker = new PickerWindow("TexturePicker"){
        public void onOutsideClick() {}
    };

    public String path = "";
    public long lastDelete;

    public TextureEditorTool() {
        super("TextureEditor", 125, 200);
        INSTANCE = this;

        ImageEditorWindow.INSTANCE.saveButton.addClickListener(() -> {
            if (this.toggled) {
                try {
                    editedTextures.removeIf(t -> t.path.equals(ImageEditorWindow.INSTANCE.imagePath.getPath()));
                    editedTextures.add(new EditedTexture(ImageEditorWindow.INSTANCE.imagePath.getPath(), ImageEditorWindow.INSTANCE.texture.getImage().getBytes()));

                    File file = new File(Settings.path + "/textureeditor/" + ImageEditorWindow.INSTANCE.imagePath.getPath().replace("/", "-"));
                    file.delete();
                    file.createNewFile();

                    Files.write(file.toPath(), ImageEditorWindow.INSTANCE.texture.getImage().getBytes());

                    mc.reloadResources();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        texturePicker.addItemSelectedListener(new Setting.ValueChangedListener(){
            public void valueChanged(Object value) {
                PickerWindow.PickerItem item = (PickerWindow.PickerItem)value;
                if (item.name.endsWith(".png")) {
                    try {
                        Identifier identifier = (Identifier)item.customObject;

                        if (texturePicker.onlyShowSelected) {
                            if (texturePicker.lastMouseButtonClick == 1) {
                                texturePicker.selected.remove(item);
                                new File(Settings.path + "/textureeditor/" + identifier.getPath().replace("/", "-")).delete();
                                editedTextures.removeIf(t -> t.path.equals(identifier.getPath()));

                                lastDelete = System.currentTimeMillis();
                                new Thread(() -> {
                                    sleep(1500);
                                    if (Math.abs(lastDelete - System.currentTimeMillis()) > 1485) {
                                        mc.reloadResources();
                                    }
                                }).start();

                                return;
                            }

                            NativeImage nativeImage = NativeImage.read(Files.readAllBytes(new File(Settings.path + "/textureeditor/" + identifier.getPath().replace("/", "-")).toPath()));
                            ImageEditorWindow.INSTANCE.setImage(nativeImage, identifier);
                        } else {
                            ImageEditorWindow.INSTANCE.setImage(NativeImage.read(mc.getResourceManager().open(identifier)), identifier);
                        }

                        ImageEditorWindow.INSTANCE.enable(texturePicker.x, texturePicker.y);

                        texturePicker.disable();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return;
                }

                path += (path.isEmpty() ? "" : "/") + item.name;
                onEnabled();
                texturePicker.title = "TexturePicker: " + path;
            }
        });

        texturePicker.deselectAll.dontRender = true;

        texturePicker.selectAll.name = "Back";
        texturePicker.noSelectAllListener = true;
        texturePicker.selectAll.addClickListener(() -> {
            if (path.contains("/")) {
                this.path = getLastSplit(this.path, "/")[0];
            } else {
                this.path = "";
            }

            this.onEnabled();
            texturePicker.title = "TexturePicker: " + path;
        });

        texturePicker.showSelected.addClickListener(() -> this.onEnabled());
        texturePicker.closeOnGuiClose = false;
    }

    //Load edited textures from config files
    public static void loadSavedTextures() {
        File dir = new File(Settings.path + "/textureeditor/");
        dir.mkdir();

        for (File file : dir.listFiles()) {
            try {
                editedTextures.add(new EditedTexture(file.getName().replace("-", "/"), Files.readAllBytes(file.toPath())));
            } catch (Exception e) {
                System.err.println("Failed loading edited texture: " + file.getName());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onEnabled() {
        this.dontRender = true;
        ResourcePack resourcePack = mc.getDefaultResourcePack();

        texturePicker.selected.clear();
        texturePicker.list.clear();

        if (!texturePicker.onlyShowSelected) {
            Set<String> namespaces = resourcePack.getNamespaces(ResourceType.CLIENT_RESOURCES);
            for (String namespace : namespaces) {
                resourcePack.findResources(ResourceType.CLIENT_RESOURCES, namespace, "textures", (id, supplier) -> {
                    String path = id.getPath();
                    if (path.endsWith(".png")) {
                        if (path.startsWith(this.path)) {
                            String[] split = path.replace(this.path, "").split("/");
                            if (split.length == 0 || this.path.endsWith(split[0])) {
                                texturePicker.add(new PickerWindow.PickerItem(split[1], split[1].endsWith(".png") ? Items.PAPER : Items.ENCHANTED_BOOK, "", id, id));
                            } else if (this.path.isEmpty()) {
                                texturePicker.add(new PickerWindow.PickerItem(split[0], split[1].endsWith(".png") ? Items.PAPER : Items.ENCHANTED_BOOK, "", id, id));
                            }
                        }
                    }
                });
            }
        } else {
            for (File file : new File(Settings.path + "/textureeditor/").listFiles()) {
                try {
                    String name = file.getName().replace("-", "/");
                    PickerWindow.PickerItem item = new PickerWindow.PickerItem(name, Items.REDSTONE, "", new Identifier(name), mc.getTextureManager().registerDynamicTexture("selectedtexture", new NativeImageBackedTexture(NativeImage.read(Files.readAllBytes(file.toPath())))));

                    texturePicker.add(item);
                    texturePicker.selected.add(item);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (!texturePicker.toggled) {
            texturePicker.enable(this.x, this.y);
        }
    }

    @Override
    public void onDisabled() {
        this.path = "";
        texturePicker.disable();
    }

    public record EditedTexture(String path, byte[] data) {}
}
