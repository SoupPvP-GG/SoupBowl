package bot.soupbowl.commands.suggestions;

import bot.soupbowl.api.SoupBowlAPI;
import bot.soupbowl.api.SuggestionBlacklistManager;
import games.negative.framework.discord.command.SlashCommand;
import games.negative.framework.discord.command.SlashInfo;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

@SlashInfo(name = "suggest", description = "Suggest something for the server!")
public class CommandSuggest extends SlashCommand {

    private final SuggestionBlacklistManager manager;

    public CommandSuggest(SoupBowlAPI api) {
        this.manager = api.getSuggestionBlacklistManager();
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("You can only use this command in a server!").setEphemeral(true).queue();
            return;
        }
        Member member = event.getMember();
        if (member == null) {
            event.reply("You can only use this command in a server!").setEphemeral(true).queue();
            return;
        }

        String id = member.getId();
        if (manager.isBlacklisted(id)) {
            event.reply("You are blacklisted from making suggestions!").setEphemeral(true).queue();
            return;
        }

        TextInput platform = TextInput.create("platform", "Platform", TextInputStyle.SHORT)
                .setMinLength(1)
                .setMaxLength(32)
                .setPlaceholder("Server, Discord, Store, etc.").setRequired(true).build();

        TextInput suggestion = TextInput.create("suggestion", "Suggestion", TextInputStyle.PARAGRAPH)
                .setMinLength(100)
                .setMaxLength(4000)
                .setPlaceholder("I would like to suggest...").setRequired(true).build();

        Modal modal = Modal.create("suggestions", "Create a Suggestion")
                .addActionRows(ActionRow.of(platform), ActionRow.of(suggestion))
                .build();

        event.replyModal(modal).queue();
    }
}
