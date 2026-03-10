package net.swordie.ms.client.character.items;

import net.swordie.ms.client.character.Char;
import net.swordie.ms.constants.ItemConstants;
import net.swordie.ms.constants.JobConstants;
import net.swordie.ms.enums.ItemGrade;
import net.swordie.ms.loaders.ItemData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminPotentialPickerSession {
    private final Equip equip;
    private final short equipPos;
    private final boolean bonus;
    private final List<Integer> candidates;
    private final List<Integer> selected = new ArrayList<>();

    public AdminPotentialPickerSession(Equip equip, short equipPos, boolean bonus) {
        this.equip = equip;
        this.equipPos = equipPos;
        this.bonus = bonus;
        Map<Integer, ItemOption> byId = new LinkedHashMap<>();
        for (ItemOption option : ItemConstants.getOptionsByEquip(equip, bonus, ItemGrade.Legendary)) {
            byId.put(option.getId(), option);
        }
        this.candidates = byId.values().stream()
                .sorted(Comparator
                        .comparing((ItemOption io) -> cleanOptionString(io).toLowerCase(Locale.ENGLISH))
                        .thenComparingInt(ItemOption::getId))
                .map(ItemOption::getId)
                .collect(Collectors.toList());
    }

    public boolean isComplete() {
        return selected.size() >= 3;
    }

    public boolean isValid() {
        return !candidates.isEmpty();
    }

    public boolean select(int index) {
        if (index < 0 || index >= candidates.size()) {
            return false;
        }
        selected.add(candidates.get(index));
        return true;
    }

    public String getSelectionText() {
        StringBuilder sb = new StringBuilder();
        sb.append("#e<Admin ").append(bonus ? "Bonus Potential" : "Potential").append(" Picker>#n\r\n");
        sb.append("Select ").append(3 - selected.size()).append(" more line");
        if (3 - selected.size() != 1) {
            sb.append("s");
        }
        sb.append(" for #v").append(equip.getItemId()).append("##t").append(equip.getItemId()).append("#.\r\n\r\n");
        sb.append("Selected:\r\n");
        if (selected.isEmpty()) {
            sb.append("None yet.\r\n");
        } else {
            for (int i = 0; i < selected.size(); i++) {
                sb.append(i + 1).append(". ").append(getOptionDisplay(selected.get(i))).append("\r\n");
            }
        }
        sb.append("\r\nAvailable lines:\r\n#b");
        for (int i = 0; i < candidates.size(); i++) {
            sb.append("#L").append(i).append("#").append(getOptionDisplay(candidates.get(i))).append("#l\r\n");
        }
        sb.append("#k");
        return sb.toString();
    }

    public String apply(Char chr) {
        for (int i = 0; i < 3; i++) {
            equip.setOption(i, selected.get(i), bonus);
        }
        equip.updateToChar(chr);
        if (JobConstants.isZero(chr.getJob()) && ItemConstants.isLongOrBigSword(equip.getItemId())) {
            int otherEquipPos = Math.abs(equipPos) == 10 ? 11 : 10;
            Equip otherEquip = (Equip) chr.getEquippedInventory().getItemBySlot(otherEquipPos);
            if (otherEquip != null) {
                otherEquip.copyItemOptionsFrom(equip);
                otherEquip.updateToChar(chr);
            }
        }
        return getResultText();
    }

    private String getResultText() {
        StringBuilder sb = new StringBuilder();
        sb.append("#e<Admin ").append(bonus ? "Bonus Potential" : "Potential").append(" Picker>#n\r\n");
        sb.append("Applied the following lines to #v").append(equip.getItemId()).append("##t").append(equip.getItemId()).append("#:\r\n\r\n");
        for (int i = 0; i < selected.size(); i++) {
            sb.append(i + 1).append(". ").append(getOptionDisplay(selected.get(i))).append("\r\n");
        }
        return sb.toString();
    }

    private String getOptionDisplay(int optionId) {
        ItemOption option = ItemData.getItemOptionById(optionId);
        if (option == null) {
            return String.valueOf(optionId);
        }
        return "#g(Legendary)#k " + cleanOptionString(option);
    }

    private String cleanOptionString(ItemOption option) {
        return option.getString(equip.getReqLevel() + equip.getiIncReq()).replace("#", "");
    }
}
