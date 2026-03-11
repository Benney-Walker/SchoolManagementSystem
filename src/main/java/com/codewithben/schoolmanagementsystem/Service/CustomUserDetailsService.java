package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Entity.Staffs;
import com.codewithben.schoolmanagementsystem.Repository.StaffsRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final StaffsRepository staffsRepository;

    public CustomUserDetailsService(StaffsRepository staffsRepository) {
        this.staffsRepository = staffsRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String staffId) throws UsernameNotFoundException {
        Staffs staff = staffsRepository.findByStaffId(staffId)
                .orElseThrow(() -> new UsernameNotFoundException("Staff not found"));

        List<SimpleGrantedAuthority> authorities = staff.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.toString()))
                .toList();
        return new User(staff.getStaffId(),
                staff.getPassword(),
                authorities);
    }
}
