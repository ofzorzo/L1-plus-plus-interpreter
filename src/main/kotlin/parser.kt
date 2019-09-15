import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.collections.ArrayList

abstract class Parser{
    private fun uniteRootLevelTerms(terms: MutableList<Term>, seps : MutableList<String>) : Term{

        if (terms.size - 1 != seps.size)
            throw ParserError() // Se o parser estiver correto isso nunca deve ocorrer
        if(seps.isEmpty()){
            return terms[0]
        }
        else{
            val currentSep = seps[0]
            //AVALIAÇÃO DA ESQUERDA PARA A DIREITA
            return when(currentSep){
                "+"   -> AddOp(terms[0], uniteRootLevelTerms(terms.subList(1, terms.size), seps.subList(1, seps.size)))
                "-"   -> SubOp(terms[0], uniteRootLevelTerms(terms.subList(1, terms.size), seps.subList(1, seps.size)))
                "*"   -> MulOp(terms[0], uniteRootLevelTerms(terms.subList(1, terms.size), seps.subList(1, seps.size)))
                "div" -> DivOp(terms[0], uniteRootLevelTerms(terms.subList(1, terms.size), seps.subList(1, seps.size)))
                ">="  -> GeOp(terms[0], uniteRootLevelTerms(terms.subList(1, terms.size), seps.subList(1, seps.size)))
                ">"   -> GrOp(terms[0], uniteRootLevelTerms(terms.subList(1, terms.size), seps.subList(1, seps.size)))
                "=="  -> EqOp(terms[0], uniteRootLevelTerms(terms.subList(1, terms.size), seps.subList(1, seps.size)))
                "<="  -> SeOp(terms[0], uniteRootLevelTerms(terms.subList(1, terms.size), seps.subList(1, seps.size)))
                "<"   -> SmOp(terms[0], uniteRootLevelTerms(terms.subList(1, terms.size), seps.subList(1, seps.size)))
                "!="  -> NeqOp(terms[0], uniteRootLevelTerms(terms.subList(1, terms.size), seps.subList(1, seps.size)))
                "and" -> AndOp(terms[0], uniteRootLevelTerms(terms.subList(1, terms.size), seps.subList(1, seps.size)))
                "or"  -> OrOp(terms[0], uniteRootLevelTerms(terms.subList(1, terms.size), seps.subList(1, seps.size)))
                "::"  -> TmCat(terms[0], uniteRootLevelTerms(terms.subList(1, terms.size), seps.subList(1, seps.size)))
                " "   -> TmApp(terms[0], uniteRootLevelTerms(terms.subList(1, terms.size), seps.subList(1, seps.size)))
                else -> throw ParserError()
            }
        }

    }

    private fun getSeparator(tokens : ArrayList<String>, index: Int, termination: String?) : Pair<String, Boolean>{
        val sep = tokens[index]
        return when(sep){
            termination -> Pair(sep, false)
            "+","*","-","div",">",">=","==","<=","<","!=","and","or", "::",termination -> Pair(sep, true)
            ":", "->", "=>", "then", "else", "rec", "in", "with" -> throw SintaxError("Invalid token $sep")//tokens que podem existir, mas serão inválidos
            else -> Pair(" ", false)//aplicação retorna false pois esse termo já é o lado direito da aplicação e não pode ser ignorado
        }
    }
    fun subParse(tokens : ArrayList<String>, index: Int ,termination: String?) : Pair<Term, Int>{

        var parsed = 0
        val rootLevelTerms = mutableListOf<Term>()
        val termSeparators = mutableListOf<String>()
        while(parsed + index< tokens.size){
            if(parsed > 0) {
                val sep = getSeparator(tokens, parsed + index, termination)
                if(sep.second)
                    parsed++
                if(sep.first == termination)
                    break

                termSeparators.add(sep.first)
            }
            val rootLevelParse = parse(tokens, parsed + index, termination)
            parsed += rootLevelParse.second
            rootLevelTerms.add(rootLevelParse.first)
        }
        return Pair(uniteRootLevelTerms(rootLevelTerms, termSeparators), parsed)

    }
    fun fromString(expression : String) : Term{
        val normExpr = normalizeExpression(expression)
        val tokens = separateInput(normExpr)
        return subParse(tokens, 0, null).first

    }
    private fun parse(tokens : ArrayList<String>, index : Int, termination : String?) : Pair<Term, Int>{
        val tryAsVal = parseValues(tokens[index])
        if (tryAsVal != null)
            return tryAsVal
        when(tokens[index]){
            "if" -> return parseIf(tokens, index + 1, termination)
            "fn" -> return parseFn(tokens, index + 1, termination)
            "isempty" -> return parseIsEmpty(tokens, index + 1, termination)
            "hd" -> return parseHd(tokens, index + 1, termination)
            "tl" -> return parseTl(tokens, index + 1, termination)
            "try" -> return parseTry(tokens, index + 1, termination)
            "let" -> return parseLet(tokens, index + 1, termination)
            "not" -> return parseNot(tokens, index + 1, termination)
            "(" -> {
                val subLevel = subParse(tokens, index + 1, ")")
                return Pair(subLevel.first, subLevel.second + 2)
            }
        }
        //se não atende nenhuma condição do when deve ser uma variável
        return Pair(TmVar(tokens[index]), 1)
    }
    private fun parseNot(tokens:ArrayList<String>, index : Int, termination : String?) : Pair<NotOp, Int>{
        val expr = subParse(tokens, index, termination)
        return Pair(NotOp(expr.first), expr.second + 1)
    }
    private fun parseIf(tokens : ArrayList<String>, index : Int, termination : String?) : Pair<TmIf, Int>{
        val condExpr : Pair<Term, Int> = subParse(tokens, index, "then")
        val thenExpr : Pair<Term, Int> = subParse(tokens, index + condExpr.second + 1, "else")
        val elseExpr : Pair<Term, Int> = subParse(tokens, index + condExpr.second + thenExpr.second + 2, termination)
        return Pair(TmIf(condExpr.first, thenExpr.first, elseExpr.first), elseExpr.second + thenExpr.second + condExpr.second + 3)
    }
    private fun parseIsEmpty(tokens : ArrayList<String>, index : Int, termination : String?) : Pair<TmIsEmpty, Int>{
        val innerExpr = subParse(tokens, index, termination)
        return Pair(TmIsEmpty(innerExpr.first), innerExpr.second + 1)
    }
    private fun parseHd(tokens : ArrayList<String>, index : Int, termination : String?) : Pair<TmHd, Int>{
        val innerExpr = subParse(tokens, index, termination)
        return Pair(TmHd(innerExpr.first), innerExpr.second + 1)
    }
    private fun parseTl(tokens : ArrayList<String>, index : Int, termination : String?) : Pair<TmTl, Int>{
        val innerExpr = subParse(tokens, index, termination)
        return Pair(TmTl(innerExpr.first), innerExpr.second + 1)
    }
    private fun parseTry(tokens : ArrayList<String>, index : Int, termination : String?) : Pair<TmTryWith, Int>{
        val tryExpr = subParse(tokens, index, "with")
        val withExpr = subParse(tokens, index + tryExpr.second + 1, termination)
        return Pair(TmTryWith(tryExpr.first, withExpr.first), tryExpr.second + withExpr.second + 2)

    }
    private fun parseLet(tokens : ArrayList<String>, index : Int, termination : String?) : Pair<Term, Int>{
        return if(tokens[index] == "rec"){
            parseLetRec(tokens, index + 1, termination)
        }
        else {
            parseNormalLet(tokens, index, termination)
        }
    }

    //Funções que necessitam de tratamento diferenciado do parser implícito / explícito
    abstract fun parseFn(tokens : ArrayList<String>, index : Int, termination : String?) : Pair<TmFn, Int>
    abstract fun parseLetRec(tokens : ArrayList<String>, index : Int, termination: String?) : Pair<TmLetRec, Int>
    abstract fun parseNormalLet(tokens : ArrayList<String>,index : Int, termination: String?) : Pair<TmLet, Int>
}

class ImplicitParser : Parser() {
    override fun parseLetRec(tokens: ArrayList<String>, index: Int, termination: String?): Pair<TmLetRec, Int> {
        // let rec f = (fn y => e1) in e2

        val tvar = subParse(tokens, index, "=")
        val fvar = tvar.first as? TmVar ?: throw SintaxError("expected the identifier of a variable after \"rec\"")

        if(tokens[index+1] != "=" || tokens[index + 2] != "(")
            throw SintaxError("Expected \"=\" sign after the identifier ${fvar.x} and expected function declared using \"(\" before the function declaration")

        val e1 = subParse(tokens, index + 3, ")")
        if (e1.first !is TmFn){
            throw SintaxError("Expected function declaration after \"(\"")
        }
        val e2 = subParse(tokens, index+3 + e1.second + 2, termination)
        return Pair(TmLetRec(fvar, TyUnknown(), TyUnknown(), (e1.first as TmFn).x, (e1.first as TmFn).e, e2.first),
                tvar.second + e1.second + e2.second + 6)

    }

    override fun parseNormalLet(tokens: ArrayList<String>, index: Int, termination: String?): Pair<TmLet, Int> {
        // let x = e1 in e2
        val tvar = subParse(tokens, index, "=")
        val fvar = tvar.first as? TmVar ?: throw SintaxError("Expected variable identifier after \"let\"")
        if(tokens[index+1] != "=")
            throw SintaxError("Expected \"=\" signal after \"${fvar.x}\" identifier")
        val e1 = subParse(tokens, index + 2, "in")
        val e2 = subParse(tokens, index + 2 + e1.second + 1, termination)
        return Pair(TmLet(fvar, TyUnknown(), e1.first, e2.first), 3 + e1.second + 1 + e2.second)

    }


    override fun parseFn(tokens: ArrayList<String>, index: Int, termination: String?): Pair<TmFn, Int> {
        val varExpr = TmVar(tokens[index])
        val funBody : Pair<Term, Int> = subParse(tokens, index + 2, termination) // soma um pelo termo '=>'
        return Pair(TmFn(varExpr, TyUnknown(), funBody.first), 2 + funBody.second + 1)//soma 2 (var + =>) //soma 1 próprio fn
    }

}
class ExplicitParser : Parser() {
    override fun parseLetRec(tokens: ArrayList<String>, index: Int, termination: String?): Pair<TmLetRec, Int> {
        // let rec f:T1->T2 = (fn y:T1 => e1) in e2

        val tvar = subParse(tokens, index, ":")
        val fvar = tvar.first as? TmVar ?: throw SintaxError("expected the identifier of a variable after \"rec\"")
        val inType = parseType(tokens, index + 2, "->")
        val outType = parseType(tokens, index + 2 + inType.second + 1, "=")

        if(tokens[index+2 + inType.second + outType.second + 1] != "=")
            throw SintaxError("Expected \"=\" sign after the identifier ${fvar.x} and expected function declared using \"(\" before the function declaration")
        val e1 = subParse(tokens, index + 2 + inType.second + 1 + outType.second + 2, ")")
        if (e1.first !is TmFn){
            throw SintaxError("Expected function declaration after \"(\"")
        }
        val e2 = subParse(tokens, index+2 + inType.second + 1 + outType.second + 2 + e1.second + 2, termination)
        return Pair(TmLetRec(fvar, inType.first, outType.first, (e1.first as TmFn).x, (e1.first as TmFn).e, e2.first),
                2 + tvar.second + 1 + inType.second + 1 + outType.second + 2 + e1.second + 2 + e2.second)

    }

    override fun parseNormalLet(tokens: ArrayList<String>, index: Int, termination: String?): Pair<TmLet, Int> {
        // let x:T = e1 in e2
        val tvar = subParse(tokens, index, ":")
        val fvar = tvar.first as? TmVar ?: throw SintaxError("Expected variable identifier after \"let\"")
        val vtype = parseType(tokens, index + 2, "=")
        if(tokens[index+2+vtype.second] != "=")
            throw SintaxError("Expected \"=\" signal after \"${fvar.x}\" identifier")
        val e1 = subParse(tokens, index+2+vtype.second + 1, "in")
        val e2 = subParse(tokens, index+2+vtype.second + e1.second + 2, termination)
        return Pair(TmLet(fvar, vtype.first, e1.first, e2.first), 3 + e1.second + 2 + e2.second + vtype.second)

    }
    fun parseType(tokens: ArrayList<String>, index : Int, termination: String? = null) : Pair<Type, Int>{



        val firstToken = tokens[index]

        val firstLevelType = when (firstToken){
            "int" -> Pair(TyInt(), 1)
            "bool" -> Pair(TyBool(), 1)
            "(" -> {
                val subLevelType = parseType(tokens, index + 1, ")")
                Pair(subLevelType.first, subLevelType.second + 2)
            }
            else -> throw SintaxError("Invalid type begining with \"$firstToken\"")
        }
        var tokInd = index + firstLevelType.second
        var currentToken = tokens[tokInd]
        if(tokInd < tokens.size && currentToken!=termination){
            return when(currentToken){
                "->" ->{
                    val subToken = parseType(tokens, tokInd + 1, termination)
                    Pair(TyFn(firstLevelType.first, subToken.first), firstLevelType.second + 1 + subToken.second)
                }
                "list"->{
                    val listType = Pair(TyList(firstLevelType.first), firstLevelType.second + 1)
                    if(tokInd+1<tokens.size && tokens[tokInd+1]!=termination){
                        if(tokens[tokInd+1] == "->")
                        {
                            val subToken = parseType(tokens, tokInd + 1, termination)
                            Pair(TyFn(listType.first, subToken.first), listType.second + 1 + subToken.second)
                        }
                        else{
                            listType
                        }
                    }
                    else{
                        listType
                    }
                }
                else -> throw SintaxError("Invalid type on \"$currentToken\"")

            }
        }
        return firstLevelType
    }

    override fun parseFn(tokens : ArrayList<String>, index : Int, termination: String?) : Pair<TmFn, Int>{
        val varExpr = TmVar(tokens[index])
        val typeExpr : Pair<Type, Int> = parseType(tokens, index + 2, "=>") // 2 : string da variável + separador ':'
        val funBody : Pair<Term, Int> = subParse(tokens, index + 2 + typeExpr.second + 1,termination) // soma um pelo termo '=>'

        return Pair(TmFn(varExpr, typeExpr.first, funBody.first), 3 + typeExpr.second + funBody.second + 1)
    }
}


fun makeList(vals : List<Term>) : Term{

    if (vals.isEmpty())
        return TmNil()
    val prevRecLevel = makeList(vals.subList(1, vals.size))
    val t = vals[0]
    return when (t)
    {
        is TmNum -> TmList(Vnum(t.n), prevRecLevel)
        is TmBool -> TmList(Vbool(t.b), prevRecLevel)
        is TmNil -> TmList(Vnil(), prevRecLevel)
        is TmList -> TmList(Vlist(t.h, buildVList(t.t)), prevRecLevel)
        else -> TmNil()
    }
}
fun parseValues(expr : String) : Pair<Term, Int>?{
    try{
        val exprCpy = "" + expr
        val num = exprCpy.toInt()
        return Pair(TmNum(num), 1)
    }catch(e : Exception){ /*DO NOTHING */}
    if (expr[0] == '[')
    {
        val exprL = expr.filter { it != '[' && it != ']' }
        val listVals = exprL.split(",")
        val L1Vals = listVals.map { (parseValues(it) as Pair<Term, Int>).first }
        return Pair(makeList(L1Vals), 1) // precisa que os itens sejam valores
    }
    return when(expr){
        "true" -> Pair(TmBool(true), 1)
        "false" -> Pair(TmBool(false), 1)
        "nil"   -> Pair(TmNil(), 1)
        "raise" -> Pair(TmRaise(), 1)
        else    -> null

    }
}

