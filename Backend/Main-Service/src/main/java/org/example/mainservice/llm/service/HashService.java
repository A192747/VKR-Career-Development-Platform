package org.example.mainservice.llm.service;

import java.security.NoSuchAlgorithmException;

public interface HashService {
    String hash(String string) throws NoSuchAlgorithmException;
}
