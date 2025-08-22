package com.pnu.springsecuritytest.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ProblemDetail toProblemDetail(HttpStatus status, Exception e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                status,
                e.getMessage()
        );
        problemDetail.setType(URI.create("https://example.com/errors"));
        return problemDetail;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgumentException(IllegalArgumentException e) {
        var detail = toProblemDetail(HttpStatus.BAD_REQUEST, e);
        detail.setTitle("잘못된 요청 오류");
        return detail;
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ProblemDetail handleNoSuchElementException(NoSuchElementException e) {
        var detail = toProblemDetail(HttpStatus.NOT_FOUND, e);
        detail.setTitle("리소스 미발견 오류");
        return detail;
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthenticationException(AuthenticationException e) {
        var detail = toProblemDetail(HttpStatus.UNAUTHORIZED, e);
        detail.setTitle("인증 실패 오류");
        return detail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneralException(Exception e) {
        var detail = toProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, e);
        detail.setTitle("서버 내부 오류");
        return detail;
    }
}
