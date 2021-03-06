package com.dfsek.terra.config.genconfig;

import com.dfsek.terra.Debug;
import com.dfsek.terra.config.TerraConfig;
import com.dfsek.terra.config.base.ConfigPack;
import com.dfsek.terra.config.exception.ConfigException;
import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.InvalidConfigurationException;
import org.polydev.gaea.math.FastNoiseLite;
import org.polydev.gaea.math.ProbabilityCollection;
import org.polydev.gaea.world.palette.Palette;
import org.polydev.gaea.world.palette.RandomPalette;
import org.polydev.gaea.world.palette.SimplexPalette;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PaletteConfig extends TerraConfig {
    private final Palette<BlockData> palette;
    private final String paletteID;
    private boolean useNoise = false;

    public PaletteConfig(File file, ConfigPack config) throws IOException, InvalidConfigurationException {
        super(file, config);
        if(!contains("id")) throw new ConfigException("Palette ID unspecified!", "null");
        this.paletteID = getString("id");
        Palette<BlockData> pal;
        if(getBoolean("simplex", false)) {
            useNoise = true;
            FastNoiseLite pNoise = new FastNoiseLite(getInt("seed", 2403));
            pNoise.setNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
            pNoise.setFractalOctaves(4);
            pNoise.setFrequency(getDouble("frequency", 0.02));
            pal = new SimplexPalette<>(pNoise);
        } else pal = new RandomPalette<>(new Random(getInt("seed", 2403)));
        palette = getPalette(getMapList("layers"), pal);
    }

    @SuppressWarnings("unchecked")
    protected static Palette<BlockData> getPalette(List<Map<?, ?>> maps, Palette<BlockData> p) throws InvalidConfigurationException {
        for(Map<?, ?> m : maps) {
            try {
                ProbabilityCollection<BlockData> layer = new ProbabilityCollection<>();
                List<Map<?, ?>> map = (List<Map<?, ?>>) m.get("materials");
                if(map.size() > 1) {
                    for(Map<?, ?> entry : map) {
                        for(Map.Entry<?, ?> type : entry.entrySet()) {
                            layer.add(Bukkit.createBlockData((String) type.getKey()), (Integer) type.getValue());
                        }
                    }
                    p.add(layer, (Integer) m.get("layers"));
                } else {
                    Debug.info("One-block palette layer!");
                    String data = "null";
                    for(Map.Entry<?, ?> e : map.get(0).entrySet()) {
                        data = (String) e.getKey();
                    }
                    p.add(Bukkit.createBlockData(data), (Integer) m.get("layers"));
                }
            } catch(ClassCastException e) {
                throw new InvalidConfigurationException("SEVERE configuration error for Palette: \n\n" + e.getMessage());
            }
        }
        return p;
    }

    @Override
    public String toString() {
        return "Palette with ID " + getID() + " with " + getPalette().getSize() + " layers, using Simplex: " + useNoise;
    }

    public Palette<BlockData> getPalette() {
        return palette;
    }

    public String getID() {
        return paletteID;
    }
}
