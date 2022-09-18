package bot.soupbowl.listeners;

import bot.soupbowl.api.command.CommandMap;
import bot.soupbowl.api.command.SlashCommand;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

@RequiredArgsConstructor
public class SlashCommandListener extends ListenerAdapter {

    private final CommandMap commandMap;

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild != null) {
            // Attempt to execute server commands
            Collection<SlashCommand> serverCommands = commandMap.getServerCommands(guild.getId());
            if (!serverCommands.isEmpty()) {
                SlashCommand command = serverCommands.stream()
                        .filter(slashCommand -> slashCommand.getName().equalsIgnoreCase(event.getName()))
                        .findFirst().orElse(null);

                if (command != null) {
                    command.runCommand(event);
                    return;
                }
            }
        }

        // Attempt to execute global commands
        Collection<SlashCommand> globalCommands = commandMap.getGlobalCommands();
        globalCommands.stream()
                .filter(slashCommand -> slashCommand.getName().equalsIgnoreCase(event.getName()))
                .findFirst().ifPresent(slashCommand -> slashCommand.runCommand(event));
    }
}
