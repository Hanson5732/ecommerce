package com.rabbuy.ecommerce.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;


@Entity
@Table(name = "user_user")
public class User {

    @Id
    @GeneratedValue(generator = "uuid-hex")
    @GenericGenerator(name = "uuid-hex", strategy = "org.hibernate.id.UUIDHexGenerator")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "CHAR(32)")
    private String id;

    @Column(name = "username", nullable = false, unique = true, length = 150)
    private String username;

    @Column(name = "password", nullable = false, length = 128)
    private String password;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "first_name", length = 150)
    private String firstName;

    @Column(name = "last_name", length = 150)
    private String lastName;

    @Column(name = "is_staff", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isStaff = false;

    @Column(name = "is_superuser", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isSuperuser = false;

    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean isActive = true;

    // Use OffsetDateTime for timezone-aware timestamp, equivalent to Django's DateTimeField with timezone support
    @Column(name = "date_joined", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private OffsetDateTime dateJoined;

    @Column(name = "last_login", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime lastLogin;

    @Column(name = "sex", nullable = false, length = 1, columnDefinition = "VARCHAR(1) DEFAULT '3'")
    private String sex = "3"; // "1": man, "2": woman, "3": unknown

    @Column(name = "birth")
    private LocalDate birth;

    @Column(name = "phone", length = 14)
    private String phone;

    @Column(name = "profile_picture", length = 255, columnDefinition = "VARCHAR(255) DEFAULT 'http://localhost:8080/media/200.png'") // Consider making this configurable
    private String profilePicture = "http://localhost:8080/media/200.png";

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Question> questions = new ArrayList<>(); // related_name='questions'

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Answer> answers = new ArrayList<>(); // related_name='answers'

    public User() {
    }

    @PrePersist
    protected void onCreate() {
        if (dateJoined == null) {
            dateJoined = OffsetDateTime.now();
        }
    }

    // --- Getters and Setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public boolean isStaff() { return isStaff; }
    public void setStaff(boolean staff) { isStaff = staff; }
    public boolean isSuperuser() { return isSuperuser; }
    public void setSuperuser(boolean superuser) { isSuperuser = superuser; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public OffsetDateTime getDateJoined() { return dateJoined; }
    public void setDateJoined(OffsetDateTime dateJoined) { this.dateJoined = dateJoined; }
    public OffsetDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(OffsetDateTime lastLogin) { this.lastLogin = lastLogin; }
    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }
    public LocalDate getBirth() { return birth; }
    public void setBirth(LocalDate birth) { this.birth = birth; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }
    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }
    public List<Answer> getAnswers() { return answers; }
    public void setAnswers(List<Answer> answers) { this.answers = answers; }

    @Override
    public String toString() {
        return username;
    }
}