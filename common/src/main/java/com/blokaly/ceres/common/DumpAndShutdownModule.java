package com.blokaly.ceres.common;

import com.google.inject.AbstractModule;
import com.netflix.governator.ShutdownHookModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

public class DumpAndShutdownModule  extends AbstractModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(DumpAndShutdownModule.class);

    @Singleton
    public static class ThreadDumpHook extends Thread {
        @Inject
        public ThreadDumpHook() {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> LOGGER.info(generateThreadDump())));
        }
    }

    @Override
    protected void configure() {
        install(new ShutdownHookModule());
        bind(ThreadDumpHook.class).asEagerSingleton();
    }

    private static String generateThreadDump() {
        final StringBuilder dump = new StringBuilder();
        final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        final ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(threadMXBean.getAllThreadIds(), 100);
        for (ThreadInfo threadInfo : threadInfos) {
            if (threadInfo != null) {
                dumpThreadInfo(dump, threadInfo);
            }
        }
        return dump.toString();
    }

    private static void dumpThreadInfo(StringBuilder dump, ThreadInfo threadInfo) {
        dump.append('"');
        dump.append(threadInfo.getThreadName());
        dump.append("\" ");
        final Thread.State state = threadInfo.getThreadState();
        dump.append("\n   java.lang.Thread.State: ");
        dump.append(state);
        final StackTraceElement[] stackTraceElements = threadInfo.getStackTrace();
        for (final StackTraceElement stackTraceElement : stackTraceElements) {
            dump.append("\n        at ");
            dump.append(stackTraceElement);
        }
        dump.append("\n\n");
    }
}
