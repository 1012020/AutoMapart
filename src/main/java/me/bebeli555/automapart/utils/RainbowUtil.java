package me.bebeli555.automapart.utils;

import me.bebeli555.automapart.utils.objects.Timer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility for calculating rainbow color loops with custom speed and factor
 */
public class RainbowUtil {
	public int speed;
    public int factor;
	public List<Integer> currentRainbowIndexes = new ArrayList<>();
	public List<Integer> rainbowArrayList = new ArrayList<>();
	public Timer timer = new Timer();
	
	public RainbowUtil() {
        for (int i = 0; i < 360; i++) {
            rainbowArrayList.add(getRainbowColor(i, 90.0f, 50.0f, 1.0f).getRGB());
            currentRainbowIndexes.add(i);
        }
	}
	
	public void setSpeed(int speed) {
		this.speed = speed;
	}
	
    public int getRainbowColorAt(int index) {
        if (index > currentRainbowIndexes.size() - 1) {
        	index = currentRainbowIndexes.size() - 1;
        }

        return rainbowArrayList.get(currentRainbowIndexes.get(index));
    }

    public void onUpdate() {
        if (timer.hasPassed(speed)) {
            timer.reset();
            moveListToNextColor();
        }
    }

    private void moveListToNextColor() {
        if (currentRainbowIndexes.isEmpty()) {
            return;
        }

        currentRainbowIndexes.remove(currentRainbowIndexes.get(0));

        int index = currentRainbowIndexes.get(currentRainbowIndexes.size() - 1) + 1;
        if (index >= rainbowArrayList.size() - 1) {
            index = 0;
        }

        currentRainbowIndexes.add(index);
    }
    
    private Color getRainbowColor(float hue, float saturation, float lightness, float alpha) {
        hue = hue % 360.0f / 360.0f;
        saturation /= 100.0f;
        lightness /= 100.0f;
        float n5;
        
        if (lightness < 0.0) {
            n5 = lightness * (1.0f + saturation);
        } else {
            n5 = lightness + saturation - saturation * lightness;
        }
        
        saturation = 2.0f * lightness - n5;
        lightness = Math.max(0.0f, calculateColor(saturation, n5, hue + 0.33333334f));
        float max = Math.max(0.0f, calculateColor(saturation, n5, hue));
        saturation = Math.max(0.0f, calculateColor(saturation, n5, hue - 0.33333334f));
        lightness = Math.min(lightness, 1.0f);
        float min = Math.min(max, 1.0f);
        saturation = Math.min(saturation, 1.0f);
        return new Color(lightness, min, saturation, alpha);
    }
    
    private float calculateColor(final float n, final float n2, float n3) {
        if (n3 < 0.0f) {
            ++n3;
        }
        
        if (n3 > 1.0f) {
            --n3;
        }
        
        if (6.0f * n3 < 1.0f) {
            return n + (n2 - n) * 6.0f * n3;
        }
        
        if (2.0f * n3 < 1.0f) {
            return n2;
        }
        
        if (3.0f * n3 < 2.0f) {
            return n + (n2 - n) * 6.0f * (0.6666667f - n3);
        }
        
        return n;
    }
}
