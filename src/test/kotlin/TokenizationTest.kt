import org.junit.Assert
import org.junit.Test

class TokenizationTest {


    @Test
    fun normalizeExpressionTest(){
        //a função normalizeExpression tem como objetivo normalizar o número de caracteres espaço numa string que será
        // expressão da linguagem
        Assert.assertEquals(normalizeExpression("   2  + 2"), "2 + 2")
        Assert.assertEquals(normalizeExpression("   2   +    2"), "2 + 2")

    }

    private val expr1 = "let x:Int=2 in (let y:Int=3 in (let l:IntList=12::32 in if isempty l then 2 + 3 else (fn a:Int => a + x) y))"
    private val expr2 = "try 12 div 0 with (let l:IntList = 2::3 in hd(l) * 3)"

    private val expr1Tok = arrayListOf<String>("let", "x", ":", "Int", "=", "2", "in", "(", "let", "y", ":",
            "Int", "=", "3", "in", "(", "let", "l", ":", "IntList", "=", "12", "::", "32", "in", "if", "isempty", "l",
            "then", "2", "+", "3", "else","(", "fn", "a", ":", "Int", "=>", "a", "+", "x", ")", "y", ")", ")")

    private val expr2Tok = arrayListOf<String>("try", "12", "div", "0", "with", "(", "let", "l", ":", "IntList",
            "=", "2", "::", "3", "in", "hd", "(", "l", ")", "*", "3", ")")
    @Test
    fun separateInputTest(){
        Assert.assertEquals(expr1Tok, separateInput(expr1))
        Assert.assertEquals(expr2Tok, separateInput(expr2))


    }


}