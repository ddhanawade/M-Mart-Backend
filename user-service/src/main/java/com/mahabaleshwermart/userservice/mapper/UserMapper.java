package com.mahabaleshwermart.userservice.mapper;

import com.mahabaleshwermart.userservice.dto.UserDto;
import com.mahabaleshwermart.userservice.entity.Address;
import com.mahabaleshwermart.userservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MapStruct mapper for User entity and DTO conversion
 */
@Mapper(componentModel = "spring", imports = {com.mahabaleshwermart.userservice.entity.User.class, com.mahabaleshwermart.userservice.entity.Address.class})
public interface UserMapper {
    
    /**
     * Convert User entity to UserDto
     */
    @Mapping(target = "isVerified", source = "verified")
    @Mapping(target = "addresses", expression = "java(mapAddressList(user.getAddresses()))")
    UserDto toDto(User user);
    
    /**
     * Convert list of User entities to list of UserDtos
     */
    List<UserDto> toDtoList(List<User> users);
    
    /**
     * Map addresses list to AddressDto list
     */
    default List<UserDto.AddressDto> mapAddressList(List<Address> addresses) {
        if (addresses == null) {
            return null;
        }
        
        return addresses.stream()
                .map(this::mapSingleAddress)
                .collect(Collectors.toList());
    }
    
    /**
     * Map single address to AddressDto
     */
    default UserDto.AddressDto mapSingleAddress(Address address) {
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