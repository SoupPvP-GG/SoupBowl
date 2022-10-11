package bot.soupbowl.config.application;

import bot.soupbowl.api.ApplicationSubmissionType;
import bot.soupbowl.api.model.ApplicationSubmissionProcess;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.ArrayList;

@Data
public class ApplicationEntry {

    private final String name;
    private final String description;
    private final String emoji;
    private final ArrayList<ApplicationQuestion> questions;
    @SerializedName("submission-process")
    private final ApplicationSubmissionProcess submissionProcess;

}
