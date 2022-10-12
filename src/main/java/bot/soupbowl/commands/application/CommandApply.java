package bot.soupbowl.commands.application;

import bot.soupbowl.api.ApplicationManager;
import bot.soupbowl.config.application.ApplicationEntry;
import games.negative.framework.discord.command.SlashCommand;
import games.negative.framework.discord.command.SlashInfo;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;

import java.util.Map;

@RequiredArgsConstructor
@SlashInfo(name = "apply", description = "Apply for something!")
public class CommandApply extends SlashCommand {

    private final ApplicationManager manager;

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {

        SelectMenu.Builder menu = SelectMenu.create("applications");
        for (Map.Entry<String, ApplicationEntry> entry : manager.getConfig().getApplications().entrySet()) {
            String key = entry.getKey();
            ApplicationEntry config = entry.getValue();

            menu.addOption(config.getName(), "app:" + key, config.getDescription());
        }

        event.reply("Select an application to apply for!")
                .setEphemeral(true)
                .addActionRow(menu.build())
                .queue();
    }
}
