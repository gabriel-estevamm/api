package com.api.voting.model.user;

import com.api.voting.model.enums.Role;

public record UserRecord(
        Integer id,
        String email,
        String fullName,
        Role role,
        Boolean enabled,
        Boolean deleted
) {}
