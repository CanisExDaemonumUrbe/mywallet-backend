package com.cedu.api;

import com.cedu.exception.InternalException;
import com.cedu.exception.InvalidAccountKindException;
import com.cedu.exception.InvalidUserException;
import com.cedu.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail notFound(NotFoundException e, HttpServletRequest request) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        pd.setTitle("Not Found");
        pd.setProperty("instance", request.getRequestURI());
        return pd;
    }

    @ExceptionHandler(InvalidUserException.class)
    public ProblemDetail invalidUser(InvalidUserException e, HttpServletRequest request) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        pd.setTitle("Invalid User");
        //pd.setProperty("instance", request.getRequestURI());
        return pd;
    }

    @ExceptionHandler(InvalidAccountKindException.class)
    public ProblemDetail invalidAccountKind(InvalidAccountKindException e, HttpServletRequest request) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        pd.setTitle("Invalid Account Kind");
        //pd.setProperty("instance", request.getRequestURI());
        return pd;
    }

    @ExceptionHandler(InternalException.class)
    public ProblemDetail internal(InternalException e, HttpServletRequest request) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        pd.setTitle("Internal Server Error");
        //pd.setProperty("instance", request.getRequestURI());
        return pd;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail generic(Exception ex, HttpServletRequest req) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        pd.setTitle("Internal Server Error");
        //pd.setProperty("instance", req.getRequestURI());
        return pd;
    }
}
