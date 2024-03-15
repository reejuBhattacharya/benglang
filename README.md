# Welcome to Benglang!

### Benglang is a fun little programming language that I created which allows me to code in my native tongue, Bengali. 
It is an interpreted, dynamically-typed, functional language that you can use to solve mathematical problems and create your own command-line projects.

## Get Started

1. Download the source files however you wish.
2. Create your code file in the directory of the source files.
3. To use Benglang for running your code, use the following command:
    ```plaintext
    ./runfile <filename.txt>
    ```
    where *filename* is the name of your code file.

## Features of Benglang

### Printing stuff:
To print something, simply use the **lekho** keyword, as shown:
```plaintext
print "Hello World";
```

### Declaring variables:
The **dhoro** keyword is used to define a variable. Benglang supports **numbers**, **strings** and **booleans**. It is a dynamically typed language, so you do not need to specify the type of the data during variable declaration. 

```plaintext
dhoro a = 1;
dhoro c = false;
dhoro name = "Reeju"
```
### Conditional Execution
Conditional statements use the keywords **jodi** and **nahole**, as follows:
```plaintext
dhoro a = 5;

jodi (a > 0) {
    bolo "Hello!";
}
```

### Repeated Execution
Loops are an essential part of any language. Benglang has support for two types of loops, analogous to *for* and *while* loops.

"while" loops can be simulated with the **jotokhon** keyword:
```plaintext
dhoro a = 5;

jotokhon(a >= 0) {
    print "loop cholche";
    a = a - 1;
}
```

Similary, for "for" loops, use the **jokhon** keyword:
```plaintext
jokhon(dhoro i=0; i<10; i++) {
    print "loop cholche";
}
```
