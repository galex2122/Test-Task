package org.galex.TestTask.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.ArrayList;

@Document(collection = "comics")
@TypeAlias("comic")
@Getter
@Setter
@AllArgsConstructor
public class ComicEntity {
    @Id
    @NotBlank
    private String id;
    @NotBlank(message = "Title must not be blank")
    private String title;
    private String description;
    @Null(message = "URI will be created automatically")
    private String resourceURI;
    private ArrayList<String> characters;
    private String thumbnail;
    private ArrayList<String> images;
}
