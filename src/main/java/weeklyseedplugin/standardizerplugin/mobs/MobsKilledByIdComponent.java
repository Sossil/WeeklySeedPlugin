package weeklyseedplugin.standardizerplugin.mobs;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.HashMap;
import java.util.Map;

public class MobsKilledByIdComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, MobsKilledByIdComponent> TYPE;

    public static void setComponentType(ComponentType<EntityStore, MobsKilledByIdComponent> type) {TYPE = type; }

    public static ComponentType<EntityStore, MobsKilledByIdComponent> getComponentType() {return TYPE; }

    public static final BuilderCodec<MobsKilledByIdComponent> CODEC = BuilderCodec
            .builder(MobsKilledByIdComponent.class, MobsKilledByIdComponent::new)
            .append(
                    new KeyedCodec<>("MobsKilled" , new MapCodec<>(Codec.LONG, HashMap::new, false)),
                    MobsKilledByIdComponent::setMobsKilled,
                    MobsKilledByIdComponent::getMobsKilled
            ).add()
            .build();

    private Map<String, Long> mobsKilled = new HashMap<>();
    public MobsKilledByIdComponent() {}

    public void addId(String dropListId) {mobsKilled.merge(dropListId,1L, Long::sum); }

    public long getMobsKilledById(String dropListId) { return mobsKilled.getOrDefault(dropListId, 0L); }

    public Map<String, Long> getMobsKilled() { return mobsKilled; }

    public void setMobsKilled(Map<String, Long> mobsKilled) {this.mobsKilled = mobsKilled; }

    @Override
    public MobsKilledByIdComponent clone() { return new MobsKilledByIdComponent(); }
}
