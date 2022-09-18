package bot.soupbowl.api;

import bot.soupbowl.api.model.SuggestionBlacklistEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public interface SuggestionBlacklistManager {

    @NotNull
    SuggestionBlacklistEntry createBlacklist(@NotNull String id, @Nullable String reason, @NotNull String staff, long date);

    @Nullable
    SuggestionBlacklistEntry getBlacklist(@NotNull String id);

    void removeBlacklist(@NotNull SuggestionBlacklistEntry entry);

    boolean isBlacklisted(@NotNull String id);

    @NotNull
    Collection<SuggestionBlacklistEntry> getBlacklists();

    CompletableFuture<Void> addBlacklistEntry(@NotNull SuggestionBlacklistEntry entry);

    CompletableFuture<Void> removeBlacklistEntry(@NotNull SuggestionBlacklistEntry entry);

    CompletableFuture<SuggestionBlacklistEntry> getBlacklistEntry(@NotNull String id);

    CompletableFuture<Collection<String>> keys();
}
