UPDATE drunk_states
SET alcohol_level    = ?,
    toxin_level      = ?,
    kicked_timestamp = ?,
    time_stamp = ?
WHERE player_uuid = ?;