package com.rabbuy.ecommerce.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "question_question") // 表名
public class Question {

    @Id
    @GeneratedValue(generator = "uuid-hex")
    @GenericGenerator(name = "uuid-hex", strategy = "org.hibernate.id.UUIDHexGenerator")
    @Column(name = "question_id", updatable = false, nullable = false, columnDefinition = "CHAR(32)")
    private String questionId;

    @Lob // 对应 TextField
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content; // 问题内容

    @Column(name = "created_time", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private OffsetDateTime createdTime; // 提问时间

    @Column(name = "updated_time", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private OffsetDateTime updatedTime; // 更新时间

    // 多对一关系：多个问题可以来自一个用户
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 多对一关系：多个问题可以关于一个产品
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 一对多关系：一个问题可以有多个回答
    // related_name='answers' 在 Answer 模型中定义
    // cascade = CascadeType.ALL: 删除 Question 时级联删除其 Answers
    // orphanRemoval = true: 从 questions.answers 集合移除 Answer 时删除该 Answer
    // OrderBy: 实现 Meta.ordering = ['-created_time']
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdTime DESC")
    private List<Answer> answers = new ArrayList<>();

    // JPA 需要无参构造函数
    public Question() {
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

    // --- Helper methods for managing Answers ---
    public void addAnswer(Answer answer) {
        answers.add(answer);
        answer.setQuestion(this);
    }

    public void removeAnswer(Answer answer) {
        answers.remove(answer);
        answer.setQuestion(null);
    }

    // --- Getters and Setters ---
    public String getQuestionId() { return questionId; }
    public void setQuestionId(String questionId) { this.questionId = questionId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public OffsetDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(OffsetDateTime createdTime) { this.createdTime = createdTime; }
    public OffsetDateTime getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(OffsetDateTime updatedTime) { this.updatedTime = updatedTime; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public List<Answer> getAnswers() { return answers; }
    public void setAnswers(List<Answer> answers) { this.answers = answers; }

    @Override
    public String toString() {
        return "Question " + questionId + ": " + content.substring(0, Math.min(content.length(), 30));
    }
}