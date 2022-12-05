package com.example.demo.scrappednews;


import org.aspectj.lang.annotation.*;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class RefetchAspect {

   public static ScrappedNewsController snc;

//   @Pointcut("execution(* com.example.demo.scrappednews.ScrappedNewsController.getScrappedNews(..))")
//   private void getScrappedNews() {}
//
//   @AfterReturning(value = "getScrappedNews()")
//   public void refetch() {
//      snc.checkIfItsTimeToRefetchAndIfSoDoSo();
//
//   }
   @Before("execution(* com.example.demo.scrappednews.ScrappedNewsController.getScrappedNews(..))")
   public void refetch() throws InterruptedException {
//      System.out.println("Refetch method is called");
//      Thread.sleep(2000);
//      System.out.println("Slept");
      snc.checkIfItsTimeToRefetchAndIfSoDoSo();

   }

}
