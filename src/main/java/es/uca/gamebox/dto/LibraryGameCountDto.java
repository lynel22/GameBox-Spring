package es.uca.gamebox.dto;


import lombok.Data;

@Data
public class LibraryGameCountDto {
    private String name;
    private long gameCount;

    public LibraryGameCountDto(String name, long gameCount) {
        this.name = name;
        this.gameCount = gameCount;
    }
}
