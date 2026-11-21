package com.dadops.soccer.web;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MultipartException;

/**
 * The servlet container rejects an oversized or malformed multipart request before
 * the controller runs. Send the user back to the page with a short notice rather
 * than letting the default error page surface the exception.
 */
@ControllerAdvice
public class UploadRejectedAdvice {

    @ExceptionHandler(MultipartException.class)
    public String uploadRejected() {
        return "redirect:/?uploadRejected";
    }
}
