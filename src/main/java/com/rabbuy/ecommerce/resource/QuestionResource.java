package com.rabbuy.ecommerce.resource;

import com.rabbuy.ecommerce.dto.*;
import com.rabbuy.ecommerce.service.QuestionService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.Context;

import java.util.List;

@Path("/question")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class QuestionResource {

    @Inject
    private QuestionService questionService;

    @Context
    private SecurityContext securityContext;

    /**
     * 获取产品的问题列表
     * @param productId
     * @return
     */
    @GET
    @Path("/product/{productId}/list")
    public Response getProductQuestions(@PathParam("productId") String productId) {
        List<QuestionResponseDto> questions = questionService.getQuestionsByProductId(productId);
        return Response.ok(ApiResponseDto.success(questions)).build();
    }

    /**
     * 为产品添加新问题（需要认证）
     * @param productId
     * @param questionDto
     * @return
     */
    @POST
    @Path("/product/{productId}/add")
    public Response addQuestion(@PathParam("productId") String productId, QuestionAddDto questionDto) {
        String currentUserId = securityContext.getUserPrincipal().getName();

        // 异常 (NotFound, IllegalArgument) 将被 GlobalExceptionMapper 捕获
        QuestionResponseDto newQuestion = questionService.addQuestion(productId, currentUserId, questionDto);
        return Response.status(Response.Status.CREATED).entity(ApiResponseDto.success(newQuestion)).build();
    }

    /**
     * 为问题添加新回答（需要认证）
     * @param questionId
     * @param answerDto
     * @return
     */
    @POST
    @Path("/answer/{questionId}/add")
    public Response addAnswer(@PathParam("questionId") String questionId, AnswerAddDto answerDto) {
        String currentUserId = securityContext.getUserPrincipal().getName();

        // 异常 (NotFound, IllegalArgument) 将被 GlobalExceptionMapper 捕获
        AnswerResponseDto newAnswer = questionService.addAnswer(questionId, currentUserId, answerDto);
        return Response.status(Response.Status.CREATED).entity(ApiResponseDto.success(newAnswer)).build();
    }
}