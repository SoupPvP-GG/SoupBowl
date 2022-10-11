package bot.soupbowl.listeners;

import bot.soupbowl.api.ApplicationManager;
import bot.soupbowl.api.ApplicationSubmissionType;
import bot.soupbowl.api.model.ApplicationSubmissionProcess;
import bot.soupbowl.config.application.ApplicationEntry;
import bot.soupbowl.config.application.ApplicationQuestion;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
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
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ApplicationSelectMenuListener extends ListenerAdapter {

    private final ApplicationManager manager;

    @Override
    public void onSelectMenuInteraction(@NotNull SelectMenuInteractionEvent event) {
        SelectOption selectOption = event.getInteraction().getSelectedOptions().get(0);
        if (selectOption == null || !selectOption.getValue().startsWith("app:"))
            return;

        String label = selectOption.getValue();
        // <type>:<application key>
        String[] split = label.split(":");
        String key = split[1];

        ApplicationEntry app = manager.getConfig().getApplications().get(key);
        String name = app.getName();
        ArrayList<ApplicationQuestion> questions = app.getQuestions();
        List<ApplicationQuestion> nextQuestions = questions.stream().limit(ApplicationManager.MAX_QUESTIONS).toList();

        Modal.Builder builder = Modal.create("app:" + key + ":1", name);
        for (ApplicationQuestion question : nextQuestions) {
            TextInput input = TextInput.create(
                    question.getId(),
                    question.getQuestion(),
                    question.getStyle()
            ).setRequired(question.isRequired()).build();

            builder.addActionRows(ActionRow.of(input));
        }

        event.replyModal(builder.build()).queue();
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        String id = event.getModalId();
        if (!id.startsWith("app"))
            return;

        // <type>:<application key>:<page>
        String[] split = id.split(":");
        String key = split[1];
        int currentPage = Integer.parseInt(split[2]);

        ApplicationEntry app = manager.getConfig().getApplications().get(key);
        ArrayList<ApplicationQuestion> questions = app.getQuestions();
        int size = questions.size();
        double maxPages = Math.ceil(((double) size / ApplicationManager.MAX_QUESTIONS));
        if (currentPage >= maxPages) {
            // Complete the application
            ApplicationSubmissionProcess process = app.getSubmissionProcess();
            ApplicationSubmissionType submissionType = process.getSubmissionType();
            if (submissionType.equals(ApplicationSubmissionType.SEND_TO_CHANNEL)) {

            }
            event.reply("Application complete!").setEphemeral(true).queue();
        } else {
            // Send confirmation to go to the next page of the application process
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("Application Process [" + currentPage + "/" + (int) maxPages + "]");
            builder.setDescription("We still have more questions for you! To continue the application process, please click the button below.");
            builder.setColor(new Color(24, 252, 3));
            event.replyEmbeds(builder.build()).setEphemeral(true).addActionRow(
                    Button.primary("app:" + key + ":" + (currentPage + 1), "Continue"),
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

        ApplicationEntry app = manager.getConfig().getApplications().get(key);
        ArrayList<ApplicationQuestion> questions = app.getQuestions();
        int size = questions.size();

        List<ApplicationQuestion> nextQuestions = questions.stream().skip((long) (currentPage - 1) * ApplicationManager.MAX_QUESTIONS).limit(ApplicationManager.MAX_QUESTIONS).collect(Collectors.toList());
        Modal.Builder builder = Modal.create("app:" + key + ":" + (currentPage + 1), app.getName());
        for (ApplicationQuestion question : nextQuestions) {
            TextInput input = TextInput.create(
                    String.valueOf(questions.indexOf(question)),
                    question.getQuestion(),
                    question.getStyle()
            ).setRequired(question.isRequired()).build();

            builder.addActionRows(ActionRow.of(input));
        }
        event.replyModal(builder.build()).queue();
    }
}
