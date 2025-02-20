package me.bebeli555.automapart.gui.windows.windows.games.tetris;

import me.bebeli555.automapart.events.game.KeyInputEvent;
import me.bebeli555.automapart.gui.Gui;
import me.bebeli555.automapart.gui.windows.TitledWindow;
import me.bebeli555.automapart.mods.ClientSettings;
import me.bebeli555.automapart.settings.Settings;
import me.bebeli555.automapart.utils.input.Keyboard;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

public class TetrisGameWindow extends TitledWindow {
    public static TetrisGameWindow INSTANCE;
    public static int fromX, toX;
    public static int fromY, toY;
    public TetrisNode currentNode;
    public int beenDown = 0;
    public boolean gameOver = true;
    public int score = 0;
    public long lastSec = 0;
    public long lastSecMove = 0;
    public int yAdd = 15;

    public TetrisGameWindow() {
        super("Tetris game", 151, 250 + 15);
        INSTANCE = this;
    }

    @Override
    public void onDraw(DrawContext context, int mouseX, int mouseY, int lastMouseX, int lastMouseY) {
        MatrixStack stack = context.getMatrices();

        //Set values
        fromX = x;
        toX = fromX + 150;
        fromY = y + yAdd;
        toY = fromY + 250;

        Gui.drawRect(stack, fromX + 1, fromY, toX, toY, 0xFF000000);

        int Divided = 150 - (score / 10);
        if (Divided < 10) {
            Divided = 10;
        }

        long sec = System.currentTimeMillis() / Divided;
        if (sec != lastSec && !gameOver) {
            //Create new node from the top.
            if (TetrisNode.nodes.isEmpty() || !currentNode.canGoDown()) {
                score++;
                mc.player.getWorld().playSound(mc.player, mc.player.getBlockPos(), SoundEvents.BLOCK_GLASS_PLACE, SoundCategory.AMBIENT, 10222.5f, 1.5f);
                removeLayer();
                setShapes();

                if (!currentNode.canGoDown()) {
                    gameOver();
                }
            }

            if (currentNode.canGoDown()) {
                currentNode.moveDown();
            }
            lastSec = sec;
        }

        long sec2 = System.currentTimeMillis() / 40;
        if (sec2 != lastSecMove) {
            // Move automatically if holding key
            if (!gameOver && currentNode != null) {
                if (Keyboard.isKeyDown(GLFW.GLFW_KEY_RIGHT) || Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT) || Keyboard.isKeyDown(GLFW.GLFW_KEY_DOWN)) {
                    beenDown++;
                    if (beenDown > 3) {
                        if (Keyboard.isKeyDown(GLFW.GLFW_KEY_RIGHT)) {
                            if (currentNode.canMoveRight()) {
                                currentNode.moveRight();
                            }
                        } else if (Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT)) {
                            if (currentNode.canMoveLeft()) {
                                currentNode.moveLeft();
                            }
                        } else {
                            if (currentNode.canGoDown()) {
                                currentNode.moveDown();
                            }
                        }
                    }
                } else {
                    beenDown = 0;
                }
            }
            lastSecMove = sec2;
        }

        //Draw the tetris blocks.
        for (int i = 0; i < TetrisNode.nodes.size(); i++) {
            TetrisNode node = TetrisNode.nodes.get(i);
            Gui.drawRect(stack, node.getX(), node.getY(), node.getX() + TetrisNode.multiplier, node.getY() - TetrisNode.multiplier, node.getColor());
        }

        //Draw score
        Gui.fontRenderer.drawString(stack, Formatting.DARK_AQUA + "Score: " + Formatting.GREEN + score, x + 3, y + yAdd + 3, 0xffff);

        if (gameOver) {
            // GameOver Screen
            stack.push();
            stack.scale(2.5F, 2.5F, 2.5F);
            Gui.fontRenderer.drawString(stack, Formatting.RED + "Game Over!", (x + 17) / 2.5f, (y + yAdd + 20) / 2.5f, 0xffff);
            Gui.fontRenderer.drawString(stack, Formatting.RED + "Score = " + Formatting.GREEN + score, (x + 25) / 2.5f, (y + yAdd + 40) / 2.5f, 0xffff);
            Gui.fontRenderer.drawString(stack, Formatting.AQUA + "Controls:", (x + 33) / 2.5f, (y + yAdd + 60) / 2.5f, 0xffff);
            stack.pop();

            stack.push();
            stack.scale(1.25F, 1.25F, 1.25F);
            Gui.fontRenderer.drawString(stack, Formatting.GREEN + "Arrow UP: " + Formatting.DARK_AQUA + "Rotate", (x + 33) / 1.25f, (y + yAdd + 100) / 1.25f, 0xffff);
            Gui.fontRenderer.drawString(stack, Formatting.GREEN + "Arrow Right: " + Formatting.DARK_AQUA + "Move Right", (x + 15) / 1.25f, (y + yAdd + 115) / 1.25f, 0xffff);
            Gui.fontRenderer.drawString(stack, Formatting.GREEN + "Arrow Left: " + Formatting.DARK_AQUA + "Move Left", (x + 25) / 1.25f, (y + yAdd + 130) / 1.25f, 0xffff);
            Gui.fontRenderer.drawString(stack, Formatting.GREEN + "Arrow Down: " + Formatting.DARK_AQUA + "Drop Soft", (x + 22) / 1.25f, (y + yAdd + 145) / 1.25f, 0xffff);
            Gui.fontRenderer.drawString(stack, Formatting.GREEN + "Space: " + Formatting.DARK_AQUA + "Drop Hard", (x + 34) / 1.25f, (y + yAdd + 160) / 1.25f, 0xffff);
            stack.pop();

            stack.push();
            stack.scale(1.8F, 1.8F, 1.8F);
            Gui.fontRenderer.drawString(stack, Formatting.GREEN + "Click to play", (x + 32) / 1.8f, (y + yAdd + 210) / 1.8f, 0xffff);
            stack.pop();
            return;
        }

        //Draw white marks at bottom.
        for (int i = 0; i < currentNode.getFamily().size(); i++) {
            if (currentNode.canGoDown()) {
                currentNode.setDownPosition();
                TetrisNode Node = currentNode.getFamily().get(i);
                int x = Node.getX();
                int y = Node.getDownPosition();
                Gui.drawRect(stack, x, y, x + 1, y - TetrisNode.multiplier, 0xFFFFFFFF);
                Gui.drawRect(stack, x, y, x + TetrisNode.multiplier, y + 1, 0xFFFFFFFF);
                Gui.drawRect(stack, x + TetrisNode.multiplier, y, x + TetrisNode.multiplier + 1, y - TetrisNode.multiplier, 0xFFFFFFFF);
                Gui.drawRect(stack, x, y - TetrisNode.multiplier, x + TetrisNode.multiplier, y - TetrisNode.multiplier + 1, 0xFFFFFFFF);
            }
        }
    }

    @Override
    public void onClick(int x, int y, int button) {
        if (fromX < x && toX > x && fromY < y && toY > y) {
            if (gameOver) {
                startGame();
            }
        }
    }

    @Override
    public void onKey(KeyInputEvent e) {
        //Control tetris block movement
        if (currentNode == null || gameOver) {
            return;
        }

        if (Keyboard.isKeyDown(GLFW.GLFW_KEY_RIGHT)) {
            if (currentNode.canMoveRight()) {
                currentNode.moveRight();
            }
        } else if (Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT)) {
            if (currentNode.canMoveLeft()) {
                currentNode.moveLeft();
            }
        } else if (Keyboard.isKeyDown(GLFW.GLFW_KEY_SPACE)) {
            currentNode.moveCompletelyDown();
        } else if (Keyboard.isKeyDown(GLFW.GLFW_KEY_UP)) {
            currentNode.rotate();
        } else if (Keyboard.isKeyDown(GLFW.GLFW_KEY_DOWN)) {
            if (currentNode.canGoDown()) {
                currentNode.moveDown();
            }
        }
    }

    @Override
    public void onGuiClose() {
        Settings.getGuiNodeFromId(ClientSettings.tetrisGame.id).toggled = false;
    }

    //Set a random shape.
    public void setShapes() {
        TetrisNode n = new TetrisNode(fromX + 50, fromY);
        n.addToFamily(n);
        int x = n.getX();
        int y = n.getY();

        Random rand = new Random();
        int random = rand.nextInt(7);
        if (random == 0) {
            n.addToFamily(new TetrisNode(x + 10, y));
            n.addToFamily(new TetrisNode(x, y + 10));
            n.addToFamily(new TetrisNode(x + 10, y + 10));
            n.setShape("O");
            n.setColor(0xFFFFFF00);
        } else if (random == 1) {
            n.addToFamily(new TetrisNode(x, y + 10));
            n.addToFamily(new TetrisNode(x, y + 20));
            n.addToFamily(new TetrisNode(x, y + 30));
            n.setShape("I");
            n.setColor(0xFF36EAFF);
        } else if (random == 2) {
            n.addToFamily(new TetrisNode(x + 10, y));
            n.addToFamily(new TetrisNode(x, y + 10));
            n.addToFamily(new TetrisNode(x - 10, y + 10));
            n.setShape("S");
            n.setColor(0xFFFF0009);
        } else if (random == 3) {
            n.addToFamily(new TetrisNode(x - 10, y));
            n.addToFamily(new TetrisNode(x, y + 10));
            n.addToFamily(new TetrisNode(x + 10, y + 10));
            n.setShape("Z");
            n.setColor(0xFF00FF2B);
        } else if (random == 4) {
            n.addToFamily(new TetrisNode(x, y + 10));
            n.addToFamily(new TetrisNode(x, y + 20));
            n.addToFamily(new TetrisNode(x + 10, y + 20));
            n.setShape("L");
            n.setColor(0xFFEC830C);
        } else if (random == 5) {
            n.addToFamily(new TetrisNode(x, y + 10));
            n.addToFamily(new TetrisNode(x, y + 20));
            n.addToFamily(new TetrisNode(x - 10, y + 20));
            n.setShape("J");
            n.setColor(0xFFFF19EF);
        } else {
            n.addToFamily(new TetrisNode(x + 10, y));
            n.addToFamily(new TetrisNode(x - 10, y));
            n.addToFamily(new TetrisNode(x, y + 10));
            n.setShape("T");
            n.setColor(0xFF9100FF);
        }

        currentNode = n;
    }

    public void removeLayer() {
        if (currentNode == null) {
            return;
        }

        for (int i = 0; i < currentNode.getFamily().size(); i++) {
            int y = currentNode.getFamily().get(i).getY();
            int x = fromX - 10;
            for (int i2 = 0; i2 < 100; i2++) {
                x = x + 10;
                if (x > toX - 10) {
                    x = fromX - 10;
                    for (int i3 = 0; i3 < 100; i3++) {
                        x = x + 10;
                        TetrisNode.nodes.remove(TetrisNode.getNode(x, y));
                    }
                    for (int i4 = 0; i4 < TetrisNode.nodes.size(); i4++) {
                        if (TetrisNode.nodes.get(i4).getY() <= y) {
                            TetrisNode.nodes.get(i4).setY(TetrisNode.nodes.get(i4).getY() + 10);
                        }
                    }

                    mc.player.getWorld().playSound(mc.player, mc.player.getBlockPos(), SoundEvents.BLOCK_ANVIL_USE, SoundCategory.AMBIENT, 10222.5f, 1.5f);
                    score += 5;
                    break;
                }

                if (TetrisNode.getNode(x, y) == null) {
                    break;
                }
            }
        }
    }

    public void gameOver() {
        gameOver = true;
        TetrisNode.nodes.clear();
    }

    public void startGame() {
        gameOver = false;
        score = 0;
    }
}
