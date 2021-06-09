package org.galex.TestTask.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.galex.TestTask.models.CharacterEntity;
import org.galex.TestTask.models.ComicEntity;
import org.galex.TestTask.repositories.CharacterRepository;
import org.galex.TestTask.repositories.ComicRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

@Tag(name = "Comics", description = "Interacting with comics")
@RestController
@RequestMapping(value = "/v1/public/comics")
@AllArgsConstructor
public class ComicController {

    private final ComicRepository comicRepository;
    private final CharacterRepository characterRepository;

    @Operation(
            summary = "Search comics",
            description = "Returns page of comics which can be sorted and filtered"
    )
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ApiResponse(
            responseCode = "200",
            description = "Returned page of comics, may be empty"
    )
    public ResponseEntity<Page<ComicEntity>> findComics(Pageable pageable,
                                                        @RequestParam("title") Optional<String> title,
                                                        @RequestParam("titleStartsWith") Optional<String> start) {

        return title.map(t -> new ResponseEntity<>(comicRepository
                .findByTitleStartsWithAndTitle(start.orElse(""), t, pageable), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(comicRepository
                        .findByTitleStartsWith(start.orElse(""), pageable), HttpStatus.OK));
    }

    @Operation(
            summary = "Search comic by id",
            description = "Returns page with comic that contains provided id if it exists, otherwise returns a blank page"
    )
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ApiResponse(
            responseCode = "200",
            description = "Returned page with comic, may be empty"
    )
    public ResponseEntity<Page<ComicEntity>> findComicById(Pageable pageable,
                                                           @PathVariable(value = "id") String id) {
        return new ResponseEntity<>(comicRepository.findById(id, pageable), HttpStatus.OK);
    }

    @Operation(
            summary = "Search characters of comic",
            description = "Returns page of characters, whose ids are present in comic. Page can be sorted and filtered"
    )
    @RequestMapping(value = "/{id}/characters", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ApiResponse(
            responseCode = "200",
            description = "Returned page with characters, may be empty"
    )
    public ResponseEntity<Page<CharacterEntity>> findComicCharacters(Pageable pageable,
                                                                     @PathVariable(value = "id") String id) {
        return new ResponseEntity<>(characterRepository.findByComics(id, pageable), HttpStatus.OK);
    }

    @Operation(
            summary = "Add new comic",
            description = "Allows adding new comic entity to database"
    )
    @RequestMapping(value = "/add", method = RequestMethod.POST, consumes = "application/json")
    @ResponseBody
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Comic created"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Comic with same id already exists"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Provided entity not valid"
            )
    }
    )
    public ResponseEntity<ComicEntity> addComic(@RequestBody @Valid ComicEntity comic,
                                                HttpServletRequest httpServletRequest) {
        if (!comicRepository.existsById(comic.getId())) {
            if (!Objects.isNull(comic.getCharacters())) {
                comic.getCharacters().removeIf(id -> !characterRepository.existsById(id));
            } else {
                comic.setCharacters(new ArrayList<>());
            }
            for (String charId :
                    comic.getCharacters()) {
                CharacterEntity character = characterRepository.findById(charId).get();
                if (!character.getComics().contains(comic.getId())) {
                    character.getComics().add(comic.getId());
                    characterRepository.save(character);
                }
            }
            comic.setResourceURI(httpServletRequest.getScheme() + "://" + httpServletRequest.getServerName()
                    + ":" + httpServletRequest.getServerPort() + "/v1/public/comics/" + comic.getId());
            comicRepository.save(comic);
            return new ResponseEntity<>(comic, HttpStatus.CREATED);
        }
        return new ResponseEntity<>(HttpStatus.CONFLICT);
    }

    @Operation(
            summary = "Update existing comic",
            description = "Allows to update already existing in database comic entity"
    )
    @RequestMapping(value = "/update", method = RequestMethod.PUT, consumes = "application/json")
    @ResponseBody
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Comic updated"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Comic with provided id not found"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Provided entity not valid"
            )
    }
    )
    public ResponseEntity<ComicEntity> updateComic(@RequestBody @Valid ComicEntity comic) {
        if (comicRepository.existsById(comic.getId())) {
            if (!Objects.isNull(comic.getCharacters())) {
                comic.getCharacters().removeIf(id -> !characterRepository.existsById(id));
            } else {
                comic.setCharacters(new ArrayList<>());
            }
            ArrayList<String> bufCharsIds = new ArrayList<>(comicRepository.findById(comic.getId()).get().getCharacters());
            bufCharsIds.removeAll(comic.getCharacters());
            for (String charId :
                    bufCharsIds) {
                CharacterEntity character = characterRepository.findById(charId).get();
                character.getComics().remove(comic.getId());
                characterRepository.save(character);
            }
            bufCharsIds = new ArrayList<>(comic.getCharacters());
            bufCharsIds.removeAll(comicRepository.findById(comic.getId()).get().getCharacters());
            for (String charId :
                    bufCharsIds) {
                CharacterEntity character = characterRepository.findById(charId).get();
                if (!character.getComics().contains(comic.getId())) {
                    character.getComics().add(comic.getId());
                    characterRepository.save(character);
                }
            }
            comic.setResourceURI(comicRepository.findById(comic.getId()).get().getResourceURI());
            comicRepository.save(comic);
            return new ResponseEntity<>(comic, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
