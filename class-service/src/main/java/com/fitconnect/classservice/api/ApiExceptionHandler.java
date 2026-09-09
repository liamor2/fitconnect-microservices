package com.fitconnect.classservice.api;
import com.fitconnect.classservice.domain.NoSpotsAvailableException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestControllerAdvice public class ApiExceptionHandler {
    @ExceptionHandler(NoSpotsAvailableException.class) ResponseEntity<ProblemDetail> conflict(NoSpotsAvailableException e){return response(HttpStatus.CONFLICT,e.getMessage());}
    @ExceptionHandler(IllegalArgumentException.class) ResponseEntity<ProblemDetail> notFound(IllegalArgumentException e){return response(HttpStatus.NOT_FOUND,e.getMessage());}
    private ResponseEntity<ProblemDetail> response(HttpStatus s,String detail){ProblemDetail p=ProblemDetail.forStatusAndDetail(s,detail);p.setTitle(s.getReasonPhrase());return ResponseEntity.status(s).body(p);}
}
