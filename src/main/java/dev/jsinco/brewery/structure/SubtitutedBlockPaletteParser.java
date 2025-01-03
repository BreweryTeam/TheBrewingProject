package dev.jsinco.brewery.structure;

import dev.thorinwasher.schem.blockpalette.BlockPaletteParser;
import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;

import java.util.Set;

public class SubtitutedBlockPaletteParser implements BlockPaletteParser {

    private final Set<String> patterns;
    private final String target;

    public SubtitutedBlockPaletteParser(Set<String> patterns, String target) {
        this.patterns = patterns;
        this.target = target;
    }

    @Override
    public BlockData parse(String materialsString) {
        return Bukkit.createBlockData(SubstitutionUtils.substituteWithTarget(materialsString, target, patterns));
    }
}
