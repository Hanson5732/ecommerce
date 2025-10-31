package com.rabbuy.ecommerce.resource;

import com.rabbuy.ecommerce.dto.AnswerAddDto;
import com.rabbuy.ecommerce.dto.AnswerResponseDto;
import com.rabbuy.ecommerce.dto.QuestionAddDto;
import com.rabbuy.ecommerce.dto.QuestionResponseDto;
import com.rabbuy.ecommerce.service.QuestionService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.List;
import java.util.UUID;

@Path("/question")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class QuestionResource {

    @Inject
    private QuestionService questionService;

    @Inject // 注入 JWT 以获取当前用户信息
    private JsonWebToken jwtPrincipal;

    /**
     * 获取产品的问题列表
     * @param productId
     * @return
     */
    @GET
    @Path("/product/{productId}/list")
    public Response getProductQuestions(@PathParam("productId") UUID productId) {
        List<QuestionResponseDto> questions = questionService.getQuestionsByProductId(productId);
        return Response.ok(questions).build();
    }

    /**
     * 为产品添加新问题（需要认证）
     * @param productId
     * @param questionDto
     * @return
     */
    @POST
    @Path("/product/{productId}/add")
    @RolesAllowed({"admin", "customer"}) // (@token_required)
    public Response addQuestion(@PathParam("productId") UUID productId, QuestionAddDto questionDto) {
        UUID currentUserId = UUID.fromString(jwtPrincipal.getName());

        // 异常 (NotFound, IllegalArgument) 将被 GlobalExceptionMapper 捕获
        QuestionResponseDto newQuestion = questionService.addQuestion(productId, currentUserId, questionDto);
        return Response.status(Response.Status.CREATED).entity(newQuestion).build();
    }

    /**
     * 为问题添加新回答（需要认证）
     * @param questionId
     * @param answerDto
     * @return
     */
    @POST
    @Path("/answer/{questionId}/add")
    @RolesAllowed({"admin", "customer"}) // (@token_required)
    public Response addAnswer(@PathParam("questionId") UUID questionId, AnswerAddDto answerDto) {
        UUID currentUserId = UUID.fromString(jwtPrincipal.getName());

        // 异常 (NotFound, IllegalArgument) 将被 GlobalExceptionMapper 捕获
        AnswerResponseDto newAnswer = questionService.addAnswer(questionId, currentUserId, answerDto);
        return Response.status(Response.Status.CREATED).entity(newAnswer).build();
    }
}