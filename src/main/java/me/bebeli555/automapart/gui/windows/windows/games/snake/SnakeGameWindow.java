package me.bebeli555.automapart.gui.windows.windows.games.snake;

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

import java.util.ArrayList;
import java.util.Random;

public class SnakeGameWindow extends TitledWindow {
    public static SnakeGameWindow INSTANCE;

    public ArrayList<Integer> bodyX = new ArrayList<>();
    public ArrayList<Integer> bodyY = new ArrayList<>();
    public int snakeSize = 0;
    public int snakeX, snakeY;
    public int lastSnakeX, lastSnakeY;
    public int lastBodyX, lastBodyY;
    public boolean gameOver = true;
    public int appleX, appleY;
    public int delay = 0;
    public long lastSec = 0;
    public String direction;
    public int yAdd = 20;

    public SnakeGameWindow() {
        super("Snake game", 201, 200 + 20);
        INSTANCE = this;
    }

    @Override
    public void onDraw(DrawContext context, int mouseX, int mouseY, int lastMouseX, int lastMouseY) {
        MatrixStack stack = context.getMatrices();
        y+= yAdd;

        Gui.drawRect(stack, x + 1, y, x + 201, y + 200, 0xFF000000);

        //Game over.
        if (snakeX < x || snakeX >= x + 200 || snakeY < y || snakeY >= y + 200) {
            gameOver();
        }

        if (!gameOver) {
            //Die if head is colliding in body
            for (int i = 0; i < bodyX.size(); i++) {
                if (bodyX.get(i) == snakeX) {
                    if (bodyY.get(i) == snakeY) {
                        gameOver();
                        y -= yAdd;
                        return;
                    }
                }
            }

            //Draw score
            Gui.fontRenderer.drawString(stack, Formatting.RED + "Score = " + Formatting.GREEN + snakeSize, x + 3, y + 2, 0xffff);

            // Generate apple
            if (appleX == 0 || appleY == 0) {
                generateApple();
            }

            // Draw apple
            Gui.drawRect(stack, appleX, appleY, appleX + 20, appleY + 20, 0xFFff0000);

            long sec = System.currentTimeMillis() / 150;
            if (sec != lastSec) {
                delay = 0;
                // Control snake movement
                if (direction.equals("Up")) {
                    snakeY = snakeY - 20;
                } else if (direction.equals("Down")) {
                    snakeY = snakeY + 20;
                } else if (direction.equals("Right")) {
                    snakeX = snakeX + 20;
                } else if (direction.equals("Left")) {
                    snakeX = snakeX - 20;
                }

                if (!bodyX.isEmpty()) {
                    bodyX.remove(bodyX.get(0));
                    bodyY.remove(bodyY.get(0));
                    bodyX.add(lastSnakeX);
                    bodyY.add(lastSnakeY);
                }
                lastSec = sec;
            }

            //Eat apple
            if (snakeX == appleX) {
                if (snakeY == appleY) {
                    snakeSize++;
                    appleX = 0;
                    appleY = 0;

                    //More body for snake bcs he fat and eating all those sugary apples
                    if (bodyX.isEmpty()) {
                        bodyX.add(lastSnakeX);
                        bodyY.add(lastSnakeY);
                    } else {
                        bodyX.add(lastBodyX);
                        bodyY.add(lastBodyY);
                    }

                    mc.player.getWorld().playSound(mc.player, mc.player.getBlockPos(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.AMBIENT, 100.0f, 2.0F);
                }
            }

            lastSnakeX = snakeX;
            lastSnakeY = snakeY;
            if (!bodyX.isEmpty()) {
                lastBodyX = bodyX.get(bodyX.size() - 1);
                lastBodyY = bodyY.get(bodyY.size() - 1);
            }

            // Draw snake
            Gui.drawRect(stack, snakeX, snakeY, snakeX + 20, snakeY + 20, 0xFF55ff00);
            Gui.drawRect(stack, snakeX + 3, snakeY + 3, snakeX + 8, snakeY + 8, 0xFF000000);
            for (int i = 0; i < bodyX.size(); i++) {
                if (!bodyX.isEmpty()) {
                    Gui.drawRect(stack, bodyX.get(i), bodyY.get(i), bodyX.get(i) + 20, bodyY.get(i) + 20, 0xFF55ff00);
                }
            }
        }

        //Game over screen
        if (gameOver) {
            stack.push();
            stack.scale(3.0F, 3.0F, 3.0F);
            Gui.fontRenderer.drawString(stack, Formatting.RED + "Game Over!", (x / 3) + 11, (y / 3) + 2, 0xffff);
            Gui.fontRenderer.drawString(stack, Formatting.LIGHT_PURPLE + "Score = " + Formatting.GREEN + snakeSize, (x / 3) + 15, (y / 3) + 12, 0xffff);
            stack.pop();
            stack.push();
            stack.scale(2.0F, 2.0F, 2.0F);
            Gui.fontRenderer.drawString(stack, Formatting.GREEN + "Click to Play!", (x / 2) + 25, (y / 2) + 85, 0xffff);
            stack.pop();
            stack.push();
            stack.scale(1.5F, 1.5F, 1.5F);
            Gui.fontRenderer.drawString(stack, Formatting.AQUA + "Use " + Formatting.GREEN + "ARROW KEYS " + Formatting.AQUA + "To play!", (int)(x / 1.5) + 12, (int)(y / 1.5) + 75, 0xffff);
            stack.pop();
        }

        y -= yAdd;
    }

    @Override
    public void onClick(int mouseX, int mouseY, int button) {
        if (x < mouseX && (x + 200) > mouseX && y + yAdd < mouseY && (y + 200 + yAdd) > mouseY) {
            if (gameOver) {
                startGame();
            }
        }
    }

    @Override
    public void onKey(KeyInputEvent e) {
        if (Keyboard.isKeyDown(GLFW.GLFW_KEY_DOWN)) {
            direction = "Down";
        } else if (Keyboard.isKeyDown(GLFW.GLFW_KEY_UP)) {
            direction = "Up";
        } else if (Keyboard.isKeyDown(GLFW.GLFW_KEY_RIGHT)) {
            direction = "Right";
        } else if (Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT)) {
            direction = "Left";
        }
    }

    @Override
    public void onGuiClose() {
        Settings.getGuiNodeFromId(ClientSettings.snakeGame.id).toggled = false;
    }

    public void gameOver() {
        if (!gameOver) {
            gameOver = true;
            bodyX.clear();
            bodyY.clear();
            lastSnakeX = 0;
            lastSnakeY = 0;
            appleX = 0;
            appleY = 0;
        }
    }

    public void startGame() {
        gameOver = false;
        snakeX = x + 100;
        snakeY = y + 180;
        direction = "Up";
        snakeSize = 1;
    }

    public void generateApple() {
        for (int i = 0; i < 100; i++) {
            Random rand = new Random();
            int random = rand.nextInt(10);
            int random2 = rand.nextInt(10);
            appleX = x + random * 20;
            appleY = y + random2 * 20;

            for (int i2 = 0; i2 < bodyX.size(); i2++) {
                if (bodyX.get(i2) == appleX) {
                    if (bodyY.get(i2) == appleY) {
                        appleX = 0;
                        appleY = 0;
                        break;
                    }
                }
            }

            if (snakeX == appleX) {
                if (snakeY == appleY) {
                    appleX = 0;
                    appleY = 0;
                    continue;
                }
            }

            if (appleX < x || appleX > x + width || appleY < y || appleY > y + height) {
                appleX = 0;
                appleY = 0;
                continue;
            }

            if (appleX != 0 && appleY != 0) {
                break;
            }
        }
    }
}
