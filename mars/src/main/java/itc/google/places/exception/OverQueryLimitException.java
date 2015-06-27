package itc.google.places.exception;

import itc.google.places.Statuses;

public class OverQueryLimitException extends GooglePlacesException {
    public OverQueryLimitException(String errorMessage) {
        super(Statuses.STATUS_OVER_QUERY_LIMIT, errorMessage);
    }

    public OverQueryLimitException() {
        this(null);
    }
}
