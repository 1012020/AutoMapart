package me.bebeli555.automapart.utils.objects;

public enum Dimension {
    OVERWORLD(),
    NETHER(),
    END();

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}
