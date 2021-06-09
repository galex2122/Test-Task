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

@Schema(description = "Comic entity")
@Document(collection = "comics")
@TypeAlias("comic")
@Getter
@Setter
@AllArgsConstructor
public class ComicEntity {
    @Schema(description = "Id of comic entity, cannot be blank")
    @Id
    @NotBlank
    private String id;
    @Schema(description = "Title of comic, cannot be blank")
    @NotBlank(message = "Title must not be blank")
    private String title;
    @Schema(description = "Description of comic, optional")
    private String description;
    @Schema(description = "URI of entity inside API, created automatically")
    @Null(message = "URI will be created automatically")
    private String resourceURI;
    @Schema(description = "Id of character which is present in this comic, optional")
    private ArrayList<String> characters;
    @Schema(description = "URL of thumbnail of this comic, optional")
    @URL
    private String thumbnail;
    @Schema(description = "URL of picture of this comic, optional")
    private ArrayList<@URL String> images;
}
