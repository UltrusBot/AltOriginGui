package me.ultrusmods.altorigingui.gui;

import io.github.apace100.origins.origin.Origin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public enum SortMode{
    IMPACT("altorigingui.sortmode.impact", (origins) -> {
        var copyArr = new ArrayList<>(origins);
        copyArr.sort((o1, o2) -> {
            int impDelta = o1.getImpact().getImpactValue() - o2.getImpact().getImpactValue();
            return impDelta == 0 ? o1.getOrder() - o2.getOrder() : impDelta;
        });
        return copyArr;
    }),
    NAME("altorigingui.sortmode.name", (origins) -> origins.stream().sorted(Comparator.comparing(origin -> origin.getIdentifier().getPath())).collect(Collectors.toList())),
    NAMESPACE("altorigingui.sortmode.namespace", (origins) -> origins.stream().sorted(Comparator.comparing(origin -> origin.getIdentifier().getNamespace())).collect(Collectors.toList()));

    private final SortFunction sortFunction;
    private final String translationKey;

    SortMode(String translationKey, SortFunction sortFunction) {
        this.sortFunction = sortFunction;
        this.translationKey = translationKey;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public List<Origin> sort(List<Origin> origins) {
        return sortFunction.sort(origins);
    }



    @FunctionalInterface
    public interface SortFunction {
        List<Origin> sort(List<Origin> origins);
    }
}
