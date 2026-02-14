package weeklyseedplugin;


import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.events.AddWorldEvent;
import com.hypixel.hytale.server.core.universe.world.meta.BlockStateModule;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import weeklyseedplugin.setseedplugin.AddWorldSetSeed;
import weeklyseedplugin.setseedplugin.SeedConfig;
import weeklyseedplugin.standardizerplugin.chests.ChestConfig;
import weeklyseedplugin.standardizerplugin.chests.UseBlockStandardizePre;
import weeklyseedplugin.standardizerplugin.mobs.MobsKilledByIdComponent;
import weeklyseedplugin.standardizerplugin.mobs.OnDeathStandardize;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import javax.annotation.Nonnull;
import java.util.Set;

public class WeeklySeedPlugin extends JavaPlugin {
    private Config<SeedConfig> seedConfig;
    private Config<ChestConfig> chestConfig;

    public WeeklySeedPlugin(JavaPluginInit init) throws Exception {
        super(init);

        WeeklySeedFetcher.fetch();
    }

    @Override
    protected void setup() {
        super.setup();

        var registry = getEntityStoreRegistry();
        var mobsKilledByIdType = registry.registerComponent(
                MobsKilledByIdComponent.class,
                "Mobs_Killed_By_Id",
                MobsKilledByIdComponent.CODEC
        );
        MobsKilledByIdComponent.setComponentType(mobsKilledByIdType);

        chestConfig = new Config<>(getDataDirectory(), "ChestConfig.json", ChestConfig.CODEC);
        chestConfig.load();
        chestConfig.save();

        UseBlockStandardizePre.setChestConfig(chestConfig);
        LookupSystem.setChestConfig(chestConfig);

        this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, WeeklySeedPlugin::onPlayerReady);

        this.getChunkStoreRegistry().registerSystem(new LookupSystem(BlockStateModule.get().getComponentType(ItemContainerState.class)));

        seedConfig = new Config<>(getDataDirectory(), "SeedConfig.json", SeedConfig.CODEC);
        seedConfig.load();
        seedConfig.save();

        UseBlockStandardizePre.setSeedConfig(seedConfig);
        OnDeathStandardize.setSeedConfig(seedConfig);
        AddWorldSetSeed.setSeedConfig(seedConfig);

        getEventRegistry().registerGlobal(AddWorldEvent.class, AddWorldSetSeed::onWorldAdd);

        getEntityStoreRegistry().registerSystem(new UseBlockStandardizePre());
        getEntityStoreRegistry().registerSystem(new OnDeathStandardize());
    }

    public static class LookupSystem extends RefSystem<ChunkStore> {
        private static Config<ChestConfig> chestConfig;
        private final ComponentType<ChunkStore, ItemContainerState> componentType;
        @Nonnull
        private final Set<Dependency<ChunkStore>> dependencies;

        public static void setChestConfig(Config<ChestConfig> config) {
            chestConfig = config;
        }

        public LookupSystem(ComponentType<ChunkStore, ItemContainerState> componentType) {
            this.componentType = componentType;
            this.dependencies = Set.of(new SystemDependency<>(Order.BEFORE, BlockStateModule.LegacyBlockStateRefSystem.class));
        }
        
        public void onEntityAdded(@NonNullDecl Ref<ChunkStore> ref, @NonNullDecl AddReason addReason, @NonNullDecl Store<ChunkStore> store, @NonNullDecl CommandBuffer<ChunkStore> commandBuffer) {
            ItemContainerState itemContainerState = store.getComponent(ref, this.componentType);
            if (itemContainerState != null && itemContainerState.getDroplist() != null) {
                int x = itemContainerState.getBlockX();
                int y = itemContainerState.getBlockY();
                int z = itemContainerState.getBlockZ();
                String droplist = itemContainerState.getDroplist();

                chestConfig.get().addChest(x, y, z, droplist);

                if (!chestConfig.get().isOpenedChest(x, y, z)) {
                    try {
                        Field dropListField = ItemContainerState.class.getDeclaredField("droplist");
                        dropListField.setAccessible(true);
                        dropListField.set(itemContainerState, null);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
                chestConfig.save();
            }

        }

        @Override
        public void onEntityRemove(@NotNull Ref<ChunkStore> ref, @NotNull RemoveReason removeReason, @NotNull Store<ChunkStore> store, @NotNull CommandBuffer<ChunkStore> commandBuffer) {

        }

        @Override
        public @Nullable Query<ChunkStore> getQuery() {
            return this.componentType;
        }

        @Nonnull
        public Set<Dependency<ChunkStore>> getDependencies() {
            return this.dependencies;
        }
    }

    public static class WeeklySeedFetcher {
        public static long seed;
        public static long offset;

        public static void fetch() throws Exception {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://raw.githubusercontent.com/Sossil/WeeklySeedPlugin/master/src/main/resources/weeklyseed.txt"))
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient
                    .newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Seed fetch failed with code: " + response.statusCode());
            }

            String[] lines = response.body().trim().split(" ");

            if (lines.length < 2) {
                throw new RuntimeException("Expected 'seed offset");
            }

            seed = Long.parseLong(lines[0].trim());
            offset = Long.parseLong(lines[1].trim());
        }
    }

    public static void onPlayerReady(PlayerReadyEvent event){
        World world = event.getPlayer().getWorld();
        world.execute(() -> {
            Ref<EntityStore> ref = event.getPlayer().getReference();
            if (ref == null) return;

            Store<EntityStore> store = event.getPlayerRef().getStore();

            var mobsKilledByIdType = MobsKilledByIdComponent.getComponentType();
            if (store.getComponent(ref, mobsKilledByIdType) == null){
                store.addComponent(ref, mobsKilledByIdType);
            }
        });
    }
}
