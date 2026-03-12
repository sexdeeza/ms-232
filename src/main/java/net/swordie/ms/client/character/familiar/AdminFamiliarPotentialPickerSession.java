package net.swordie.ms.client.character.familiar;

import net.swordie.ms.client.character.Char;
import net.swordie.ms.client.character.items.ItemOption;
import net.swordie.ms.connection.packet.UserLocal;
import net.swordie.ms.life.Familiar;
import net.swordie.ms.loaders.ItemData;
import net.swordie.ms.loaders.StringData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminFamiliarPotentialPickerSession {
    private final List<Familiar> familiars;
    private final List<Integer> candidates;
    private final List<Integer> selected = new ArrayList<>();
    private Familiar familiar;

    public AdminFamiliarPotentialPickerSession(Char chr) {
        familiars = chr.getFamiliarCodexManager().getFamiliars().stream()
                .sorted(Comparator
                        .comparing((Familiar fam) -> fam.getName().toLowerCase(Locale.ENGLISH))
                        .thenComparingInt(Familiar::getGrade)
                        .thenComparingInt(Familiar::getFamiliarID)
                        .thenComparingLong(Familiar::getId))
                .collect(Collectors.toList());

        Map<Integer, ItemOption> byId = new LinkedHashMap<>();
        for (ItemOption option : ItemData.getFamiliarOptionByGrade(4)) {
            byId.put(option.getId(), option);
        }
        for (ItemOption option : ItemData.getFamiliarOptionByGrade(5)) {
            byId.put(option.getId(), option);
        }
        candidates = byId.values().stream()
                .sorted(Comparator
                        .comparing((ItemOption io) -> cleanOptionString(io).toLowerCase(Locale.ENGLISH))
                        .thenComparingInt(ItemOption::getId))
                .map(ItemOption::getId)
                .collect(Collectors.toList());
    }

    public boolean isValid() {
        return !familiars.isEmpty() && !candidates.isEmpty();
    }

    public boolean isComplete() {
        return familiar != null && selected.size() >= 2;
    }

    public boolean select(int index) {
        if (familiar == null) {
            if (index < 0 || index >= familiars.size()) {
                return false;
            }
            familiar = familiars.get(index);
            return true;
        }
        if (index < 0 || index >= candidates.size()) {
            return false;
        }
        selected.add(candidates.get(index));
        return true;
    }

    public String getSelectionText() {
        if (familiar == null) {
            return getFamiliarSelectionText();
        }
        return getLineSelectionText();
    }

    public String apply(Char chr) {
        familiar.removeCurFamiliarBuffs();
        familiar.getOptions()[0] = selected.get(0);
        familiar.getOptions()[1] = selected.get(1);
        chr.write(UserLocal.familiarAddResult(familiar,
                FamiliarModifiedMask.PotLines0.getVal() + FamiliarModifiedMask.PotLines1.getVal()));
        return getResultText();
    }

    private String getFamiliarSelectionText() {
        StringBuilder sb = new StringBuilder();
        sb.append("#e<Admin Familiar Potential Picker>#n\r\n");
        sb.append("Select a familiar to edit.\r\n");
        sb.append("Legendary lines can be applied to any selected familiar.\r\n\r\n#b");
        for (int i = 0; i < familiars.size(); i++) {
            Familiar fam = familiars.get(i);
            sb.append("#L").append(i).append("#")
                    .append(getFamiliarDisplay(fam))
                    .append("#l\r\n");
        }
        sb.append("#k");
        return sb.toString();
    }

    private String getLineSelectionText() {
        StringBuilder sb = new StringBuilder();
        sb.append("#e<Admin Familiar Potential Picker>#n\r\n");
        sb.append("Selected familiar: ").append(getFamiliarDisplay(familiar)).append("\r\n");
        sb.append("Choose ").append(2 - selected.size()).append(" more line");
        if (2 - selected.size() != 1) {
            sb.append("s");
        }
        sb.append(".\r\n\r\nCurrent lines:\r\n");
        for (int i = 0; i < familiar.getOptions().length; i++) {
            sb.append(i + 1).append(". ").append(getOptionDisplay(familiar.getOptions()[i])).append("\r\n");
        }
        sb.append("\r\nSelected lines:\r\n");
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

    private String getResultText() {
        StringBuilder sb = new StringBuilder();
        sb.append("#e<Admin Familiar Potential Picker>#n\r\n");
        sb.append("Applied the following lines to ").append(getFamiliarDisplay(familiar)).append(":\r\n\r\n");
        for (int i = 0; i < selected.size(); i++) {
            sb.append(i + 1).append(". ").append(getOptionDisplay(selected.get(i))).append("\r\n");
        }
        return sb.toString();
    }

    private String getFamiliarDisplay(Familiar fam) {
        String familiarName = StringData.getMobStrings().getOrDefault(fam.getFamiliarID(), "Unknown Familiar");
        return fam.getName() + " ["
                + getGradeName(fam.getGrade())
                + "] (" + fam.getFamiliarID() + " - " + familiarName + ", id " + fam.getId() + ")";
    }

    private String getOptionDisplay(int optionId) {
        ItemOption option = ItemData.getFamiliarOptionById(optionId);
        return option == null ? String.valueOf(optionId) : cleanOptionString(option);
    }

    private String cleanOptionString(ItemOption option) {
        return option.getString(10).replace("#", "");
    }

    private String getGradeName(int grade) {
        return switch (grade) {
            case 0 -> "Common";
            case 1 -> "Rare";
            case 2 -> "Epic";
            case 3 -> "Unique";
            case 4 -> "Legendary";
            default -> "Unknown";
        };
    }
}
