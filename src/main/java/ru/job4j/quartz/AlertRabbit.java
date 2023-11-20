package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class AlertRabbit {
    public static void main(String[] args) {
        Properties properties = loadProperties();
        try (Connection connection = DriverManager.getConnection(
                properties.getProperty("db.url"),
                properties.getProperty("db.username"),
                properties.getProperty("db.password")
        )) {

            int interval = Integer.parseInt(properties.getProperty("rabbit.interval"));
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("connection", connection);
            JobDetail job = newJob(Rabbit.class)
                    .usingJobData(jobDataMap)
                    .build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(interval)
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            int runtimeApp = Integer.parseInt(properties.getProperty("application.runtime", "10000"));
            Thread.sleep(runtimeApp);
            scheduler.shutdown();
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    public static class Rabbit implements Job {
        @Override
        public void execute(JobExecutionContext context) {
            Connection connection = (Connection) context.getJobDetail().getJobDataMap().get("connection");
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO rabbit (created_date) VALUES (?)")) {
                statement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                statement.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream in = AlertRabbit.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

}
