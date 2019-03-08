package com.threathunter.basictools.eslogger;

import com.threathunter.babel.meta.ServiceMeta;
import com.threathunter.babel.rpc.Service;
import com.threathunter.basictools.eslogger.util.DiskFullException;
import com.threathunter.basictools.eslogger.util.DiskUsageChecker;
import com.threathunter.basictools.eslogger.util.EsloggerMetricsHelper;
import com.threathunter.model.Event;
import com.threathunter.model.EventMeta;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.joda.time.DateTime;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by daisy on 2015/9/17.
 */
public class EsloggerService implements Service {

    private Client client;
    private ServiceMeta serviceMeta;
    private LogLevel logLevel;

    private DiskUsageChecker diskUsageChecker;
    private ScheduledExecutorService scheduledExecutorService;
    private volatile boolean writeAvailable = true;

    public EsloggerService(Map<String, Object> esConfigMap, Object serviceJsonObject) {
        this.serviceMeta = ServiceMeta.from_json_object(serviceJsonObject);

        this.diskUsageChecker = new DiskUsageChecker((String)esConfigMap.get("disk_metrics_db"),
                (String)esConfigMap.get("disk_metrics_name"), (Double)esConfigMap.get("disk_usage_threshold"), (String)esConfigMap.get("es_ip"));
        this.scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
        this.scheduledExecutorService.scheduleAtFixedRate(() -> checkWritable(), 0, 60, TimeUnit.SECONDS);;

        try {
            Settings settings = Settings.settingsBuilder()
                    .put("cluster.name", (String)esConfigMap.get("es_node_name")).build();
            this.client = TransportClient.builder().settings(settings).build().addTransportAddress(new InetSocketTransportAddress(
                    InetAddress.getByName((String)esConfigMap.get("es_ip")), (Integer)esConfigMap.get("es_port")));
        } catch (UnknownHostException e) {
            throw new ElasticsearchException(e);
        }

        this.logLevel = LogLevel.valueOf((String)esConfigMap.get("es_level"));
    }

    public Event process(Event event) {
        Map<String, Object> properties = event.getPropertyValues();

        String level = (String)properties.get("level");
        if (LogLevel.valueOf(level.toLowerCase()).compareTo(logLevel) < 0) {
            return null;
        }

        String index = null;
        String type = null;
        if (writeAvailable) {
            try {
                DateTime dateTime = new DateTime(event.getTimestamp());
                index = String.format("%s_%s", properties.get("index"), dateTime.toString("yyyy-MM-dd"));
                type = (String) properties.get("type");
                String json = (String) properties.get("payload");
                this.client.prepareIndex(index, type).setSource(json).execute().actionGet();
                EsloggerMetricsHelper.getInstance().addIncomeLoggerMetrics(index, type);
            } catch (Exception e) {
                EsloggerMetricsHelper.getInstance().addErrorMetrics(index, type, e);
            }
        } else {
            EsloggerMetricsHelper.getInstance().addErrorMetrics(index, type, new DiskFullException("reject write log, disk almost full."));
        }
        return null;
    }

    public ServiceMeta getServiceMeta() {
        return this.serviceMeta;
    }

    public EventMeta getRequestEventMeta() {
        return null;
    }

    public EventMeta getResponseEventMeta() {
        return null;
    }

    @Override
    public void close() {
        client.close();
    }

    private void checkWritable() {
        try {
            this.writeAvailable = this.diskUsageChecker.ifDiskWritable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
