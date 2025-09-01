UPDATE drunk_states_v2
SET kicked_timestamp = ?,
    time_stamp = ?
WHERE player_uuid = ?;