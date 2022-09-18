package bot.soupbowl.core.provider;

import bot.soupbowl.api.command.CommandMap;
import bot.soupbowl.api.command.SlashCommand;
import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CommandMapProvider implements CommandMap {

    private final HashMap<String, SlashCommand> globalCommands = Maps.newHashMap();
    private final HashMap<String, HashMap<String, SlashCommand>> serverCommands = Maps.newHashMap();

    @Override
    public void registerGlobalCommand(@NotNull String name, @NotNull SlashCommand command) {
        globalCommands.putIfAbsent(name, command);
    }

    @Override
    public void registerServerCommand(@NotNull String serverID, @NotNull String name, @NotNull SlashCommand command) {
        HashMap<String, SlashCommand> serverCommandMap = serverCommands.get(serverID);
        if (serverCommandMap == null)
            serverCommandMap = new HashMap<>();

        serverCommandMap.putIfAbsent(name, command);

        if (serverCommands.containsKey(serverID))
            serverCommands.replace(serverID, serverCommandMap);
        else
            serverCommands.put(serverID, serverCommandMap);
    }

    @Override
    public @NotNull Collection<SlashCommand> getGlobalCommands() {
        return globalCommands.values();
    }

    @Override
    public @NotNull Collection<SlashCommand> getServerCommands(@NotNull String serverID) {
        Optional<Map.Entry<String, HashMap<String, SlashCommand>>> first = serverCommands.entrySet().stream()
                .filter(commandEntry -> commandEntry.getKey().equalsIgnoreCase(serverID)).findFirst();

        if (first.isEmpty())
            return Collections.emptyList();

        Map.Entry<String, HashMap<String, SlashCommand>> commandEntries = first.get();
        return commandEntries.getValue().values();
    }

    @Override
    public HashMap<String, Collection<SlashCommand>> getAllServerCommands() {
        HashMap<String, Collection<SlashCommand>> allCommands = new HashMap<>();
        serverCommands.forEach((label, commands) -> allCommands.putIfAbsent(label, commands.values()));
        return allCommands;
    }
}
