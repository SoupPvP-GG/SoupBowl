package bot.soupbowl.config.application;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

@Data
public class ApplicationQuestion {

    private final String id;
    private final String question;
    private final String placeholder;
    private final TextInputStyle style;
    private final boolean required;

    @SerializedName("min-length")
    private final int min;

    @SerializedName("max-length")
    private final int max;

}
