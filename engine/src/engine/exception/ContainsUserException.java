package engine.exception;

import engine.User;

public class ContainsUserException extends Exception {


    private final String EXCEPTION_MESSAGE;

    public ContainsUserException(User userTry) {
        EXCEPTION_MESSAGE = "ERROR! You've tried to insert " + userTry.getName() + ", but the user already exists in the system! Please rename one of the users!";
    }

    @Override
    public String getMessage() {
        return EXCEPTION_MESSAGE;
    }
}
