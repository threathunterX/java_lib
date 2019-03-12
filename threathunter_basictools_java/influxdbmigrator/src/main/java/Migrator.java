import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Serie;
import org.influxdb.dto.ShardSpace;
import sun.security.provider.SHA;

import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

/**
 * created by www.threathunter.cn
 */
public class Migrator {
    private static ResourceBundle resourceBundle = ResourceBundle.getBundle("config");
    public static void main(String[] args) {
        String metricsUrlFrom = resourceBundle.getString("influxdb_from");
        String metricsUrlTo = resourceBundle.getString("influxdb_to");

        InfluxDB influxDBFrom = InfluxDBFactory.connect(metricsUrlFrom, "root", "influxdbthreathunter");
//        InfluxDB influxDBTo = InfluxDBFactory.connect(metricsUrlTo, "root", "influxdbthreathunter");

        List<ShardSpace> shardSpaceList = influxDBFrom.getShardSpaces();

        for (ShardSpace shardSpace : shardSpaceList) {
            System.out.println(shardSpace.toString());
        }
    }
}
