package bot.soupbowl.commands;

import bot.soupbowl.Bot;
import bot.soupbowl.api.command.SlashCommand;
import bot.soupbowl.api.command.SlashInfo;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

@SlashInfo(name = "reload", description = "Reloads the configuration files for the bot!")
public class CommandReload extends SlashCommand {

    public CommandReload(JDABuilder builder) {
        setData(commandData -> {
            commandData.setDefaultPermissions(DefaultMemberPermissions.enabledFor(
                    Permission.ADMINISTRATOR
            ));
        });

        builder.addEventListeners(
                new ButtonListener()
        );
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        event.reply("Confirm reload command?").setEphemeral(true).addActionRow(
                Button.primary("reload:confirm", "Confirm"),
                Button.danger("reload:cancel", "Cancel")
        ).queue();
    }

    private static class ButtonListener extends ListenerAdapter {
        @Override
        public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
            Button button = event.getButton();
            String id = button.getId();
            if (id == null || !id.contains("reload"))
                return;

            String[] split = id.split(":");
            String action = split[1];
            if (action.equalsIgnoreCase("confirm")) {
                Bot.getInstance().reloadConfig();
                event.reply("Reloaded configuration files").setEphemeral(true).queue();
                return;
            }

            if (action.equalsIgnoreCase("cancel")) {
                event.reply("Cancelled reload command").setEphemeral(true).queue();
            }
        }
    }
}
