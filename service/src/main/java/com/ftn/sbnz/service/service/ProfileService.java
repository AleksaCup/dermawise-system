package com.ftn.sbnz.service.service;

import com.ftn.sbnz.model.enums.FaceRegion;
import com.ftn.sbnz.service.dto.IngredientDto;
import com.ftn.sbnz.service.dto.LifestyleSection;
import com.ftn.sbnz.service.dto.ProductDto;
import com.ftn.sbnz.service.dto.ProfileResponse;
import com.ftn.sbnz.service.dto.QuestionnaireRequest;
import com.ftn.sbnz.service.entity.AppUser;
import com.ftn.sbnz.service.entity.SkinProfile;
import com.ftn.sbnz.service.exception.BadRequestException;
import com.ftn.sbnz.service.exception.NotFoundException;
import com.ftn.sbnz.service.repository.SkinProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final SkinProfileRepository profiles;
    private final FactAssembler assembler;
    private final CepSessionManager cepSessionManager;

    @Transactional
    public ProfileResponse saveQuestionnaire(AppUser user, QuestionnaireRequest request) {
        for (FaceRegion region : FaceRegion.values()) {
            if (request.lesions().get(region) == null) {
                throw new BadRequestException("Nedostaje lezija za region " + region);
            }
        }
        QuestionnaireRequest normalized = normalize(request);

        SkinProfile profile = profiles.findByUser(user).orElseGet(() -> {
            SkinProfile created = new SkinProfile();
            created.setUser(user);
            return created;
        });
        profile.setQuestionnaireJson(assembler.writeJson(normalized));
        profile.setPregnant(normalized.lifestyle().pregnant());
        profile.setBreastfeeding(normalized.lifestyle().breastfeeding());
        profile.setUpdatedAt(Instant.now());
        SkinProfile saved = profiles.save(profile);
        cepSessionManager.invalidate(user.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ProfileResponse get(AppUser user) {
        return toResponse(require(user));
    }

    public SkinProfile require(AppUser user) {
        return profiles.findByUser(user)
                .orElseThrow(() -> new NotFoundException("Korisnik još nije popunio upitnik"));
    }

    public ProfileResponse toResponse(SkinProfile profile) {
        return new ProfileResponse(profile.getUser().getId(), profile.getUser().getFullName(),
                assembler.readQuestionnaire(profile), profile.getFototip(), profile.getMed(), profile.isPregnant(),
                profile.isBreastfeeding(), profile.getUpdatedAt());
    }

    private QuestionnaireRequest normalize(QuestionnaireRequest q) {
        LifestyleSection l = q.lifestyle();
        List<String> allergies = l.allergies() == null ? List.of()
                : l.allergies().stream().filter(a -> a != null && !a.isBlank()).map(ProfileService::name).toList();
        List<ProductDto> products = l.currentProducts() == null ? List.of()
                : l.currentProducts().stream().map(ProfileService::normalize).toList();
        LifestyleSection lifestyle = new LifestyleSection(l.pregnant(), l.breastfeeding(), l.hormonalTherapy(),
                allergies, products, l.dailySunMinutes());
        return new QuestionnaireRequest(q.phototype(), q.lesions(), q.rosacea(), q.scorad(), lifestyle);
    }

    static ProductDto normalize(ProductDto product) {
        List<IngredientDto> ingredients = product.ingredients() == null ? List.of() : product.ingredients().stream()
                .map(i -> new IngredientDto(name(i.name()), i.concentration()))
                .toList();
        return new ProductDto(product.name().trim(), ingredients, product.applicationAreas());
    }

    static String name(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
