package es.uca.gamebox.dto;

import lombok.Data;

@Data
public class RawgPlatformDto {
    private Platform platform;

    // Devuelve el nombre de la plataforma (si existe)
    public String getName() {
        return platform != null ? platform.getName() : null;
    }

    @Data
    public static class Platform {
        private int id;
        private String name;
    }
}

