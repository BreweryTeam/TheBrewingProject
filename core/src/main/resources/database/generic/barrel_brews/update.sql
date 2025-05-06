UPDATE barrel_brews
SET brew = ?
WHERE unique_x = ?
  AND unique_y = ?
  AND unique_z = ?
  AND world_uuid = ?
  AND pos = ?