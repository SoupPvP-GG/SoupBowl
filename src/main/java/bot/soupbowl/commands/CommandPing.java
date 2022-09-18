package bot.soupbowl.commands;

import bot.soupbowl.api.command.SlashCommand;
import bot.soupbowl.api.command.SlashInfo;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@SlashInfo(name = "ping", description = "Ping pong!")
public class CommandPing extends SlashCommand {
    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        event.reply("Pong!").setEphemeral(true).queue();
    }
}
