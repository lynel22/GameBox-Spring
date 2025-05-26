package es.uca.gamebox.dto;

import lombok.Data;

@Data
public class RawgPlatformDto {
    private Platform platform;

    public static class Platform {
        private int id;
        private String name;
    }
}
