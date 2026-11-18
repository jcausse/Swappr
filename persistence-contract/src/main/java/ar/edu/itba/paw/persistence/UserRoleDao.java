package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Role;
import ar.edu.itba.paw.model.User;

import java.util.List;

public interface UserRoleDao {
    void addRole(User user, Role role);
    List<Role> getRoles(User user);
    boolean hasRole(User user, Role role);
}
