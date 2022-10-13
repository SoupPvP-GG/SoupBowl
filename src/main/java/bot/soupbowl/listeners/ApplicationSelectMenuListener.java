package bot.soupbowl.listeners;

import bot.soupbowl.Bot;
import bot.soupbowl.api.ApplicationManager;
import bot.soupbowl.api.ApplicationSubmissionType;
import bot.soupbowl.api.model.ApplicationSubmissionProcess;
import bot.soupbowl.config.application.ApplicationEntry;
import bot.soupbowl.config.application.ApplicationQuestion;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import games.negative.framework.discord.runnable.RepeatingRunnable;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class ApplicationSelectMenuListener extends ListenerAdapter {

    private final ApplicationManager manager;
    private final Multimap<String, ApplicationAnswer> answers;
    private final Multimap<String, ApplicationSessionData> sessions;
    private final static int MAX_SESSIONS = 100;
    private final static long SESSION_TIMEOUT = ((1000 * 60) * 30);
    private final static long ANSWERS_TIMEOUT = ((1000 * 60) * 30);

    public ApplicationSelectMenuListener(ApplicationManager manager) {
        this.manager = manager;
        this.sessions = ArrayListMultimap.create();
        this.answers = ArrayListMultimap.create();

        Bot.getInstance().getScheduler().runRepeatingRunnable(new ApplicationAnswerEOLRunnable(), 0, ((1000L * 60) * 5));
    }

    @Override
    public void onSelectMenuInteraction(@NotNull SelectMenuInteractionEvent event) {
        SelectOption selectOption = event.getInteraction().getSelectedOptions().get(0);
        if (selectOption == null || !selectOption.getValue().startsWith("app:"))
            return;

        String label = selectOption.getValue();
        // <type>:<application key>:<page>:<session id>
        String[] split = label.split(":");
        String key = split[1];

        ApplicationEntry app = manager.getConfig().getApplications().get(key);
        String name = app.getName();
        ArrayList<ApplicationQuestion> questions = app.getQuestions();
        List<ApplicationQuestion> nextQuestions = questions.stream().limit(ApplicationManager.MAX_QUESTIONS).toList();

        Modal.Builder builder = Modal.create("app:" + key + ":1:" + generateSessionID(event.getUser().getId()), name);
        for (ApplicationQuestion question : nextQuestions) {
            TextInput input = TextInput.create(
                    question.getId(),
                    question.getQuestion(),
                    question.getStyle()
            ).setPlaceholder(question.getPlaceholder())
                    .setRequired(question.isRequired())
                    .build();

            builder.addActionRows(ActionRow.of(input));
        }

        event.replyModal(builder.build()).queue();
    }

    private String generateSessionID(String id) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int generated = random.nextInt(0, MAX_SESSIONS);
        // Check with the current sessions to make sure it's unique
        // If it's not, generate a new one and keep checking
        Collection<ApplicationSessionData> sessionData = sessions.values();
        for (ApplicationSessionData data : sessionData) {
            if (data.sessionKey().equalsIgnoreCase(String.valueOf(generated)))
                return generateSessionID(id);
        }

        // Add generated session id to the session map
        sessions.put(id, new ApplicationSessionData(String.valueOf(generated), System.currentTimeMillis()));

        return String.valueOf(generated);
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        String id = event.getModalId();
        if (!id.startsWith("app"))
            return;

        // <type>:<application key>:<page>:<session key>
        String[] split = id.split(":");
        String key = split[1];
        int currentPage = Integer.parseInt(split[2]);
        String sessionKey = split[3];

        // Save the answers
        List<ModalMapping> values = event.getValues();
        for (ModalMapping value : values) {
            answers.put(sessionKey, new ApplicationAnswer(value.getId(), value.getAsString(), System.currentTimeMillis()));
        }

        ApplicationEntry app = manager.getConfig().getApplications().get(key);
        ArrayList<ApplicationQuestion> questions = app.getQuestions();
        int size = questions.size();
        double maxPages = Math.ceil(((double) size / ApplicationManager.MAX_QUESTIONS));
        if (currentPage >= maxPages) {
            // Complete the application
            ApplicationSubmissionProcess process = app.getSubmissionProcess();
            ApplicationSubmissionType submissionType = process.getSubmissionType();
            if (submissionType.equals(ApplicationSubmissionType.SEND_TO_CHANNEL)) {
                Guild guild = event.getGuild();
                if (guild == null) {
                    event.reply("An error occurred while trying to submit your application.").setEphemeral(true).queue();
                    return;
                }

                String channelId = process.getChannelId();
                TextChannel channel = guild.getTextChannelById(channelId);
                if (channel == null) {
                    event.reply("The channel for this application is not found.").setEphemeral(true).queue();
                    return;
                }

                EmbedBuilder builder = new EmbedBuilder();
                builder.setTitle("New Application! [" + app.getName() + "]");
                builder.setColor(Color.GREEN);
                builder.setAuthor(event.getUser().getAsTag(), null, event.getUser().getEffectiveAvatarUrl());
                builder.setFooter("Application Session Key: " + sessionKey);

                StringBuilder descriptionBuilder = new StringBuilder();

                Collection<ApplicationAnswer> answers = this.answers.get(sessionKey);
                for (ApplicationAnswer answer : answers) {
                    String questionID = answer.id();
                    ApplicationQuestion question = app.getQuestionByID(questionID);
                    if (question == null)
                        continue;

                    descriptionBuilder.append("**")
                            .append(question.getQuestion())
                            .append(":**").append("\n")
                            .append(answer.answer()).append("\n\n");
                }
                builder.setDescription(descriptionBuilder.toString());
                channel.sendMessageEmbeds(builder.build()).queue();
                this.answers.removeAll(sessionKey);
                this.sessions.removeAll(event.getUser().getId());
                event.reply("Your application has been submitted.").setEphemeral(true).queue();
                return;
            }
            event.reply("Application complete!").setEphemeral(true).queue();
        } else {
            // Send confirmation to go to the next page of the application process
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("Application Process [" + currentPage + "/" + (int) maxPages + "]");
            builder.setDescription("We still have more questions for you! To continue the application process, please click the button below.");
            builder.setColor(new Color(24, 252, 3));
            event.replyEmbeds(builder.build()).setEphemeral(true).addActionRow(
                    Button.primary("app:" + key + ":" + (currentPage + 1) + ":" + sessionKey, "Continue"),
                    Button.danger("app:cancel", "Cancel")
            ).queue();
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        Button button = event.getButton();
        String id = button.getId();
        if (id == null || !id.startsWith("app"))
            return;

        // <type>:<application key>:<page>
        String[] split = id.split(":");
        if (split[1].equalsIgnoreCase("cancel")) {
            event.getMessage().delete().queue();
            event.reply("Application process cancelled.").setEphemeral(true).queue();
        }

        String key = split[1];
        int currentPage = Integer.parseInt(split[2]);

        String sessionKey = split[3];

        ApplicationEntry app = manager.getConfig().getApplications().get(key);
        ArrayList<ApplicationQuestion> questions = app.getQuestions();
        int size = questions.size();

        List<ApplicationQuestion> nextQuestions = questions.stream().skip((long) (currentPage - 1) * ApplicationManager.MAX_QUESTIONS).limit(ApplicationManager.MAX_QUESTIONS).collect(Collectors.toList());
        Modal.Builder builder = Modal.create("app:" + key + ":" + currentPage + ":" + sessionKey, app.getName());
        for (ApplicationQuestion question : nextQuestions) {
            TextInput input = TextInput.create(
                    question.getId(),
                    question.getQuestion(),
                    question.getStyle()
            ).setPlaceholder(question.getPlaceholder())
                    .setRequired(question.isRequired()).build();

            builder.addActionRows(ActionRow.of(input));
        }
        event.replyModal(builder.build()).queue();
    }

    private record ApplicationAnswer(String id, String answer, long submitted) {

    }

    private record ApplicationSessionData(String sessionKey, long submitted) {

    }

    private class ApplicationAnswerEOLRunnable implements RepeatingRunnable {

        @Override
        public void execute() {
            List<String> toRemove = Lists.newArrayList();
            for (Map.Entry<String, ApplicationAnswer> entry : answers.entries()) {
                ApplicationAnswer value = entry.getValue();
                long submitted = value.submitted();
                // If the submission time was longer than x minutes ago, remove it
                if (System.currentTimeMillis() >= (submitted + ANSWERS_TIMEOUT)) {
                    toRemove.add(entry.getKey());
                }
            }

            for (String key : toRemove) {
                answers.removeAll(key);
            }

            toRemove.clear();

            for (Map.Entry<String, ApplicationSessionData> entry : sessions.entries()) {
                ApplicationSessionData value = entry.getValue();
                long submitted = value.submitted();
                // If the submission time was longer than x minutes ago, remove it
                if (System.currentTimeMillis() >= (submitted + SESSION_TIMEOUT)) {
                    toRemove.add(entry.getKey());
                }
            }
        }
    }
}
