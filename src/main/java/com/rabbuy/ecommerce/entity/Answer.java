package com.rabbuy.ecommerce.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.OffsetDateTime;

@Entity
@Table(name = "question_answer")
public class Answer {

    @Id
    @GeneratedValue(generator = "uuid-hex")
    @GenericGenerator(name = "uuid-hex", strategy = "org.hibernate.id.UUIDHexGenerator")
    @Column(name = "answer_id", updatable = false, nullable = false, columnDefinition = "CHAR(32)")
    private String answerId;

    @Lob // 对应 TextField
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_time", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private OffsetDateTime createdTime;

    @Column(name = "updated_time", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private OffsetDateTime updatedTime;

    // 多对一关系：多个回答可以属于一个问题
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    // 多对一关系：多个回答可以来自一个用户
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // JPA 需要无参构造函数
    public Answer() {
    }

    // 生命周期回调
    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdTime == null) {
            createdTime = now;
        }
        updatedTime = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTime = OffsetDateTime.now();
    }

    // --- Getters and Setters ---
    public String getAnswerId() { return answerId; }
    public void setAnswerId(String answerId) { this.answerId = answerId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public OffsetDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(OffsetDateTime createdTime) { this.createdTime = createdTime; }
    public OffsetDateTime getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(OffsetDateTime updatedTime) { this.updatedTime = updatedTime; }
    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    @Override
    public String toString() {
        return "Answer " + answerId + ": " + content.substring(0, Math.min(content.length(), 30));
    }
}