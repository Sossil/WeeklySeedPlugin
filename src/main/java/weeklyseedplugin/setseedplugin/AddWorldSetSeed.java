package weeklyseedplugin.setseedplugin;

import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.events.AddWorldEvent;
import com.hypixel.hytale.server.core.util.Config;
import weeklyseedplugin.WeeklySeedPlugin;

public class AddWorldSetSeed {
    private static Config<SeedConfig> seedConfig;

    public static void setSeedConfig(Config<SeedConfig> config) {
        seedConfig = config;
    }

    public static void onWorldAdd(AddWorldEvent event){
        World world = event.getWorld();
        WorldConfig worldConfig = world.getWorldConfig();
        SeedConfig config = seedConfig.get();

        String displayName = worldConfig.getDisplayName();
        String[] trimName = displayName.trim().split("\\s+");
        long seedName;
        long offsetName;

        if (trimName.length == 2) {
            try {
                if (Universe.get().getDefaultWorld() != null) {
                    String defaultDisplayName = Universe.get().getDefaultWorld().getWorldConfig().getDisplayName();
                    if (!displayName.equals(defaultDisplayName)) {
                        trimName = defaultDisplayName.trim().split("\\s+");
                    }
                }

                seedName = Long.parseLong(trimName[0]);
                offsetName = Long.parseLong(trimName[1]);

                worldConfig.setSeed(seedName);
                config.setSeed(seedName);
                config.setOffset(offsetName);
                seedConfig.save();
                return;
            } catch (NumberFormatException ignored) {}
        }
        config.setSeed(WeeklySeedPlugin.WeeklySeedFetcher.seed);
        config.setOffset(WeeklySeedPlugin.WeeklySeedFetcher.offset);
        worldConfig.setSeed(config.getSeed());
        seedConfig.save();
    }

}
