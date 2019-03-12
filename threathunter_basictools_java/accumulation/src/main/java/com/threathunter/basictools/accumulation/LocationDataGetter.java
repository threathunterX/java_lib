package com.threathunter.basictools.accumulation;

import com.threathunter.geo.GeoUtil;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.joda.time.DateTime;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * created by www.threathunter.cn
 */
public class LocationDataGetter {
    private String ip;
    private int port;

    public LocationDataGetter(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public Map<String, Map<String, AtomicInteger>> getLocationTopoJson(String index_prefix, String types, long startTimeInSec, long periodInSec) {
        String index1 = getSearchIndex(index_prefix, startTimeInSec);
        String index2 = getSearchIndex(index_prefix, startTimeInSec + periodInSec);
        Map<String, Map<String, AtomicInteger>> map;

        long startTimeMillis = startTimeInSec * 1000;
        long endTimeMillis = startTimeMillis + periodInSec * 1000;
        if (index1.equals(index2)) {
            map = getAllLineMapFromEs(new String[]{index1}, types.split(","), startTimeMillis, endTimeMillis);
        } else {
            map = getAllLineMapFromEs(new String[]{index1, index2}, types.split(","), startTimeMillis, endTimeMillis);
        }
        return map;
    }

    // because we will run once a day, so need need to keep the client
    private Map<String, Map<String, AtomicInteger>> getAllLineMapFromEs(String[] index, String[] types, long startTimestamp, long endTimestamp) {
        TransportClient client = new TransportClient().addTransportAddress(new InetSocketTransportAddress(ip, port));

        Map<String, Map<String, AtomicInteger>> lineMap = new HashMap<>();

        System.out.println(String.format("[%s] search from es...", DateTime.now().toString()));
        String[] fields = {"mobile", "ip", "srcip"};
        SearchResponse scrollResponse = client.prepareSearch(index).setTypes(types)
                .setSearchType(SearchType.SCAN)
                .setScroll(new TimeValue(60000))
                .setSize(1000)
                .setPostFilter(FilterBuilders.orFilter(FilterBuilders.existsFilter("ip"), FilterBuilders.existsFilter("mobile")))
                .setPostFilter(FilterBuilders.rangeFilter("@timestamp").from(startTimestamp).to(endTimestamp))
                .addFields(fields)
                .execute().actionGet();

        try {
            do {
                for (SearchHit hit : scrollResponse.getHits().getHits()) {
                    SearchHitField scrip = hit.field("srcip");
                    SearchHitField ip = hit.field("ip");
                    SearchHitField mobile = hit.field("mobile");

                    String localFrom;
                    String localTo;

                    if (scrip == null || scrip.value().toString().trim().equals("")) {
                        continue;
                    }

                    boolean ipExist = true;
                    if (ip == null || ip.value().toString().trim().equals("")) {
                        ipExist = false;
                        if (mobile == null || mobile.value().toString().trim().equals("")) {
                            continue;
                        }
                    }
                    try {
                        if (ipExist) {
                            localFrom = GeoUtil.getCNIPCity(ip.value().toString().trim());
                        } else {
                            localFrom = GeoUtil.getCNPhoneCity(mobile.value().toString().trim());
                        }
                        localTo = GeoUtil.getCNIPCity(scrip.value().toString().trim());
                    } catch (Exception e) {
                        System.out.println(String.format("[Error] invalid ip or mobile, from: ip %s, mobile %s, to: ip %s, ipexist: %b", ip.value(), mobile.value(), scrip.value(), ipExist));
                        continue;
                    }

                    if (localFrom == null) {
                        localFrom = "unknown";
                    }
                    if (localTo == null) {
                        localTo = "unknown";
                    }

                    if (!lineMap.containsKey(localTo)) {
                        lineMap.put(localTo, new HashMap<>());
                    }
                    Map<String, AtomicInteger> map = lineMap.get(localTo);
                    if (!map.containsKey(localFrom)) {
                        map.put(localFrom, new AtomicInteger());
                    }
                    map.get(localFrom).incrementAndGet();
                }
                scrollResponse = client.prepareSearchScroll(scrollResponse.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
            } while (scrollResponse.getHits().getHits().length > 0);
        } finally {
            client.close();
        }

        return lineMap;
    }

    private String getSearchIndex(String index_prefix, long startTimestampInSec) {
        long dateLong = startTimestampInSec * 1000;
        DateTime dateTime = new DateTime(dateLong);
        return String.format("%s_%s", index_prefix, dateTime.toString("yyyy-MM-dd"));
    }
}
