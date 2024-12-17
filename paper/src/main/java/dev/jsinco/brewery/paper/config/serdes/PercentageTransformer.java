package dev.jsinco.brewery.paper.config.serdes;

import dev.jsinco.brewery.api.config.Percentage;
import eu.okaeri.configs.schema.GenericsPair;
import eu.okaeri.configs.serdes.BidirectionalTransformer;
import eu.okaeri.configs.serdes.SerdesContext;
import org.jetbrains.annotations.NotNull;

public class PercentageTransformer extends BidirectionalTransformer<String, Percentage> {

    @Override
    public GenericsPair<String, Percentage> getPair() {
        return this.genericsPair(String.class, Percentage.class);
    }

    @Override
    public Percentage leftToRight(@NotNull String data, @NotNull SerdesContext serdesContext) {
        return new Percentage(data);
    }

    @Override
    public String rightToLeft(Percentage data, @NotNull SerdesContext serdesContext) {
        return data.toString();
    }

}