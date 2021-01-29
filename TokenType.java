/*
 * Copyright (C) 2016 Richard Blumenthal, All Rights Reserved
 * Dissemination or reproduction of this code is strictly forbidden
 * unless prior written permission is obtained from Dr. Blumenthal
 */
package edu.regis.cs390.scan.tok;

/**
 * Defines the Token types allowed in Scott's (2016) Simple Calculator
 * LL(1) grammar (with minor extensions).
 * 
 * @author Rickb
 */
public enum TokenType {
    ASSIGN,  // Lexeme: ":="
    BOOLEAN,
    COLON,         // Not defined in Scott's grammar
    DIVIDE, 
    EOF,
    EQUAL,         // Not defined in Scott's grammar
    ERROR,
    FALSE,
    ID,
    INTEGER,
    LPAREN,
    NOT,
    NUMBER,
    MINUS, 
    MULTIPLY,
    PLUS, 
    READ, 
    RPAREN,
    TRUE,
    WRITE;
}