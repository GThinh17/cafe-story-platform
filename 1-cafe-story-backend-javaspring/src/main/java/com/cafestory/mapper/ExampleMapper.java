package com.cafestory.mapper;

import org.mapstruct.Mapper;

// componentModel = "spring" tells MapStruct to generate a Spring @Component
// so you can easily @Autowired or inject this mapper into your services.
@Mapper(componentModel = "spring")
public interface ExampleMapper {
    // Define your mapping methods here. For example:
    // UserDTO toUserDTO(User entity);
}
