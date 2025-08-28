package dev.jsinco.brewery.api.moment;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public sealed interface Moment permits Interval, PassedMoment {

    int SECOND = 20;
    int MINUTE = SECOND * 60;
    int DEFAULT_AGING_YEAR = MINUTE * 20; //TODO: make this a setting

    Serializer SERIALIZER = new Serializer();

    /**
     * @return A time representing this moment instance
     */
    long moment();

    /**
     * Can change the result of {@link #moment()}
     *
     * @param lastStep A last step time
     * @return A new interval with a new last step property
     */
    Interval withLastStep(long lastStep);

    /**
     * Does not change the result of {@link #moment()}
     *
     * @param newStart A new start time
     * @return A new interval with a moved ending
     */
    Interval withMovedEnding(long newStart);

    class Serializer {

        public JsonElement serialize(Moment moment) {
            return switch (moment) {
                case Interval(long start, long end) -> {
                    JsonArray array = new JsonArray();
                    array.add(new JsonPrimitive(start));
                    array.add(new JsonPrimitive(end));
                    yield array;
                }
                case PassedMoment(long time) -> new JsonPrimitive(time);
            };
        }

        public Moment deserialize(JsonElement json) {
            if (json instanceof JsonPrimitive primitive) {
                return new PassedMoment(primitive.getAsLong());
            }
            JsonArray array = json.getAsJsonArray();
            return new Interval(array.get(0).getAsLong(), array.get(1).getAsLong());
        }
    }
}
