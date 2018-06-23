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
/*
    @Test
    fun ifTest() {
        val ident = identTable(hashMapOf())
        val ans = mutableListOf<String>("bool=bool", "int=int")
        Assert.assertEquals(Pair("int", ans), typeConsColl(if1, ident, 0, mutableListOf(), false, 0))

        val ans2= mutableListOf<String>("int=int", "int=int", "bool=bool", "int=int") // os dois primeiros "int=int" se referem às constraints de equal1
        Assert.assertEquals(Pair("int", ans2), typeConsColl(if2, ident, 0, mutableListOf(), false, 0))
    }*/
/*
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
    }*/

    /*
    private val fVar = TmVar("fat")
    private val intTy = TyInt()
    private val fatBody = TmIf(SeOp(defVar, TmNum(0)), TmNum(1), MulOp(defVar, TmApp(fVar, SubOp(defVar, TmNum(1)))))
    private val letRec1 = TmLetRec(fVar, intTy, intTy, defVar, fatBody, TmApp(fVar, TmNum(5)))
    @Test
    fun letRecExplicit() {
        var ident = identTable(hashMapOf())
        var teste = typeConsColl(letRec1, ident, 0, mutableListOf(), false, 0)
        var ans = mutableListOf<String>("int=int", "int=int", "int=int", "int=int", "int->int=int->X0", "int=int", "X0=int", "bool=bool", "int=int", "int->int=int->X0", "int=int")
        if (teste == Pair("X0", ans))
            println("OK, DEU CERTO!")
        else {
            println("DEU RUIM")
            println(Pair("X0", ans))
            println(teste)
        }
    }*/
/*
    private val inteiroParaBool = TyFn(TyInt(), TyBool())
    private val f = TmVar("f")
    private val e1 = EqOp(f, f)
    private val fn1 = TmFn(f, TyInt(), e1)
    @Test
    fun fnExplicit(){ //depois que o joão adicionar os parênteses, fazer um teste passando uma funcao como parametro
        var ident = identTable(hashMapOf())
        var ans = mutableListOf<String>("int=int", "int=int")
        Assert.assertEquals(Pair("(int->bool)", ans), typeConsColl(fn1 , ident, 0, mutableListOf(), false, 0))
    }*/


    private val list1 = TmList(Vnum(12), TmList(Vnum(-42), TmNil()))
    private val list4 = TmNil()
    private val funcName = TmVar("teste")
    private val entryName = TmVar("y")
    private val intLista = TyList(TyInt())
    private val boolTeste = TyBool()
    private val corpo = TmIf(TmIsEmpty(entryName), TmBool(true), TmApp(funcName, TmTl(entryName)))
    private val e2 = TmApp(funcName, list1)
    private val e22 = TmApp(funcName, list4)
    private val letRec2 = TmLetRec(funcName, intLista, boolTeste, entryName, corpo, e2)
    private val letRec3  = TmLetRec(funcName, intLista, boolTeste, entryName, corpo, e22)
    @Test
    fun letRecExplicit(){
        //var ident = identTable(hashMapOf())
        //var ans = mutableListOf<String>("int list=X0 list", "int list=X1 list", "(int list->bool)=(X1 list->X0)", "bool=bool", "bool=X0", "(int list->bool)=(int list->X0)", "bool=bool")
        //Assert.assertEquals(Pair("X0", ans), typeConsColl(letRec2 , ident, 0, mutableListOf(), false, 0))

        var ident2 = identTable(hashMapOf())
        var ans2 = mutableListOf<String>("int list=X0 list", "int list=X1 list", "(int list->bool)=(X1 list->X0)", "bool=bool", "bool=X0", "(int list->bool)=(int list->X0)", "bool=bool")
        println(typeConsColl(letRec3 , ident2, 0, mutableListOf(), false, 0))
        println(ident2.d)
        //Assert.assertEquals(Pair("z0", ans2), typeConsColl(letRec3 , ident2, 0, mutableListOf(), false, 0))
    }

    private val list2 = TmList(Vnum(12), TmList(Vnum(-32), TmNil()))
    private val list3 = TmList(Vnum(12), TmNil())
    private val tail = TmTl(list2)
    private val tail2 = TmTl(list3)
    /*@Test
    fun tail(){
        var ident = identTable(hashMapOf())
        Assert.assertEquals(Pair("X0 list", mutableListOf<String>("int list=X0 list")), typeConsColl(tail, ident, 0, mutableListOf(), false, 0))

        var ident2 = identTable(hashMapOf()) //o tipo de tail(lista), para uma lista com apenas um elemento, será "tipo_elemento list"
        Assert.assertEquals(Pair("X0 list", mutableListOf<String>("int list=X0 list")), typeConsColl(tail2, ident2, 0, mutableListOf(), false, 0))
    }*/

}