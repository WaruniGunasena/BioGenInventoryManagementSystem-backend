package com.biogenholdings.InventoryMgtSystem.services.impl;

import com.biogenholdings.InventoryMgtSystem.dtos.*;
import com.biogenholdings.InventoryMgtSystem.enums.UserRole;
import com.biogenholdings.InventoryMgtSystem.enums.UserStatus;
import com.biogenholdings.InventoryMgtSystem.exceptions.InvalidCredentialsException;
import com.biogenholdings.InventoryMgtSystem.exceptions.NotFoundException;
import com.biogenholdings.InventoryMgtSystem.models.User;
import com.biogenholdings.InventoryMgtSystem.repositories.UserRepository;
import com.biogenholdings.InventoryMgtSystem.security.JWTUtils;
import com.biogenholdings.InventoryMgtSystem.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j

public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final JWTUtils jwtUtils;
    private final EmailServiceImpl emailService;

    @Override
    public Response registerUser(RegisterRequest registerRequest) {

        UserRole role = UserRole.ADMIN;

        if (registerRequest.getRole() != null){
            role = registerRequest.getRole();
        }

        User userToSave = User.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(role)
                .build();

        userRepository.save(userToSave);

        return Response.builder()
                .status(200)
                .message("User was successfully registered")
                .build();
    }

    @Override
//    @PreAuthorize("hasAny('ADMIN')")
    public Response registerEmployee(EmpRegisterRequest empRegisterRequest) {

        String tempPassword = UUID.randomUUID().toString().substring(0, 8);

            User userToSave = User.builder()
                    .name(empRegisterRequest.getName())
                    .email(empRegisterRequest.getEmail())
                    .role(empRegisterRequest.getRole())
                    .password(tempPassword)
                    .isTempPassword(true)
                    .userStatus(UserStatus.PENDING)
                    .nicNumber(empRegisterRequest.getNicNumber())
                    .address(empRegisterRequest.getAddress())
                    .build();

            userRepository.save(userToSave);

            emailService.sendEmployeeCredentials(
                    userToSave.getEmail(),
                    tempPassword
            );

            return Response.builder()
                    .status(200)
                    .message("User was successfully registered")
                    .build();
        }

    @Override
    public Response loginUser(LoginRequest loginRequest) {

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new NotFoundException("Email Not Found"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())){
            throw new InvalidCredentialsException("Password does not Match");
        }

        String token = jwtUtils.generateToken(user.getEmail());

        return Response.builder()
                .status(200)
                .message("User Logged in successfully")
                .role(user.getRole())
                .token(token)
                .expirationTime("6 months")
                .build();
    }

    @Override
    public Response getAllUsers() {

        List<User> users = userRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        List<UserDTO> userDTOS = modelMapper.map(users, new TypeToken<List<UserDTO>>() {}.getType());

        return Response.builder()
                .status(200)
                .message("success")
                .users(userDTOS)
                .build();
    }

    @Override
    public User getCurrentLoggedInUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        return userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("User Not Found"));
    }

    @Override
    public Response resetTempPassword(Long userId,String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new NotFoundException("User Not Found"));

        if(user.getIsTempPassword()){
            user.setPassword(password);
            user.setIsTempPassword(false);
            user.setUserStatus(UserStatus.ACTIVE);
        }

        userRepository.save(user);

        return Response.builder()
                .message("Password Changed Sucessfully")
                .status(200)
                .build();
    }

    @Override
    public Response getUserById(Long id) {

      User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User Not Found"));

      UserDTO userDTO = modelMapper.map(user, UserDTO.class);

      return Response.builder()
                .status(200)
                .message("success")
                .user(userDTO)
                .build();
    }

    @Override
    public Response updateUser(Long id, UserDTO userDTO) {

        User existingUser = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User Not Found"));

        if (userDTO.getEmail() != null) existingUser.setEmail(userDTO.getEmail());
        if (userDTO.getPhoneNumber() != null) existingUser.setPhoneNumber(userDTO.getPhoneNumber());
        if (userDTO.getName() != null) existingUser.setName(userDTO.getName());
        if (userDTO.getRole() != null) existingUser.setRole(userDTO.getRole());

        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()){
            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        userRepository.save(existingUser);

        return Response.builder()
                .status(200)
                .message("User Successfully Updated")
                .build();
    }

    @Override
    public Response deleteUser(Long id) {

        userRepository.findById(id).orElseThrow(() -> new NotFoundException("User Not Found"));
        userRepository.deleteById(id);

        return Response.builder()
                .status(200)
                .message("User successfully Deleted")
                .build();
    }

}
