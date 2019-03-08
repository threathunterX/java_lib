package com.threathunter.metrics.model;

import java.util.Map;

/**
 * Created by daisy on 2015/6/3.
 */
public class LegendData {
    private Map<String, Object> legend;
    private Map<Long, Double> tsValues;

    public LegendData() {}
    public LegendData(Map<String, Object> legend, Map<Long, Double> tsValues) {
        this.legend = legend;
        this.tsValues = tsValues;
    }

    public Map<String, Object> getLegend() {
        return legend;
    }

    public void setLegend(Map<String, Object> legend) {
        this.legend = legend;
    }

    public Map<Long, Double> getTsValues() {
        return tsValues;
    }

    public void setTsValues(Map<Long, Double> tsValues) {
        this.tsValues = tsValues;
    }

    @Override
    public String toString() {
        return String.format("legend: %s, data: %s", legend.toString(), tsValues.toString());
    }
}
