package org.galex.TestTask.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.ArrayList;
import java.util.Map;

@Document(collection = "characters")
@TypeAlias("character")
@Getter
@Setter
@AllArgsConstructor
public class CharacterEntity {
    @Id
    @NotBlank
    private String id;
    @NotBlank(message = "Name must not be blank")
    private String name;
    private String description;
    private String thumbnail;
    private ArrayList<String> comics;
    @Null(message = "URI will be created automatically")
    private String resourceURI;
}

