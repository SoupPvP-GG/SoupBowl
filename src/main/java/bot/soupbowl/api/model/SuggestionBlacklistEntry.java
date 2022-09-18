package bot.soupbowl.api.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record SuggestionBlacklistEntry(@NotNull String id, @Nullable String reason, @NotNull String staff, long date) {

}
