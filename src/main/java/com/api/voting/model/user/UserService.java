package com.api.voting.model.user;

import com.api.voting.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public List<UserRecord> findAll() {
        return userRepository.findAll().stream()
                .filter(user -> !user.getDeleted())
                .map(this::toRecord)
                .toList();
    }

    public UserRecord findById(Integer id) {
        return userRepository.findById(id)
                .filter(user -> !user.getDeleted())
                .map(this::toRecord)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));
    }

    public UserRecord save(User user) {
        validateEmail(user);
        return toRecord(userRepository.save(user));
    }

    public void softDelete(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));
        user.setDeleted(true);
        userRepository.save(user);
    }

    private void validateEmail(User user) {
        boolean exists = userRepository.existsByEmailAndDeletedFalse(user.getEmail());

        if (!exists) {
            return; // email não está em uso por usuários ativos
        }

        // Se for atualização, permitir manter o próprio email
        if (user.getId() != null) {
            userRepository.findById(user.getId())
                    .filter(existing -> existing.getEmail().equals(user.getEmail()))
                    .ifPresent(existing -> {
                        // é o mesmo usuário mantendo o próprio email → permitido
                        return;
                    });
        }

        throw new BusinessException("Email já está em uso por outro usuário ativo");
    }


    private UserRecord toRecord(User user) {
        return new UserRecord(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getEnabled(),
                user.getDeleted()
        );
    }
}