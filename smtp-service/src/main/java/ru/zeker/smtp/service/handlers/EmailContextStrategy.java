package ru.zeker.smtp.service.handlers;

import ru.zeker.common.dto.kafka.smtp.EmailEvent;
import ru.zeker.smtp.dto.EmailContext;

public interface EmailContextStrategy {
    EmailContext handle(EmailEvent event);
}
