package com.threathunter.basictools.accumulation;

import org.apache.commons.dbcp.BasicDataSource;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Serie;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * created by www.threathunter.cn
 */
public class HistoryAccumulationHelper {
    private BasicDataSource dataSource = null;
    InfluxDB influxDB = null;
    Random random = new Random();

    private Double totalFactor = 1.0;
    private Double riskFactor = 1.0;

    public HistoryAccumulationHelper() {
        influxDB = InfluxDBFactory.connect(ConfigHelper.getInstance().getInfluxdbUrl(), ConfigHelper.getInstance().getInfluxdbUser(), ConfigHelper.getInstance().getInfluxdbPass());
        dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl(ConfigHelper.getInstance().getMysqlUrl());
        dataSource.setUsername(ConfigHelper.getInstance().getMysqlUser());
        dataSource.setPassword(ConfigHelper.getInstance().getMysqlPassword());

        this.totalFactor = ConfigHelper.getInstance().getTotalFactor();
        this.riskFactor = ConfigHelper.getInstance().getRiskFactor();

        try {
            Connection connection = dataSource.getConnection();
            String sql = "CREATE TABLE IF NOT EXISTS "+ ConfigHelper.getInstance().getMysqlTableName() +
                    " (id INT NOT NULL AUTO_INCREMENT, name VARCHAR(10) NOT NULL, count BIGINT NOT NULL, " +
                    " createts BIGINT NOT NULL, updatets BIGINT NOT NULL, PRIMARY KEY (id));";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // get data when start and then run every day
        initialMysqlData();
    }

    public HistoryCount getHistoryTotal() {
        long current = System.currentTimeMillis();
        HistoryCount historyCount = getHistoryCountFromMysql("total");
        if (current > historyCount.timestamp) {
            Double remaining = getRemainingTotalFromInfluxdb(historyCount.timestamp, System.currentTimeMillis()) * totalFactor + random.nextInt(totalFactor.intValue());
            historyCount.count += remaining.longValue();
            historyCount.timestamp = current;
        }
        return historyCount;
    }

    public HistoryCount getHistoryRisk() {
        long current = System.currentTimeMillis();
        HistoryCount historyCount = getHistoryCountFromMysql("risk");
        if (current > historyCount.timestamp) {
            Double remaining = getRemainingRiskFromInfluxdb(historyCount.timestamp, System.currentTimeMillis()) * riskFactor + random.nextInt(riskFactor.intValue());
            historyCount.count += remaining.longValue();
            historyCount.timestamp = current;
        }
        return historyCount;
    }

    private void initialMysqlData() {
        long current = System.currentTimeMillis();
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            String sql = String.format("select * from %s where name = \'%s\';", ConfigHelper.getInstance().getMysqlTableName(), "total");
            connection = dataSource.getConnection();
            statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                HistoryCount total = getHistoryTotal();
                HistoryCount risk = getHistoryRisk();

                updateMysql("total", total.count, total.timestamp);
                updateMysql("risk", risk.count, risk.timestamp);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        insertIntoMysql("total", getRemainingTotalFromInfluxdb(-1, current) * ConfigHelper.getInstance().getResizeFactor(), current);
        insertIntoMysql("risk", getRemainingRiskFromInfluxdb(-1, current) * ConfigHelper.getInstance().getResizeFactor(), current);
    }

    public void everyDayMysqlTask() {
        HistoryCount total = getHistoryTotal();
        HistoryCount risk = getHistoryRisk();

        updateMysql("total", total.count, total.timestamp);
        updateMysql("risk", risk.count, risk.timestamp);
    }

    private HistoryCount getHistoryCountFromMysql(String name) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            HistoryCount historyCount = new HistoryCount();
            String sql = String.format("select * from %s where name = \'%s\';", ConfigHelper.getInstance().getMysqlTableName(), name);
            connection = dataSource.getConnection();
            statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                historyCount.count = rs.getLong("count");
                historyCount.timestamp = rs.getLong("updatets");
                return historyCount;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private void insertIntoMysql(String name, long count, long createts) {
        Connection connection = null;
        PreparedStatement statement = null;
        try{
            String sql = String.format("insert into %s (name, count, createts, updatets) values (\'%s\', \'%d\', \'%d\', \'%d\');",
                    ConfigHelper.getInstance().getMysqlTableName(), name, count, createts, createts);
            connection = dataSource.getConnection();
            statement = connection.prepareStatement(sql);
            statement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateMysql(String name, long count, long updatets) {
        Connection connection = null;
        PreparedStatement statement = null;
        try{
            String sql = String.format("update %s set updatets = %d, count = %d where name = \'%s\';",
                    ConfigHelper.getInstance().getMysqlTableName(), updatets, count, name);
            connection = dataSource.getConnection();
            statement = connection.prepareStatement(sql);
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private long getRemainingTotalFromInfluxdb(long fromTimestampInMillis, long toTimestampInMillis) {
        String query;
        if (fromTimestampInMillis < 0) {
            query = String.format("select sum(value) from auth_pv where time < %dms and time > now()-7d", toTimestampInMillis);
        } else {
            query = String.format("select sum(value) from auth_pv where time > %dms and time < %dms",fromTimestampInMillis, toTimestampInMillis);
        }
        List<Serie> series = influxDB.query("ip_risk_repo", query, TimeUnit.MILLISECONDS);
        if (series.size() <= 0) {
            return 0;
        }
        Serie result = series.get(0);
        return ((Double)result.getRows().get(0).get("sum")).longValue();
    }

    private long getRemainingRiskFromInfluxdb(long fromTimestampInSec, long toTimestampInSec) {
        String query;
        if (fromTimestampInSec < 0) {
            query = String.format("select sum(value) from auth_pv where time < %dms and time > now()-7d and (risk_score_scope=\'20-50\' or risk_score_scope=\'50-80\'or risk_score_scope=\'80-100\')",
                    toTimestampInSec);
        } else {
            query = String.format("select sum(value) from auth_pv where time > %dms and time < %dms and (risk_score_scope=\'20-50\' or risk_score_scope=\'50-80\'or risk_score_scope=\'80-100\')",
                    fromTimestampInSec, toTimestampInSec);
        }
        List<Serie> series = influxDB.query("ip_risk_repo", query, TimeUnit.MILLISECONDS);
        if (series.size() <= 0) {
            return 0;
        }
        Serie result = series.get(0);
        return ((Double)result.getRows().get(0).get("sum")).longValue();
    }

    class HistoryCount {
        long count;
        long timestamp;
    }
}
