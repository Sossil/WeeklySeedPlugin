package weeklyseedplugin.setseedplugin;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class SeedConfig {

    public static final BuilderCodec<SeedConfig> CODEC;

    private long seed;

    private long offset;

    public SeedConfig() {
    }

    public long getOffset() {return offset;}

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    static {
        CODEC = BuilderCodec
                .builder(SeedConfig.class, SeedConfig::new)
                .append(new KeyedCodec<>("Seed", Codec.LONG),
                        SeedConfig::setSeed,
                        SeedConfig::getSeed)
                .add()
                .append(new KeyedCodec<>("Offset", Codec.LONG),
                        SeedConfig::setOffset,
                        SeedConfig::getOffset)
                .add().build();
    }
}
