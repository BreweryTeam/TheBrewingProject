PRAGMA foreign_keys = OFF;

DELETE
FROM barrel_brews
WHERE (unique_x, unique_y, unique_z, world_uuid) NOT IN (SELECT unique_x, unique_y, unique_z, world_uuid FROM barrels);

DELETE
FROM distillery_brews
WHERE (unique_x, unique_y, unique_z, world_uuid) NOT IN (SELECT unique_x, unique_y, unique_z, world_uuid FROM distilleries);
PRAGMA foreign_keys = ON;