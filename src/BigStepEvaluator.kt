import java.util.*

//Sealed class é usada para restringir uma hierarquia, ou seja, se um valor pode ser de um conjunto limitado de tipos
//ele é de um tipo que é como se fosse uma enum de classes essa enum de classes é representada em Kotlin como sealed
//class
sealed class Term

sealed class Type
class TyInt : Type()
class TyBool : Type()
data class TyFn(val inp : Type, val out : Type) : Type()
data class TyList (val T : Type) : Type()


//Data class é a representação de uma classe cuja função principal é armazenar valores relacionados, a vantagem
//de usar data class é que códigos para getters e setters são gerados automáticamente assim como outras
//funcionalidades

//Termos da sintaxe abstrata

data class TmBool(val b: Boolean) : Term()           // Valores booleanos
data class TmNum (val n: Int): Term()                // Valores numéricos
class TmRaise() : Term()                             // raise, não usei data class pois não armazena valores

//Listas
data class TmList(val h: Term, val t : Term) : Term()
class TmNil : Term()
enum class Op (val f: (TmNum, TmNum)->Term){                                          //enum de funções para representar
                                                                                        //os operadores da linguagem
    fun_add(fun(e1: TmNum, e2: TmNum): TmNum  { return TmNum(e1.n + e2.n) }),       // e1 +  e2
    fun_sub(fun(e1: TmNum, e2: TmNum): TmNum  { return TmNum(e1.n - e2.n) }),       // e1 -  e2
    fun_mul(fun(e1: TmNum, e2: TmNum): TmNum  { return TmNum(e1.n * e2.n) }),       // e1 *  e2
    fun_div(fun(e1: TmNum, e2: TmNum): TmNum  { return TmNum(e1.n / e2.n) }),       // e1 /  e2
    fun_g  (fun(e1: TmNum, e2: TmNum): TmBool { return TmBool(e1.n > e2.n) }),      // e1 >  e2
    fun_ge (fun(e1: TmNum, e2: TmNum): TmBool { return TmBool(e1.n >= e2.n) }),     // e1 >= e2
    fun_s  (fun(e1: TmNum, e2: TmNum): TmBool { return TmBool(e1.n < e2.n) }),      // e1 <  e2
    fun_se (fun(e1: TmNum, e2: TmNum): TmBool { return TmBool(e1.n <= e2.n) }),     // e1 <= e2
    fun_eq (fun(e1: TmNum, e2: TmNum): TmBool { return TmBool(e1.n == e2.n) }),     // e1 == e2
    fun_neq(fun(e1: TmNum, e2: TmNum): TmBool { return TmBool(e1.n != e2.n) })      // e1 != e2

}

//TmVar usa string pois é um identificador
data class TmVar(val x : String) : Term()
data class TmBinOp(val op : Op,val e1 : Term, val e2: Term) : Term()
data class TmIf(val e1: Term, val e2 : Term, val e3 : Term) : Term()
data class TmApp(val e1 : Term, val e2 : Term) : Term()
data class TmFn(val x : TmVar, val t : Type , val e: Term) : Term()
data class TmLet(val x : TmVar, val t: Type, val e1 : Term, val e2:Term) : Term()
data class TmLetRec(val f: TmVar, val fin : Type, val fout : Type, val x: TmVar, val e1 : Term, val e2:Term) : Term()
data class TmTryWith(val e1 : Term, val e2:Term) : Term()
data class TmHd (val e : Term) : Term()
data class TmTl (val e : Term) : Term()
data class TmIsEmpty(val e : Term) : Term()
data class TmCat(val e1 : Term, val e2 : Term) : Term()

data class Env(val d : Dictionary<TmVar, Term>)

sealed class Value
data class Vnum (val n: Int) : Value()
data class Vbool (val b: Boolean) : Value()
data class Vlist (val l: List<Value>) : Value()
data class VClosure(val x : Term, val e : Term, val env : Env) : Term()
data class VRecClosure(val f : Term, val x : Term, val e : Term, val env : Env) : Term()





