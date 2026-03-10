package net.swordie.ms.client.character.items;

import net.swordie.ms.client.character.Char;
import net.swordie.ms.constants.ItemConstants;
import net.swordie.ms.constants.JobConstants;
import net.swordie.ms.enums.FlameStat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class AdminFlamePickerSession {
    private final Equip equip;
    private final short equipPos;
    private final List<FlameChoice> candidates = new ArrayList<>();
    private final List<FlameChoice> selected = new ArrayList<>();

    public AdminFlamePickerSession(Equip equip, short equipPos) {
        this.equip = equip;
        this.equipPos = equipPos;
        if (!ItemConstants.canEquipHaveFlame(equip)) {
            return;
        }
        for (FlameStat flameStat : FlameStat.values()) {
            if (!isEligible(flameStat)) {
                continue;
            }
            candidates.add(new FlameChoice(flameStat, (short) 6, getDisplay(flameStat, (short) 6)));
            candidates.add(new FlameChoice(flameStat, (short) 7, getDisplay(flameStat, (short) 7)));
        }
        candidates.sort(Comparator.comparing(choice -> choice.display.toLowerCase(Locale.ENGLISH)));
    }

    public boolean isComplete() {
        return selected.size() >= 4;
    }

    public boolean isValid() {
        return !candidates.isEmpty();
    }

    public boolean select(int index) {
        if (index < 0 || index >= candidates.size()) {
            return false;
        }
        selected.add(candidates.remove(index));
        return true;
    }

    public String getSelectionText() {
        StringBuilder sb = new StringBuilder();
        sb.append("#e<Admin Flame Picker>#n\r\n");
        sb.append("Select ").append(4 - selected.size()).append(" more line");
        if (4 - selected.size() != 1) {
            sb.append("s");
        }
        sb.append(" for #v").append(equip.getItemId()).append("##t").append(equip.getItemId()).append("#.\r\n\r\n");
        sb.append("Selected:\r\n");
        if (selected.isEmpty()) {
            sb.append("None yet.\r\n");
        } else {
            for (int i = 0; i < selected.size(); i++) {
                sb.append(i + 1).append(". ").append(selected.get(i).display).append("\r\n");
            }
        }
        sb.append("\r\nAvailable lines:\r\n#b");
        for (int i = 0; i < candidates.size(); i++) {
            sb.append("#L").append(i).append("#").append(candidates.get(i).display).append("#l\r\n");
        }
        sb.append("#k");
        return sb.toString();
    }

    public String apply(Char chr) {
        equip.resetFlameStats();
        long exGradeOption = 0;
        for (int i = 0; i < selected.size(); i++) {
            FlameChoice choice = selected.get(i);
            apply(choice.flameStat, choice.tier);
            exGradeOption += (long) (Math.pow(1000, i) * (choice.tier + 10 * choice.flameStat.getExGrade()));
        }
        equip.setExGradeOption(exGradeOption);
        equip.updateToChar(chr);
        if (JobConstants.isZero(chr.getJob()) && ItemConstants.isLongOrBigSword(equip.getItemId())) {
            int otherEquipPos = Math.abs(equipPos) == 10 ? 11 : 10;
            Equip otherEquip = (Equip) chr.getEquippedInventory().getItemBySlot(otherEquipPos);
            if (otherEquip != null) {
                otherEquip.copyFlameStatsFrom(equip);
                otherEquip.updateToChar(chr);
            }
        }
        return getResultText();
    }

    private String getResultText() {
        StringBuilder sb = new StringBuilder();
        sb.append("#e<Admin Flame Picker>#n\r\n");
        sb.append("Applied the following lines to #v").append(equip.getItemId()).append("##t").append(equip.getItemId()).append("#:\r\n\r\n");
        for (int i = 0; i < selected.size(); i++) {
            sb.append(i + 1).append(". ").append(selected.get(i).display).append("\r\n");
        }
        return sb.toString();
    }

    private boolean isEligible(FlameStat flameStat) {
        if ((flameStat == FlameStat.BossDamage || flameStat == FlameStat.Damage) && !ItemConstants.isWeapon(equip.getItemId())) {
            return false;
        }
        return flameStat != FlameStat.LevelReduction || equip.getReqLevel() + equip.getiIncReq() >= 5;
    }

    private String getDisplay(FlameStat flameStat, short tier) {
        switch (flameStat) {
            case STR:
                return String.format("STR +%d (T%d)", tier * equip.getFlameLevelExtended(), tier);
            case DEX:
                return String.format("DEX +%d (T%d)", tier * equip.getFlameLevelExtended(), tier);
            case INT:
                return String.format("INT +%d (T%d)", tier * equip.getFlameLevelExtended(), tier);
            case LUK:
                return String.format("LUK +%d (T%d)", tier * equip.getFlameLevelExtended(), tier);
            case STRDEX:
                return String.format("STR/DEX +%d (T%d)", tier * equip.getFlameLevel(), tier);
            case STRINT:
                return String.format("STR/INT +%d (T%d)", tier * equip.getFlameLevel(), tier);
            case STRLUK:
                return String.format("STR/LUK +%d (T%d)", tier * equip.getFlameLevel(), tier);
            case DEXINT:
                return String.format("DEX/INT +%d (T%d)", tier * equip.getFlameLevel(), tier);
            case DEXLUK:
                return String.format("DEX/LUK +%d (T%d)", tier * equip.getFlameLevel(), tier);
            case INTLUK:
                return String.format("INT/LUK +%d (T%d)", tier * equip.getFlameLevel(), tier);
            case Attack:
                return String.format("ATT +%d (T%d)", equip.getATTBonus(tier), tier);
            case MagicAttack:
                return String.format("MATT +%d (T%d)", equip.getATTBonus(tier), tier);
            case Defense:
                return String.format("DEF +%d (T%d)", tier * equip.getFlameLevelExtended(), tier);
            case MaxHP:
                return String.format("MaxHP +%d (T%d)", ((equip.getReqLevel() + equip.getiIncReq()) / 10) * 30 * tier, tier);
            case MaxMP:
                return String.format("MaxMP +%d (T%d)", ((equip.getReqLevel() + equip.getiIncReq()) / 10) * 30 * tier, tier);
            case Speed:
                return String.format("Speed +%d (T%d)", tier, tier);
            case Jump:
                return String.format("Jump +%d (T%d)", tier, tier);
            case AllStats:
                return String.format("All Stat +%d%% (T%d)", tier, tier);
            case BossDamage:
                return String.format("Boss Damage +%d%% (T%d)", tier * 2, tier);
            case Damage:
                return String.format("Damage +%d%% (T%d)", tier, tier);
            case LevelReduction:
                return String.format("Level Reduction -%d (T%d)", 5 * tier, tier);
            default:
                return flameStat.name();
        }
    }

    private void apply(FlameStat flameStat, short tier) {
        int addedStat = tier * equip.getFlameLevel();
        int addedStatExtended = tier * equip.getFlameLevelExtended();
        switch (flameStat) {
            case STR -> equip.setfSTR(equip.getfSTR() + addedStatExtended);
            case DEX -> equip.setfDEX(equip.getfDEX() + addedStatExtended);
            case INT -> equip.setfINT(equip.getfINT() + addedStatExtended);
            case LUK -> equip.setfLUK(equip.getfLUK() + addedStatExtended);
            case STRDEX -> {
                equip.setfSTR(equip.getfSTR() + addedStat);
                equip.setfDEX(equip.getfDEX() + addedStat);
            }
            case STRINT -> {
                equip.setfSTR(equip.getfSTR() + addedStat);
                equip.setfINT(equip.getfINT() + addedStat);
            }
            case STRLUK -> {
                equip.setfSTR(equip.getfSTR() + addedStat);
                equip.setfLUK(equip.getfLUK() + addedStat);
            }
            case DEXINT -> {
                equip.setfDEX(equip.getfDEX() + addedStat);
                equip.setfINT(equip.getfINT() + addedStat);
            }
            case DEXLUK -> {
                equip.setfDEX(equip.getfDEX() + addedStat);
                equip.setfLUK(equip.getfLUK() + addedStat);
            }
            case INTLUK -> {
                equip.setfINT(equip.getfINT() + addedStat);
                equip.setfLUK(equip.getfLUK() + addedStat);
            }
            case Attack -> equip.setfATT(equip.getfATT() + equip.getATTBonus(tier));
            case MagicAttack -> equip.setfMATT(equip.getfMATT() + equip.getATTBonus(tier));
            case Defense -> equip.setfDEF(equip.getfDEF() + addedStatExtended);
            case MaxHP -> equip.setfHP(equip.getfHP() + ((equip.getReqLevel() + equip.getiIncReq()) / 10) * 30 * tier);
            case MaxMP -> equip.setfMP(equip.getfMP() + ((equip.getReqLevel() + equip.getiIncReq()) / 10) * 30 * tier);
            case Speed -> equip.setfSpeed(equip.getfSpeed() + tier);
            case Jump -> equip.setfJump(equip.getfJump() + tier);
            case AllStats -> equip.setfAllStat(equip.getfAllStat() + tier);
            case BossDamage -> equip.setfBoss(equip.getfBoss() + tier * 2);
            case Damage -> equip.setfDamage(equip.getfDamage() + tier);
            case LevelReduction -> equip.setfLevel(equip.getfLevel() + (5 * tier));
        }
    }

    private static class FlameChoice {
        private final FlameStat flameStat;
        private final short tier;
        private final String display;

        private FlameChoice(FlameStat flameStat, short tier, String display) {
            this.flameStat = flameStat;
            this.tier = tier;
            this.display = display;
        }
    }
}
