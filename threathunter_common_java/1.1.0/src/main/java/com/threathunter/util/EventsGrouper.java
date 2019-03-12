package com.threathunter.util;

import com.threathunter.model.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * created by www.threathunter.cn
 */
public class EventsGrouper {
    private final TreeMap<Long, List<Event>> timedGroupMap = new TreeMap<>();

    private final int groupTime;

    private long minTimestamp = 0;
    private long maxTimestamp = 0;

    public EventsGrouper(int groupTime) {
        this.groupTime = groupTime;
    }

    public void addEvents(final List<Event> events) {
        events.forEach(event -> addEvent(event));
    }

    public void addEvent(final Event event) {
        Long key = event.getTimestamp() / this.groupTime * this.groupTime;
        if (key < minTimestamp) {
            // ignore expired events
            if (event.getName() != null) {
                MetricsHelper.getInstance().addMetrics("grouper.events.expire.count", 1.0);
            }
            return;
        }

        if (key > maxTimestamp) {
            maxTimestamp = key;
        }

        timedGroupMap.computeIfAbsent(key, k -> new ArrayList<>(200)).add(event);
        if (event.getName() != null) {
            MetricsHelper.getInstance().addMetrics("grouper.events.cache.count", 1.0);
        }
    }

    public List<Event> getNextEventsGroup() {
        // new min timestamp
//        System.out.println("come to get events");
        this.minTimestamp = this.maxTimestamp / this.groupTime * this.groupTime - this.groupTime;

        if (!this.timedGroupMap.isEmpty()) {
            if (this.timedGroupMap.firstKey() < minTimestamp) {
                return this.timedGroupMap.pollFirstEntry().getValue();
            }
        }
        return new ArrayList<>();
    }
}
