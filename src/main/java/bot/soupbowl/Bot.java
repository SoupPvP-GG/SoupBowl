package bot.soupbowl;

import bot.soupbowl.api.Scheduler;
import bot.soupbowl.api.SoupBowlAPI;
import bot.soupbowl.commands.CommandAnnounce;
import bot.soupbowl.commands.CommandPing;
import bot.soupbowl.commands.CommandReload;
import bot.soupbowl.commands.application.CommandApply;
import bot.soupbowl.commands.suggestions.CommandSuggest;
import bot.soupbowl.commands.suggestions.CommandSuggestions;
import bot.soupbowl.config.EmbedsConfig;
import bot.soupbowl.config.SoupConfig;
import bot.soupbowl.core.provider.SoupBowlAPIProvider;
import bot.soupbowl.listeners.ApplicationSelectMenuListener;
import bot.soupbowl.listeners.SuggestionModalListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import games.negative.framework.discord.DiscordBot;
import games.negative.framework.discord.config.json.JSONConfigManager;
import lombok.Getter;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.util.List;

@Getter
public class Bot extends DiscordBot {
    @Getter
    private static Bot instance;

    private final JDA jda;

    private final SoupBowlAPI api;
    private SoupConfig config = null;
    private EmbedsConfig embedsConfig;

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

        JDABuilder builder = create(config.getBotToken(), List.of(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES));

        builder.setStatus(OnlineStatus.ONLINE);
        this.api = new SoupBowlAPIProvider();
        builder.addEventListeners(
                new SuggestionModalListener(),
                new ApplicationSelectMenuListener(api.getApplicationManager())
        );

        registerServerCommand("953360052784336896", new CommandPing());
        registerServerCommand("953360052784336896", new CommandSuggest(this.api));
        registerServerCommand("953360052784336896", new CommandSuggestions(this.api));
        registerServerCommand("953360052784336896", new CommandReload(builder));
        registerServerCommand("953360052784336896", new CommandApply(api.getApplicationManager()));
        registerServerCommand("953360052784336896", new CommandAnnounce(builder));

        jda = builder.build().awaitReady();
        initializeCommands(jda);

        Scheduler scheduler = api.getScheduler();

        JSONConfigManager json = getJsonConfigManager();
        this.embedsConfig = json.loadOrCreate("config", "embeds", EmbedsConfig.class, new EmbedsConfig(), null);
    }


    @SneakyThrows
    private void saveConfig(File file, Gson gson) {
        Writer writer = new FileWriter(file, false);
        gson.toJson(this.config, writer);
        writer.flush();
        writer.close();
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
