SELECT alcohol_level, toxin_level, kicked_timestamp, time_stamp
FROM drunk_states_v2
WHERE player_uuid = ?;