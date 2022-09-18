package bot.soupbowl.core.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

@RequiredArgsConstructor
@SuppressWarnings("all")
public abstract class ObjectCache<T> {

    private final String path;
    private final Class<T[]> clazz;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Save the Cache to the JSON file
     *
     * @param cacheArrayList Class Type ArrayList
     */
    public void save(@NotNull ArrayList<T> cacheArrayList) throws IOException {
        File file = getFile(path);
        file.getParentFile().mkdir();
        file.createNewFile();

        Writer writer = new FileWriter(file, false);
        gson.toJson(cacheArrayList, writer);
        writer.flush();
        writer.close();
    }

    /**
     * Load the Cache from the JSON file
     *
     * @return A new instance of an ArrayList with the new Cache
     */
    @NotNull
    public ArrayList<T> load() throws IOException {
        File file = getFile(path);
        if (file.exists()) {
            Reader reader = new FileReader(file);
            T[] p = gson.fromJson(reader, clazz);
            return new ArrayList<>(Arrays.asList(p));
        }
        return new ArrayList<>();
    }

    private File getFile(@NotNull String path) {
        return new File(path);
    }
}