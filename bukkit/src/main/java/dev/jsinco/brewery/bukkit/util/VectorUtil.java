package dev.jsinco.brewery.bukkit.util;

import dev.jsinco.brewery.api.vector.BreweryVector;
import org.joml.Matrix3d;
import org.joml.Vector3d;
import org.joml.Vector3i;

public class VectorUtil {

    /**
     * Will not mutate the initial vector
     *
     * @param initial
     * @param transformation
     * @return A new transformed vector
     */
    public static Vector3i transform(Vector3i initial, Matrix3d transformation) {
        Vector3d transformedVector = transformation.transform(new Vector3d(initial));
        return new Vector3i(
                (int) transformedVector.x(),
                (int) transformedVector.y(),
                (int) transformedVector.z()
        );
    }

    public static Vector3i toJoml(BreweryVector vector) {
        return new Vector3i(vector.x(), vector.y(), vector.z());
    }

    public static BreweryVector toBreweryVector(Vector3i vector) {
        return new BreweryVector(vector.x(), vector.y(), vector.z());
    }
}
