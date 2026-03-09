from java.util import LinkedHashSet

from net.swordie.ms.client.jobs.legend import SkillStealManager
from net.swordie.ms.connection.packet import UserLocal
from net.swordie.ms.constants import JobConstants
from net.swordie.ms.loaders import SkillData


def ordered_skill_set(skills):
    skill_set = LinkedHashSet()
    for skill in skills:
        skill_set.add(skill)
    return skill_set


def get_job_skills(job_id, root_jobs):
    skills = []
    for root_job in root_jobs:
        skills += SkillData.getSkillsByJob(root_job)
    return (job_id, ordered_skill_set(skills))


job_entries = [
    ("Hero", get_job_skills(112, [100, 110, 111, 112])),
    ("Paladin", get_job_skills(122, [100, 120, 121, 122])),
    ("Dark Knight", get_job_skills(132, [100, 130, 131, 132])),
    ("Fire/Poison", get_job_skills(212, [200, 210, 211, 212])),
    ("Ice/Lightning", get_job_skills(222, [200, 220, 221, 222])),
    ("Bishop", get_job_skills(232, [200, 230, 231, 232])),
    ("Bowmaster", get_job_skills(312, [300, 310, 311, 312])),
    ("Marksman", get_job_skills(322, [300, 320, 321, 322])),
    ("Pathfinder", get_job_skills(332, [301, 330, 331, 332])),
    ("Night Lord", get_job_skills(412, [400, 410, 411, 412])),
    ("Shadower", get_job_skills(422, [400, 420, 421, 422])),
    ("Blade Master", get_job_skills(434, [430, 431, 432, 433, 434])),
    ("Buccaneer", get_job_skills(512, [500, 510, 511, 512])),
    ("Corsair", get_job_skills(522, [500, 520, 521, 522])),
    ("Cannon Master", get_job_skills(532, [501, 530, 531, 532])),
]

hyper_skill_ids = []
for _, (job_id, _) in job_entries:
    for skill in SkillData.getSkillsByJob(job_id):
        skill_info = SkillData.getSkillInfoById(skill.getSkillId())
        if skill_info is not None and skill_info.getHyper() == 2:
            hyper_skill_ids.append(skill.getSkillId())
hyper_skill_ids = sorted(set(hyper_skill_ids))


def open_job_skill_ui(job_id, skills):
    chr.write(UserLocal.resultStealSkillList(skills, 4, 1, job_id))
    sm.dispose()


def open_hyper_skill_ui():
    if len(hyper_skill_ids) == 0:
        sm.sendSayOkay("No stealable hyper skills were found.")
        sm.dispose()
        return

    text = "#e<Hyper Skills>#n\r\nSelect a hyper skill to steal directly at max level.\r\n#b\r\n"
    for idx, skill_id in enumerate(hyper_skill_ids):
        text += "#L{}##s{}##q{}##l\r\n".format(idx, skill_id, skill_id)
    selection = sm.sendNext(text)
    if selection < 0 or selection >= len(hyper_skill_ids):
        sm.dispose()
        return

    skill_id = hyper_skill_ids[selection]
    skill = SkillData.getSkillDeepCopyById(skill_id)
    if skill is None:
        sm.sendSayOkay("That hyper skill could not be loaded.")
        sm.dispose()
        return

    skill_level = skill.getMasterLevel()
    stole = SkillStealManager.tryAddStolenSkill(chr, skill_id, skill_level, skill_level)
    if stole:
        sm.sendSayOkay("Stole #s{}##q{}# at max level.".format(skill_id, skill_id))
    sm.dispose()


if not JobConstants.isPhantom(chr.getJob()):
    sm.sendSayOkay("This skill is only available to Phantoms.")
    sm.dispose()
else:
    menu = "#e<Skill Swipe>#n\r\nChoose a job to preview its maxed stealable skills, or open the Hyper Skills list.\r\n#b\r\n"
    menu += "#L0#Reset my stolen skills#l\r\n"
    menu += "#L1#Hyper Skills#l\r\n"

    for idx, (label, _) in enumerate(job_entries, start=2):
        menu += "#L{}#{}#l\r\n".format(idx, label)

    selection = sm.sendNext(menu)

    if selection == 0:
        if sm.sendAskYesNo("Reset all stolen skills?"):
            SkillStealManager.resetStolenSkills(chr)
        sm.dispose()
    elif selection == 1:
        open_hyper_skill_ui()
    elif 2 <= selection < 2 + len(job_entries):
        job_id, skills = job_entries[selection - 2][1]
        open_job_skill_ui(job_id, skills)
    else:
        sm.dispose()
