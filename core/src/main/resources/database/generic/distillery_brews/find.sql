SELECT pos, is_distillate, brew
FROM distillery_brews
WHERE unique_x = ?
  AND unique_y = ?
  AND unique_z = ?
  AND world_uuid = ?;