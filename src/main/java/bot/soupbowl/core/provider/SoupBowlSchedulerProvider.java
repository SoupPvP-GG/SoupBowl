package bot.soupbowl.core.provider;

import bot.soupbowl.api.Scheduler;
import bot.soupbowl.api.model.RepeatingRunnable;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.TimerTask;

public class SoupBowlSchedulerProvider implements Scheduler {

    /**
     * Register a {@link RepeatingRunnable}.
     *
     * @param runnable Runnable to be registered.
     * @param delay    Delay before the runnable starts.
     * @param period   Period pattern of which the runnable repeats.
     * @return {@link RepeatingRunnable} instance.
     */
    @Override
    public @NotNull RepeatingRunnable runRepeatingRunnable(@NotNull RepeatingRunnable runnable, long delay, long period) {
        new TaskThread(runnable, delay, period).start();
        return runnable;
    }

    @RequiredArgsConstructor
    private class TaskThread extends Thread {

        private final RepeatingRunnable runnable;
        private final long delay;
        private final long period;

        @Override
        public void run() {
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new Task(runnable), delay, period);
        }
    }

    @RequiredArgsConstructor
    private class Task extends TimerTask {

        private final RepeatingRunnable runnable;

        /**
         * The action to be performed by this timer task.
         */
        @Override
        public void run() {
            runnable.execute();
        }
    }
}
