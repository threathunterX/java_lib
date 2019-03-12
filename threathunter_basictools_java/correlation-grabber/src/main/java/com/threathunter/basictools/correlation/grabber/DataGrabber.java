package com.threathunter.basictools.correlation.grabber;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.joda.time.DateTime;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.search.SearchHit;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * created by www.threathunter.cn
 */
public class DataGrabber {
    private final String ip;
    private final Integer port;
    private final String prefix;
    private final String types;
    private final Integer before;
    private final String directory;

    public DataGrabber(String ip, Integer port, String prefix, String types, Integer before, String directory) {
        this.ip = ip;
        this.port = port;
        this.prefix = prefix;
        this.types = types;
        this.before = before;
        this.directory = directory;
    }

    public void getRelationFile() {
        TransportClient client = new TransportClient().addTransportAddress(new InetSocketTransportAddress(ip, port));
        String[] fields = {"mobile", "ip"};
        String index = getPreviousDayIndex(prefix, before);
        SearchResponse scrollResponse = client.prepareSearch(index).setTypes(types.split(","))
                .setSearchType(SearchType.SCAN)
                .setScroll(new TimeValue(60000))
                .setSize(1000)
                .setPostFilter(FilterBuilders.boolFilter().must(FilterBuilders.existsFilter("ip"), FilterBuilders.existsFilter("mobile")))
                .addFields(fields)
                .execute().actionGet();

        BufferedWriter bw = null;
        try {
            if (!directory.isEmpty()) {
                bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(directory+index+".tmp")));
            } else {
                bw = new BufferedWriter(new OutputStreamWriter(System.out));
            }
            int count = 0;
            do {
                for (SearchHit hit : scrollResponse.getHits().getHits()) {
                    for (String field : fields) {
                        try {
                            bw.write(hit.field(field).value().toString());
                            bw.write(" ");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    bw.newLine();
                    count++;
                    if (count % 20000 == 0) {
                        bw.flush();
                    }
                }
                scrollResponse = client.prepareSearchScroll(scrollResponse.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
            } while(scrollResponse.getHits().getHits().length != 0);
            bw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.flush();
                    bw.close();
                } catch (Exception e) {
                    System.exit(-1);
                }
            }
        }
    }

    private String getPreviousDayIndex(String indexPrefix, int daysdatabefore) {
        long someday = System.currentTimeMillis() - 86400000 * daysdatabefore;
        DateTime dateTime = new DateTime(someday);
        String index = String.format("%s_%s", indexPrefix, dateTime.toString("yyyy-MM-dd"));
        return index;
    }

    public static void main(String[] args) {
        OptionParser parser = new OptionParser();
        parser.accepts( "ip" ).withRequiredArg().ofType(String.class).defaultsTo("127.0.0.1").required();
        parser.accepts( "port" ).withRequiredArg().ofType(Integer.class).defaultsTo(9300).required();
        parser.accepts( "prefix" ).withRequiredArg().ofType(String.class).defaultsTo("corrlation_logs").required();
        parser.accepts( "types" ).withRequiredArg().ofType(String.class).defaultsTo("redq").required();
        parser.accepts( "before" ).withRequiredArg().ofType(Integer.class).defaultsTo(0);
        parser.accepts( "d" ).withRequiredArg().ofType(String.class).defaultsTo("");

        DataGrabber grabber = null;
        try {
            OptionSet options = parser.parse(args);
            String ip = (String)options.valueOf("ip");
            Integer port = (Integer)options.valueOf("port");
            String prefix = (String)options.valueOf("prefix");
            String types = (String)options.valueOf("types");
            Integer before = (Integer)options.valueOf("before");
            String directory = (String)options.valueOf("d");
            grabber = new DataGrabber(ip, port, prefix, types, before, directory);
        } catch (Exception ex) {
            System.err.println("Invalid command");
            try {
                parser.printHelpOn(System.err);
            } catch (IOException ignore) {
            }
            System.exit(1);
        }

        grabber.getRelationFile();
    }
}
