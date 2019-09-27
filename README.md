# L1-plus-plus-interpreter
Big-step interpreter for the L1++ academic language. Developed for UFRGS' formal semantics course (2018/1).

## Building and running

Execute
```
gradlew build
```
to build the project. To run it, execute
```
gradlew run --console=plain
```
Now you may choose if you want the explicit or implicit interpreter. For example, to find the factorial of 5 you should run
```
let rec fat:int->int = (fn x:int=>if x == 0 then 1 else x * (fat) (x - 1)) in (fat) (5)
```
if you opted for the explicit interpreter. If you chose the implicit one, you should run
```
let rec fat = (fn x=>if x == 0 then 1 else x * (fat) (x - 1)) in (fat) (5)
```

### To be done:
- [ ] Define the academic language being interpreted
