package bot.soupbowl.api;

import bot.soupbowl.config.application.ApplicationConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface ApplicationManager {

    int MAX_QUESTIONS = 5;

    @NotNull
    ApplicationConfig getConfig();

}
