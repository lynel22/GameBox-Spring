package es.uca.gamebox.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class DeveloperDto {
    private String name;
    private UUID id;
}
