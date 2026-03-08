package net.swordie.ms.client.character.items;

import net.swordie.ms.client.character.Char;
import net.swordie.ms.connection.packet.field.FieldPacket;
import net.swordie.ms.constants.ItemConstants;
import net.swordie.ms.constants.JobConstants;
import net.swordie.ms.enums.ItemGrade;
import net.swordie.ms.loaders.ItemData;
import net.swordie.ms.util.Util;

import java.util.ArrayList;
import java.util.List;

public class VioletCubeSession {
    private final Equip equip;
    private final Item cubeItem;
    private final short equipPos;
    private final ItemGrade targetGrade;
    private final List<Integer> candidateLines = new ArrayList<>();
    private final List<Integer> selectedLines = new ArrayList<>();
    private final int requiredSelections;

    public VioletCubeSession(Equip equip, Item cubeItem, short equipPos, ItemGrade targetGrade) {
        this.equip = equip;
        this.cubeItem = cubeItem;
        this.equipPos = equipPos;
        this.targetGrade = targetGrade;
        this.requiredSelections = equip.getOptionBase(2) == 0 ? 2 : 3;
        int candidateCount = requiredSelections * 2;
        List<Integer> weightedOptions = ItemConstants.getWeightedOptionsByEquip(equip, false, targetGrade);
        for (int i = 0; i < candidateCount; i++) {
            candidateLines.add(Util.getRandomFromCollection(weightedOptions));
        }
    }

    public boolean isComplete() {
        return selectedLines.size() >= requiredSelections;
    }

    public boolean select(int index) {
        if (index < 0 || index >= candidateLines.size()) {
            return false;
        }
        selectedLines.add(candidateLines.remove(index));
        return true;
    }

    public String getSelectionText() {
        StringBuilder sb = new StringBuilder();
        sb.append("#e<Violet Cube>#n\r\n");
        sb.append("Select ").append(requiredSelections - selectedLines.size()).append(" more line");
        if (requiredSelections - selectedLines.size() != 1) {
            sb.append("s");
        }
        sb.append(" for #v").append(equip.getItemId()).append("##t").append(equip.getItemId()).append("#.\r\n\r\n");
        sb.append("Resulting rank: ").append(getGradeText(targetGrade)).append("\r\n\r\n");
        sb.append("Selected:\r\n");
        if (selectedLines.isEmpty()) {
            sb.append("None yet.\r\n");
        } else {
            for (int i = 0; i < selectedLines.size(); i++) {
                sb.append(i + 1).append(". ").append(getOptionDisplay(selectedLines.get(i))).append("\r\n");
            }
        }
        sb.append("\r\nAvailable lines:\r\n#b");
        for (int i = 0; i < candidateLines.size(); i++) {
            sb.append("#L").append(i).append("#").append(getOptionDisplay(candidateLines.get(i))).append("#l\r\n");
        }
        sb.append("#k");
        return sb.toString();
    }

    public String apply(Char chr) {
        for (int i = 0; i < requiredSelections; i++) {
            equip.setOptionBase(i, selectedLines.get(i));
        }
        for (int i = requiredSelections; i < 3; i++) {
            equip.setOptionBase(i, 0);
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
        chr.write(FieldPacket.showItemReleaseEffect(chr.getId(), equipPos, false));
        chr.consumeItem(cubeItem);
        return getResultText();
    }

    private String getResultText() {
        StringBuilder sb = new StringBuilder();
        sb.append("#e<Violet Cube>#n\r\n");
        sb.append("Applied the following lines to #v").append(equip.getItemId()).append("##t").append(equip.getItemId()).append("#:\r\n\r\n");
        for (int i = 0; i < selectedLines.size(); i++) {
            sb.append(i + 1).append(". ").append(getOptionDisplay(selectedLines.get(i))).append("\r\n");
        }
        return sb.toString();
    }

    private String getOptionDisplay(int optionId) {
        int tier = optionId / 10000;
        String colorPrefix;
        if (tier <= 1) {
            colorPrefix = "#b(Rare)#k ";
        } else if (tier == 2) {
            colorPrefix = "#d(Epic)#k ";
        } else if (tier == 3) {
            colorPrefix = "#r(Unique)#k ";
        } else {
            colorPrefix = "#g(Legendary)#k ";
        }
        ItemOption option = ItemData.getItemOptionById(optionId);
        String optionString = option != null ? option.getString(equip.getReqLevel()).replace("#", "") : String.valueOf(optionId);
        return colorPrefix + optionString;
    }

    private String getGradeText(ItemGrade grade) {
        switch (grade) {
            case Rare:
            case HiddenRare:
                return "Rare";
            case Epic:
            case HiddenEpic:
                return "Epic";
            case Unique:
            case HiddenUnique:
                return "Unique";
            case Legendary:
            case HiddenLegendary:
                return "Legendary";
            default:
                return grade.name();
        }
    }
}
