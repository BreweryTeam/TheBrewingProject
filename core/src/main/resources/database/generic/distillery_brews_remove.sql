DELETE
FROM distillery_brews
WHERE unique_x = ?
  AND unique_y = ?
  AND unique_z = ?
  AND world_uuid = ?
  AND pos = ?
  AND is_distillate = ?;