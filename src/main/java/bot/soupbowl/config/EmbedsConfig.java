package bot.soupbowl.config;

import com.google.gson.annotations.SerializedName;
import games.negative.framework.discord.config.json.model.JsonRGB;
import games.negative.framework.discord.config.json.model.embed.JsonEmbedAuthor;
import games.negative.framework.discord.config.json.model.embed.JsonEmbedBuilder;
import games.negative.framework.discord.config.json.model.embed.JsonEmbedDescription;
import games.negative.framework.discord.config.json.model.embed.JsonEmbedTitle;
import lombok.Data;

import java.util.Collections;

@Data
public class EmbedsConfig {

    private final JsonEmbedBuilder error;

    @SerializedName("announcement-success-embed")
    private final JsonEmbedBuilder announcementSuccess;

    public EmbedsConfig() {
        error = new JsonEmbedBuilder();
        error.setTitle(new JsonEmbedTitle("Error!", null));
        error.setColor(new JsonRGB(255, 0, 0));
        error.setDescription(new JsonEmbedDescription("An error has occurred!"));

        announcementSuccess = new JsonEmbedBuilder();
        announcementSuccess.setTitle(new JsonEmbedTitle(
                "%announcement-title%", null
        ));
        announcementSuccess.setDescription(new JsonEmbedDescription(
                "%announcement-description%"
        ));
        announcementSuccess.setThumbnailUrl("%announcement-thumbnail%");
        announcementSuccess.setAuthor(new JsonEmbedAuthor(
                "%announcement-author%", null, null
        ));
        announcementSuccess.setColor(null);
        announcementSuccess.setImageUrl(null);
        announcementSuccess.setFooter(null);
        announcementSuccess.setFields(Collections.emptyList());
    }
}
