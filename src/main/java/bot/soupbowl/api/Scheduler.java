package bot.soupbowl.api;

import bot.soupbowl.api.model.RepeatingRunnable;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the management class for {@link RepeatingRunnable}
 */
public interface Scheduler {

    /**
     * Register a {@link RepeatingRunnable}.
     * @param runnable Runnable to be registered.
     * @param delay Delay before the runnable starts.
     * @param period Period pattern of which the runnable repeats.
     * @return {@link RepeatingRunnable} instance.
     */
    @NotNull
    RepeatingRunnable runRepeatingRunnable(@NotNull RepeatingRunnable runnable, long delay, long period);

}
