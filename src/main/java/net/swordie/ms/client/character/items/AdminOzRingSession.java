package net.swordie.ms.client.character.items;

import net.swordie.ms.client.character.Char;
import net.swordie.ms.constants.ItemConstants;
import net.swordie.ms.loaders.ItemData;
import net.swordie.ms.loaders.StringData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AdminOzRingSession {
    private static final int[] OZ_RING_IDS = {
            1113098, 1113099, 1113100, 1113101, 1113102, 1113103, 1113104, 1113105, 1113106, 1113107,
            1113108, 1113109, 1113110, 1113111, 1113112, 1113113, 1113114, 1113115, 1113116, 1113117,
            1113118, 1113119, 1113120, 1113121, 1113122, 1113123, 1113124, 1113125, 1113126, 1113127,
            1113128
    };
    private static final short OZ_RING_LEVEL = 4;

    private final List<Integer> ringIds = new ArrayList<>();
    private Integer selectedRingId;

    public AdminOzRingSession() {
        for (int ringId : OZ_RING_IDS) {
            if (ItemConstants.isOzRing(ringId)) {
                ringIds.add(ringId);
            }
        }
        ringIds.sort(Comparator.comparing(this::getDisplayName));
    }

    public boolean isComplete() {
        return selectedRingId != null;
    }

    public boolean isValid() {
        return !ringIds.isEmpty();
    }

    public boolean select(int index) {
        if (index < 0 || index >= ringIds.size()) {
            return false;
        }
        selectedRingId = ringIds.get(index);
        return true;
    }

    public String getSelectionText() {
        StringBuilder sb = new StringBuilder();
        sb.append("#e<Admin Oz Ring Picker>#n\r\n");
        sb.append("Select an Oz ring to receive at level 4.\r\n\r\n#b");
        for (int i = 0; i < ringIds.size(); i++) {
            int ringId = ringIds.get(i);
            sb.append("#L").append(i).append("##v").append(ringId).append("##t").append(ringId).append("# (#gLv. 4#k)#l\r\n");
        }
        sb.append("#k");
        return sb.toString();
    }

    public String apply(Char chr) {
        if (selectedRingId == null) {
            return "No Oz ring selected.";
        }
        if (!chr.canHoldItem(selectedRingId, 1)) {
            return "Please make space in your Equip inventory first.";
        }
        Equip ring = ItemData.getEquipDeepCopy(selectedRingId, false);
        if (ring == null) {
            return "Could not create the selected Oz ring.";
        }
        ring.setLevel(OZ_RING_LEVEL);
        chr.addItemToInventory(ring);
        return String.format("Received #v%d##t%d# at level %d.", selectedRingId, selectedRingId, (int) OZ_RING_LEVEL);
    }

    private String getDisplayName(int itemId) {
        String itemName = StringData.getItemStringById(itemId);
        return itemName != null ? itemName : String.valueOf(itemId);
    }
}
