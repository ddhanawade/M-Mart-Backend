package com.mahabaleshwermart.userservice.mapper;

import com.mahabaleshwermart.userservice.dto.UserDto;
import com.mahabaleshwermart.userservice.entity.Address;
import com.mahabaleshwermart.userservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

/**
 * MapStruct mapper for User entity and DTO conversion
 */
@Mapper(componentModel = "spring")
public interface UserMapper {
    
    /**
     * Convert User entity to UserDto
     */
    @Mapping(target = "addresses", source = "addresses", qualifiedByName = "mapAddresses")
    @Mapping(target = "isVerified", source = "verified")
    UserDto toDto(User user);
    
    /**
     * Convert list of User entities to list of UserDtos
     */
    List<UserDto> toDtoList(List<User> users);
    
    /**
     * Map addresses to AddressDto
     */
    @Named("mapAddresses")
    default List<UserDto.AddressDto> mapAddresses(List<Address> addresses) {
        if (addresses == null) {
            return null;
        }
        
        return addresses.stream()
                .map(this::mapAddress)
                .toList();
    }
    
    /**
     * Map single address to AddressDto
     */
    default UserDto.AddressDto mapAddress(Address address) {
        if (address == null) {
            return null;
        }
        
        return UserDto.AddressDto.builder()
                .id(address.getId())
                .type(address.getType().name().toLowerCase())
                .name(address.getName())
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .pincode(address.getPincode())
                .landmark(address.getLandmark())
                .isDefault(address.isDefault())
                .build();
    }
} 