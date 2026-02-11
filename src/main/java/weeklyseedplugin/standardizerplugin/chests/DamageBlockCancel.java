package weeklyseedplugin.standardizerplugin.chests;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.event.events.ecs.DamageBlockEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import weeklyseedplugin.WeeklySeedPlugin;

public class DamageBlockCancel extends EntityEventSystem<EntityStore, DamageBlockEvent> {
    public DamageBlockCancel(@NotNull Class<DamageBlockEvent> eventType) {
        super(eventType);
    }

    @Override
    public void handle(int i, @NotNull ArchetypeChunk<EntityStore> archetypeChunk, @NotNull Store<EntityStore> store, @NotNull CommandBuffer<EntityStore> commandBuffer, @NotNull DamageBlockEvent damageBlockEvent) {
        Vector3i pos = damageBlockEvent.getTargetBlock();

        if (WeeklySeedPlugin.LookupSystem.getDropList(pos.getX(), pos.getY()+1, pos.getZ()) == null &&
                WeeklySeedPlugin.LookupSystem.getDropList(pos.getX(), pos.getY(), pos.getZ()) == null) {
            return;
        }

        if (WeeklySeedPlugin.LookupSystem.isFirstChestOpen(pos.getX(), pos.getY(), pos.getZ()) ||
                WeeklySeedPlugin.LookupSystem.isFirstChestOpen(pos.getX(), pos.getY()+1, pos.getZ())) {
            damageBlockEvent.setCancelled(true);
        }
    }

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return Query.any();
    }
}