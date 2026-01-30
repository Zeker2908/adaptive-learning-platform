package ru.zeker.notification.service.handlers;

import ru.zeker.common.dto.kafka.notification.EmailEvent;
import ru.zeker.notification.dto.EmailContext;

public interface EmailContextStrategy {
    EmailContext handle(EmailEvent event);
}
