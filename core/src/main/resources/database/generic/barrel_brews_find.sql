SELECT brew, pos
FROM barrel_brews
WHERE unique_x = ?
  AND unique_y = ?
  AND unique_z = ?
  AND world_uuid = ?;