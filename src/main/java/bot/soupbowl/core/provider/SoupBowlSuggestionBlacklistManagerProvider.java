package bot.soupbowl.core.provider;

import bot.soupbowl.api.SuggestionBlacklistManager;
import bot.soupbowl.api.model.SuggestionBlacklistEntry;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class SoupBowlSuggestionBlacklistManagerProvider implements SuggestionBlacklistManager {

    private final Map<String, SuggestionBlacklistEntry> entries = Maps.newHashMap();
    private Connection connection;

    @SneakyThrows
    public SoupBowlSuggestionBlacklistManagerProvider() {
        File file = new File("data", "suggestions_blacklist.db");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }

        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        CompletableFuture.runAsync(() -> {
            if (connection != null) {
                // Make table if it doesn't exist
                try {
                    PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS blacklist_entries (id VARCHAR(255) PRIMARY KEY, reason LONGTEXT, staff VARCHAR(255), time BIGINT)");
                    statement.executeUpdate();
                    statement.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        CompletableFuture.runAsync(() -> {
            if (connection != null) {
                try {
                    PreparedStatement statement = connection.prepareStatement("SELECT * FROM blacklist_entries");
                    ResultSet set = statement.executeQuery();
                    while (set.next()) {
                        String id = set.getString("id");
                        String reason = set.getString("reason");
                        String staff = set.getString("staff");
                        long time = set.getLong("time");
                        SuggestionBlacklistEntry entry = new SuggestionBlacklistEntry(id, reason, staff, time);
                        entries.put(id, entry);
                    }
                    statement.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public @NotNull SuggestionBlacklistEntry createBlacklist(@NotNull String id, @Nullable String reason, @NotNull String staff, long date) {
        SuggestionBlacklistEntry entry = new SuggestionBlacklistEntry(id, reason, staff, date);
        addBlacklistEntry(entry);
        entries.putIfAbsent(id, entry);
        return entry;
    }

    @Override
    public @Nullable SuggestionBlacklistEntry getBlacklist(@NotNull String id) {
        return entries.getOrDefault(id, null);
    }

    @Override
    public void removeBlacklist(@NotNull SuggestionBlacklistEntry entry) {
        removeBlacklistEntry(entry);
        entries.remove(entry.id());
    }

    @Override
    public boolean isBlacklisted(@NotNull String id) {
        return entries.containsKey(id);
    }

    @Override
    public @NotNull Collection<SuggestionBlacklistEntry> getBlacklists() {
        return Collections.unmodifiableCollection(entries.values());
    }

    @Override
    public CompletableFuture<Void> addBlacklistEntry(@NotNull SuggestionBlacklistEntry entry) {
        return CompletableFuture.runAsync(() -> {
            if (connection != null) {
                try {
                    PreparedStatement statement = connection.prepareStatement("INSERT INTO blacklist_entries (id, reason, staff, time) VALUES (?, ?, ?, ?)");
                    statement.setString(1, entry.id());
                    statement.setString(2, entry.reason());
                    statement.setString(3, entry.staff());
                    statement.setLong(4, entry.date());
                    statement.executeUpdate();
                    statement.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public CompletableFuture<Void> removeBlacklistEntry(@NotNull SuggestionBlacklistEntry entry) {
        return CompletableFuture.runAsync(() -> {
            if (connection != null) {
                try {
                    PreparedStatement statement = connection.prepareStatement("DELETE FROM blacklist_entries WHERE id = ?");
                    statement.setString(1, entry.id());
                    statement.executeUpdate();
                    statement.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public CompletableFuture<SuggestionBlacklistEntry> getBlacklistEntry(@NotNull String id) {
        return CompletableFuture.supplyAsync(() -> {
            if (connection != null) {
                try {
                    PreparedStatement statement = connection.prepareStatement("SELECT * FROM blacklist_entries WHERE id = ?");
                    statement.setString(1, id);
                    ResultSet result = statement.executeQuery();
                    if (result.next()) {
                        String reason = result.getString("reason");
                        String staff = result.getString("staff");
                        long date = result.getLong("time");

                        SuggestionBlacklistEntry entry = new SuggestionBlacklistEntry(id, reason, staff, date);
                        result.close();
                        statement.close();
                        return entry;
                    }
                    result.close();
                    statement.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<Collection<String>> keys() {
        return CompletableFuture.supplyAsync(() -> {
            if (connection != null) {
                try {
                    PreparedStatement statement = connection.prepareStatement("SELECT id FROM blacklist_entries");
                    ResultSet result = statement.executeQuery();
                    Collection<String> keys = Lists.newArrayList();
                    while (result.next()) {
                        keys.add(result.getString("id"));
                    }
                    result.close();
                    statement.close();
                    return keys;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            return Collections.emptyList();
        });
    }
}
