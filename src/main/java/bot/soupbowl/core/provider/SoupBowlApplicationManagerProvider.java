package bot.soupbowl.core.provider;

import bot.soupbowl.api.ApplicationManager;
import bot.soupbowl.api.ApplicationSubmissionType;
import bot.soupbowl.api.model.ApplicationSubmissionProcess;
import bot.soupbowl.config.application.ApplicationConfig;
import bot.soupbowl.config.application.ApplicationEntry;
import bot.soupbowl.config.application.ApplicationQuestion;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;

public class SoupBowlApplicationManagerProvider implements ApplicationManager {

    private ApplicationConfig config;

    @SneakyThrows
    public SoupBowlApplicationManagerProvider() {
        File file = new File("config/applications.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
        if (!file.exists()) {
            file.createNewFile();

            HashMap<String, ApplicationEntry> applications = Maps.newHashMap();
            applications.put("test", new ApplicationEntry("Test", "This is a test application", "🧪", new ArrayList<>() {{
                add(new ApplicationQuestion("name", "What is your name?", "John Doe", TextInputStyle.SHORT, true, 1, 32));
                add(new ApplicationQuestion("age", "What is your age?", "18", TextInputStyle.SHORT, true, 1, 3));
            }}, new ApplicationSubmissionProcess(
                    ApplicationSubmissionType.SEND_TO_CHANNEL,
                    "channel-id-here"
            )));

            config = new ApplicationConfig(applications);

            try (Writer writer = new FileWriter(file)) {
                gson.toJson(config, writer);
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try(Reader reader = new FileReader(file)) {
                config = gson.fromJson(reader, ApplicationConfig.class);
                reader.close();

                try (Writer writer = new FileWriter(file)) {
                    gson.toJson(config, writer);
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to load applications.json file", e);
            }

        }
    }

    @Override
    public void reloadConfig() {
        File file = new File("config/applications.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

        try(Reader reader = new FileReader(file)) {
            config = gson.fromJson(reader, ApplicationConfig.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load applications.json file", e);
        }
    }

    @NotNull
    @Override
    public ApplicationConfig getConfig() {
        return config;
    }

}
