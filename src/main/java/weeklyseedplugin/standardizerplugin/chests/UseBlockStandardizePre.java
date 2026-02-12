package weeklyseedplugin.standardizerplugin.chests;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemDrop;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemDropList;
import com.hypixel.hytale.server.core.asset.type.item.config.container.ItemDropContainer;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import weeklyseedplugin.WeeklySeedPlugin;
import weeklyseedplugin.setseedplugin.SeedConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class UseBlockStandardizePre extends EntityEventSystem<EntityStore, UseBlockEvent.Post> {
    private static Config<SeedConfig> seedConfig;
    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    public UseBlockStandardizePre() {
        super(UseBlockEvent.Post.class);
    }

    public static void setSeedConfig(Config<SeedConfig> config) {
        seedConfig = config;
    }

    @Override
    public void handle(int index, ArchetypeChunk<EntityStore> archetypeChunk,
                       Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer,
                       UseBlockEvent.Post event) {

        Player player = (Player) store.getComponent(event.getContext().getEntity(), Player.getComponentType());
        Vector3i pos = event.getTargetBlock();
        BlockState blockstate = player.getWorld().getState(pos.getX(), pos.getY(), pos.getZ(), true);
        if (blockstate instanceof ItemContainerState container) {
            if (event.getInteractionType() == InteractionType.Use) {
                standardizeChestContents(container, pos);
            }
        }
    }

    private void standardizeChestContents(ItemContainerState container, Vector3i pos) {
        String dropList = WeeklySeedPlugin.LookupSystem.getDropList(pos.getX(), pos.getY(), pos.getZ());

        if (WeeklySeedPlugin.LookupSystem.isFirstChestOpen(pos.getX(), pos.getY(), pos.getZ())) {
            container.getItemContainer().clear();
            List<ItemStack> drops = this.getSeededItemDrops(dropList, pos);

            if (!drops.isEmpty()) {
                short capacity = container.getItemContainer().getCapacity();
                List<Short> slots = new ArrayList();

                for (short s = 0; s < capacity; ++s) {
                    slots.add(s);
                }
                Collections.shuffle(slots, ThreadLocalRandom.current());

                for (int idx = 0; idx < drops.size() && idx < slots.size(); ++idx) {
                    short slot = slots.get(idx);
                    container.getItemContainer().setItemStackForSlot(slot, (ItemStack) drops.get(idx));
                }
            }
            WeeklySeedPlugin.LookupSystem.markChestOpen(pos.getX(), pos.getY(), pos.getZ());
        }
    }

    @Nonnull
    public List<ItemStack> getSeededItemDrops(@Nullable String dropListId, Vector3i pos) {
        if (dropListId == null) {
            return Collections.emptyList();
        } else {
            ItemDropList itemDropList = (ItemDropList)ItemDropList.getAssetMap().getAsset(dropListId);
            if (itemDropList != null && itemDropList.getContainer() != null) {
                List<ItemStack> generatedItemDrops = new ObjectArrayList();
                long seed = seedConfig.get().getSeed();
                long positionSeed = ((long)pos.getX() * 31 + pos.getY() * 31 + pos.getZ());
                long combinedSeed = seed ^ positionSeed;
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

    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }
}


