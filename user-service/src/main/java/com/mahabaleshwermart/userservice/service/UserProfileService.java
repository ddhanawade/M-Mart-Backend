package com.mahabaleshwermart.userservice.service;

import com.mahabaleshwermart.userservice.dto.UpdateProfileRequest;
import com.mahabaleshwermart.userservice.dto.UserDto;
import com.mahabaleshwermart.userservice.entity.Address;
import com.mahabaleshwermart.userservice.entity.User;
import com.mahabaleshwermart.userservice.mapper.UserMapper;
import com.mahabaleshwermart.userservice.repository.AddressRepository;
import com.mahabaleshwermart.userservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserDto updateCurrentUser(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmailWithAddresses(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName().trim());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim());
        }

        // Replace addresses if provided
        if (request.getAddresses() != null) {
            // remove existing addresses and recreate based on payload
            List<Address> existing = user.getAddresses();
            if (existing != null) {
                for (Address addr : existing) {
                    addressRepository.delete(addr);
                }
            }

            List<Address> newList = new ArrayList<>();
            boolean hasDefault = false;
            for (UpdateProfileRequest.AddressDto dto : request.getAddresses()) {
                Address address = new Address();
                address.setUser(user);
                address.setName(dto.getName());
                address.setStreet(dto.getStreet());
                address.setCity(dto.getCity());
                address.setState(dto.getState());
                address.setPincode(dto.getPincode());
                address.setLandmark(dto.getLandmark());
                address.setType(parseType(dto.getType()));
                boolean isDefault = Boolean.TRUE.equals(dto.getIsDefault());
                address.setDefault(isDefault);
                hasDefault = hasDefault || isDefault;
                newList.add(addressRepository.save(address));
            }
            // If none marked default but there are addresses, set first as default
            if (!hasDefault && !newList.isEmpty()) {
                newList.get(0).setDefault(true);
            }
            user.setAddresses(newList);
        }

        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    @Transactional
    public UserDto updateUserById(String requesterEmail, String userId, UpdateProfileRequest request) {
        User user = userRepository.findByIdWithAddresses(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Ensure the requester is the resource owner
        if (!user.getEmail().equalsIgnoreCase(requesterEmail)) {
            throw new SecurityException("Not authorized to update this profile");
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName().trim());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim());
        }

        if (request.getAddresses() != null) {
            List<Address> existing = user.getAddresses();
            if (existing != null) {
                for (Address addr : existing) {
                    addressRepository.delete(addr);
                }
            }

            List<Address> newList = new ArrayList<>();
            boolean hasDefault = false;
            for (UpdateProfileRequest.AddressDto dto : request.getAddresses()) {
                Address address = new Address();
                address.setUser(user);
                address.setName(dto.getName());
                address.setStreet(dto.getStreet());
                address.setCity(dto.getCity());
                address.setState(dto.getState());
                address.setPincode(dto.getPincode());
                address.setLandmark(dto.getLandmark());
                address.setType(parseType(dto.getType()));
                boolean isDefault = Boolean.TRUE.equals(dto.getIsDefault());
                address.setDefault(isDefault);
                hasDefault = hasDefault || isDefault;
                newList.add(addressRepository.save(address));
            }
            if (!hasDefault && !newList.isEmpty()) {
                newList.get(0).setDefault(true);
            }
            user.setAddresses(newList);
        }

        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    /**
     * Get user by ID for internal service calls
     */
    public UserDto getUserById(String userId) {
        User user = userRepository.findByIdWithAddresses(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        return userMapper.toDto(user);
    }

    private Address.AddressType parseType(String type) {
        if (type == null) return Address.AddressType.HOME;
        String t = type.toUpperCase(Locale.ROOT);
        for (Address.AddressType at : Address.AddressType.values()) {
            if (Objects.equals(at.name(), t)) return at;
        }
        return Address.AddressType.HOME;
    }
}


