package bot.soupbowl.core.provider;

import bot.soupbowl.api.SoupBowlAPI;
import bot.soupbowl.api.Scheduler;
import bot.soupbowl.api.SuggestionBlacklistManager;

public class SoupBowlAPIProvider implements SoupBowlAPI {

    private final Scheduler scheduler;
    private final SuggestionBlacklistManager suggestionBlacklistManager;

    public SoupBowlAPIProvider() {
        this.scheduler = new SoupBowlSchedulerProvider();
        this.suggestionBlacklistManager = new SoupBowlSuggestionBlacklistManagerProvider();
    }


    /**
     * Get the {@link Scheduler} instance
     *
     * @return {@link Scheduler}
     */
    @Override
    public Scheduler getScheduler() {
        return scheduler;
    }

    @Override
    public SuggestionBlacklistManager getSuggestionBlacklistManager() {
        return suggestionBlacklistManager;
    }

}
