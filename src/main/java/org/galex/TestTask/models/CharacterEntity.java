package org.galex.TestTask.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Null;
import java.util.ArrayList;

@Schema(description = "Comic character entity")
@Document(collection = "characters")
@TypeAlias("character")
@Getter
@Setter
@AllArgsConstructor
public class CharacterEntity {
    @Schema(description = "Id of character entity, cannot be blank")
    @Id
    @NotBlank
    private String id;
    @Schema(description = "Name of character, cannot be blank")
    @NotBlank(message = "Name must not be blank")
    private String name;
    @Schema(description = "Description of character, optional")
    private String description;
    @Schema(description = "URL of thumbnail of this character, optional")
    @URL
    private String thumbnail;
    @Schema(description = "Id of comic in which this character is present, optional")
    private ArrayList<String> comics;
    @Schema(description = "URI of entity inside API, created automatically")
    @Null(message = "URI will be created automatically")
    private String resourceURI;
}
