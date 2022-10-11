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

    /**
     * Get the {@link Scheduler} instance
     * @return {@link Scheduler}
     */
    Scheduler getScheduler();


    SuggestionBlacklistManager getSuggestionBlacklistManager();

    ApplicationManager getApplicationManager();
}
