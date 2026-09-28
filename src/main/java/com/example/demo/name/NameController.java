package com.example.demo.name;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.security.CurrentUser;
import com.example.demo.user.User;

@RestController
@RequestMapping("/api/v1/names")
public class NameController {

    private final NameService nameService;
    private final CurrentUser currentUser;

    public NameController(NameService nameService, CurrentUser currentUser) {
        this.nameService = nameService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<NameView> search(@RequestParam(required = false) String query,
                                 @RequestParam(required = false) Gender gender,
                                 @RequestParam(required = false) String origin,
                                 @RequestParam(required = false) String province) {
        return nameService.search(query, gender, origin, province);
    }

    @GetMapping("/suggest")
    public List<NameView> suggest(@RequestParam(required = false) Gender gender,
                                  @RequestParam(required = false) String origin,
                                  @RequestParam(defaultValue = "5") int count) {
        return nameService.suggest(gender, origin, count);
    }

    @GetMapping("/origins")
    public List<String> origins() {
        return nameService.origins();
    }

    @GetMapping("/provinces")
    public List<String> provinces() {
        return nameService.provinces();
    }

    @GetMapping("/{id}")
    public NameView get(@PathVariable Long id) {
        return nameService.get(id);
    }

    @GetMapping("/favorites")
    public List<NameView> favorites() {
        User user = currentUser.get();
        return nameService.favorites(user);
    }

    @PostMapping("/{id}/favorite")
    @ResponseStatus(HttpStatus.CREATED)
    public NameView favorite(@PathVariable Long id) {
        User user = currentUser.get();
        return nameService.favorite(user, id);
    }

    @DeleteMapping("/{id}/favorite")
    public ResponseEntity<Void> unfavorite(@PathVariable Long id) {
        User user = currentUser.get();
        nameService.unfavorite(user, id);
        return ResponseEntity.noContent().build();
    }
}