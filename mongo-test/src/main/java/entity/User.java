package entity;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
public class User extends BaseEntity {
    String name;
    Integer age;
    String profilePictureUrl;
}
