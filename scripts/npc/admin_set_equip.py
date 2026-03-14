def run_selection(subsession, empty_text):
    if not subsession.isValid():
        sm.sendSayOkay(empty_text)
        return

    while not subsession.isComplete():
        sel = sm.sendNext(subsession.getSelectionText())
        if not subsession.select(sel):
            sm.sendSayOkay("Invalid selection.")
            return

    sm.sendSayOkay(subsession.apply(chr))


if not session.isValid():
    sm.sendSayOkay("There is no equip on that position.")
    sm.dispose()
else:
    action = sm.sendNext(session.getMenuText())

    if action == 0:
        if not session.hasEditableStats():
            sm.sendSayOkay("No editable stats were found for that equip.")
        else:
            stat_sel = sm.sendNext(session.getStatSelectionText())
            if not session.isValidStatIndex(stat_sel):
                sm.sendSayOkay("Invalid stat selection.")
            else:
                current_value = session.getStatValue(stat_sel)
                max_value = session.getStatMaxValue(stat_sel)
                value = sm.sendAskNumber(
                    "Set {} to what value?".format(session.getStatDisplay(stat_sel)),
                    current_value,
                    0,
                    max_value,
                )
                sm.sendSayOkay(session.applyStat(chr, stat_sel, value))
    elif action == 1:
        value = sm.sendAskNumber(
            "Set remaining enhancements for this equip:",
            session.getRemainingEnhancements(),
            0,
            session.getRemainingEnhancementsMax(),
        )
        sm.sendSayOkay(session.applyRemainingEnhancements(chr, value))
    elif action == 2:
        run_selection(session.createPotentialSession(False), "No valid potential lines were found for that item.")
    elif action == 3:
        run_selection(session.createPotentialSession(True), "No valid bonus potential lines were found for that item.")
    elif action == 4:
        if not session.canEditFlames():
            sm.sendSayOkay("That equip cannot have flames.")
        else:
            run_selection(session.createFlameSession(), "No valid flame lines were found for that item.")
    elif action == 5:
        value = sm.sendAskNumber(
            "Set Star Force for this equip:",
            session.getStarForce(),
            0,
            session.getStarForceMax(),
        )
        sm.sendSayOkay(session.applyStarForce(chr, value))
    else:
        sm.sendSayOkay("Invalid selection.")

    sm.dispose()
