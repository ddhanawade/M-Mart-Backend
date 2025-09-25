package com.mahabaleshwermart.orderservice.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AddressDto(
    String id,
    String type,
    String name,
    String street,
    String city,
    String state,
    String pincode,
    String landmark,
    @JsonProperty("default") boolean defaultAddress
) {}
