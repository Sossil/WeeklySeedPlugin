package weeklyseedplugin.standardizerplugin.mobs;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemDrop;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemDropList;
import com.hypixel.hytale.server.core.asset.type.item.config.container.ItemDropContainer;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.systems.NPCDamageSystems;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import weeklyseedplugin.setseedplugin.SeedConfig;
import weeklyseedplugin.standardizerplugin.chests.UseBlockStandardizePre;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.*;

public class OnDeathStandardize extends RefChangeSystem<EntityStore, DeathComponent> {
    UseBlockStandardizePre useBlockInstance = new UseBlockStandardizePre();
    private static Config<SeedConfig> seedConfig;

    public static void setSeedConfig(Config<SeedConfig> config) {
        seedConfig = config;
    }

    @Nonnull
    @Override
    public Set<Dependency<EntityStore>> getDependencies() {
        return Set.of(new SystemDependency(Order.BEFORE, NPCDamageSystems.DropDeathItems.class));
    }


    @Override
    public @NotNull ComponentType<EntityStore, DeathComponent> componentType() {
        return DeathComponent.getComponentType();
    }

    @Override
    public void onComponentAdded(@NotNull Ref<EntityStore> ref, @NotNull DeathComponent deathComponent, @NotNull Store<EntityStore> store, @NotNull CommandBuffer<EntityStore> commandBuffer) {
        NPCEntity npc = commandBuffer.getComponent(ref, Objects.requireNonNull(NPCEntity.getComponentType()));
        if (npc == null) return;

        Role role = npc.getRole();
        if (role == null) return;

        String dropListId = role.getDropListId();
        if (dropListId == null) return;

        Vector3d position = npc.getOldPosition();
        if (position == null) return;

        try {
            Field dropListField = role.getClass().getDeclaredField("dropListId");
            dropListField.setAccessible(true);
            dropListField.set(role, null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        List<ItemStack> drops = this.getSeededMobDrops(dropListId);
        for (ItemStack item : drops) {
            spawnDrop(store, commandBuffer, item, position);
        }

    }

    @Override
    public void onComponentSet(@NotNull Ref<EntityStore> ref, @Nullable DeathComponent deathComponent, @NotNull DeathComponent t1, @NotNull Store<EntityStore> store, @NotNull CommandBuffer<EntityStore> commandBuffer) {

    }

    @Override
    public void onComponentRemoved(@NotNull Ref<EntityStore> ref, @NotNull DeathComponent deathComponent, @NotNull Store<EntityStore> store, @NotNull CommandBuffer<EntityStore> commandBuffer) {

    }

    private static void spawnDrop(Store<EntityStore> store, CommandBuffer<EntityStore> cmd, ItemStack item, Vector3d pos) {

        Holder<EntityStore>[] dropHolders = ItemComponent.generateItemDrops(
                store,
                List.of(item),
                pos.clone().add(0, 0.5, 0),
                Vector3f.ZERO
        );

        cmd.addEntities(dropHolders, AddReason.SPAWN);
    }

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return Query.any();
    }

    @Nonnull
    public List<ItemStack> getSeededMobDrops(@javax.annotation.Nullable String dropListId) {
        if (dropListId == null) {
            return Collections.emptyList();
        } else {
            ItemDropList itemDropList = (ItemDropList)ItemDropList.getAssetMap().getAsset(dropListId);
            if (itemDropList != null && itemDropList.getContainer() != null) {
                List<ItemStack> generatedItemDrops = new ObjectArrayList();
                long seed = seedConfig.get().getSeed();
                long offset = seedConfig.get().getOffset();
                long combinedSeed = seed ^ offset;
                Random seededRandom = new Random(combinedSeed);
                List<ItemDrop> configuredItemDrops = new ObjectArrayList();
                ItemDropContainer var10000 = itemDropList.getContainer();
                Objects.requireNonNull(seededRandom);
                var10000.populateDrops(configuredItemDrops, seededRandom::nextDouble, dropListId);

                for(ItemDrop drop : configuredItemDrops) {
                    if (drop != null && drop.getItemId() != null) {
                        int amount = drop.getRandomQuantity(seededRandom);
                        if (amount > 0) {
                            generatedItemDrops.add(new ItemStack(drop.getItemId(), amount, drop.getMetadata()));
                        }
                    }
                }

                return generatedItemDrops;
            } else {
                return Collections.emptyList();
            }
        }
    }
}
