package bot.soupbowl.commands;

import bot.soupbowl.Bot;
import bot.soupbowl.config.SoupConfig;
import games.negative.framework.discord.command.SlashCommand;
import games.negative.framework.discord.command.SlashInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Objects;

@SlashInfo(name = "announce", description = "Announce something to the server")
public class CommandAnnounce extends SlashCommand {

    public CommandAnnounce(JDABuilder builder) {
        setData(data -> {
            data.setDefaultPermissions(DefaultMemberPermissions.enabledFor(
                    Permission.ADMINISTRATOR
            ));
        });

        builder.addEventListeners(
                new AnnouncementListener()
        );
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        Modal.Builder builder = Modal.create("announce", "Create an Announcement");

        TextInput title = TextInput.create("title", "Title", TextInputStyle.SHORT).setRequired(true).build();
        TextInput rgb = TextInput.create("rgb", "RGB", TextInputStyle.SHORT).setRequired(false).build();
        TextInput contents = TextInput.create("contents", "Announcement Contents", TextInputStyle.PARAGRAPH).setRequired(true).build();

        builder.addActionRows(
                ActionRow.of(title),
                ActionRow.of(rgb),
                ActionRow.of(contents)
        );

        event.replyModal(builder.build()).queue();
    }

    private static class AnnouncementListener extends ListenerAdapter {

        @Override
        public void onModalInteraction(@NotNull ModalInteractionEvent event) {
            String id = event.getModalId();
            if (!id.equals("announce"))
                return;

            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle(Objects.requireNonNull(event.getValue("title")).getAsString());
            builder.setDescription(Objects.requireNonNull(event.getValue("contents")).getAsString());
            builder.setThumbnail(event.getUser().getAvatarUrl());

            String rgbRaw = null;
            if (event.getValue("rgb") != null && !Objects.requireNonNull(event.getValue("rgb")).getAsString().isEmpty()) {
                rgbRaw = Objects.requireNonNull(event.getValue("rgb")).getAsString();
                String[] rgb = rgbRaw.split(", ");
                builder.setColor(new Color(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2])));
            } else {
                builder.setColor(Color.GREEN);
            }

            event.replyEmbeds(builder.build()).addActionRow(
                    Button.success("announce:confirm", "Confirm Announcement"),
                    Button.danger("announce:cancel", "Cancel Announcement")
            ).queue();
        }

        @Override
        public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
            Button button = event.getButton();
            String id = button.getId();
            if (id == null || !id.startsWith("announce"))
                return;

            String[] split = id.split(":");
            String action = split[1];
            if (action.equalsIgnoreCase("confirm")) {
                // Send announcement
                MessageEmbed messageEmbed = event.getMessage().getEmbeds().get(0);

                EmbedBuilder builder = new EmbedBuilder(messageEmbed);
                builder.setFooter("Announcement by " + event.getUser().getName());

                SoupConfig config = Bot.getInstance().getConfig();
                String channelRaw = config.getAnnouncementsChannel();
                if (channelRaw == null) {
                    event.reply("Announcements channel not set!").setEphemeral(true).queue();
                    return;
                }

                Guild guild = event.getGuild();
                if (guild == null) {
                    event.reply("Guild not found!").setEphemeral(true).queue();
                    return;
                }

                TextChannel channel = guild.getTextChannelById(channelRaw);
                if (channel == null) {
                    event.reply("Announcements channel not found!").setEphemeral(true).queue();
                    return;
                }

                channel.sendMessageEmbeds(builder.build()).queue();
                event.getMessage().delete().queue();
                event.reply("Announcement sent!").setEphemeral(true).queue();
            } else {
                // Cancel announcement
                event.getMessage().delete().queue();
                event.reply("Announcement cancelled").setEphemeral(true).queue();
            }
        }
    }


}
