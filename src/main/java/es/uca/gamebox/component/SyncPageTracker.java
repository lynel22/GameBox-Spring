package es.uca.gamebox.component;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class SyncPageTracker {

    private static final String FILE_PATH = "data/sync_page.txt";

    public int getLastSyncedPage() {
        try {
            Path path = Paths.get(FILE_PATH);
            if (Files.exists(path)) {
                String content = Files.readString(path).trim();
                return Integer.parseInt(content);
            }
        } catch (IOException | NumberFormatException ignored) {}
        return 1;
    }

    public void saveLastSyncedPage(int page) {
        try {
            Files.writeString(Paths.get(FILE_PATH), String.valueOf(page));
        } catch (IOException e) {
            throw new RuntimeException("Error saving last synced page", e);
        }
    }
}
