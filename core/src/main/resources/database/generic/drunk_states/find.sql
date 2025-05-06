SELECT alcohol_level, toxin_level, kicked_timestamp, time_stamp
FROM drunk_states
WHERE player_uuid = ?;