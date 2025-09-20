package com.mahabaleshwermart.userservice.mapper;

import com.mahabaleshwermart.userservice.dto.UserDto;
import com.mahabaleshwermart.userservice.entity.User;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-09-19T22:57:22+0530",
    comments = "version: 1.6.0, compiler: javac, environment: Java 21.0.7 (Amazon.com Inc.)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserDto toDto(User user) {
        if ( user == null ) {
            return null;
        }

        UserDto.UserDtoBuilder userDto = UserDto.builder();

        userDto.addresses( mapAddresses( user.getAddresses() ) );
        userDto.isVerified( user.isVerified() );
        userDto.id( user.getId() );
        userDto.email( user.getEmail() );
        userDto.name( user.getName() );
        userDto.phone( user.getPhone() );
        userDto.avatar( user.getAvatar() );
        userDto.createdAt( user.getCreatedAt() );

        return userDto.build();
    }

    @Override
    public List<UserDto> toDtoList(List<User> users) {
        if ( users == null ) {
            return null;
        }

        List<UserDto> list = new ArrayList<UserDto>( users.size() );
        for ( User user : users ) {
            list.add( toDto( user ) );
        }

        return list;
    }
}
