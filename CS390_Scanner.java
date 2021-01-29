/*
 * Copyright (C) 2016 Richard Blumenthal, All Rights Reserved
 * Dissemination or reproduction of this code is strictly forbidden
 * unless prior written permission is obtained from Dr. Blumenthal
 */
package edu.regis.cs390.scan;

import edu.regis.cs390.scan.tok.Token;
import java.io.IOException;

/**
 * A scanning example using Scott's (2016) Simple Calculator LL(1) grammar. 
 * 
 * @author Rickb
 */
public class CS390_Scanner {
    /**
     * Default location of the source file to scan.
     */
    public static final String SOURCE = "F:\\Share\\Cody Personal\\School\\CS390\\Assn2\\Register_Assignment_4-I\\input.txt";
    
    /**
     * Entry point that begins scanning
     * 
     * @param args not used
     */
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(SOURCE);
            
            for (Token token : scanner.scanAll())
                    System.out.println(token.lexeme + " : " + token.type);

        } catch (IOException e) {
            System.out.println(e.toString());
        }
        
    }
}
