package net.swordie.ms.client.character.items;

import net.swordie.ms.client.character.Char;
import net.swordie.ms.constants.ItemConstants;
import net.swordie.ms.constants.JobConstants;
import net.swordie.ms.enums.EquipBaseStat;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class AdminSetEquipSession {
    private static final int DEFAULT_STAT_MAX = 30000;
    private static final int REMAINING_ENHANCEMENTS_MAX = 255;
    private static final int STAR_FORCE_MAX = 100;

    private final Equip equip;
    private final short equipPos;
    private final List<EditableStat> editableStats = new ArrayList<>();

    public AdminSetEquipSession(Equip equip, short equipPos) {
        this.equip = equip;
        this.equipPos = equipPos;

        addStat("STR", EquipBaseStat.iStr);
        addStat("DEX", EquipBaseStat.iDex);
        addStat("INT", EquipBaseStat.iInt);
        addStat("LUK", EquipBaseStat.iLuk);
        addStat("MaxHP", EquipBaseStat.iMaxHP);
        addStat("MaxMP", EquipBaseStat.iMaxMP);
        addStat("ATT", EquipBaseStat.iPAD);
        addStat("MATT", EquipBaseStat.iMAD);
        addStat("DEF", EquipBaseStat.iDEF);
        addStat("Speed", EquipBaseStat.iSpeed);
        addStat("Jump", EquipBaseStat.iJump);
        addStat("Boss Damage %", EquipBaseStat.bdr, this::canUseOffensivePercentStats);
        addStat("Ignore DEF %", EquipBaseStat.imdr, this::canUseOffensivePercentStats);
        addStat("Damage %", EquipBaseStat.damR, this::canUseOffensivePercentStats);
        addStat("All Stat %", EquipBaseStat.statR);
        addStat("PvP Damage", EquipBaseStat.iPvpDamage);
        addStat("Required Level Reduction", EquipBaseStat.iReduceReq);
    }

    public boolean isValid() {
        return equip != null;
    }

    public String getMenuText() {
        return new StringBuilder()
                .append("#e<Admin Equip Editor>#n\r\n")
                .append("Editing #v").append(equip.getItemId()).append("##t").append(equip.getItemId()).append("# (slot ")
                .append(equipPos).append(").\r\n\r\n")
                .append("Current values:\r\n")
                .append("- Remaining Enhancements: #b").append(getRemainingEnhancements()).append("#k\r\n")
                .append("- Star Force: #b").append(getStarForce()).append("#k\r\n\r\n")
                .append("#L0#Individual Stats#l\r\n")
                .append("#L1#Remaining Enhancements#l\r\n")
                .append("#L2#Potential#l\r\n")
                .append("#L3#Bonus Potential#l\r\n")
                .append("#L4#Flames#l\r\n")
                .append("#L5#Star Force#l")
                .toString();
    }

    public boolean hasEditableStats() {
        return !editableStats.isEmpty();
    }

    public String getStatSelectionText() {
        StringBuilder sb = new StringBuilder();
        sb.append("#e<Admin Equip Editor - Individual Stats>#n\r\n");
        sb.append("Select a stat to edit for #v").append(equip.getItemId()).append("##t").append(equip.getItemId()).append("#.\r\n\r\n");
        sb.append("#L0#All Available Stats (add to each)#l\r\n");
        for (int i = 0; i < editableStats.size(); i++) {
            EditableStat editableStat = editableStats.get(i);
            sb.append("#L").append(i + 1).append("#")
                    .append(editableStat.display)
                    .append(" (current: ").append(getStatValue(i)).append(")")
                    .append("#l\r\n");
        }
        return sb.toString();
    }

    public boolean isValidStatIndex(int index) {
        return index >= 0 && index <= editableStats.size();
    }

    public String getStatDisplay(int index) {
        if (index == 0) {
            return "All Available Stats";
        }
        return editableStats.get(index - 1).display;
    }

    public int getStatValue(int index) {
        if (index == 0) {
            return 0;
        }
        return (int) equip.getBaseStat(editableStats.get(index - 1).baseStat);
    }

    public int getStatMaxValue(int index) {
        if (index == 0) {
            return DEFAULT_STAT_MAX;
        }
        return editableStats.get(index - 1).baseStat == EquipBaseStat.iReduceReq ? 255 : DEFAULT_STAT_MAX;
    }

    public String applyStat(Char chr, int index, int value) {
        if (index == 0) {
            for (EditableStat editableStat : editableStats) {
                equip.setBaseStat(editableStat.baseStat, equip.getBaseStat(editableStat.baseStat) + value);
            }
            applyAndSync(chr, otherEquip -> {
                for (EditableStat editableStat : editableStats) {
                    otherEquip.setBaseStat(editableStat.baseStat, otherEquip.getBaseStat(editableStat.baseStat) + value);
                }
            });
            return String.format("Added %d to every available stat on #v%d##t%d#.", value, equip.getItemId(), equip.getItemId());
        }
        EditableStat editableStat = editableStats.get(index - 1);
        equip.setBaseStat(editableStat.baseStat, value);
        applyAndSync(chr, otherEquip -> otherEquip.setBaseStat(editableStat.baseStat, value));
        return String.format("Set %s to %d on #v%d##t%d#.", editableStat.display, value, equip.getItemId(), equip.getItemId());
    }

    public int getRemainingEnhancements() {
        return equip.getTuc();
    }

    public String applyRemainingEnhancements(Char chr, int value) {
        equip.setTuc((short) value);
        applyAndSync(chr, otherEquip -> otherEquip.setTuc((short) value));
        return String.format("Set remaining enhancements to %d on #v%d##t%d#.", value, equip.getItemId(), equip.getItemId());
    }

    public int getStarForce() {
        return equip.getChuc();
    }

    public String applyStarForce(Char chr, int value) {
        equip.setChuc((short) value);
        applyAndSync(chr, otherEquip -> otherEquip.setChuc((short) value));
        return String.format("Set Star Force to %d on #v%d##t%d#.", value, equip.getItemId(), equip.getItemId());
    }

    public boolean canEditFlames() {
        return ItemConstants.canEquipHaveFlame(equip);
    }

    public AdminPotentialPickerSession createPotentialSession(boolean bonus) {
        return new AdminPotentialPickerSession(equip, equipPos, bonus);
    }

    public AdminFlamePickerSession createFlameSession() {
        return new AdminFlamePickerSession(equip, equipPos);
    }

    public int getRemainingEnhancementsMax() {
        return REMAINING_ENHANCEMENTS_MAX;
    }

    public int getStarForceMax() {
        return STAR_FORCE_MAX;
    }

    private void addStat(String display, EquipBaseStat baseStat) {
        addStat(display, baseStat, ignored -> true);
    }

    private void addStat(String display, EquipBaseStat baseStat, Predicate<Equip> predicate) {
        if (predicate.test(equip)) {
            editableStats.add(new EditableStat(display, baseStat));
        }
    }

    private boolean canUseOffensivePercentStats(Equip ignored) {
        int itemId = equip.getItemId();
        return ItemConstants.isWeapon(itemId) || ItemConstants.isSecondary(itemId)
                || ItemConstants.isShield(itemId) || ItemConstants.isEmblem(itemId);
    }

    private void applyAndSync(Char chr, java.util.function.Consumer<Equip> zeroSync) {
        equip.recalcEnchantmentStats();
        equip.updateToChar(chr);
        if (JobConstants.isZero(chr.getJob()) && ItemConstants.isLongOrBigSword(equip.getItemId())) {
            int otherEquipPos = Math.abs(equipPos) == 10 ? 11 : 10;
            Equip otherEquip = (Equip) chr.getEquippedInventory().getItemBySlot(otherEquipPos);
            if (otherEquip != null) {
                zeroSync.accept(otherEquip);
                otherEquip.recalcEnchantmentStats();
                otherEquip.updateToChar(chr);
            }
        }
    }

    private record EditableStat(String display, EquipBaseStat baseStat) {
    }
}
