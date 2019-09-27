# L1-plus-plus-interpreter
Big-step interpreter for the L1++ academic language. Developed for UFRGS' formal semantics course (2018/1). The language is defined in **L1++_definition.pdf**.

## Building and running

To build the project execute:
```
gradlew build
```
To run it, use the following command:
```
gradlew run --console=plain
```
Now you may choose if you want to use the explicit or implicit interpreter. For example, to find the factorial of 5 using the explicit interpreter you should run:
```
let rec fat:int->int = (fn x:int=>if x == 0 then 1 else x * (fat) (x - 1)) in (fat) (5)
```
If you chose the implicit one, there's no need to annotate the operands' types. Therefore, the following code should be used:
```
let rec fat = (fn x=>if x == 0 then 1 else x * (fat) (x - 1)) in (fat) (5)
```
