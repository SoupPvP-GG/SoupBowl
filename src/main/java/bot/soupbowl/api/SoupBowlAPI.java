package bot.soupbowl.api;

/**
 * Represents the bridge between classes and the rest of the API.
 */
public interface SoupBowlAPI {

//    /**
//     * Get the {@link MongoManager} instance
//     * @return {@link MongoManager}
//     */
//    @NotNull
//    MongoManager getMongoManager();
//


    SuggestionBlacklistManager getSuggestionBlacklistManager();

    ApplicationManager getApplicationManager();
}
