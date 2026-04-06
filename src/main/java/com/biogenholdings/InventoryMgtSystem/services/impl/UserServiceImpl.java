package com.biogenholdings.InventoryMgtSystem.services.impl;

import com.biogenholdings.InventoryMgtSystem.dtos.*;
import com.biogenholdings.InventoryMgtSystem.enums.FilterEnum;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

        if (role == UserRole.ADMIN) {
            // If they are, check if one already exists in the DB
            if (userRepository.existsByRoleAndIsDeletedFalse(UserRole.ADMIN)) {
                throw new RuntimeException("Initial setup already completed. Only one Super Admin is allowed.");
            }
        }

        User userToSave = User.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .isTempPassword(false)
                .isDeleted(false)
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

        if (empRegisterRequest.getRole() == UserRole.ADMIN) {
            // If they are, check if one already exists in the DB
            if (userRepository.existsByRoleAndIsDeletedFalse(UserRole.ADMIN)) {
                throw new RuntimeException("Initial setup already completed. Only one Super Admin is allowed.");
            }
        }

        String tempPassword = UUID.randomUUID().toString().substring(0, 8);

            User userToSave = User.builder()
                    .name(empRegisterRequest.getName())
                    .email(empRegisterRequest.getEmail())
                    .role(empRegisterRequest.getRole())
                    .password(passwordEncoder.encode(tempPassword))
                    .isTempPassword(true)
                    .userStatus(UserStatus.PENDING)
                    .nicNumber(empRegisterRequest.getNicNumber())
                    .address(empRegisterRequest.getAddress())
                    .isDeleted(false)
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

        List<User> users = userRepository.findByIsDeletedFalse(Sort.by(Sort.Direction.DESC, "id"));

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
    public Response resetTempPassword(ResetPasswordDto resetPasswordDto) {
        User user = userRepository.findById(resetPasswordDto.getUserId())
                .orElseThrow(()-> new NotFoundException("User Not Found"));

        if(user.getIsTempPassword()){
            user.setPassword(passwordEncoder.encode(resetPasswordDto.getPassword()));
            user.setIsTempPassword(false);
            if(user.getUserStatus() != UserStatus.ACTIVE) {
                user.setUserStatus(UserStatus.ACTIVE);
            }
        }

        userRepository.save(user);

        return Response.builder()
                .message("Password Changed Successfully")
                .status(200)
                .build();
    }

    @Override
    public Response generateTempPasswordForForgetPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not Found"));

        String tempPassword = UUID.randomUUID().toString().substring(0, 8);

        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setIsTempPassword(true);

        userRepository.save(user);

        emailService.sendEmployeeCredentials(email,tempPassword);

        return Response.builder()
                .status(200)
                .message("Temp password generated")
                .build();

    }

    @Override
    public Response softDeleteEmployee(Long id, Long userId) {

        User employee = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Employee Not Found"));

        User admin = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User Not Found"));

        employee.setIsDeleted(true);
        employee.setDeletedBy(admin);
        employee.setEmail(employee.getEmail() + "_deleted_ " + System.currentTimeMillis());

        userRepository.save(employee);

        return Response.builder()
                .status(204)
                .message("Employee Deleted Successfully")
                .build();
    }

    @Override
    public Response searchEmployee(String searchKey) {

        List<User> users = userRepository.findByNameContaining(searchKey);

        List<UserDTO> userDTOList = modelMapper.map(users, new TypeToken<List<UserDTO>>() {}.getType());

        return Response.builder()
                .status(200)
                .message("success")
                .users(userDTOList)
                .build();
    }

    @Override
    public Response getPaginatedEmployees(Integer page, Integer size, FilterEnum filter) {
        Pageable pageable = PageRequest.of(page,size,getSortByFilter(filter));

        Page<User> userPage = userRepository.findByIsDeletedFalse(pageable);

        List<User> userList = userPage.getContent();

        List<UserDTO> userDTOList = modelMapper.map(userList, new TypeToken<List<UserDTO>>() {}.getType());

        return Response.builder()
                .status(200)
                .message("Success")
                .users(userDTOList)
                .totalPages(userPage.getTotalPages())
                .totalElements(userPage.getTotalElements())
                .build();
    }

    @Override
    public Boolean AdminRoleExists() {
        return userRepository.existsByRoleAndIsDeletedFalse(UserRole.ADMIN);
    }

    private Sort getSortByFilter(FilterEnum filter) {
        log.info(filter.toString());
        if (filter == FilterEnum.DESC) {
            return Sort.by(Sort.Direction.DESC, "name");
        } else {
            return Sort.by(Sort.Direction.ASC, "name");
        }
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
