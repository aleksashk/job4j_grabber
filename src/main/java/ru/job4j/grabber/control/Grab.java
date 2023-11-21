package ru.job4j.grabber.control;

import org.quartz.SchedulerException;

public interface Grab {
    void init() throws SchedulerException;
}
