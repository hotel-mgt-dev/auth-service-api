package com.hotel_mgt_system.auth_service_api.util;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class PasswordGenerator {

    private static final String UPPER_CASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER_CASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL_CHARACTERS = "!@#$%^&*()_+";

    private static final String ALL_CHAR = UPPER_CASE + LOWER_CASE + DIGITS + SPECIAL_CHARACTERS;

    public final String generatePassword() {
        StringBuilder password = new StringBuilder(6);
        Random random = new Random();

        password.append(UPPER_CASE.charAt(random.nextInt(UPPER_CASE.length())));
        password.append(LOWER_CASE.charAt(random.nextInt(LOWER_CASE.length())));
        password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        password.append(SPECIAL_CHARACTERS.charAt(random.nextInt(SPECIAL_CHARACTERS.length())));

        for (int i = 4; i < 6; i++) {
            password.append(ALL_CHAR.charAt(random.nextInt(SPECIAL_CHARACTERS.length())));
        }
        return shuffleString(password.toString(), random);

    }

    private String shuffleString(String input, Random random) {
        char[] characters = input.toCharArray();
        for (int i = characters.length - 1; i > 0; i--) {
            int randomIndex = random.nextInt(i+1);
            char temp = characters[i];
            characters[i] = characters[randomIndex];
            characters[randomIndex] = temp;
        }
        return new String(characters);
    }


}
