package com.model;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;

@Document(collection = "users")
@Data
@NoArgsConstructor
public class AppUser {
    @Id
    private String id;
    private String username;
    private String password;

    private Role role;
    
}
