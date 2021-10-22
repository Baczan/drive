package com.baczan.session_authorization_server.helpers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PasswordValidator {

  private static final String PASSWORD_REGEX_PATTERN =
      "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$";

  public static boolean validatePassword(String password) {

    Pattern passwordPattern = Pattern.compile(PASSWORD_REGEX_PATTERN);

    Matcher passwordMatcher = passwordPattern.matcher(password);

    return passwordMatcher.matches();
  }
}
