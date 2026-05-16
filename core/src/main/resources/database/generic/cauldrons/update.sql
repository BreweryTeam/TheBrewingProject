UPDATE cauldrons
SET brew = ?, cauldron_type = ?
WHERE cauldron_x = ?
  AND cauldron_y = ?
  AND cauldron_z = ?
  AND world_uuid = ?;