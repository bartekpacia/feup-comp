; class with syntax accepted by jasmin 2.3

.class public HelloComplex
.super java/lang/Object

.field public static intField B

; standard initializer
.method public <init>()V
    aload_0

    invokenonvirtual java/lang/Object/<init>()V
    return
.end method

.method public static main([Ljava/lang/String;)V
    .limit stack 2
    ;. limit locals 2 ; this example does not need local variables
    
    ldc ""
    invokestatic Test.foo(Ljava/lang/Object;)I
    
    return
.end method

.method public static foo(Ljava/lang/Object;)I
    .limit stack 99
    .limit locals 99
    
    ; Doesn't work for some reason
    ; aload 0                     ; -> objectref
    ; bipush 10                     ; -> value
    ; putfield Test/intField B    ; objectref, value →
   
    bipush 10                     ;       -> value
    putstatic Test/intField B     ; value ->
    
    ; aload 0                     ; -> objectref
    ; getfield Test.intField B    ; objectref -> value 
    ; istore 1                    ; value ->
    ; iload 0                     ; -> value
    
    getstatic java/lang/System.out Ljava/io/PrintStream;
    ldc "Hello World!"
    invokevirtual java/io/PrintStream.println(Ljava/lang/String;)V
    
    bipush 0
    ireturn                     ; value ->
.end method
