package weeklyseedplugin.setseedplugin;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class SeedConfig {

    public static final BuilderCodec<SeedConfig> CODEC;

    private static long seed = 0L;

    public SeedConfig () {
    }

    public long getSeed() {
        return this.seed;
    }

    public static void setSeed(long seed) {
        SeedConfig.seed = seed;
    }

    static {
        CODEC = ((BuilderCodec.Builder<SeedConfig>) BuilderCodec
                .builder(SeedConfig.class, SeedConfig::new)
                .append(new KeyedCodec<>("Seed", Codec.LONG),
                        (config, value) -> config.seed = value,
                        SeedConfig::getSeed)
                .add()
        ).build();
    }
}
