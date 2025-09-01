UPDATE drunk_states_v2
SET alcohol_level    = ?,
    toxin_level      = ?,
    kicked_timestamp = ?,
    time_stamp = ?
WHERE player_uuid = ?;