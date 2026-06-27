package com.cafestory.service.model;

import com.cafestory.entity.CafePage;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.ActorContextType;

public record ActorContext(
        User actorUser,
        ActorContextType actorContextType,
        CafePage actorCafePage) {
}
