package bot.soupbowl.api.command;

import bot.soupbowl.api.command.exception.InvalidCommandInformationException;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.List;
import java.util.function.Consumer;

@Getter @Setter
public abstract class SlashCommand {

    private final String name;
    private final String description;
    private List<String> aliases;
    private Consumer<SlashCommandData> data;

    public SlashCommand() {
        if (!getClass().isAnnotationPresent(SlashInfo.class))
            throw new InvalidCommandInformationException("Class " + this.getClass().getName() + " must be annotated with @SlashInfo");

        this.aliases = Lists.newArrayList();

        SlashInfo info = getClass().getAnnotation(SlashInfo.class);
        this.name = info.name();
        this.description = info.description();
        if (!info.args()[0].isEmpty()) {
            this.aliases = List.of(info.args());
        }

    }

    public void runCommand(SlashCommandInteractionEvent event) {
        onCommand(event);
    }

    public abstract void onCommand(SlashCommandInteractionEvent event);

}
