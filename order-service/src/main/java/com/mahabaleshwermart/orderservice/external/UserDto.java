package com.mahabaleshwermart.orderservice.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserDto(
    String id,
    String name,
    String email,
    String phone,
    LocalDateTime createdAt,
    List<AddressDto> addresses,
    boolean verified
) {}


