package dev.jsinco.brewery.api.config

public interface Configuration {

  Locale language();

  Barrels barrels();

  Cauldrons cauldrons();

  interface Barrels {

    long agingYearTicks();
  }

  interface Cauldrons {

    long cookingMinuteTicks();
  }
}
