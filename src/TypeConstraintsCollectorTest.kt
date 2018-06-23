import org.junit.Assert
import org.junit.Test

class TypeConstraintsCollectorTest {
    private val True = TmBool(true)
    private val equal1 = EqOp(TmNum(1), TmNum(2))
    private val num2 = TmNum(2)
    private val num3 = TmNum(3)
    private val if1 = TmIf(True, num2, num3)
    private val if2 = TmIf(equal1, num2, num3)
    private val defVar = TmVar("x")
    private val addX = AddOp(defVar, defVar)
    private val andX = AndOp(defVar, defVar)
    private val let1 = TmLet(defVar, TyInt(), num2, addX) // let x: Int = 2 in x+x
    private val let2 = TmLet(defVar, TyBool(), num2, addX) // let x: Bool = 2 in x+x
    private val let3 = TmLet(defVar, TyBool(), num2, andX) // let x: Bool = 2 in x and x
    private val let4 = TmLet(defVar, TyBool(), True, andX) // let x: Bool = true in x and x
    private val letR1 = TmLetRec(TmVar("f"), TyInt(), TyFn(TyInt(), TyBool()), TmVar("y"), EqOp(num2, num2), TmVar("y"))

    private val fVar = TmVar("fat")
    private val intTy = TyInt()
    private val fatBody = TmIf(SeOp(defVar, TmNum(0)), TmNum(1), MulOp(defVar, TmApp(fVar, SubOp(defVar, TmNum(1)))))

    private val letRec1 = TmLetRec(fVar, intTy, intTy, defVar, fatBody, TmApp(fVar, TmNum(5)))

    @Test
    fun ifTest() {
        val ident = identTable(hashMapOf())
        val ans = mutableListOf<String>("bool=bool", "int=int")
        Assert.assertEquals(Pair("int", ans), typeConsColl(if1, ident, 0, mutableListOf(), false, 0))

        val ans2= mutableListOf<String>("int=int", "int=int", "bool=bool", "int=int") // os dois primeiros "int=int" se referem às constraints de equal1
        Assert.assertEquals(Pair("int", ans2), typeConsColl(if2, ident, 0, mutableListOf(), false, 0))
    }

    @Test
    fun letExplicit() {
        var ident = identTable(hashMapOf())

        val ans = mutableListOf<String>("int=int", "int=int", "int=int") // dois primeiros int=int vêm da adição em e2, terceiro vêm de T=T1
        Assert.assertEquals(Pair("int", ans), typeConsColl(let1, ident, 0, mutableListOf(), false, 0))
        println(ident.d)
        val ans6 = mutableListOf<String>()
        Assert.assertEquals(Pair("int", ans6), typeConsColl(defVar, ident, 0, mutableListOf(), false, 0))

        ident = identTable(hashMapOf())
        val ans2 = mutableListOf<String>("bool=int", "bool=int", "bool=int") // os dois primeiros bool=int vêm do fato de que estou tentando adicionar dois bools
        Assert.assertEquals(Pair("int", ans2), typeConsColl(let2, ident, 0, mutableListOf(), false, 0))
        println(ident.d)

        ident = identTable(hashMapOf())
        val ans3 = mutableListOf<String>("bool=bool", "bool=bool", "bool=int") // esse int vem do fato de que a ultima constraint é T=T1; T o usuário informou que é bool, e T1 é o tipo da expressão e1, que no caso é int (num2)
        Assert.assertEquals(Pair("bool", ans3), typeConsColl(let3, ident, 0, mutableListOf(), false, 0))
        println(ident.d)

        ident = identTable(hashMapOf())
        val ans4 = mutableListOf<String>("bool=bool", "bool=bool", "bool=bool")
        Assert.assertEquals(Pair("bool", ans4), typeConsColl(let4, ident, 0, mutableListOf(), false, 0))
        println(ident.d)
        val ans5 = mutableListOf<String>()
        Assert.assertEquals(Pair("bool", ans5), typeConsColl(defVar, ident, 0, mutableListOf(), false, 0))
    }

    @Test
    fun letRecExplicit(){
        var ident = identTable(hashMapOf())
        println(typeConsColl(letRec1, ident, 0, mutableListOf(), false, 0))
        var ans = mutableListOf<String>("int=int", "int=int", "int=int", "int=int", "int->int=int->X1", "int=int", "X1=int", "bool=bool", "int=int", "int->int=int->X0", "int=int")
        var ident2 = identTable(hashMapOf())
        Assert.assertEquals(Pair("X0", ans), typeConsColl(letRec1, ident2, 0, mutableListOf(), false, 0))
    }


}