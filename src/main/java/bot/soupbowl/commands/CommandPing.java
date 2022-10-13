package bot.soupbowl.commands;

import games.negative.framework.discord.command.SlashCommand;
import games.negative.framework.discord.command.SlashInfo;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@SlashInfo(name = "ping", description = "Ping pong!")
public class CommandPing extends SlashCommand {

    public CommandPing() {
        setCooldownFunction(event -> 5000L);
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        event.reply("Pong!").queue();
    }
}
