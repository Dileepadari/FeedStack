package com.sismics.reader.rest.resource.validation;

import com.sismics.reader.rest.resource.ValidationException;
import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;
import java.util.regex.Pattern;

public class EmailValidator extends BaseValidator {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(".+@.+\\..+");

    private static final int LENGTH_MIN = 3;

    private static final int LENGTH_MAX = 50;

    @Override
    public void validate(RegistrationRequest request) throws ValidationException {
        String email = StringUtils.strip(request.getEmail());
        request.setEmail(email);

        if (StringUtils.isEmpty(email)) {
            throw new ValidationException("email must be set");
        }
        if (email.length() < LENGTH_MIN) {
            throw new ValidationException(
                    MessageFormat.format("email must be more than {0} characters", LENGTH_MIN));
        }
        if (email.length() > LENGTH_MAX) {
            throw new ValidationException(
                    MessageFormat.format("email must be less than {0} characters", LENGTH_MAX));
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("email must be an email");
        }

        // Uniqueness is a database concern, not a format one, and it has to run after the
        // username availability check so a duplicate username still reports as such.
        // RegistrationFacade owns it.

        validateNext(request);
    }
}
