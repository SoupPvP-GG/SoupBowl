package bot.soupbowl.listeners;

import bot.soupbowl.Bot;
import bot.soupbowl.config.SoupConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Objects;

public class SuggestionModalListener extends ListenerAdapter {

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if (!event.getModalId().equalsIgnoreCase("suggestions"))
            return;

        event.reply("Thank you for your suggestion!").setEphemeral(true).queue();

        String suggestion = Objects.requireNonNull(event.getValue("suggestion")).getAsString();
        String platform = Objects.requireNonNull(event.getValue("platform")).getAsString();

        SoupConfig config = Bot.getInstance().getConfig();
        String suggestionsChannel = config.getSuggestionsChannel();

        Guild guild = event.getGuild();
        if (guild == null)
            return;

        TextChannel channel = guild.getTextChannelById(suggestionsChannel);
        if (channel == null) {
            event.reply("The suggestions channel is not set up!").setEphemeral(true).queue();
            return;
        }

        User user = event.getUser();

        EmbedBuilder builder = new EmbedBuilder();

        builder.setTitle("New Suggestion!");
        builder.setThumbnail(user.getAvatarUrl());
        builder.setDescription(
                "**Platform:** " + platform + "\n" +
                        "**Suggested By:** " + user.getAsMention() + "\n" +
                        "**Suggestion:** \n" +
                        suggestion
        );
        builder.setColor(new Color(255, 142, 0));

        Button accept = Button.success("suggestion:accept:" + user.getId(), "Accept Suggestion").withEmoji(Emoji.fromUnicode("✅"));
        Button deny = Button.danger("suggestion:deny:" + user.getId(), "Deny Suggestion").withEmoji(Emoji.fromUnicode("❌"));

        channel.sendMessageEmbeds(builder.build()).addActionRow(accept, deny).queue(message -> {

            message.addReaction(Emoji.fromUnicode("\uD83D\uDC4D")).queue();
            message.addReaction(Emoji.fromUnicode("\uD83D\uDC4E")).queue();

        });
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        Button button = event.getButton();
        String id = button.getId();
        if (id == null || !id.contains("suggestion"))
            return;

        // Check permissions of the user
        Member member = event.getMember();
        if (member == null) {
            event.reply("You do not have the proper role in order to do this!").setEphemeral(true).queue();
            return;
        }

        boolean hasRole = member.getRoles().stream().anyMatch(role -> role.getId().equalsIgnoreCase(Bot.getInstance().getConfig().getSuggestionsManagerRoleID()));
        if (!hasRole) {
            event.reply("You do not have the proper role in order to do this!").setEphemeral(true).queue();
            return;
        }


        // 0             1         2
        // suggestion : <type> : <user id>
        String[] split = id.split(":");
        String type = split[1];
        String userId = split[2];

        if (type.equalsIgnoreCase("accept")) {
            Message message = event.getMessage();
            event.getJDA().retrieveUserById(userId).queue(user -> {
                user.openPrivateChannel().queue(channel -> {
                    EmbedBuilder successMessage = new EmbedBuilder();
                    successMessage.setTitle("Your suggestion was approved! :)");
                    successMessage.setDescription(message.getJumpUrl());
                    successMessage.setColor(new Color(0, 255, 0));

                    channel.sendMessageEmbeds(successMessage.build()).queue();
                });
            });

            MessageEmbed embed = message.getEmbeds().get(0);
            EmbedBuilder builder = new EmbedBuilder(embed);
            builder.setColor(new Color(0, 255, 0));
            builder.setTitle("New Suggestion! | Accepted!");
            StringBuilder descriptionBuilder = builder.getDescriptionBuilder();
            descriptionBuilder.append("\n\n**Accepted at:** <t:").append(System.currentTimeMillis()).append(">");
            message.editMessageEmbeds(builder.build()).queue();

            event.reply("Successfully accepted suggestion!").setEphemeral(true).queue();
        }

        if (type.equalsIgnoreCase("deny")) {
            Message message = event.getMessage();
            event.getJDA().retrieveUserById(userId).queue(user -> {
                user.openPrivateChannel().queue(channel -> {
                    EmbedBuilder successMessage = new EmbedBuilder();
                    successMessage.setTitle("Your suggestion was denied! :(");
                    successMessage.setDescription(message.getJumpUrl());
                    successMessage.setColor(new Color(255, 0, 0));

                    channel.sendMessageEmbeds(successMessage.build()).queue();
                });
            });

            MessageEmbed embed = message.getEmbeds().get(0);
            EmbedBuilder builder = new EmbedBuilder(embed);
            builder.setColor(new Color(255, 0, 0));
            builder.setTitle("New Suggestion! | Denied!");
            StringBuilder descriptionBuilder = builder.getDescriptionBuilder();
            descriptionBuilder.append("\n\n**Denied at:** <t:").append(System.currentTimeMillis()).append(">");
            message.editMessageEmbeds(builder.build()).queue();

            event.reply("Successfully denied suggestion!").setEphemeral(true).queue();
        }


    }
}
