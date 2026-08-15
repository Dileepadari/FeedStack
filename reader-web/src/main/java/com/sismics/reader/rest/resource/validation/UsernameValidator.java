package com.sismics.reader.rest.resource.validation;

import com.sismics.reader.rest.resource.ValidationException;
import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;
import java.util.regex.Pattern;

public class UsernameValidator extends BaseValidator {
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("[a-zA-Z0-9_]+");

    private static final int LENGTH_MIN = 3;

    private static final int LENGTH_MAX = 50;

    @Override
    public void validate(RegistrationRequest request) throws ValidationException {
        // Strip surrounding whitespace first, otherwise "   bb  " passes the length check.
        String username = StringUtils.strip(request.getUsername());
        request.setUsername(username);

        if (StringUtils.isEmpty(username)) {
            throw new ValidationException("username must be set");
        }
        if (username.length() < LENGTH_MIN) {
            throw new ValidationException(
                    MessageFormat.format("username must be more than {0} characters", LENGTH_MIN));
        }
        if (username.length() > LENGTH_MAX) {
            throw new ValidationException(
                    MessageFormat.format("username must be less than {0} characters", LENGTH_MAX));
        }
        if (!ALPHANUMERIC_PATTERN.matcher(username).matches()) {
            throw new ValidationException("username must have only alphanumeric or underscore characters");
        }

        validateNext(request);
    }
}
