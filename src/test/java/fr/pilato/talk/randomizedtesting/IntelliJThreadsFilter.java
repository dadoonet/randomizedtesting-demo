package fr.pilato.talk.randomizedtesting;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Predicate;

/**
 * This filter is only needed when running the tests from IntelliJ
 */
public class IntelliJThreadsFilter implements Predicate<Thread> {
    private static final Logger LOGGER = LogManager.getLogger();
    public boolean test(Thread t) {
        boolean intellijThreads = t.getName().startsWith("JMX server") || t.getName().startsWith("RMI TCP Connection");
        if (intellijThreads) {
            LOGGER.warn("Detected IntelliJ threads [{}], if you are running the tests from IntelliJ, " +
                            "you can ignore this warning or add [{}] to the thread leak filters",
                    t.getName(), IntelliJThreadsFilter.class.getSimpleName());
        }
        return intellijThreads;
    }
}
