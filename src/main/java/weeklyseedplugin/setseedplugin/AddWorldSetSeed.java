package weeklyseedplugin.setseedplugin;

import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.events.AddWorldEvent;
import com.hypixel.hytale.server.core.util.Config;

public class AddWorldSetSeed {
    private static Config<SeedConfig> seedConfig;

    public static void setSeedConfig(Config<SeedConfig> config) {
        seedConfig = config;
    }

    public static void onWorldAdd(AddWorldEvent event){
        World world = event.getWorld();
        WorldConfig worldConfig = world.getWorldConfig();
        long seed = seedConfig.get().getSeed();
        worldConfig.setSeed(seed);
    }

}
