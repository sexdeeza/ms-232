valid = True

while valid and not violetCube.isComplete():
    selection = sm.sendNext(violetCube.getSelectionText())
    if not violetCube.select(selection):
        sm.sendSayOkay("Invalid selection.")
        valid = False

if valid:
    sm.sendSayOkay(violetCube.apply(chr))
