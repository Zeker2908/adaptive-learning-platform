package ru.zeker.sandbox.client;

import feign.FeignException;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import ru.zeker.common.dto.judge0.request.SubmissionRequest;
import ru.zeker.common.dto.judge0.response.SubmissionResponse;

@FeignClient(name = "judge0", url = "${judge0.url:https://judge0-ce.p.rapidapi.com}")
public interface Judge0Client {


    @Retryable(
            retryFor = {FeignException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @PostMapping(value = "/submissions")
    SubmissionResponse submitCode(
            @RequestBody @Valid SubmissionRequest request,
            @RequestParam("base64_encoded") boolean base64Encoded,
            @RequestParam("wait") boolean wait,
            @RequestHeader(value = "x-rapidapi-key") String authToken,
            @RequestHeader(value = "x-rapidapi-host") String host);


    @Retryable(
            retryFor = {FeignException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @PostMapping(value = "/submissions")
    SubmissionResponse submitCode(
            @RequestBody @Valid SubmissionRequest request,
            @RequestParam("base64_encoded") boolean base64Encoded,
            @RequestParam("wait") boolean wait);
}