package bot.soupbowl.core.provider;

import bot.soupbowl.api.ApplicationManager;
import bot.soupbowl.api.SoupBowlAPI;
import bot.soupbowl.api.SuggestionBlacklistManager;

public class SoupBowlAPIProvider implements SoupBowlAPI {

    private final SuggestionBlacklistManager suggestionBlacklistManager;
    private final ApplicationManager applicationManager;

    public SoupBowlAPIProvider() {
        this.suggestionBlacklistManager = new SoupBowlSuggestionBlacklistManagerProvider();
        this.applicationManager = new SoupBowlApplicationManagerProvider();
    }


    @Override
    public SuggestionBlacklistManager getSuggestionBlacklistManager() {
        return suggestionBlacklistManager;
    }

    @Override
    public ApplicationManager getApplicationManager() {
        return applicationManager;
    }

}
