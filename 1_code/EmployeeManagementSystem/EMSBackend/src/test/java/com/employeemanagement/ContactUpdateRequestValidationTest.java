/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.employeemanagement;
import com.employeemanagement.model.ContactUpdateRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
/**
 *
 * @author NerdyGirl44
 */
public class ContactUpdateRequestValidationTest {
    private static ValidatorFactory factory;
    private static Validator validator;
    
    @BeforeAll
    static void setup () {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator ();
    }
    
    @AfterAll
    static void cleanup () {
        factory.close ();
        
    }
    
    @Test
    void validContactInformationPasses() {
        ContactUpdateRequest request =
                   new ContactUpdateRequest ("employee@example.com", "3175551234");
                    assertTrue(validator.validate(request).isEmpty());
                    
                
    }
    @Test
    void phoneNumberOver25CharactersIsRejected(){
        ContactUpdateRequest request =
                new ContactUpdateRequest("employee@example.com", "12345678901234567890123456");
                        
                        assertFalse(validator.validate(request).isEmpty());
                               
    }
   @Test
   void invalidEmailsIsRejected() {
       ContactUpdateRequest request =
       new ContactUpdateRequest("invalid-email", "3175551234");

assertFalse(validator.validate(request).isEmpty());      
   }
}
