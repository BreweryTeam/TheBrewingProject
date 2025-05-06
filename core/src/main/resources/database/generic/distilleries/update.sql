UPDATE distilleries
SET start_time    = ?
WHERE unique_x = ?
  AND unique_y = ?
  AND unique_z = ?
  AND world_uuid = ?;