package com.codecool.queststore.DAO;

import com.codecool.queststore.model.user.Admin;

import java.util.List;

public interface AdminDAO {
    void add(Admin admin);
    void remove(Admin admin);
    void update(Admin admin);
    Admin getAdmin(int id);
    List<Admin> getAllAdmins();
}
