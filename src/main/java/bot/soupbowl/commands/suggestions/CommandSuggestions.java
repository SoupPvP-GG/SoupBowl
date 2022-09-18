package bot.soupbowl.commands.suggestions;

import bot.soupbowl.api.SoupBowlAPI;
import bot.soupbowl.api.SuggestionBlacklistManager;
import bot.soupbowl.api.command.SlashCommand;
import bot.soupbowl.api.command.SlashInfo;
import bot.soupbowl.api.model.SuggestionBlacklistEntry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.awt.*;
import java.util.Objects;

@SlashInfo(name = "suggestions", description = "Manage suggestions for the server")
public class CommandSuggestions extends SlashCommand {

    private final SuggestionBlacklistManager manager;
    public CommandSuggestions(SoupBowlAPI api) {
        this.manager = api.getSuggestionBlacklistManager();
        setData(commandData -> {
            commandData.setDefaultPermissions(DefaultMemberPermissions.enabledFor(
                    Permission.BAN_MEMBERS
            ));

            commandData.addSubcommands(
                    new SubcommandData("blacklist", "Blacklist a user from making suggestions")
                            .addOptions(new OptionData(OptionType.USER, "user", "The user to blacklist", true),
                                    new OptionData(OptionType.STRING, "reason", "The reason for blacklisting the user", false)),
                    new SubcommandData("unblacklist", "Unblacklist a user from making suggestions")
                            .addOptions(new OptionData(OptionType.USER, "user", "The user to unblacklist", true)),
                    new SubcommandData("viewblacklist", "View the blacklist of a user")
                            .addOption(OptionType.USER, "user", "The user to view the blacklist of", true)
            );

        });
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        String name = event.getSubcommandName();
        if (name == null) {
            event.reply("Invalid subcommand").setEphemeral(true).queue();
            return;
        }

        if (name.equalsIgnoreCase("blacklist")) {
            User user = Objects.requireNonNull(event.getOption("user")).getAsUser();

            String reason = null;
            OptionMapping reasonOption = event.getOption("reason");
            if (reasonOption != null)
                reason = reasonOption.getAsString();

            if (manager.isBlacklisted(user.getId())) {
                event.reply("That user is already blacklisted!").setEphemeral(true).queue();
                return;
            }

            manager.createBlacklist(user.getId(), reason, event.getUser().getId(), System.currentTimeMillis());
            event.reply("Blacklisted user " + user.getAsMention() + " from making suggestions").setEphemeral(true).queue();
            return;
        }

        if (name.equalsIgnoreCase("unblacklist")) {
            User user = Objects.requireNonNull(event.getOption("user")).getAsUser();

            if (!manager.isBlacklisted(user.getId())) {
                event.reply("That user is not blacklisted!").setEphemeral(true).queue();
                return;
            }

            SuggestionBlacklistEntry blacklist = manager.getBlacklist(user.getId());
            assert blacklist != null; // We can assume blacklist != null
            manager.removeBlacklist(blacklist);

            event.reply("Unblacklisted user " + user.getAsMention() + " from making suggestions").setEphemeral(true).queue();
        }

        if (name.equalsIgnoreCase("viewblacklist")) {
            User user = Objects.requireNonNull(event.getOption("user")).getAsUser();

            if (!manager.isBlacklisted(user.getId())) {
                event.reply("That user is not blacklisted!").setEphemeral(true).queue();
                return;
            }

            SuggestionBlacklistEntry blacklist = manager.getBlacklist(user.getId());
            assert blacklist != null; // We can assume blacklist != null

            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("Viewing blacklist of " + user.getAsTag());
            builder.setColor(new Color(14, 83, 194));
            builder.addField("Staff Mention", "<@" + blacklist.staff() + ">", true);
            builder.addField("Staff ID", blacklist.staff(), true);
            builder.addField("Reason", (blacklist.reason() == null ? "No Reason Provided" : blacklist.reason()), false);
            builder.addField("Date", "<t:" + (blacklist.date() / 1000) + ">", false);
            builder.setThumbnail(user.getAvatarUrl());

            event.replyEmbeds(builder.build()).setEphemeral(true).queue();
        }
    }
}
