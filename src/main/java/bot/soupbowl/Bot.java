package bot.soupbowl;

import bot.soupbowl.api.Scheduler;
import bot.soupbowl.api.SoupBowlAPI;
import bot.soupbowl.api.command.CommandMap;
import bot.soupbowl.api.command.SlashCommand;
import bot.soupbowl.commands.CommandPing;
import bot.soupbowl.commands.CommandReload;
import bot.soupbowl.commands.suggestions.CommandSuggest;
import bot.soupbowl.commands.suggestions.CommandSuggestions;
import bot.soupbowl.config.SoupConfig;
import bot.soupbowl.core.provider.CommandMapProvider;
import bot.soupbowl.core.provider.SoupBowlAPIProvider;
import bot.soupbowl.listeners.SlashCommandListener;
import bot.soupbowl.listeners.SuggestionModalListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Getter
public class Bot {
    @Getter
    private static Bot instance;

    private final JDA jda;

    private final SoupBowlAPI api;
    private SoupConfig config = null;
    private final CommandMap commandMap;

    @SneakyThrows
    public Bot() {
        instance = this;

        // Config loader
        File file = new File("config", "main.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
        if (!file.exists()) {
            file.getParentFile().mkdir();
            file.createNewFile();

            this.config = new SoupConfig();
            saveConfig(file, gson);
        }

        try (FileReader reader = new FileReader(file)) {
            config = gson.fromJson(reader, SoupConfig.class);
            saveConfig(file, gson);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JDABuilder builder = JDABuilder.create(config.getBotToken(), List.of(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES));

        builder.setStatus(OnlineStatus.ONLINE);
        this.api = new SoupBowlAPIProvider();
        this.commandMap = new CommandMapProvider();
        builder.addEventListeners(
                new SlashCommandListener(this.commandMap),
                new SuggestionModalListener()
        );

        registerServerCommand("953360052784336896", new CommandPing());
        registerServerCommand("953360052784336896", new CommandSuggest(this.api));
        registerServerCommand("953360052784336896", new CommandSuggestions(this.api));
        registerServerCommand("953360052784336896", new CommandReload(builder));

        jda = builder.build().awaitReady();
        initializeCommands(jda);

        Scheduler scheduler = api.getScheduler();
    }

    @SneakyThrows
    private void saveConfig(File file, Gson gson) {
        Writer writer = new FileWriter(file, false);
        gson.toJson(this.config, writer);
        writer.flush();
        writer.close();
    }

    /**
     * Register a {@link SlashCommand} as a global command
     * @param command {@link SlashCommand} instance
     * @apiNote This may take up to an hour for Discord to register it!
     */
    public void registerGlobalCommand(@NotNull SlashCommand command) {
        commandMap.registerGlobalCommand(command.getName(), command);
    }

    /**
     * Register a {@link SlashCommand} as a server command
     * @param serverID {@link Guild} ID
     * @param command {@link SlashCommand} instance
     * @apiNote This should register almost instantly!
     */
    public void registerServerCommand(@NotNull String serverID, @NotNull SlashCommand command) {
        commandMap.registerServerCommand(serverID, command.getName(), command);
    }

    /**
     * Initalize all the commands in the {@link CommandMap} to Discord
     * @apiNote This should be called after {@link JDABuilder#build()#awaitReady()}
     * @param jda {@link JDA} instance
     */
    @SuppressWarnings("all")
    public void initializeCommands(@NotNull JDA jda) {
        // Global Commands
        Collection<SlashCommand> globalCommands = commandMap.getGlobalCommands();
        CommandListUpdateAction commands = jda.updateCommands();

        globalCommands.forEach(command -> {
            if (!command.getAliases().isEmpty()) {
                command.getAliases().forEach(name -> {
                    SlashCommandData commandData = new CommandDataImpl(name, command.getDescription());
                    Optional.ofNullable(command.getData()).ifPresent(data -> data.accept(commandData));
//                    if (!command.getSubCommands().isEmpty()) {
//                        command.getSubCommands().forEach(subCommand -> {
//                            if (!subCommand.getAliases().isEmpty()) {
//                                subCommand.getAliases().forEach(subName -> {
//                                    SubcommandData subcommandData = new SubcommandData(subName, subCommand.getDescription());
//                                    Optional.ofNullable(subCommand.getData()).ifPresent(data -> data.accept(subcommandData));
//                                    commandData.addSubcommands(subcommandData);
//                                });
//                            }
//
//                            SubcommandData subcommandData = new SubcommandData(subCommand.getName(), subCommand.getDescription());
//                            Optional.ofNullable(subCommand.getData()).ifPresent(data -> data.accept(subcommandData));
//                            commandData.addSubcommands(subcommandData);
//                        });
//                    }
                    System.out.println("[Command Registry] Registered Global Command `" + commandData.getName() +"`");
                    commands.addCommands(commandData);
                });
            }

            SlashCommandData commandData = new CommandDataImpl(command.getName(), command.getDescription());
            Optional.ofNullable(command.getData()).ifPresent(data -> data.accept(commandData));
//            if (!command.getSubCommands().isEmpty()) {
//                command.getSubCommands().forEach(subCommand -> {
//                    if (!subCommand.getAliases().isEmpty()) {
//                        subCommand.getAliases().forEach(name -> {
//                            SubcommandData subcommandData = new SubcommandData(name, subCommand.getDescription());
//                            Optional.ofNullable(subCommand.getData()).ifPresent(data -> data.accept(subcommandData));
//                            commandData.addSubcommands(subcommandData);
//                        });
//                    }
//
//                    SubcommandData subcommandData = new SubcommandData(subCommand.getName(), subCommand.getDescription());
//                    Optional.ofNullable(subCommand.getData()).ifPresent(data -> data.accept(subcommandData));
//                    commandData.addSubcommands(subcommandData);
//                });
//            }
            System.out.println("[Command Registry] Registered Global Command `" + commandData.getName() +"`");
            commands.addCommands(commandData);
        });

        commands.queue();

        // Server Bound Commands
        commandMap.getAllServerCommands().entrySet().stream().filter(serverEntry -> jda.getGuildById(serverEntry.getKey()) != null).forEach(serverEntry -> {
            Guild guild = jda.getGuildById(serverEntry.getKey());
            assert guild != null;
            CommandListUpdateAction guildCommands = guild.updateCommands();

            Collection<SlashCommand> serverCommands = serverEntry.getValue();
            serverCommands.forEach(command -> {
                if (!command.getAliases().isEmpty()) {
                    command.getAliases().forEach(name -> {
                        SlashCommandData commandData = new CommandDataImpl(name, command.getDescription());
                        Optional.ofNullable(command.getData()).ifPresent(data -> data.accept(commandData));
//                        if (!command.getSubCommands().isEmpty()) {
//                            command.getSubCommands().forEach(subCommand -> {
//                                if (!subCommand.getAliases().isEmpty()) {
//                                    subCommand.getAliases().forEach(subName -> {
//                                        SubcommandData subcommandData = new SubcommandData(subName, subCommand.getDescription());
//                                        Optional.ofNullable(subCommand.getData()).ifPresent(data -> data.accept(subcommandData));
//                                        commandData.addSubcommands(subcommandData);
//                                    });
//                                }
//
//                                SubcommandData subcommandData = new SubcommandData(subCommand.getName(), subCommand.getDescription());
//                                Optional.ofNullable(subCommand.getData()).ifPresent(data -> data.accept(subcommandData));
//                                commandData.addSubcommands(subcommandData);
//                            });
//                        }
                        System.out.println("[Command Registry] Registered Server Command `" + commandData.getName()
                                + "` to Guild `" + guild.getName() + "`");
                        guildCommands.addCommands(commandData);
                    });
                }

                SlashCommandData commandData = new CommandDataImpl(command.getName(), command.getDescription());
                Optional.ofNullable(command.getData()).ifPresent(data -> data.accept(commandData));
//                if (!command.getSubCommands().isEmpty()) {
//                    command.getSubCommands().forEach(subCommand -> {
//                        if (!subCommand.getAliases().isEmpty()) {
//                            subCommand.getAliases().forEach(name -> {
//                                SubcommandData subcommandData = new SubcommandData(name, subCommand.getDescription());
//                                Optional.ofNullable(subCommand.getData()).ifPresent(data -> data.accept(subcommandData));
//                                commandData.addSubcommands(subcommandData);
//                            });
//                        }
//
//                        SubcommandData subcommandData = new SubcommandData(subCommand.getName(), subCommand.getDescription());
//                        Optional.ofNullable(subCommand.getData()).ifPresent(data -> data.accept(subcommandData));
//                        commandData.addSubcommands(subcommandData);
//                    });
//                }
                System.out.println("[Command Registry] Registered Server Command `" + commandData.getName()
                        + "` to Guild `" + guild.getName() + "`");
                guildCommands.addCommands(commandData);
            });

            guildCommands.queue();

        });

    }

    public void reloadConfig() {
        File file = new File("config", "main.json");
        // Should not be possible but whatever.
        if (!file.exists())
            return;

        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
        try (FileReader reader = new FileReader(file)) {
            config = gson.fromJson(reader, SoupConfig.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
