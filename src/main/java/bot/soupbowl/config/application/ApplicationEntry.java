package bot.soupbowl.config.application;

import bot.soupbowl.api.ApplicationSubmissionType;
import bot.soupbowl.api.model.ApplicationSubmissionProcess;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

@Data
public class ApplicationEntry {

    private final String name;
    private final String description;
    private final String emoji;
    private final ArrayList<ApplicationQuestion> questions;
    @SerializedName("submission-process")
    private final ApplicationSubmissionProcess submissionProcess;

    @Nullable
    public ApplicationQuestion getQuestionByID(String id) {
        for (ApplicationQuestion question : questions) {
            if (question.getId().equals(id)) {
                return question;
            }
        }
        return null;
    }

}
