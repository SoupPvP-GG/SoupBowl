package bot.soupbowl.config;

import games.negative.framework.discord.config.json.model.JsonRGB;
import games.negative.framework.discord.config.json.model.embed.JsonEmbedBuilder;
import games.negative.framework.discord.config.json.model.embed.JsonEmbedDescription;
import games.negative.framework.discord.config.json.model.embed.JsonEmbedTitle;
import lombok.Data;

@Data
public class EmbedsConfig {

    private final JsonEmbedBuilder error;

    public EmbedsConfig() {
        error = new JsonEmbedBuilder();
        error.setTitle(new JsonEmbedTitle("Error!", null));
        error.setColor(new JsonRGB(255, 0, 0));
        error.setDescription(new JsonEmbedDescription("An error has occurred!"));
    }
}
