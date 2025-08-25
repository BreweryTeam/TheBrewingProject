package dev.jsinco.brewery.moment;

/**
 * @param moment A moment without any timestamp
 */
public record PassedMoment(long moment) implements Moment {
    @Override
    public Interval withLastStep(long lastStep) {
        return new Interval(lastStep - moment, lastStep);
    }

    @Override
    public Interval withMovedEnding(long newEnd) {
        return new Interval(newEnd - moment, newEnd);
    }
}
