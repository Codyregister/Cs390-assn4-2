/*
 * Copyright (C) 2016 Richard Blumenthal, All Rights Reserved
 * Dissemination or reproduction of this code is strictly forbidden
 * unless prior written permission is obtained from Dr. Blumenthal
 */


/* Author: Cody Register
 * CS390 Assignment 4 - I
 * Date: 20210126
 */
package edu.regis.cs390.scan;

import edu.regis.cs390.scan.tok.Token;
import edu.regis.cs390.scan.tok.TokenType;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.util.ArrayList;

/**
 * A lexical analysis Scanner for Scott's (2016) Simple Calculator LL(1) grammar
 * (see TokenType for allowed tokens).
 *
 * @author Rickb
 */
public class Scanner {

   /**
    * True, if line breaks in the source file are defined with a carriage return
    * line feed sequence (i.e. Windows), false if a single CR character is used
    * (i.e. Unix)
    */
   private static final boolean CR_LF = true;

   /**
    * Maximum number of characters allowed in one source file line.
    */
   private static final int MAX_LINE_SIZE = 256;

   /**
    * The input source program's file path
    */
   private final String sourceFile;

   /**
    * Used to read the source file a char at a time (with peek ahead).
    */
   private final PushbackReader buffer;

   /**
    * The current lexeme being read from the source file buffer. (from 0 to
    * endPos)
    */
   private final char[] lexeme;

   /**
    * Current position of the last character in the lexeme being read.
    */
   private int endPos = 0;

   /**
    * True, in the middle of scanning a lexeme waiting for a delimiter.
    */
   private boolean isLexeme;

   /**
    * The current line number being read in the input source file.
    */
   private int lineNo = 0;

   /**
    * Initialize this Scanner.
    *
    * @param path source file (e.g. "C:/Users/Rickb/Documents/Prog.txt")
    * @throws IOException an unexpected non-recoverable error
    */
   public Scanner(String path) throws IOException {
      sourceFile = path;

      lexeme = new char[MAX_LINE_SIZE];

      buffer = new PushbackReader(new BufferedReader(new FileReader(path)));
   }

   /**
    * Return the next Token in the input source file. Single line comments are
    * handled by skipping ahead until a line break is found. Multi line comments
    * skip ahead until it reaches the end of file or a * / sequence (without the
    * space in between)
    *
    * @return a Token with TokenType and Lexeme
    * @throws IOException an unexpected non-recoverable error occurred
    */
   public Token next() throws IOException {
      isLexeme = false;
      while (true) {
         char ch = nextChar();
         if (ch == '/') { //enter the Comment Loop
            ch = nextChar(); //Get next character
            switch (ch) {
               case '*'://Entering multi line comment
                  ch = nextChar();
                  while (ch != '\0') { //Until end of file
                     ch = nextChar();
                     while (ch == '*') { //Possible end tag
                        ch = nextChar();
                        if (ch == '/') { //Confirmed
                           endPos = 0; //Clears lexeme buffer
                           return next();
                        }
                     }
                     ch = nextChar();
                  }  if (ch == '\0') { //End of File
                     return new Token("", TokenType.EOF);
                  }  
                  break;
               case '/': //Single line comment
                  while (ch != '\n' && ch != '\r') { //Scan for new line
                     ch = nextChar();
                  }  endPos = 0; //Clears lexeme buffer
                  return next();
               default: //Not a comment, parse as a divide
                  return scanSingleCharToken('/');
            }

         } else { //Not a comment, move to normal processing
            switch (ch) {
               case '\0':
                  if (isLexeme) {
                     return scanLexeme();
                  } else {
                     return new Token("", TokenType.EOF);
                  }

               case ';':
               case '\n':
               case '\r':
               case ' ':
               case 255:      // non-breaking space
               case '\t':
                  if (isLexeme) {
                     return scanLexeme();
                  } else {
                     return next();
                  }

               case ':':
                  return scanColon();

               case '=': // single char tokens
               case '*':
               case '+':
               case '-':
               case '!':
                  return scanSingleCharToken(ch);

               default:
                  isLexeme = true;

            }
         }
      }
   }

   /**
    * Return the current line number being read in the source file.
    *
    * @return the current line number
    */
   public int getLineNo() {
      return lineNo;
   }

   /**
    * Read and return the next character in the source file.
    *
    * @return the next char or '\0', if EOR
    */
   private char nextChar() throws IOException {
      int ch = buffer.read();

      switch (ch) {
         case ';':
         case '\n':            // ASCII Line feed, LF or
         case '\r':            //  carriage return, CR
            if ((CR_LF) && (ch == '\r')) {
               nextChar();
            } else {

               lineNo++;
            }

            return (char) ch;

         case ' ':                // space
         case 255:                // non-breaking space
            return (char) ch;

         case -1:                 // Java read nothing, so
            return '\0';        // we're at EOF

         default:
            lexeme[endPos++] = (char) ch;
            return (char) ch;
      }
   }

   /**
    * As a delimiter was encountered during scanning, determine and return the
    * Token for the current lexeme.
    *
    * @return a Token encapsulating the current lexeme
    */
   private Token scanLexeme() {
      String lexemeStr = String.copyValueOf(lexeme, 0, endPos);
      //System.out.println("Debug: Printing lexemeStr: " + lexemeStr);

      endPos = 0;
      isLexeme = false;

      if (lexemeStr.equalsIgnoreCase("READ")) {
         return new Token(lexemeStr, TokenType.READ);

      } else if (lexemeStr.equalsIgnoreCase("WRITE")) {
         return new Token(lexemeStr, TokenType.WRITE);

      } else if (lexemeStr.equalsIgnoreCase("TRUE")) {
         return new Token(lexemeStr, TokenType.TRUE);

      } else if (lexemeStr.equalsIgnoreCase("FALSE")) {
         return new Token(lexemeStr, TokenType.FALSE);

      } else if (lexemeStr.equalsIgnoreCase("int")) {
         return new Token(lexemeStr, TokenType.INTEGER);

      } else if (lexemeStr.equalsIgnoreCase("bool")) {
         return new Token(lexemeStr, TokenType.BOOLEAN);
      } else {
         try {
            Integer.parseInt(lexemeStr); // is lexeme a number?

            return new Token(lexemeStr, TokenType.NUMBER);

         } catch (NumberFormatException e) { // no, it's an ID
            return new Token(lexemeStr, TokenType.ID);
         }
      }
   }

   /**
    * If we're not in the middle of reading another token, return a token for
    * the given single character, otherwise return the token we're in the middle
    * of reading and push the character back into the input buffer.
    *
    * @param ch a single character token (see TokenType)
    */
   private Token scanSingleCharToken(char ch) throws IOException {
      if (isLexeme) {              // Middle of reading another token
         buffer.unread((int) ch);
         endPos--;
         return scanLexeme();

      } else {
         endPos = 0;

         switch ((int) ch) {
            case '=':
               return new Token(String.valueOf(ch), TokenType.EQUAL);

            case '+':
               return new Token(String.valueOf(ch), TokenType.PLUS);

            case '-':
               return new Token(String.valueOf(ch), TokenType.MINUS);

            case '/':
               return new Token(String.valueOf(ch), TokenType.DIVIDE);

            case '*':
               return new Token(String.valueOf(ch), TokenType.MULTIPLY);
            case '!':
               return new Token(String.valueOf(ch), TokenType.NOT);

            case '(':
               return new Token(String.valueOf(ch), TokenType.LPAREN);
            case ')':
               return new Token(String.valueOf(ch), TokenType.RPAREN);
            default:
               return new Token(String.valueOf(ch), TokenType.ERROR);
         }
      }
   }

   /**
    * As a single colon has been read, if were within a lexeme, treat it as a
    * delimiter, otherwise check for an assignment statement or error.
    *
    * @return token that is an ID, ASSIGN, or ERROR
    * @throws IOException an unexpected non-recoverable error occurred
    */
   private Token scanColon() throws IOException {
      if (isLexeme) {
         return scanLexeme();
      } else if (nextChar() == '=') {
         endPos = 0;
         return new Token(":=", TokenType.ASSIGN);
      } else {
         endPos = 0;
         return new Token(":", TokenType.ERROR);
      }
   }

   /**
    * A debugging utility for batch scanning the entire source file
    *
    * @return a list of tokens in the source file
    * @throws IOException an unexpected non-recoverable error occurred
    */
   public ArrayList<Token> scanAll() throws IOException {
      ArrayList<Token> tokens = new ArrayList<>();

      Token token;

      do {
         token = next();

         tokens.add(token);

      } while (token.type != TokenType.EOF);

      return tokens;
   }
}
