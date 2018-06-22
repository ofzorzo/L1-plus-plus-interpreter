import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.collections.ArrayList


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



sealed class BinNumOp(val t1: Term, val t2:Term, val f:(Int, Int)->Int) : Term()
data class AddOp(private val e1:Term, private val e2:Term) : BinNumOp(e1, e2, fun(a:Int, b:Int):Int{return a+b})
data class SubOp(private val e1:Term, private val e2:Term) : BinNumOp(e1, e2, fun(a:Int, b:Int):Int{return a-b})
data class MulOp(private val e1:Term, private val e2:Term) : BinNumOp(e1, e2, fun(a:Int, b:Int):Int{return a*b})
data class DivOp(private val e1:Term, private val e2:Term) : BinNumOp(e1, e2, fun(a:Int, b:Int):Int{return a/b})

sealed class BinBoolOp(val t1: Term, val t2:Term, val f:(Boolean, Boolean)->Boolean) : Term()
data class AndOp(private val e1: Term, private val e2:Term) : BinBoolOp(e1, e2, fun(a:Boolean, b:Boolean):Boolean{return a&&b})
data class OrOp(private val e1: Term, private val e2:Term) : BinBoolOp(e1, e2, fun(a:Boolean, b:Boolean):Boolean{return a||b})

sealed class CompNumOp(val t1: Term, val t2:Term, val f:(Int, Int)->Boolean) : Term()
data class GrOp(private val e1:Term, private val e2:Term) : CompNumOp(e1, e2, fun(a:Int, b:Int):Boolean{return a>b})
data class GeOp(private val e1:Term, private val e2:Term) : CompNumOp(e1, e2, fun(a:Int, b:Int):Boolean{return a>=b})
data class SmOp(private val e1:Term, private val e2:Term) : CompNumOp(e1, e2, fun(a:Int, b:Int):Boolean{return a<b})
data class SeOp(private val e1:Term, private val e2:Term) : CompNumOp(e1, e2, fun(a:Int, b:Int):Boolean{return a<=b})
data class EqOp(private val e1:Term, private val e2:Term) : CompNumOp(e1, e2, fun(a:Int, b:Int):Boolean{return a==b})
data class NeqOp(private val e1:Term, private val e2:Term) : CompNumOp(e1, e2, fun(a:Int, b:Int):Boolean{return a!=b})

sealed class UnaryBoolOp(val t1: Term, val f:(Boolean)->Boolean) : Term()
data class NotOp(private val e1: Term) : UnaryBoolOp(e1, fun(a:Boolean):Boolean{return !a})

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


class NoRuleApplies : Throwable()
class IdentNotDefined : Throwable()
class SintaxError : Throwable()
class ParentesisDoesNotMatch : Throwable()

fun buildVList(l : Term) : Value{

    return when (l)
    {
        is TmNil -> Vnil()
        is TmList -> Vlist(l.h, buildVList(l.t))
        else -> throw NoRuleApplies() //*
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

data class identTable(val d : Dictionary<String, String>) //dicionario de string(ident)->string(tipo)

fun typeConsColl(e : Term, ident : identTable, recursionLevel : Int, constraints : MutableList<String>, name : String): MutableList<String> {
    return when (e){
        is TmLet -> {
            var newConstraints: MutableList<String>
            var newConstraints2: MutableList<String>
            var endConstraints: MutableList<String>
            newConstraints = typeConsColl(e.e1, ident, recursionLevel + 1, constraints)
            newConstraints2 = typeConsColl(e.e2, ident, recursionLevel + 1, constraints)
            endConstraints = addConstraints(constraints)
            endConstraints = addConstraints(endConstraints, newConstraints)
            endConstraints = addConstraints(endConstraints, newConstraints2)
            endConstraints.add("X=T1")
            ident.d.put(e.x.x, "X")
            endConstraints
        }
        is TmNum -> constraints
        is TmBool -> constraints
        is TmIf -> {
            var newConstraints: MutableList<String>
            var newConstraints2: MutableList<String>
            var newConstraints3: MutableList<String>
            var endConstraints: MutableList<String>
            newConstraints = typeConsColl(e.e1, ident, recursionLevel + 1, constraints)
            newConstraints2 = typeConsColl(e.e2, ident, recursionLevel + 1, constraints)
            newConstraints3 = typeConsColl(e.e3, ident, recursionLevel + 1, constraints)
            endConstraints = addConstraints(constraints, newConstraints)
            endConstraints = addConstraints(endConstraints, newConstraints2)
            endConstraints = addConstraints(endConstraints, newConstraints3)
            endConstraints
        }
    }
}

//Trechos marcados com:
//  * : nunca devem acontecer se verificação de tipos for correta
fun bigStep (e : Term, env : Env) : ValueOrRaise{
    return when (e){
        is TmNum -> Vnum(e.n) // BS-NUM
        is TmBool -> Vbool(e.b) // BS-BOOL
        is TmVar -> { try{ env.hm[e]!! } catch (ex : Exception){ throw IdentNotDefined() } } //BS-ID
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
        else -> throw  NoRuleApplies()
    }
}
/*
fun fromString(expression: String ) : Term
{
    val normExpr = normalizeExpression(expression)
    val tokens = separateInput(normExpr)

    return parse(tokens).first

}


fun checkParentesis(s: String) : Pair<Boolean, ArrayList<Pair<Int, Int>>>{
    val parQueue : Queue<Int> = ArrayDeque<Int>()
    val matchedParentesis : ArrayList<Pair<Int, Int>> = arrayListOf()

    for(i:Int in 0 until s.length){
        if(s[i] == '(') {
            parQueue.add(i + 1)
        }else if(s[i] == ')')
        {
            if(parQueue.isEmpty())
                return Pair(false, matchedParentesis)
            else
                matchedParentesis.add(Pair(parQueue.remove(), i-1))
        }
    }

    if (parQueue.isEmpty())
    {
        return Pair(true, matchedParentesis)
    }
    return Pair(false, matchedParentesis)
}





fun parseIf(tokens : ArrayList<String>, index : Int) : Pair<TmIf, Int>{
    val condExpr : Pair<Term, Int> = parse(tokens, index, "then")
    val thenExpr : Pair<Term, Int> = parse(tokens, index + condExpr.second, "else")
    val elseExpr : Pair<Term, Int> = parse(tokens, index + condExpr.second + thenExpr.second, "")
    return Pair(TmIf(condExpr.first, thenExpr.first, elseExpr.first), elseExpr.second + thenExpr.second + condExpr.second)
}

fun parseFn(tokens : ArrayList<String>, index : Int) : Pair<TmFn, Int>{
    val varExpr : TmVar = TmVar(tokens[index])
    val typeExpr : Pair<Type, Int> = parseType(tokens, index + 2) // 2 : string da variável + separador ':'
    val funBody : Pair<Term, Int> = parse(tokens, index + 2 + typeExpr.second + 1) // soma um pelo termo '=>'

    return Pair(TmFn(varExpr, typeExpr.first, funBody.first), 2 + typeExpr.second + funBody.second)
}

fun parseType(tokens: ArrayList<String>, index : Int, termination: String? = null) : Pair<Type, Int>{

    return when {
        "(" -> {
            val sublevelTree: Pair<Term, Int> = parseType(tokens, index + 1, ")")
            Pair(sublevelTree.first, sublevelTree.second + 2) // soma a abertura e fechamento de parencesis
        }
    }
}

fun parse(tokens : ArrayList<String>, index:Int = 0, termination:String? = null) : Pair<Term, Int>{
    val firstToken : String = tokens[index]

    if(termination != null && firstToken == termination)
        return Pair(TmFinishExpr(), 0)

    return when(firstToken)
    {
        /**
         *data class TmVar(val x : String) : Term()
        class TmIf(val e1: Term, val e2 : Term, val e3 : Term) : Term()
        data class TmApp(val e1 : Term, val e2 : Term) : Term()
        data class TmFn(val x : TmVar, val t : Type , val e: Term) : Term()
        data class TmLet(val x : TmVar, val t: Type, val e1 : Term, val e2:Term) : Term()
        data class TmLetRec(val f: TmVar, val fin : Type, val fout : Type, val x: TmVar, val e1 : Term, val e2:Term) : Term()
        data class TmTryWith(val e1 : Term, val e2:Term) : Term()
        data class TmHd (val e : Term) : Term()
        data class TmTl (val e : Term) : Term()
        data class TmIsEmpty(val e : Term) : Term()
        data class TmCat(val e1 : Term, val e2 : Term) : Term()
         * */
        "(" -> {
            val sublevelTree : Pair<Term, Int> = parse(tokens, index + 1, ")")
            Pair(sublevelTree.first, sublevelTree.second + 2) // soma a abertura e fechamento de parencesis
        }
        "if" ->parseIf(tokens, index + 1)
        "fn" -> parseFn(tokens, index + 1)
        "let" -> parseLet(tokens, index + 1)
        "isempty" -> parseIsEmpty(tokens, index + 1)
        "hd" -> parseHd(tokens, index + 1)
        "tl" -> parseTl(tokens, index + 1)
        "raise" -> parseRaise(tokens, index + 1)
        "false" -> parseFalse(tokens, index + 1)
        "true" -> parseTrue(tokens, index + 1)
        "nil" -> parseNil(tokens, index + 1)
        "try" -> parseTry(tokens, index + 1)
        "not" -> parseNot(tokens, index + 1)
        else -> {
            // Nesse caso o termo pode ser:
            // * Um operador binário (aritmeticos, booleanos, aplicação, concatenação, etc)
            // * Uma variável
            // * Um valor
        }


    }
}

//retorna o termo e um inteiro com a quantidade de tonkens usadas
fun parse(tokens : ArrayList<String>) : Pair<Term, Int>{

    val firstToken : String = tokens[0]
    return when(firstToken){

        "if" -> parseIf(tokens)
    }

}

*/
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
    println(spaceSeparated)
    val tokens = arrayListOf<String>()
    for (subs in spaceSeparated){
        val sepTok = tokenizeIndependentTokens(subs)
        for (tok in sepTok)
        {
            tokens.addAll(tokenize(tok))
        }
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

fun toString(t : Type) : String
{
    return when(t)
    {
        is TyInt -> "int"
        is TyBool -> "bool"
        is TyFn -> toString(t.inp) + "->" + toString(t.out)
        is TyList -> toString(t.T) + "List"
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

        else -> throw NoRuleApplies()
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

/*
fun evaluate(expression: String) : String
{
    val initTerm = fromString(expression)
    val initHash : HashMap<TmVar, Value> = hashMapOf()
    val initEnv = Env(initHash)
    val normalForm = bigStep(initTerm, initEnv)

    return toString(normalForm)

}



fun main(args: Array<String>) {


    println("Interpretador of L1++: \n")
    while (true)
    {
        print(">> ")
        try {
            val typedExpr: String? = readLine()
            val result:String = evaluate(typedExpr!!)
            println(" = $result") // usa template $ para substituição ao invés de concatenação
        }catch(e : Throwable){
            error("Exception: " + e.message)
        }
    }

}
*/