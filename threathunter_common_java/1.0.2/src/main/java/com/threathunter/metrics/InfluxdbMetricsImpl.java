package com.threathunter.metrics;

import com.threathunter.metrics.model.LegendData;
import com.threathunter.metrics.util.InfluxdbPool;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Serie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * created by www.threathunter.cn
 */
public class InfluxdbMetricsImpl implements Metrics{
    private static final Logger logger = LoggerFactory.getLogger(InfluxdbMetricsImpl.class);
    private InfluxdbPool influxdbPool;

    public InfluxdbMetricsImpl(String url, String username, String password) {
        this.influxdbPool = new InfluxdbPool(url, username, password);
    }


    public void addMetrics(String db, String metricsName, Map<String, Object> tags, Double value, int expireSeconds) {
        InfluxDB influxDB = null;
        try {
            influxDB = this.influxdbPool.getResource();
            influxDB.write(db, TimeUnit.MILLISECONDS, this.getEventSerie(metricsName, tags, value));
        } catch (Exception e) {
            logger.error(String.format("network:error in writing metrics, name: %s, tags: %s, value: %f, expire seconds: %d", metricsName, tags.toString(), value, expireSeconds), e);
        } finally {
            this.returnInfluxdbResource(influxDB);
        }
    }

    private Serie getEventSerie(String metricsName, Map<String, Object> tags, Double value) {
        int count = tags.size() + 1;
        String[] columns = new String[count];
        Object[] values = new Object[count];

        int index = 0;
        for (Map.Entry<String, Object> tag : tags.entrySet()) {
            columns[index] = tag.getKey();
            values[index] = tag.getValue();
            index++;
        }
        columns[index] = "value";
        values[index] = value;
        Serie serie = new Serie.Builder(metricsName).columns(columns).values(values).build();
        return serie;
    }

    public List<LegendData> query(String db, String metricsName, String aggregationType, long timeStart, long timeEnd, int timeInterval, Map<String, List<Object>> filterTags, String... groupTags) {
        InfluxDB influxDB = null;
        String queryString;
        List<Serie> series = null;
        try {
            influxDB = influxdbPool.getResource();
            queryString = generateQueryString(metricsName, aggregationType, timeStart - 1, timeEnd, timeInterval, filterTags, groupTags);
            series = influxDB.query(db, queryString, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            logger.error("network:error in retriving row metrics from influxdb", e);
        } finally {
            this.returnInfluxdbResource(influxDB);
        }

        if (series != null && series.size() > 0) {
            Map<String, LegendData> legendDataMap = new HashMap<>();
            for (Map<String, Object> row : series.get(0).getRows()) {
                Map<String, Object> legendsMap = new HashMap<>();
                StringBuilder stringBuilder = new StringBuilder();
                Double value = null;
                Long ts = null;
                for (Map.Entry<String, Object> entry : row.entrySet()) {
                    if (entry.getKey().equals(aggregationType)) {
                        value = (Double)entry.getValue();
                        continue;
                    } else if (entry.getKey().equals("time")) {
                        ts = ((Double)entry.getValue()).longValue();
                        continue;
                    } else {
                        stringBuilder.append(entry.getValue() + ".");
                        legendsMap.put(entry.getKey(), entry.getValue());
                    }
                }

                if (!legendDataMap.containsKey(stringBuilder.toString())) {
                    LegendData legendData = new LegendData();
                    legendData.setLegend(legendsMap);
                    legendData.setTsValues(new HashMap<>());
                    legendDataMap.put(stringBuilder.toString(), legendData);
                }
                legendDataMap.get(stringBuilder.toString()).getTsValues().put(ts, value);
            }
            return new ArrayList<>(legendDataMap.values());
        }
        return null;
    }

    private String generateQueryString(String metricsName, String aggregationType, long timeStart, long timeEnd, int timeInterval, Map<String, List<Object>> filterTags, String[] groupTags) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("select %s(value) from \"%s\"", aggregationType, metricsName));
        stringBuilder.append(" where");
        stringBuilder.append(String.format(" time > %dms and time < %dms", timeStart, timeEnd));
        if (filterTags != null) {
            for (Map.Entry<String, List<Object>> tag : filterTags.entrySet()) {
                // for random access linkedlist will be ineffecient
                int index = 0;
                stringBuilder.append(" and (");
                for (Object obj : tag.getValue()) {
                    if (index > 0) {
                        stringBuilder.append(" or ");
                    }
                    stringBuilder.append(String.format("\"%s\" = \'%s\'", tag.getKey(), obj.toString()));
                    index++;
                }
                stringBuilder.append(")");
            }
        }
        stringBuilder.append(" group by ");
        if (groupTags != null && groupTags.length > 0) {
            for (String groupTag : groupTags) {
                stringBuilder.append(groupTag + ", ");
            }
        }
        stringBuilder.append(String.format("time(%ds)", timeInterval));
        return stringBuilder.toString();
    }

    private void returnInfluxdbResource(InfluxDB influxDB) {
        if (influxDB != null) {
            try {
                this.influxdbPool.returnResource(influxDB);
            } catch (Exception e) {
                this.influxdbPool.returnBrokenResource(influxDB);
            }
        }
    }
}
