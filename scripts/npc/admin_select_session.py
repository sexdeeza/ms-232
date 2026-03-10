if not session.isValid():
    sm.sendSayOkay("No valid lines were found for that item.")
    sm.dispose()
else:
    while not session.isComplete():
        sel = sm.sendNext(session.getSelectionText())
        if not session.select(sel):
            sm.sendSayOkay("Invalid selection.")
            sm.dispose()
            break

    if session.isComplete():
        sm.sendSayOkay(session.apply(chr))
        sm.dispose()
