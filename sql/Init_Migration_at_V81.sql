CREATE TABLE migration_history (`version` INT NOT NULL DEFAULT 0, `script_name` VARCHAR(255) NULL, `executed_at` DATETIME NULL);
INSERT INTO migration_history(version, script_name, executed_at) values (81, 'V81_AddColumn_CharacterStats_CashPetPickUpOn.sql', 260322000000);
