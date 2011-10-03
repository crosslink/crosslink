package crosslink.util;

import java.awt.Color;
import java.util.HashMap;
import java.util.Random;

public class ColorCache {
	
    public static final Color[][] spColor = {
            {new Color(150, 0, 0), new Color(150, 0, 0), new Color(150, 0, 0)},
            {new Color(0, 150, 0), new Color(0, 150, 0), new Color(0, 150, 0)},
            {new Color(0, 0, 150), new Color(0, 0, 150), new Color(0, 0, 150)},
            {new Color(150, 150, 0), new Color(150, 150, 0), new Color(150, 150, 0)},
            {new Color(200, 0, 200), new Color(200, 0, 200), new Color(200, 0, 200)}
        };
    
    private HashMap<String, Color> colorCache = new HashMap<String, Color>();
    private Random generator = new Random();
    
    private static ColorCache instance = null;
    
    public static void newInstance() {
    	instance = new ColorCache();
    }
    public static ColorCache getInstance() {
    	return instance;
    }
    
    public Color getPredefinedColor(String name, int colorCount) {
    	colorCache.put(name, spColor[colorCount][0]);
    	return colorCache.get(name);
    }
    
    public Color getRandomColor(String name) {
    	colorCache.put(name, new Color(generator.nextInt(255) + 1, generator.nextInt(255) + 1, generator.nextInt(255) + 1));
    	return colorCache.get(name);
    }
    
    public Color getColor(String name) {
    	Color color = colorCache.get(name);
    	if (color == null) {
    		color = new Color(generator.nextInt(255) + 1, generator.nextInt(255) + 1, generator.nextInt(255) + 1);
    		colorCache.put(name, color);
    	}
    	return color;
    }
}
