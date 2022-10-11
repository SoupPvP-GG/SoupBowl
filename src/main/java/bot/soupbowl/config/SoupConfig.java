package bot.soupbowl.config;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class SoupConfig {

    @SerializedName("token")
    private String botToken;

    @SerializedName("suggestions-channel")
    private String suggestionsChannel;

    @SerializedName("suggestions-admin-role")
    private String suggestionsManagerRoleID;

    @SerializedName("announcement-channel")
    private String announcementsChannel;

}
