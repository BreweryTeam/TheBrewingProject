DELETE FROM mixers
WHERE cauldron_x = ?
  AND cauldron_y = ?
  AND cauldron_z = ?
  AND world_uuid = ?;