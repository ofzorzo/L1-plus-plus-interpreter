import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.system.exitProcess


//Sealed class é usada para restringir uma hierarquia, ou seja, se um valor pode ser de um conjunto limitado de tipos
//ele é de um tipo que é como se fosse uma enum de classes essa enum de classes é representada em Kotlin como sealed
//class
sealed class Term

sealed class Type
class TyInt : Type()
class TyBool : Type()
class TyUnknown : Type()
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



sealed class BinNumOp(val t1: Term, val t2:Term, val f:(Int, Int)->Int) : Term()
data class AddOp(val e1:Term, val e2:Term) : BinNumOp(e1, e2, fun(a:Int, b:Int):Int{return a+b})
data class SubOp(val e1:Term, val e2:Term) : BinNumOp(e1, e2, fun(a:Int, b:Int):Int{return a-b})
data class MulOp(val e1:Term, val e2:Term) : BinNumOp(e1, e2, fun(a:Int, b:Int):Int{return a*b})
data class DivOp(val e1:Term, val e2:Term) : BinNumOp(e1, e2, fun(a:Int, b:Int):Int{return a/b})

sealed class BinBoolOp(val t1: Term, val t2:Term, val f:(Boolean, Boolean)->Boolean) : Term()
data class AndOp(val e1: Term, val e2:Term) : BinBoolOp(e1, e2, fun(a:Boolean, b:Boolean):Boolean{return a&&b})
data class OrOp(val e1: Term, val e2:Term) : BinBoolOp(e1, e2, fun(a:Boolean, b:Boolean):Boolean{return a||b})

sealed class CompNumOp(val t1: Term, val t2:Term, val f:(Int, Int)->Boolean) : Term()
data class GrOp(val e1:Term, val e2:Term) : CompNumOp(e1, e2, fun(a:Int, b:Int):Boolean{return a>b})
data class GeOp(val e1:Term, val e2:Term) : CompNumOp(e1, e2, fun(a:Int, b:Int):Boolean{return a>=b})
data class SmOp(val e1:Term, val e2:Term) : CompNumOp(e1, e2, fun(a:Int, b:Int):Boolean{return a<b})
data class SeOp(val e1:Term, val e2:Term) : CompNumOp(e1, e2, fun(a:Int, b:Int):Boolean{return a<=b})
data class EqOp(val e1:Term, val e2:Term) : CompNumOp(e1, e2, fun(a:Int, b:Int):Boolean{return a==b})
data class NeqOp(val e1:Term, val e2:Term) : CompNumOp(e1, e2, fun(a:Int, b:Int):Boolean{return a!=b})

sealed class UnaryBoolOp(val t1: Term, val f:(Boolean)->Boolean) : Term()
data class NotOp(val e1: Term) : UnaryBoolOp(e1, fun(a:Boolean):Boolean{return !a})

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

//termo útil para marcar finalizações durante a construção da árvore sintática abstrata
class TmFinishExpr : Term()

//Classe criada para retorno da função bigStep, pois raise não pode ser considerado valor a preço do determinismo da
// linguagem
sealed class ValueOrRaise

sealed class Value : ValueOrRaise()
data class Vnum (val n: Int) : Value()
data class Vbool (val b: Boolean) : Value()
data class Vlist (val h: Value, val t: Value) : Value()
class Vnil : Value()
data class VClosure(val x : TmVar, val e : Term, val env : Env) : Value()
data class VRecClosure(val f : TmVar, val x : TmVar, val e : Term, val env : Env) : Value()

class Raise : ValueOrRaise()

data class Env(val hm : HashMap<TmVar, Value>)


class NoRuleApplies(override var message: String) : Throwable(message)
class UnifyFail : Throwable()
class IdentNotDefined(override var message: String) : Throwable(message)
class SintaxError(override var message: String) : Throwable(message)
class ParserError : Throwable()

fun buildVList(l : Term) : Value{

    return when (l)
    {
        is TmNil -> Vnil()
        is TmList -> Vlist(l.h, buildVList(l.t))
        else -> throw NoRuleApplies("Expected list but other expression found") //*
    }

}

fun catLists(l1 : Value, l2 : Value) : Value
{

    return if(l1 is Vnil){
        l2
    } else{
        Vlist((l1 as Vlist).h, catLists(l1.t, l2))
    }
}

fun addConstraints (list1 : MutableList<String>, list2 : MutableList<String>): MutableList<String>{
    var newList = mutableListOf<String>()
    for(constraint in list1){
        newList.add(constraint)
    }
    for(constraint in list2){
        newList.add(constraint)
    }
    return newList
}

fun addConstraints(list : MutableList<String>): MutableList<String>{
    var newList = mutableListOf<String>()
    for(constraint in list){
        newList.add(constraint)
    }
    return newList
}

data class identTable(var d : HashMap<String, String>) //dicionario de string(ident)->string(tipo)
//data class identTable(val d : Dictionary<String, MutableList<String>>) // dicionario de string(ident)->constraints
var x : Int = 0
fun typeConsColl(e : Term, ident : identTable, recursionLevel : Int, constraints : MutableList<String>, implicit: Boolean, resetx : Boolean): Pair<String, MutableList<String>> {
    if(resetx)
        x = 0
    //println("current X index: "+x.toString())
    return when (e){
        is TmFn ->{
            if(implicit) {
                var newConstraints: Pair<String, MutableList<String>>
                var endConstraints: MutableList<String>
                var X : String = "X"+x.toString()
                x += 1

                ident.d.put(e.x.x, X)
                newConstraints = typeConsColl(e.e, ident, recursionLevel + 1, constraints, implicit, false)

                endConstraints = addConstraints(constraints, newConstraints.second)

                Pair("("+X+"->"+newConstraints.first+")", endConstraints)
            }
            else{
                var newConstraints: Pair<String, MutableList<String>>
                var endConstraints: MutableList<String>

                ident.d.put(e.x.x, toString(e.t)) //bota identificador no dicionario e o seu tipo tbm

                //pega tipo e constraints da expressao
                newConstraints = typeConsColl(e.e, ident, recursionLevel + 1, constraints, implicit, false)
                var string : String = toString(e.t)+"->"+newConstraints.first

                endConstraints = addConstraints(constraints, newConstraints.second)
                Pair("("+string+")", endConstraints)
            }
        }
        is TmLet -> {
            if(implicit) {
                var newConstraints: Pair<String, MutableList<String>>
                var newConstraints2: Pair<String, MutableList<String>>
                var endConstraints: MutableList<String>
                var X : String = "X"+x.toString()
                x += 1

                newConstraints = typeConsColl(e.e1, ident, recursionLevel + 1, constraints, implicit, false)

                ident.d.put(e.x.x, X)
                newConstraints2 = typeConsColl(e.e2, ident, recursionLevel + 1, constraints, implicit, false)

                endConstraints = addConstraints(constraints, newConstraints.second)
                endConstraints = addConstraints(endConstraints, newConstraints2.second)
                endConstraints.add(X+"="+newConstraints.first)
                Pair(newConstraints2.first, endConstraints)
            }
            else{
                var newConstraints: Pair<String, MutableList<String>>
                var newConstraints2: Pair<String, MutableList<String>>
                var endConstraints: MutableList<String>

                //vai conter T1
                newConstraints = typeConsColl(e.e1, ident, recursionLevel + 1, constraints, implicit, false)

                ident.d.put(e.x.x, toString(e.t))
                //vai conter T2
                newConstraints2 = typeConsColl(e.e2, ident, recursionLevel + 1, constraints, implicit, false)

                endConstraints = addConstraints(constraints, newConstraints.second)
                endConstraints = addConstraints(endConstraints, newConstraints2.second)
                endConstraints.add(toString(e.t)+"="+newConstraints.first)
                Pair(newConstraints2.first, endConstraints)
            }
        }
        is TmLetRec -> {
            ////println("TmLetRec")
            if(implicit){
                var newConstraints: Pair<String, MutableList<String>>
                var newConstraints2: Pair<String, MutableList<String>>
                var endConstraints: MutableList<String>
                var string1: String = "X"+x.toString()
                x += 1
                var string2: String = "X"+x.toString()
                x += 1
                ident.d.put(e.f.x, string1) // x: X
                newConstraints2 = typeConsColl(e.e2, ident, recursionLevel + 1, constraints, implicit, false)

                ident.d.put(e.x.x, string2) // y: Y
                newConstraints = typeConsColl(e.e1, ident, recursionLevel + 1, constraints, implicit, false)

                endConstraints = addConstraints(constraints, newConstraints.second)
                endConstraints = addConstraints(endConstraints, newConstraints2.second)
                endConstraints.add(string1+"="+"("+string2+"->"+newConstraints.first+")")
                Pair(newConstraints2.first, endConstraints)
            }
            else{
                var newConstraints: Pair<String, MutableList<String>>
                var newConstraints2: Pair<String, MutableList<String>>
                var endConstraints: MutableList<String>

                ident.d.put(e.f.x, "("+toString(e.fin)+"->"+toString(e.fout)+")")
                //vai conter T2
                newConstraints2 = typeConsColl(e.e2, ident, recursionLevel + 1, constraints, implicit, false)

                ident.d.put(e.x.x, toString(e.fin))
                //vai conter T1
                newConstraints = typeConsColl(e.e1, ident, recursionLevel + 1, constraints, implicit, false)

                endConstraints = addConstraints(constraints, newConstraints.second)
                endConstraints = addConstraints(endConstraints, newConstraints2.second)
                endConstraints.add(toString(e.fout)+"="+newConstraints.first)
                Pair(newConstraints2.first, endConstraints)
            }
        }
        is TmNum -> {
            //println("TmNum")
            Pair("int", constraints)
        }
        is TmBool -> {
            //println("TmBool")
            Pair("bool", constraints)
        }
        is TmList ->{
            //println("TmList")
            when(e.h){
                is Vnum -> Pair("int list", constraints)
                is Vbool -> Pair("bool list", constraints)
                else -> throw NoRuleApplies("Expected number or boolean as head of list but other expression found")
            }
        }
        is AddOp -> {
            var newConstraints: Pair<String, MutableList<String>>
            var newConstraints2: Pair<String, MutableList<String>>
            var endConstraints: MutableList<String>

            newConstraints = typeConsColl(e.e1, ident, recursionLevel + 1, constraints, implicit, false)
            newConstraints2 = typeConsColl(e.e2, ident, recursionLevel + 1, constraints, implicit, false)
            endConstraints = addConstraints(constraints, newConstraints.second)
            endConstraints = addConstraints(endConstraints, newConstraints2.second)
            endConstraints.add(newConstraints.first+"=int")
            endConstraints.add(newConstraints2.first+"=int")
            Pair("int", endConstraints)
        }
        is SubOp -> {
            var newConstraints: Pair<String, MutableList<String>>
            var newConstraints2: Pair<String, MutableList<String>>
            var endConstraints: MutableList<String>

            newConstraints = typeConsColl(e.e1, ident, recursionLevel + 1, constraints, implicit, false)
            newConstraints2 = typeConsColl(e.e2, ident, recursionLevel + 1, constraints, implicit, false)
            endConstraints = addConstraints(constraints, newConstraints.second)
            endConstraints = addConstraints(endConstraints, newConstraints2.second)
            endConstraints.add(newConstraints.first+"=int")
            endConstraints.add(newConstraints2.first+"=int")
            Pair("int", endConstraints)
        }
        is DivOp -> {
            var newConstraints: Pair<String, MutableList<String>>
            var newConstraints2: Pair<String, MutableList<String>>
            var endConstraints: MutableList<String>

            newConstraints = typeConsColl(e.e1, ident, recursionLevel + 1, constraints, implicit, false)
            newConstraints2 = typeConsColl(e.e2, ident, recursionLevel + 1, constraints, implicit, false)
            endConstraints = addConstraints(constraints, newConstraints.second)
            endConstraints = addConstraints(endConstraints, newConstraints2.second)
            endConstraints.add(newConstraints.first+"=int")
            endConstraints.add(newConstraints2.first+"=int")
            Pair("int", endConstraints)
        }
        is MulOp -> {
            var newConstraints: Pair<String, MutableList<String>>
            var newConstraints2: Pair<String, MutableList<String>>
            var endConstraints: MutableList<String>

            newConstraints = typeConsColl(e.e1, ident, recursionLevel + 1, constraints, implicit, false)
            newConstraints2 = typeConsColl(e.e2, ident, recursionLevel + 1, constraints, implicit, false)
            endConstraints = addConstraints(constraints, newConstraints.second)
            endConstraints = addConstraints(endConstraints, newConstraints2.second)
            endConstraints.add(newConstraints.first+"=int")
            endConstraints.add(newConstraints2.first+"=int")
            Pair("int", endConstraints)
        }
        is EqOp -> {
            var newConstraints: Pair<String, MutableList<String>>
            var newConstraints2: Pair<String, MutableList<String>>
            var endConstraints: MutableList<String>

            newConstraints = typeConsColl(e.e1, ident, recursionLevel + 1, constraints, implicit, false)
            newConstraints2 = typeConsColl(e.e2, ident, recursionLevel + 1, constraints, implicit, false)
            endConstraints = addConstraints(constraints, newConstraints.second)
            endConstraints = addConstraints(endConstraints, newConstraints2.second)
            endConstraints.add(newConstraints.first+"=int")
            endConstraints.add(newConstraints2.first+"=int")
            Pair("bool", endConstraints)
        }
        is SeOp -> {
            var newConstraints: Pair<String, MutableList<String>>
            var newConstraints2: Pair<String, MutableList<String>>
            var endConstraints: MutableList<String>

            newConstraints = typeConsColl(e.e1, ident, recursionLevel + 1, constraints, implicit, false)
            newConstraints2 = typeConsColl(e.e2, ident, recursionLevel + 1, constraints, implicit, false)
            endConstraints = addConstraints(constraints, newConstraints.second)
            endConstraints = addConstraints(endConstraints, newConstraints2.second)
            endConstraints.add(newConstraints.first+"=int")
            endConstraints.add(newConstraints2.first+"=int")
            Pair("bool", endConstraints)
        }
        is SmOp -> {
            var newConstraints: Pair<String, MutableList<String>>
            var newConstraints2: Pair<String, MutableList<String>>
            var endConstraints: MutableList<String>

            newConstraints = typeConsColl(e.e1, ident, recursionLevel + 1, constraints, implicit, false)
            newConstraints2 = typeConsColl(e.e2, ident, recursionLevel + 1, constraints, implicit, false)
            endConstraints = addConstraints(constraints, newConstraints.second)
            endConstraints = addConstraints(endConstraints, newConstraints2.second)
            endConstraints.add(newConstraints.first+"=int")
            endConstraints.add(newConstraints2.first+"=int")
            Pair("bool", endConstraints)
        }
        is GrOp -> {
            var newConstraints: Pair<String, MutableList<String>>
            var newConstraints2: Pair<String, MutableList<String>>
            var endConstraints: MutableList<String>

            newConstraints = typeConsColl(e.e1, ident, recursionLevel + 1, constraints, implicit, false)
            newConstraints2 = typeConsColl(e.e2, ident, recursionLevel + 1, constraints, implicit, false)
            endConstraints = addConstraints(constraints, newConstraints.second)
            endConstraints = addConstraints(endConstraints, newConstraints2.second)
            endConstraints.add(newConstraints.first+"=int")
            endConstraints.add(newConstraints2.first+"=int")
            Pair("bool", endConstraints)
        }
        is GeOp -> {
            var newConstraints: Pair<String, MutableList<String>>
            var newConstraints2: Pair<String, MutableList<String>>
            var endConstraints: MutableList<String>

            newConstraints = typeConsColl(e.e1, ident, recursionLevel + 1, constraints, implicit, false)
            newConstraints2 = typeConsColl(e.e2, ident, recursionLevel + 1, constraints, implicit, false)
            endConstraints = addConstraints(constraints, newConstraints.second)
            endConstraints = addConstraints(endConstraints, newConstraints2.second)
            endConstraints.add(newConstraints.first+"=int")
            endConstraints.add(newConstraints2.first+"=int")
            Pair("bool", endConstraints)
        }
        is NeqOp -> {
            var newConstraints: Pair<String, MutableList<String>>
            var newConstraints2: Pair<String, MutableList<String>>
            var endConstraints: MutableList<String>

            newConstraints = typeConsColl(e.e1, ident, recursionLevel + 1, constraints, implicit, false)
            newConstraints2 = typeConsColl(e.e2, ident, recursionLevel + 1, constraints, implicit, false)
            endConstraints = addConstraints(constraints, newConstraints.second)
            endConstraints = addConstraints(endConstraints, newConstraints2.second)
            endConstraints.add(newConstraints.first+"=int")
            endConstraints.add(newConstraints2.first+"=int")
            Pair("bool", endConstraints)
        }
        is NotOp -> {
            var newConstraints: Pair<String, MutableList<String>>
            var endConstraints: MutableList<String>

            newConstraints = typeConsColl(e.e1, ident, recursionLevel + 1, constraints, implicit, false)
            endConstraints = addConstraints(constraints, newConstraints.second)
            endConstraints.add(newConstraints.first+"=bool")
            Pair("bool", endConstraints)
        }
        is AndOp -> {
            var newConstraints: Pair<String, MutableList<String>>
            var newConstraints2: Pair<String, MutableList<String>>
            var endConstraints: MutableList<String>

            newConstraints = typeConsColl(e.e1, ident, recursionLevel + 1, constraints, implicit, false)
            newConstraints2 = typeConsColl(e.e2, ident, recursionLevel + 1, constraints, implicit, false)
            endConstraints = addConstraints(constraints, newConstraints.second)
            endConstraints = addConstraints(endConstraints, newConstraints2.second)
            endConstraints.add(newConstraints.first+"=bool")
            endConstraints.add(newConstraints2.first+"=bool")
            Pair("bool", endConstraints)
        }
        is OrOp -> {
            var newConstraints: Pair<String, MutableList<String>>
            var newConstraints2: Pair<String, MutableList<String>>
            var endConstraints: MutableList<String>

            newConstraints = typeConsColl(e.e1, ident, recursionLevel + 1, constraints, implicit, false)
            newConstraints2 = typeConsColl(e.e2, ident, recursionLevel + 1, constraints, implicit, false)
            endConstraints = addConstraints(constraints, newConstraints.second)
            endConstraints = addConstraints(endConstraints, newConstraints2.second)
            endConstraints.add(newConstraints.first+"=bool")
            endConstraints.add(newConstraints2.first+"=bool")
            Pair("bool", endConstraints)
        }
        is TmIf -> {
            //println("TmIf")
            var newConstraints: Pair<String, MutableList<String>>
            var newConstraints2: Pair<String, MutableList<String>>
            var newConstraints3: Pair<String, MutableList<String>>
            var endConstraints: MutableList<String>
            newConstraints = typeConsColl(e.e1, ident, recursionLevel + 1, constraints, implicit, false)
            newConstraints2 = typeConsColl(e.e2, ident, recursionLevel + 1, constraints, implicit, false)
            newConstraints3 = typeConsColl(e.e3, ident, recursionLevel + 1, constraints, implicit, false)
            endConstraints = addConstraints(constraints, newConstraints.second)
            endConstraints = addConstraints(endConstraints, newConstraints2.second)
            endConstraints = addConstraints(endConstraints, newConstraints3.second)
            endConstraints.add(newConstraints.first+"=bool")
            endConstraints.add(newConstraints2.first+"="+newConstraints3.first)
            Pair(newConstraints2.first, endConstraints)
        }
        is TmVar -> { //C-Id
            var tipo: String = if (ident.d[e.x] != null) ident.d[e.x]!! else throw IdentNotDefined("Identifier \"" + e.x + "\" is nonexistent. Type constraints collection ended abnormally.")
            Pair(tipo, constraints)
        }
        is TmApp -> {
            //println("TmApp")
            var newConstraints: Pair<String, MutableList<String>>
            var newConstraints2: Pair<String, MutableList<String>>
            var endConstraints: MutableList<String>
            var X : String = "X"+x.toString()
            x += 1
            newConstraints = typeConsColl(e.e1, ident, recursionLevel + 1, constraints, implicit, false)
            newConstraints2 = typeConsColl(e.e2, ident, recursionLevel + 1, constraints, implicit, false)

            endConstraints = addConstraints(constraints, newConstraints.second)
            endConstraints = addConstraints(endConstraints, newConstraints2.second)
            endConstraints.add(newConstraints.first+"="+"("+newConstraints2.first+"->"+X+")")
            Pair(X, endConstraints)
        }
        is TmNil -> {
            //println("TmNil")
            var string : String = "X"+x.toString()+" list"
            x += 1
            Pair(string, constraints)
        }
        is TmCat -> {
            var newConstraints: Pair<String, MutableList<String>>
            var newConstraints2: Pair<String, MutableList<String>>
            var endConstraints: MutableList<String>

            newConstraints = typeConsColl(e.e1, ident, recursionLevel + 1, constraints, implicit, false)
            newConstraints2 = typeConsColl(e.e2, ident, recursionLevel + 1, constraints, implicit, false)

            endConstraints = addConstraints(constraints, newConstraints.second)
            endConstraints = addConstraints(endConstraints, newConstraints2.second)
            endConstraints.add(newConstraints.first+" list="+newConstraints2.first)

            Pair(newConstraints2.first, endConstraints)
        }
        is TmHd -> {
            var newConstraints: Pair<String, MutableList<String>>
            var endConstraints: MutableList<String>
            var X : String = "X"+x.toString()
            x += 1

            newConstraints = typeConsColl(e.e, ident, recursionLevel + 1, constraints, implicit, false)

            endConstraints = addConstraints(constraints, newConstraints.second)
            endConstraints.add(newConstraints.first+"="+X+" list")
            Pair(X, endConstraints)
        }
        is TmTl -> {
            //println("TmTl")
            var newConstraints: Pair<String, MutableList<String>>
            var endConstraints: MutableList<String>
            var string : String = "X"+x.toString()+" list"
            x += 1

            newConstraints = typeConsColl(e.e, ident, recursionLevel + 1, constraints, implicit, false)
            endConstraints = addConstraints(constraints, newConstraints.second)
            endConstraints.add(newConstraints.first+"="+string)
            Pair(string, endConstraints)
        }
        is TmIsEmpty -> {
            //println("TmIsEmpty")
            var newConstraints: Pair<String, MutableList<String>>
            var endConstraints: MutableList<String>
            var string : String = "X"+x.toString()+" list"
            x += 1

            newConstraints = typeConsColl(e.e, ident, recursionLevel + 1, constraints, implicit, false)

            endConstraints = addConstraints(constraints, newConstraints.second)
            endConstraints.add(newConstraints.first+"="+string)
            Pair("bool", endConstraints)
        }
        is TmRaise -> {
            var X : String = "X"+x.toString()
            x += 1
            Pair(X, constraints)
        }
        is TmTryWith -> {
            var newConstraints: Pair<String, MutableList<String>>
            var newConstraints2: Pair<String, MutableList<String>>
            var endConstraints: MutableList<String>

            newConstraints = typeConsColl(e.e1, ident, recursionLevel + 1, constraints, implicit, false)
            newConstraints2 = typeConsColl(e.e2, ident, recursionLevel + 1, constraints, implicit, false)

            endConstraints = addConstraints(constraints, newConstraints.second)
            endConstraints = addConstraints(endConstraints, newConstraints2.second)
            endConstraints.add(newConstraints.first+"="+newConstraints2.first)

            Pair(newConstraints2.first, endConstraints)
        }
        else -> throw  NoRuleApplies("Invalid expression")
    }
}

fun collectFunctionParts(function : String) : MutableList<String> {
    if (function.contains("->")) {
        var openParentesisCounter = 0
        var closeParentesisCounter = 0
        var charIsLine = false
        var cutIndex = -1
        for (i in 0 until function.length) {
            if (function[i].compareTo('(') == 0) {
                openParentesisCounter += 1
                charIsLine = false
            } else if (function[i].compareTo(')') == 0) {
                closeParentesisCounter += 1
                charIsLine = false
            } else if (function[i].compareTo('-') == 0) {
                charIsLine = true
            } else if (function[i].compareTo('>') == 0 && charIsLine) {
                if (openParentesisCounter == closeParentesisCounter + 1)
                    cutIndex = i
                charIsLine = false
            }
        }
        var parts: MutableList<String> = mutableListOf()
        var firstPart = function.substring(1, cutIndex - 1)
        parts.add(0, firstPart)
        var secondPart = function.substring(cutIndex + 1, function.length - 1)
        parts.add(1, secondPart)
        return parts
    } else {
        return mutableListOf()
    }
}

fun unify(constraints : MutableList<String>, substitions : MutableList<String>) : MutableList<String>{
    if (constraints.isEmpty()){
        return substitions;
    } else {
        var firstConstraint : String
        var typesToCompare : List<String>
        var S : String
        var T : String
        var termsS : MutableList<String>
        var termsT : MutableList<String>

        firstConstraint = constraints.get(0)
        typesToCompare = firstConstraint.split("=")
        S = typesToCompare.get(0)
        T = typesToCompare.get(1)
        termsS = collectFunctionParts(S)
        termsT = collectFunctionParts(T)

        if (S.contains(" List") && T.contains(" List")) {
            S = S.replace(" List", "")
            T = T.replace(" List", "")
        } else if (S.contains(" List") || T.contains(" List")) {
            throw UnifyFail()
        }

        if (S == T) {
            constraints.removeAt(0)
            return unify(constraints, substitions)
        } else if (termsS.size == termsT.size && termsS.size > 1) {
            constraints.removeAt(0)
            for (i in 0 until termsS.size)
                constraints.add(termsS.get(i) + "=" + termsT.get(i))
            return unify(constraints, substitions)
        } else if (S.startsWith("X") && !T.contains(S)) {
            constraints.removeAt(0)
            for (i in 0 until constraints.size)
                constraints.set(i, constraints.get(i).replace(S, T, false))
            for (j in 0 until substitions.size)
                substitions.set(j, substitions.get(j).replace(S, T, false))
            substitions.add(S + "=" + T)
            return unify(constraints, substitions)
        } else if (T.startsWith("X") && !S.contains(T)) {
            constraints.removeAt(0)
            for (i in 0 until constraints.size)
                constraints.set(i, constraints.get(i).replace(T, S, false))
            for (j in 0 until substitions.size)
                substitions.set(j, substitions.get(j).replace(T, S, false))
            substitions.add(T + "=" + S)
            return unify(constraints, substitions)
        } else {
            throw UnifyFail()
        }
    }
}

fun separateSubstitution(substitution : String) : Pair<String, String> {
    if (substitution.contains("=")) {
        var parts = substitution.split("=")
        return Pair(parts[0], parts[1])
    } else
        throw UnifyFail()
}

fun applySubs (substitutions: MutableList<String>, T : String) : String {
    var finalT = T
    for (i in 0 until substitutions.size) {
        val parts = separateSubstitution(substitutions.get(i))
        finalT = finalT.replace(parts.first.replace(" list", ""), parts.second.replace(" list", ""))
    }
    return finalT
}

fun typeInfer(e : Term, ident: identTable, implicit : Boolean) : String {
    val constraints = typeConsColl(e, ident, 0, mutableListOf(), implicit, true)
    val substitutions = unify(constraints.second, mutableListOf())
    return applySubs(substitutions, constraints.first)
}


//Trechos marcados com:
//  * : nunca devem acontecer se verificação de tipos for correta
fun bigStep (e : Term, env : Env) : ValueOrRaise{
    return when (e){
        is TmNum -> Vnum(e.n) // BS-NUM
        is TmBool -> Vbool(e.b) // BS-BOOL
        is TmVar -> { try{ env.hm[e]!! } catch (ex : Exception){ throw IdentNotDefined(e.x) } } //BS-ID
        is TmList -> buildVList(e) // BS-LIST
        is TmNil -> Vnil() //BS-NIL
        is TmRaise -> Raise() // RAISE
        is TmIf -> {
            val condition : ValueOrRaise = bigStep(e.e1, env)
            when {
                condition is Raise -> Raise() //BS-IFRAISE
                condition !is Vbool -> throw NoRuleApplies("Expected boolean as condition of \"if\" expression") // *
                condition.b -> bigStep(e.e2, env) // BS-IFTR
                else -> bigStep(e.e3, env) //BS-IFFLS
            }
        }
        is TmFn -> VClosure(e.x, e.e, env) //BS-FN
        is TmLet ->{
            val resultE1 : ValueOrRaise = bigStep(e.e1, env)
            if(resultE1 is Value) {
                val envExt: Env = env.copy()
                envExt.hm[e.x] = resultE1
                bigStep(e.e2, envExt) // valor retornado // BS-LET
            }
            else {
                Raise() // BS-LETRAISE
            }
        }
        is TmLetRec ->{

            val rclos = VRecClosure(e.f, e.x, e.e1, env)
            val envExt: Env = env.copy()
            envExt.hm[e.f] = rclos
            bigStep(e.e2, envExt) // valor retornado // BS-LETREC

        }
        is TmApp->{
            val e1Result = bigStep(e.e1, env)
            val e2Result = bigStep(e.e2, env)

            if (e1Result is Value && e2Result is Value){
                when (e1Result) {
                    is VClosure -> {
                        val extEnv = e1Result.env.copy()
                        extEnv.hm[e1Result.x] = e2Result
                        bigStep(e1Result.e, extEnv) //BS-APP
                    }
                    is VRecClosure -> {
                        val extEnv = e1Result.env.copy()
                        extEnv.hm[e1Result.x] = e2Result
                        extEnv.hm[e1Result.f] = e1Result
                        bigStep(e1Result.e, extEnv) //BS-APPREC
                    }
                    else -> throw NoRuleApplies("Cannot execute expression application when the left side is not function nor recursive function") //*
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
                else -> throw NoRuleApplies("isempty expects list") //*
            }
        }

        is TmHd->{
            when (e.e){
                is TmNil -> Raise() // BS-HDNIL
                is TmList -> e.e.h // BS-HD
                is TmRaise -> Raise() //bs-hdraise
                else -> throw  NoRuleApplies("hd expects list") //*
            }
        }

        is TmTl->{
            when(e.e){
                is TmNil -> Raise() // BS-TLNIL
                is TmList -> buildVList(e.e.t) // BS-TL
                is TmRaise -> Raise() //BS-TLRAISE
                else -> throw NoRuleApplies("tl expects list") //*
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
                catLists(e1Result, e2Result)
            }
            else
                Raise()
        }
        is BinNumOp->{
            val e1Result = bigStep(e.t1, env)
            val e2Result = bigStep(e.t2, env)
            if(e is DivOp && e2Result is Vnum && e2Result.n == 0){
                Raise()
            }
            else{
                when {
                    e1Result is Raise || e2Result is Raise -> Raise()
                    e1Result is Vnum && e2Result is Vnum -> Vnum(e.f(e1Result.n, e2Result.n))
                    else -> throw NoRuleApplies("Binary arithmetic operation can only work with numbers")
                }
            }
        }
        is BinBoolOp->{
            val e1Result = bigStep(e.t1, env)
            val e2Result = bigStep(e.t2, env)
            when {
                e1Result is Raise || e2Result is Raise -> Raise()
                e1Result is Vbool && e2Result is Vbool -> Vbool(e.f(e1Result.b, e2Result.b))
                else -> throw NoRuleApplies("Binary logic operation can only work with booleans")
            }
        }
        is CompNumOp->{
            val e1Result = bigStep(e.t1, env)
            val e2Result = bigStep(e.t2, env)
            when {
                e1Result is Raise || e2Result is Raise -> Raise()
                e1Result is Vnum && e2Result is Vnum -> Vbool(e.f(e1Result.n, e2Result.n))
                else -> throw NoRuleApplies("Comparision operations can only work with numbers")
            }
        }
        is UnaryBoolOp->{
            val e1Result = bigStep(e.t1, env)
            when (e1Result) {
                is Raise -> Raise()
                is Vbool -> Vbool(e.f(e1Result.b))
                else -> throw NoRuleApplies("Unary operator \"not\" expects boolean operand")
            }
        }
        else -> throw  NoRuleApplies("Invalid expression")
    }
}



fun toString(t : Type) : String
{
    return when(t)
    {
        is TyInt -> "int"
        is TyBool -> "bool"
        is TyFn -> "("+toString(t.inp) + "->" + toString(t.out)+")"
        is TyList -> toString(t.T) + " list"
        is TyUnknown -> "X"
    }
}

fun toString(t : Term) : String
{
    return when(t)
    {
        is TmBool -> t.b.toString()
        is TmNum -> t.n.toString()
        is TmRaise -> "raise"
        is TmList -> {
            var h: String = ""
            if (t.h !is Vnil)
                h = toString(t.h)
            var ta : String = ""
            if (t.t !is TmNil) {
                ta = toString(t.t)
                ta = ta.filter { it!='[' && it!=']' }
            }
            if (ta == "")
                "[" + h + "]"
            else
                "[" + h + "," + ta + "]"
        }
        is TmNil -> "nil"
        is TmVar -> t.x
        is TmIf -> "if " + toString(t.e1) + " then " + toString(t.e2) + " else " + toString(t.e3)
        is TmFn -> "fn " + t.x.x + ":" + toString(t.t) + "=>" + toString(t.e)
        is TmApp -> "(" + toString(t.e1) + ")" + " " + "(" + toString(t.e2) + ")"
        is TmLet -> "let " + t.x.x + ":" + toString(t.t) + "=" + toString(t.e1) + " in " + toString(t.e2)
        is TmLetRec -> "let rec "+ t.f.x + ":" + toString(t.fin) + "->" + toString(t.fout) +
                " = (fn " + t.x.x + ":" + toString(t.fin) + "=>" + toString(t.e1) + ") in " + toString(t.e2)
        is TmHd -> "hd " + toString(t.e)
        is TmTl -> "tl " + toString(t.e)
        is TmTryWith -> "try " + toString(t.e1) + " with " + toString(t.e2)
        is TmIsEmpty -> "isempty " + toString(t.e)
        is TmCat -> toString(t.e1) + "::" + toString(t.e2)

        is AddOp -> toString(t.t1) + " + " + toString(t.t2)
        is SubOp -> toString(t.t1) + " - " + toString(t.t2)
        is MulOp -> toString(t.t1) + " * " + toString(t.t2)
        is DivOp -> toString(t.t1) + " / " + toString(t.t2)

        is AndOp -> toString(t.t1) + " and " +toString(t.t2)
        is OrOp -> toString(t.t1) + " or " +toString(t.t2)
        is NotOp -> "not " + toString(t.t1)

        is GrOp -> toString(t.t1) + " > " + toString(t.t2)
        is GeOp -> toString(t.t1) + " >= " + toString(t.t2)
        is SmOp -> toString(t.t1) + " < " + toString(t.t2)
        is SeOp -> toString(t.t1) + " <= " + toString(t.t2)
        is EqOp -> toString(t.t1) + " == " + toString(t.t2)
        is NeqOp -> toString(t.t1) + " != " + toString(t.t2)

        else -> throw NoRuleApplies("Invalid expression")
    }
}

fun toString(v: ValueOrRaise) : String
{
    return when(v)
    {
        is Raise -> "raise"
        is Vnum -> v.n.toString()
        is Vbool -> v.b.toString()
        is Vnil -> "nil"
        is Vlist -> toString(v.h) + "::" + toString(v.t)
        is VClosure -> "< " + v.x.x + ", " + toString(v.e) + ", " + v.env.hm.toString() + ">"
        is VRecClosure -> "< "+ v.f.x +", "+ v.x.x + ", " + toString(v.e) + ", " + v.env.hm.toString() + ">"
    }
}

fun oneCharToken(c : Char) : Boolean{
    return c == '+' || c == '-' || c == '*' || c == '>' || c == '<' || c =='=' || c == ':' || c == '(' || c == ')'
}
fun twoCharToken(s: String) : Boolean{
    return s == "::" || s == ">=" || s == "<=" || s == "==" || s == "!=" || s == "->" || s == "=>"
}
//é considerado um token independente aquele que pode ter como vizinho qualquer outro caractere e continuará sendo
//o mesmo token
fun isIndependentToken(s : String) : Boolean{
    return when {
        s.length == 1 -> oneCharToken(s[0])
        s.length == 2 -> twoCharToken(s)
        else -> false
    }
}
// A diferença desse para twoCharToken é que os tokens dessa não podem estar conectados com outros caracteres que não sejam tokens
// ex: if2 seria considerado variável e não expressão if enquanto que >=2 são considerados 2 tokens
fun twoCharSepToken (s : String) : Boolean {
    val possibleToken = s.substring(0,2)
    return when {
        s.length == 2 -> s == "or" || s == "in" || s == "hd" || s == "tl" || s == "fn" || s == "if"
        else -> twoCharToken(possibleToken) && (isIndependentToken(s.substring(2,3)) || isIndependentToken(s.substring(2,4)))
    }
}
fun threeCharToken(s:String) : Boolean{
    val possibleToken = s.substring(0,3)
    return when{
        s.length == 3 -> s == "try" || s == "int" || s == "let" || s == "rec" || s == "nil" || s == "div" || s == "not" ||
                s == "and" || s == "try"
        else -> threeCharToken(possibleToken) && (isIndependentToken(s.substring(3,4)) || isIndependentToken(s.substring(3, 5)))
    }
}
fun fourCharToken(s:String) : Boolean{
    val possibleToken = s.substring(0,4)
    return when{
        s.length == 4 -> s == "bool" || s == "true" || s == "then" || s == "else" || s == "with" || s == "list"
        else -> fourCharToken(possibleToken) && (isIndependentToken(s.substring(4, 5)) || isIndependentToken(s.substring(4, 6)))
    }
}

fun tokenizeIndependentTokens(s:String) : ArrayList<String>{
    val tokens = arrayListOf<String>()

    val sepIndexes = arrayListOf<Int>(0)

    var i = 0
    while (i < s.length) {
        if (s.substring(i).length >= 2 && twoCharToken(s.substring(i, i+2))) {
            sepIndexes.add(i)
            sepIndexes.add(i + 2)
            i+=2
        }
        else if (oneCharToken(s.substring(i)[0])) {
            sepIndexes.add(i)
            sepIndexes.add(i + 1)
            i+=1
        }
        else
            i+=1
    }

    sepIndexes.add(s.length)
    for(j in 0 until sepIndexes.size - 1){
        val first = sepIndexes[j]
        val second = sepIndexes[j+1]

        tokens.add(s.substring(first, second))
    }
    return tokens

}

fun tokenize(s:String) : ArrayList<String>{
    if(s.isEmpty())
        return arrayListOf()
    val firstChar = s[0]
    val tokens = arrayListOf<String>()

    when{
        s.length >= 5 && s.substring(0,5) == "raise" ->{
            tokens.add("raise")
            tokens.addAll(tokenize(s.substring(5)))
        }

        s.length >= 7 && s.substring(0,7) == "isempty" ->{
            tokens.add("isempty")
            tokens.addAll(tokenize(s.substring(7)))
        }
        s.length >= 4 && fourCharToken(s) ->{
            tokens.add(s.substring(0,4))
            tokens.addAll(tokenize(s.substring(4)))
        }
        s.length >= 3 && threeCharToken(s)->{
            tokens.add(s.substring(0,3))
            tokens.addAll(tokenize(s.substring(3)))
        }
        s.length >= 2 && twoCharSepToken(s)->{
            tokens.add(s.substring(0,2))
            tokens.addAll(tokenize(s.substring(2)))
        }
        s.length >= 2 && twoCharToken(s.substring(0,2)) -> {
            tokens.add(s.substring(0,2))
            tokens.addAll(tokenize(s.substring(2)))
        }
        s.isNotEmpty() && oneCharToken(firstChar) ->{
            tokens.add(firstChar.toString())
            tokens.addAll(tokenize(s.substring(1)))
        }
        else -> {

            tokens.add(s) // variável ou número inteiro, ambos podem ter qualquer tamanho
        }
    }

    return tokens
}

fun separateInput(s : String) : ArrayList<String>{
    val spaceSeparated = s.split(" ")
    val tokens = arrayListOf<String>()
    for (subs in spaceSeparated){
        val sepTok = tokenizeIndependentTokens(subs)
        for (tok in sepTok)
        {
            tokens.addAll(tokenize(tok))
        }
    }
    val specialTokens = listOf("+", "-") // colocar aqui tokens que podem ter mais de um significado semantico
    var mistakenIndexes = listOf<Int>()
    for(i in 0 until tokens.size){
        for (j in 0 until specialTokens.size) { // sinal
            if (tokens[i] == specialTokens[j]) {
                // - / + precisa trocar quando representam o sinal do número
                //representam o sinal do número quando o token anterior for '(' || '*' || operadores de comparação ||
                // operadores aritméticos || :: || if || try || with || = || then || else
                //quando representam sinal vão ser unidos com o token seguinte
                if (i == 0)
                    mistakenIndexes = mistakenIndexes.plus(i)
                else if (i < tokens.size + 1) {
                    val prev = tokens[i - 1]
                    if (prev == "(" || prev == "*" || prev == "+" || prev == "-" || prev == "div" || prev == "<" ||
                            prev == "<=" || prev == "==" || prev == "!=" || prev == ">=" || prev == ">" || prev == "::" ||
                            prev == "if" || prev == "try" || prev == "with" || prev == "=" || prev == "then" ||
                            prev == "else"
                    ) {
                        mistakenIndexes = mistakenIndexes.plus(i)
                    }

                }

            }
        }

    }

    for (index in mistakenIndexes.reversed()) { // reverte para não precisar atualizar índices
        val newToken = tokens[index].plus(tokens[index+1])
        tokens.removeAt(index)
        tokens.removeAt(index) // o próximo
        tokens.add(index, newToken)
    }
    return tokens

}


fun normalizeExpression(expression: String) : String
{

    val multipleSpaces = " +" // um ou mais espaços
    val mSpacesRegex : Pattern = Pattern.compile(multipleSpaces)
    val mSpaceMatcher : Matcher = mSpacesRegex.matcher(expression)

    //remove espaços no inicio
    var readyExpr : String = mSpaceMatcher.replaceAll(" ")
    if (readyExpr[0] == ' ')
        readyExpr = readyExpr.substring(1)


    return readyExpr
}