package com.example.demo.name;

import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.user.User;

@Service
public class NameService {

    private final NameRepository nameRepository;
    private final UserFavoriteRepository favoriteRepository;

    public NameService(NameRepository nameRepository, UserFavoriteRepository favoriteRepository) {
        this.nameRepository = nameRepository;
        this.favoriteRepository = favoriteRepository;
    }

    @Transactional(readOnly = true)
    public List<NameView> search(String query, Gender gender, String origin, String province) {
        return nameRepository.search(blankToNull(query), gender, blankToNull(origin), blankToNull(province))
                .stream()
                .map(NameView::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NameView> suggest(Gender gender, String origin, int count) {
        List<NameRecord> candidates = nameRepository.suggest(gender, blankToNull(origin));
        Collections.shuffle(candidates);
        return candidates.stream()
                .limit(Math.min(count, candidates.size()))
                .map(NameView::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public NameView get(Long id) {
        return nameRepository.findById(id)
                .map(NameView::from)
                .orElseThrow(() -> new NameNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<String> origins() {
        return nameRepository.findDistinctOrigins();
    }

    @Transactional(readOnly = true)
    public List<String> provinces() {
        return nameRepository.findDistinctProvinces();
    }

    @Transactional(readOnly = true)
    public List<NameView> favorites(User user) {
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(UserFavorite::getName)
                .map(NameView::from)
                .toList();
    }

    @Transactional
    public NameView favorite(User user, Long nameId) {
        NameRecord name = nameRepository.findById(nameId).orElseThrow(() -> new NameNotFoundException(nameId));
        if (!favoriteRepository.existsByUserIdAndNameId(user.getId(), nameId)) {
            UserFavorite favorite = new UserFavorite();
            favorite.setUser(user);
            favorite.setName(name);
            favoriteRepository.save(favorite);
        }
        return NameView.from(name);
    }

    @Transactional
    public void unfavorite(User user, Long nameId) {
        favoriteRepository.findByUserIdAndNameId(user.getId(), nameId)
                .ifPresent(favoriteRepository::delete);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}