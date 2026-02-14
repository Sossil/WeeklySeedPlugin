package weeklyseedplugin.standardizerplugin.chests;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChestConfig {

    private Map<String, String> positionToDropList = new ConcurrentHashMap<>();
    private Map<String, String> openedChests = new ConcurrentHashMap<>();


    public static final BuilderCodec<ChestConfig> CODEC = BuilderCodec
            .builder(ChestConfig.class, ChestConfig::new)
            .append(
                    new KeyedCodec<>("PositionToDropList" , new MapCodec<>(Codec.STRING, HashMap::new, false)),
                    ChestConfig::setPositionToDropList,
                    ChestConfig::getPositionToDropList
            ).add()
            .append(
                    new KeyedCodec<>("OpenedChests", new MapCodec<>(Codec.STRING, HashMap::new, false)),
                    ChestConfig::setOpenedChests,
                    ChestConfig::getOpenedChests
            ).add()
            .build();

    public Map<String, String> getPositionToDropList() { return positionToDropList; }
    public void setPositionToDropList(Map<String, String> positionToDropList) { this.positionToDropList = positionToDropList; }
    public Map<String, String> getOpenedChests() { return openedChests; }
    public void setOpenedChests(Map<String, String> openedChests) { this.openedChests = openedChests; }

    public boolean isOpenedChest(int x, int y, int z) {
        String posKey = x + "," + y + "," + z;
        return openedChests.containsKey(posKey);
    }

    public void markOpenedChest(int x, int y, int z) {
        String posKey = x + "," + y + "," + z;
        openedChests.put(posKey, "");
    }

    public String getDropList(int x, int y, int z) {
        String posKey = x + "," + y + "," + z;
        return positionToDropList.get(posKey);
    }

    public void addChest(int x, int y, int z, String dropList) {
        String posKey = x + "," + y + "," + z;
        positionToDropList.put(posKey, dropList);
    }
}
