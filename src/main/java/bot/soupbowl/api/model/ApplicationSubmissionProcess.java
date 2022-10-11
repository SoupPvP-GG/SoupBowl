package bot.soupbowl.api.model;

import bot.soupbowl.api.ApplicationSubmissionType;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class ApplicationSubmissionProcess {

    @SerializedName("submission-type")
    private final ApplicationSubmissionType submissionType;

    @SerializedName("channel-id")
    private final String channelId;
}
