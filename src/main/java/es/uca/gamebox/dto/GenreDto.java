package es.uca.gamebox.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class GenreDto {
    private UUID id;
    private String name;
    private String spanishName;
}