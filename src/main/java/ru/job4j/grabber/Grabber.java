package ru.job4j.grabber;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import ru.job4j.grabber.control.Grab;
import ru.job4j.grabber.extractor.Parse;
import ru.job4j.grabber.model.Post;
import ru.job4j.grabber.store.PsqlStore;
import ru.job4j.grabber.store.Store;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;
import static ru.job4j.grabber.HabrCareerParse.loadProperties;

public class Grabber implements Grab {
    private final Parse parse;
    private final Store store;
    private final Scheduler scheduler;
    private final int time;

    private final Properties cfg;

    public Grabber(Properties cfg) throws SchedulerException {
        this.cfg = cfg;
        this.parse = new HabrCareerParse(new HabrCareerDateTimeParser());
        this.store = new PsqlStore(cfg);
        this.scheduler = StdSchedulerFactory.getDefaultScheduler();
        this.time = Integer.parseInt(cfg.getProperty("time"));
    }

    @Override
    public void init() throws SchedulerException {
        JobDataMap data = new JobDataMap();
        data.put("store", store);
        data.put("parse", parse);
        JobDetail job = newJob(GrabJob.class)
                .usingJobData(data)
                .build();
        SimpleScheduleBuilder times = simpleSchedule()
                .withIntervalInSeconds(time)
                .repeatForever();
        Trigger trigger = newTrigger()
                .startNow()
                .withSchedule(times)
                .build();
        scheduler.scheduleJob(job, trigger);
    }

    public static class GrabJob implements Job {
        @Override
        public void execute(JobExecutionContext context) {
            JobDataMap map = context.getJobDetail().getJobDataMap();
            Store store = (Store) map.get("store");
            Parse parse = (Parse) map.get("parse");

            Properties properties = loadProperties();
            processVacancyPages(store, parse, properties);
        }

        private static void processVacancyPages(Store store, Parse parse, Properties properties) {
            int numberOfPage = Integer.parseInt(properties.getProperty("habr.number.pages", "1"));
            String sourceLink = properties.getProperty("source.link", "https://career.habr.com");
            String sourcePrefix = properties.getProperty("source.prefix", "/vacancies?page=");
            String sourceSuffix = properties.getProperty("source.suffix", "&q=Java%20developer&type=all");

            for (int i = 1; i <= numberOfPage; i++) {
                String fullLink = "%s%s%d%s".formatted(sourceLink, sourcePrefix, i, sourceSuffix);
                List<Post> posts = parseVacancies(parse, fullLink);
                savePosts(store, posts);
            }
        }

        private static List<Post> parseVacancies(Parse parse, String url) {
            return parse.list(url);
        }

        private static void savePosts(Store store, List<Post> posts) {
            posts.forEach(store::save);
        }
    }

    public void web(Store store) {
        new Thread(() -> {
            try (ServerSocket server = new ServerSocket(Integer.parseInt(cfg.getProperty("port")))) {
                while (!server.isClosed()) {
                    Socket socket = server.accept();
                    try (OutputStream out = socket.getOutputStream()) {
                        out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
                        for (Post post : store.getAll()) {
                            out.write(post.toString().getBytes(Charset.forName("Windows-1251")));
                            out.write(System.lineSeparator().getBytes());
                        }
                    } catch (IOException io) {
                        io.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) throws Exception {
        Properties cfg = new Properties();
        try (InputStream in = Grabber.class.getClassLoader().getResourceAsStream("app.properties")) {
            cfg.load(in);
        }
        Grabber grab = new Grabber(cfg);
        grab.init();
        grab.web(grab.store);
    }
}