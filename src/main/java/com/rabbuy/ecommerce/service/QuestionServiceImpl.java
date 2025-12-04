package com.rabbuy.ecommerce.service;

import com.rabbuy.ecommerce.dao.AnswerDao;
import com.rabbuy.ecommerce.dao.ProductDao;
import com.rabbuy.ecommerce.dao.QuestionDao;
import com.rabbuy.ecommerce.dao.UserDao;
import com.rabbuy.ecommerce.dto.AnswerAddDto;
import com.rabbuy.ecommerce.dto.AnswerResponseDto;
import com.rabbuy.ecommerce.dto.CommentUserDto;
import com.rabbuy.ecommerce.dto.QuestionAddDto;
import com.rabbuy.ecommerce.dto.QuestionResponseDto;
import com.rabbuy.ecommerce.entity.Answer;
import com.rabbuy.ecommerce.entity.Product;
import com.rabbuy.ecommerce.entity.Question;
import com.rabbuy.ecommerce.entity.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class QuestionServiceImpl implements QuestionService {

    @Inject
    private QuestionDao questionDao;
    @Inject
    private AnswerDao answerDao;
    @Inject
    private UserDao userDao;
    @Inject
    private ProductDao productDao;

    // --- 辅助方法 ---

    /**
     * 简单模拟 Django 的 strip_tags
     *
     * 警告：在生产环境中，应使用更强大的库 (如 JSoup) 来防止 XSS
     */
    private String stripTags(String html) {
        if (html == null) return null;
        return html.replaceAll("<[^>]*>", ""); // 移除所有 HTML 标签
    }

    // 辅助方法：将 User 转换为 DTO
    private CommentUserDto toUserDto(User user) {
        if (user == null) return null;
        return new CommentUserDto(user.getUsername(), user.getProfilePicture());
    }

    // 辅助方法：将 Answer 转换为 DTO
    private AnswerResponseDto toAnswerDto(Answer answer) {
        return new AnswerResponseDto(
                answer.getAnswerId(),
                toUserDto(answer.getUser()), // 嵌套 User DTO
                answer.getContent(),
                answer.getCreatedTime()
        );
    }

    // 辅助方法：将 Question 转换为 DTO
    private QuestionResponseDto toQuestionDto(Question question) {
        List<AnswerResponseDto> answerDtos = question.getAnswers().stream()
                .map(this::toAnswerDto) // 转换嵌套的 Answer 列表
                .collect(Collectors.toList());

        return new QuestionResponseDto(
                question.getQuestionId(),
                toUserDto(question.getUser()), // 嵌套 User DTO
                question.getContent(),
                question.getCreatedTime(),
                answerDtos,
                answerDtos.size() // answers_count
        );
    }

    // --- 服务实现 ---

    @Override
    public List<QuestionResponseDto> getQuestionsByProductId(String productId) {
        //
        // DAO 层负责预加载 (JOIN FETCH) 所有关联数据
        List<Question> questions = questionDao.findByProductId(productId);

        // Service 层负责将实体转换为 DTO
        return questions.stream()
                .map(this::toQuestionDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public QuestionResponseDto addQuestion(String productId, String userId, QuestionAddDto dto) throws NotFoundException {
        //
        // 1. 验证输入
        if (dto.content() == null || dto.content().trim().isEmpty()) {
            throw new IllegalArgumentException("内容不能为空");
        }

        // 2. 获取关联实体
        User user = userDao.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Product product = productDao.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        // 3. 清理内容并创建
        String content = stripTags(dto.content()); //

        Question newQuestion = new Question();
        newQuestion.setUser(user);
        newQuestion.setProduct(product);
        newQuestion.setContent(content);
        // (时间戳由 @PrePersist 自动设置)

        questionDao.save(newQuestion);

        // 4. 返回新创建问题的 DTO
        return toQuestionDto(newQuestion);
    }

    @Override
    @Transactional
    public AnswerResponseDto addAnswer(String questionId, String userId, AnswerAddDto dto) throws NotFoundException {
        //
        // 1. 验证输入
        if (dto.content() == null || dto.content().trim().isEmpty()) {
            throw new IllegalArgumentException("内容不能为空");
        }

        // 2. 获取关联实体
        User user = userDao.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Question question = questionDao.findById(questionId)
                .orElseThrow(() -> new NotFoundException("Question not found"));

        // 3. 清理内容并创建
        String content = stripTags(dto.content()); //

        Answer newAnswer = new Answer();
        newAnswer.setUser(user);
        newAnswer.setQuestion(question);
        newAnswer.setContent(content);
        // (时间戳由 @PrePersist 自动设置)

        answerDao.save(newAnswer);

        // 4. 返回新创建回答的 DTO
        return toAnswerDto(newAnswer);
    }
}