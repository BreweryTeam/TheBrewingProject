package dev.jsinco.brewery.api.util.color;

import java.io.IOException;
import java.io.InputStream;

@FunctionalInterface
public interface InputStreamSupplier {

    InputStream get() throws IOException;
}
