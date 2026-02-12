package weeklyseedplugin.setseedplugin;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class SeedConfig {

    public static final BuilderCodec<SeedConfig> CODEC;

    private static long seed;

    private static long offset;

    public SeedConfig() {
    }

    public long getOffset() {return offset;}

    public void setOffset(long offset) {
        SeedConfig.offset = offset;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        SeedConfig.seed = seed;
    }

    static {
        CODEC = BuilderCodec
                .builder(SeedConfig.class, SeedConfig::new)
                .append(new KeyedCodec<>("Seed", Codec.LONG),
                        (config, value) -> seed = value,
                        SeedConfig::getSeed)
                .add()
                .append(new KeyedCodec<>("Offset", Codec.LONG),
                        (config, value) -> offset = value,
                        SeedConfig::getOffset)
                .add().build();
    }
}
