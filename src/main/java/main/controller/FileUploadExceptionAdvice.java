package main.controller;

import main.api.responses.ApiResponseBody;
import main.api.responses.bodies.ErrorsBody;
import main.model.enums.Errors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
public class FileUploadExceptionAdvice {

    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxImageSize;

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponseBody> handleMaxSizeException () {
        return ResponseEntity.badRequest().body(ApiResponseBody.builder()
                .result(false)
                .errors(ErrorsBody.builder()
                        .image(Errors.IMAGE_IS_BIG.getTitle())
                        .build())
                .build());
    }
}
