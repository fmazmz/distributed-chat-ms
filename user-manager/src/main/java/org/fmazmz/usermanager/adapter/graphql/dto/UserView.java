package org.fmazmz.usermanager.adapter.graphql.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserView {
    private final String id;
    private final String userName;
    private final String email;
}
