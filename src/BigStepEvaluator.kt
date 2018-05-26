import java.util.*
import kotlin.reflect.jvm.internal.ReflectProperties


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
class TmRaise : Term()                             // raise, não usei data class pois não armazena valores

//Listas
data class TmList(val h: Value, val t : Term) : Term()
class TmNil : Term()



abstract class BinNumOp(val t1: Term, val t2:Term, val f:(Int, Int)->Int) : Term()
data class addOp(private val e1:Term, private val e2:Term) : BinNumOp(e1, e2, fun(a:Int, b:Int):Int{return a+b})
data class subOp(private val e1:Term, private val e2:Term) : BinNumOp(e1, e2, fun(a:Int, b:Int):Int{return a-b})
data class mulOp(private val e1:Term, private val e2:Term) : BinNumOp(e1, e2, fun(a:Int, b:Int):Int{return a*b})
data class divOp(private val e1:Term, private val e2:Term) : BinNumOp(e1, e2, fun(a:Int, b:Int):Int{return a/b})

abstract class BinBoolOp(val t1: Term, val t2:Term, val f:(Boolean, Boolean)->Boolean) : Term()
data class andOp(private val e1: Term, private val e2:Term) : BinBoolOp(e1, e2, fun(a:Boolean, b:Boolean):Boolean{return a&&b})
data class orOp(private val e1: Term, private val e2:Term) : BinBoolOp(e1, e2, fun(a:Boolean, b:Boolean):Boolean{return a||b})

abstract class CompNumOp(val t1: Term, val t2:Term, val f:(Int, Int)->Boolean) : Term()
data class grOp(private val e1:Term, private val e2:Term) : CompNumOp(e1, e2, fun(a:Int, b:Int):Boolean{return a>b})
data class geOp(private val e1:Term, private val e2:Term) : CompNumOp(e1, e2, fun(a:Int, b:Int):Boolean{return a>=b})
data class smOp(private val e1:Term, private val e2:Term) : CompNumOp(e1, e2, fun(a:Int, b:Int):Boolean{return a<b})
data class seOp(private val e1:Term, private val e2:Term) : CompNumOp(e1, e2, fun(a:Int, b:Int):Boolean{return a<=b})
data class eqOp(private val e1:Term, private val e2:Term) : CompNumOp(e1, e2, fun(a:Int, b:Int):Boolean{return a==b})
data class neqOp(private val e1:Term, private val e2:Term) : CompNumOp(e1, e2, fun(a:Int, b:Int):Boolean{return a!=b})

abstract class UnaryBoolOp(val t1: Term, val f:(Boolean)->Boolean) : Term()
data class notOp(private val e1: Term) : UnaryBoolOp(e1, fun(a:Boolean):Boolean{return !a})

//TmVar usa string pois é um identificador
data class TmVar(val x : String) : Term()
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



//Classe criada para retorno da função bigStep, pois raise não pode ser considerado valor a preço do determinismo da
// linguagem
sealed class ValueOrRaise

sealed class Value : ValueOrRaise()
data class Vnum (val n: Int) : Value()
data class Vbool (val b: Boolean) : Value()
data class Vlist (val h: Value, val t: Value) : Value()
class Vnil() : Value()
data class VClosure(val x : TmVar, val e : Term, val env : Env) : Value()
data class VRecClosure(val f : TmVar, val x : TmVar, val e : Term, val env : Env) : Value()

class Raise : ValueOrRaise()

data class Env(val d : Dictionary<TmVar, Value>)


class NoRuleApplies : Throwable()
class IdentNotDefined : Throwable()

fun buildVList(l : Term) : Value{

    return when (l)
    {
        is TmNil -> Vnil()
        is TmList -> Vlist(l.h, buildVList(l.t))
        else -> throw NoRuleApplies() //*
    }


}

//Trechos marcados com:
//  * : nunca devem acontecer se verificação de tipos for correta
fun bigStep (e : Term, env : Env) : ValueOrRaise{
    return when (e){
        is TmNum -> Vnum(e.n) // BS-NUM
        is TmBool -> Vbool(e.b) // BS-BOOL
        is TmVar -> try{ env.d[e]} catch (ex : Exception){throw IdentNotDefined()} //BS-ID
        is TmList -> buildVList(e) // BS-LIST
        is TmNil -> Vnil() //BS-NIL
        is TmRaise -> Raise() // RAISE
        is TmIf -> {
            val condition : ValueOrRaise = bigStep(e.e1, env)
            when {
                condition is Raise -> Raise() //BS-IFRAISE
                condition !is Vbool -> throw NoRuleApplies() // *
                condition.b -> bigStep(e.e2, env) // BS-IFTR
                else -> bigStep(e.e3, env) //BS-IFFLS
            }
        }
        is TmFn -> VClosure(e.x, e.e, env) //BS-FN
        is TmLet ->{
            val resultE1 : ValueOrRaise = bigStep(e.e1, env)
            if(resultE1 is Value) {
                var envExt: Env = env.copy()
                envExt.d.put(e.x, resultE1)
                bigStep(e.e2, envExt) // valor retornado // BS-LET
            }
            else {
                Raise() // BS-LETRAISE
            }
        }
        is TmLetRec ->{

            val rclos = VRecClosure(e.f, e.x, e.e1, env)
            var envExt: Env = env.copy()
            envExt.d.put(e.f, rclos)
            bigStep(e.e2, envExt) // valor retornado // BS-LETREC

        }
        is TmApp->{
            val e1Result = bigStep(e.e1, env)
            val e2Result = bigStep(e.e2, env)

            if (e1Result is Value && e2Result is Value){
                when (e1Result) {
                    is VClosure -> {
                        var extEnv = e1Result.env.copy()
                        extEnv.d.put(e1Result.x, e2Result)
                        bigStep(e1Result.e, extEnv) //BS-APP
                    }
                    is VRecClosure -> {
                        var extEnv = e1Result.env.copy()
                        extEnv.d.put(e1Result.x, e2Result)
                        extEnv.d.put(e1Result.f, e1Result)
                        bigStep(e1Result.e, extEnv) //BS-APPREC
                    }
                    else -> throw NoRuleApplies() //*
                }
            }
            else
                Raise() // BS-APPLSRAISE e BS-APPRSRAISE
        }
        is TmIsEmpty->{
            when (e.e){
                is TmNil -> Vbool(true) // BS-EMPTYNIL
                is TmList -> Vbool(false) // BS-EMPTYLIST
                is TmRaise -> Raise() //BS-EMPTYRAISE
                else -> throw NoRuleApplies() //*
            }
        }

        is TmHd->{
            when (e.e){
                is TmNil -> Raise() // BS-HDNIL
                is TmList -> e.e.h // BS-HD
                is TmRaise -> Raise() //bs-hdraise
                else -> throw  NoRuleApplies() //*
            }
        }

        is TmTl->{
            when(e.e){
                is TmNil -> Raise() // BS-TLNIL
                is TmList -> buildVList(e.e.t) // BS-TL
                is TmRaise -> Raise() //BS-TLRAISE
                else -> throw NoRuleApplies() //*
            }
        }

        is TmTryWith->{
            val tryResult = bigStep(e.e1, env)
            when(tryResult){
                is Value -> tryResult
                is Raise -> bigStep(e.e2, env)
            }
        }

        is TmCat->{
            val e1Result = bigStep(e.e1, env)
            val e2Result = bigStep(e.e2, env)
            if(e1Result is Value && e2Result is Value){
                Vlist(e1Result, e2Result)
            }
            else
                Raise()
        }
        is BinNumOp->{
            val e1Result = bigStep(e.t1, env)
            val e2Result = bigStep(e.t2, env)
            if(e is divOp && e2Result is Vnum && e2Result.n == 0){
                Raise()
            }
            else{
                when {
                    e1Result is Raise || e2Result is Raise -> Raise()
                    e1Result is Vnum && e2Result is Vnum -> Vnum(e.f(e1Result.n, e2Result.n))
                    else -> throw NoRuleApplies()
                }
            }
        }
        is BinBoolOp->{
            val e1Result = bigStep(e.t1, env)
            val e2Result = bigStep(e.t2, env)
            when {
                e1Result is Raise || e2Result is Raise -> Raise()
                e1Result is Vbool && e2Result is Vbool -> Vbool(e.f(e1Result.b, e2Result.b))
                else -> throw NoRuleApplies()
            }
        }
        is CompNumOp->{
            val e1Result = bigStep(e.t1, env)
            val e2Result = bigStep(e.t2, env)
            when {
                e1Result is Raise || e2Result is Raise -> Raise()
                e1Result is Vnum && e2Result is Vnum -> Vbool(e.f(e1Result.n, e2Result.n))
                else -> throw NoRuleApplies()
            }
        }
        is UnaryBoolOp->{
            val e1Result = bigStep(e.t1, env)
            when (e1Result) {
                is Raise -> Raise()
                is Vbool -> Vbool(e.f(e1Result.b))
                else -> throw NoRuleApplies()
            }
        }
    }
}

