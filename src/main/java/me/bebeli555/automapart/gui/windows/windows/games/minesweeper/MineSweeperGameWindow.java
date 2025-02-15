package me.bebeli555.automapart.gui.windows.windows.games.minesweeper;

import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.gui.Gui;
import me.bebeli555.automapart.gui.windows.TitledWindow;
import me.bebeli555.automapart.gui.windows.components.ButtonComponent;
import me.bebeli555.automapart.mods.ClientSettings;
import me.bebeli555.automapart.settings.Settings;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class MineSweeperGameWindow extends TitledWindow {
    public static MineSweeperGameWindow INSTANCE;

    //TEXTURES
    private static final int TEX_SIZE = 16;
    private static final MineSweeperTexture TEX_EMPTY_BACKGROUND = new MineSweeperTexture(0, 0, false);
    private static final MineSweeperTexture TEX_ONE = new MineSweeperTexture(TEX_SIZE, 0, true);
    private static final MineSweeperTexture TEX_TWO = new MineSweeperTexture(TEX_SIZE, 0, true);
    private static final MineSweeperTexture TEX_THREE = new MineSweeperTexture(TEX_SIZE, 0, true);
    private static final MineSweeperTexture TEX_FOUR = new MineSweeperTexture(0, TEX_SIZE, false);
    private static final MineSweeperTexture TEX_FIVE = new MineSweeperTexture(TEX_SIZE, 0, true);
    private static final MineSweeperTexture TEX_SIX = new MineSweeperTexture(TEX_SIZE, 0, true);
    private static final MineSweeperTexture TEX_SEVEN = new MineSweeperTexture(TEX_SIZE, 0, true);
    private static final MineSweeperTexture TEX_EIGHT = new MineSweeperTexture(0, TEX_SIZE * 2, false);
    private static final MineSweeperTexture TEX_FILLED_BACKGROUND = new MineSweeperTexture(TEX_SIZE, 0, true);
    private static final MineSweeperTexture TEX_FLAG = new MineSweeperTexture(TEX_SIZE, 0, true);
    private static final MineSweeperTexture TEX_FLAGGED_FLAG = new MineSweeperTexture(TEX_SIZE, 0, true);
    private static final MineSweeperTexture TEX_BOMB = new MineSweeperTexture(0, TEX_SIZE * 3, false);
    private static final MineSweeperTexture TEX_EXPLODED_BOMB = new MineSweeperTexture(TEX_SIZE, 0, true);

    public Identifier identifier = new Identifier(Mod.MOD_ID, "minesweeper.png");
    public ButtonComponent newGameButton = new ButtonComponent(this, "New game");

    public boolean gameOver;
    public long gameStartTime, gameEndTime;

    public MineSweeperGameWindow() {
        super("MineSweeper", 0, 0);
        INSTANCE = this;

        newGameButton.addClickListener(() -> onEnabled());
    }

    @Override
    public void onEnabled() {
        //Initialize game-board
        this.title = "MineSweeper";
        MineSweeperNode.all.clear();
        gameOver = false;
        gameStartTime = System.currentTimeMillis();

        int size = ClientSettings.mineSweeperGameBoardSize.asInt(); //15x15
        int bombs = ClientSettings.mineSweeperGameBombs.asInt();

        this.width = size * TEX_SIZE;
        this.height = size * TEX_SIZE + 20;

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                MineSweeperNode.all.add(new MineSweeperNode(x, y));
            }
        }

        //Set bombs
        for (int i = 0; i < bombs; i++) {
            MineSweeperNode random = MineSweeperNode.getFromCoordinates(random(0, size - 1), random(0, size - 1));
            random.setAsBomb();
        }
    }

    @Override
    public void onDraw(DrawContext context, int mouseX, int mouseY, int lastMouseX, int lastMouseY) {
        MatrixStack stack = context.getMatrices();
        newGameButton.render(context, 5, 8);

        //Render passed time on right
        if (!gameOver) gameEndTime = System.currentTimeMillis();
        long ms = Math.abs(gameEndTime - gameStartTime);
        long minutes = (ms / (60 * 1000)) % 60;
        long seconds = (ms / 1000) % 60;
        String s = String.format("%02d:%02d", minutes, seconds);
        Gui.fontRenderer.drawString(stack, s, x + width - Gui.fontRenderer.getWidth(stack, s) - 2, y + 12, ClientSettings.titledWindowButtonText.asInt());

        for (MineSweeperNode node : MineSweeperNode.all) {
            drawNode(context, node);
        }
    }

    public void drawNode(DrawContext context, MineSweeperNode node) {
        MineSweeperTexture texture;
        if (node.isBomb() && node.isExploded()) {
            texture = TEX_EXPLODED_BOMB;
        } else if (node.isBomb() && gameOver) {
            texture = TEX_BOMB;
        } else if (node.isFlagged() && !node.isRevealed()) {
            texture = TEX_FLAG;
        } else if (!node.isRevealed()) {
            texture = TEX_FILLED_BACKGROUND;
        } else {
            texture = switch(node.getCloseBombsCount()) {
                case 0 -> TEX_EMPTY_BACKGROUND;
                case 1 -> TEX_ONE;
                case 2 -> TEX_TWO;
                case 3 -> TEX_THREE;
                case 4 -> TEX_FOUR;
                case 5 -> TEX_FIVE;
                case 6 -> TEX_SIX;
                case 7 -> TEX_SEVEN;
                case 8 -> TEX_EIGHT;
                default -> null;
            };
        }

        Gui.drawRect(context.getMatrices(), 0, 0, 0, 0, -1);
        context.drawTexture(identifier,
                this.x + node.getX() * TEX_SIZE,
                this.y + node.getY() * TEX_SIZE + 20,
                texture.u, texture.v,
                TEX_SIZE, TEX_SIZE,
                64, 64
        );
    }

    @Override
    public void onClick(int mouseX, int mouseY, int button) {
        mouseY -= 2;

        if (!gameOver) {
            MineSweeperNode node = MineSweeperNode.getFromCoordinates((mouseX - this.x) / TEX_SIZE, ((mouseY - this.y) / TEX_SIZE) - 1);
            if (node != null) {
                if (button == 0) {
                    if (node.isFlagged()) {
                        return;
                    }

                    if (node.isBomb()) {
                        node.explode();
                        gameOver = true;
                        this.title = "MineSweeper: OOF! you lost D:";
                    } else {
                        node.reveal();
                        gameOver = MineSweeperNode.all.stream().noneMatch(n -> !n.isRevealed() && !n.isBomb());

                        if (gameOver) {
                            this.title = "MineSweeper: YOU WON!! :DD";
                        }
                    }
                } else {
                    node.flag();
                }
            }
        }
    }

    @Override
    public void onGuiClose() {
        Settings.getGuiNodeFromId(ClientSettings.mineSweeperGame.id).toggled = false;
    }

    public static class MineSweeperTexture {
        private static int lastU, lastV;
        public int u, v;

        public MineSweeperTexture(int u, int v, boolean add) {
            if (add) {
                this.u = lastU + u;
                this.v = lastV + v;
            } else {
                this.u = u;
                this.v = v;
            }

            lastU = this.u;
            lastV = this.v;
        }
    }
}
