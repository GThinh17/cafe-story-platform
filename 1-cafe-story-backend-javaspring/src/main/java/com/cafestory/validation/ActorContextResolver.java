package com.cafestory.validation;

import com.cafestory.entity.CafePage;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.ActorContextType;
import com.cafestory.service.model.ActorContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Component
public class ActorContextResolver {

    private final UserValidator userValidator;
    private final CafePageValidator cafePageValidator;

    public ActorContextResolver(UserValidator userValidator, CafePageValidator cafePageValidator) {
        this.userValidator = userValidator;
        this.cafePageValidator = cafePageValidator;
    }

    public ActorContext resolve(UUID actorUserId, ActorContextType requestedType, UUID actorCafePageId) {
        User actorUser = userValidator.validateUserExists(actorUserId);
        userValidator.validateUserActive(actorUser);

        ActorContextType contextType = requestedType == null ? ActorContextType.USER : requestedType;
        if (contextType == ActorContextType.USER) {
            return new ActorContext(actorUser, ActorContextType.USER, null);
        }

        if (actorCafePageId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "actorCafePageId is required when acting as cafe page");
        }
        cafePageValidator.validateUserCanManagePage(actorCafePageId, actorUserId);
        CafePage cafePage = cafePageValidator.validateCafePageExists(actorCafePageId);
        return new ActorContext(actorUser, ActorContextType.CAFE_PAGE, cafePage);
    }
}
