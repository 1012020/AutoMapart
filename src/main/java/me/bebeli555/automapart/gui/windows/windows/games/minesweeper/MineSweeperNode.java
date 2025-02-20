package me.bebeli555.automapart.gui.windows.windows.games.minesweeper;

import java.util.ArrayList;
import java.util.List;

public class MineSweeperNode {
    public static List<MineSweeperNode> all = new ArrayList<>();

    private final int x, y;
    private boolean revealed, flagged, isBomb, exploded;
    private int bombsClose;
    private boolean isVisited;

    public MineSweeperNode(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Gets the neighbors according to game rules for this node
     */
    public List<MineSweeperNode> getNeighbors() {
        List<MineSweeperNode> list = new ArrayList<>();
        for (MineSweeperNode node : all) {
            int distance = Math.abs(this.getX() - node.getX()) + Math.abs(this.getY() - node.getY());
            if (distance == 1 || distance == 2 && this.getX() != node.getX() && this.getY() != node.getY()) {
                list.add(node);
            }
        }

        return list;
    }

    public static MineSweeperNode getFromCoordinates(int x, int y) {
        for (MineSweeperNode node : all) {
            if (node.getX() == x && node.getY() == y) {
                return node;
            }
        }

        return null;
    }

    /**
     * Sets this node as revealed
     */
    public void reveal() {
        this.revealed = true;
        if (this.isVisited() || this.isBomb()) {
            return;
        }

        setCloseBombCount();
        this.setVisited(true);
        if (this.getCloseBombsCount() == 0) {
            for (MineSweeperNode neighbor : this.getNeighbors()) {
                if (!neighbor.isVisited() && !neighbor.isBomb()) {
                    neighbor.reveal();
                }
            }
        }
    }

    public boolean isRevealed() {
        return this.revealed;
    }

    public void setAsBomb() {
        this.isBomb = true;
    }

    public boolean isBomb() {
        return this.isBomb;
    }

    /**
     * Toggles the flag
     */
    public void flag() {
        this.flagged = !this.flagged;
    }

    public boolean isFlagged() {
        return this.flagged;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public void setCloseBombCount() {
        this.bombsClose = this.getNeighbors().stream().filter(MineSweeperNode::isBomb).toList().size();
    }

    public int getCloseBombsCount() {
        return this.bombsClose;
    }

    /**
     * Explodes this bomb
     */
    public void explode() {
        this.exploded = true;
    }

    public boolean isExploded() {
        return this.exploded;
    }

    private boolean isVisited() {
        return this.isVisited;
    }

    private void setVisited(boolean value) {
        this.isVisited = value;
    }
}
