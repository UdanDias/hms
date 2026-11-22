package com.hms.web;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
public class PhoneValidator implements ConstraintValidator<ValidPhone,String> {
 @Override public boolean isValid(String value, ConstraintValidatorContext context) {
   if(value==null)return false;
   String raw=value.trim();
   if(!raw.matches("[+0-9\\s()-]+"))return false;
   String compact=raw.replaceAll("[\\s()-]", "");
   if(compact.startsWith("+94"))return compact.matches("\\+94[1-9][0-9]{8}");
   if(compact.startsWith("+"))return compact.matches("\\+[1-9][0-9]{6,14}");
   return compact.matches("0[1-9][0-9]{8}");
 }
}
