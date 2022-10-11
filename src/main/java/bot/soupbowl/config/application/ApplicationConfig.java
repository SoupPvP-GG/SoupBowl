package bot.soupbowl.config.application;

import lombok.Data;

import java.util.HashMap;

@Data
public class ApplicationConfig {

    private final HashMap<String, ApplicationEntry> applications;

}
