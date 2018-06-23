import org.junit.Assert
import org.junit.Test
import kotlin.math.exp

class UnifyTest {

    /* Testes que recebem como entrada as saídas da função typeConsColl(...) */

    @Test
    fun ifTests() {

        val num1 = TmNum(1)
        val num2 = TmNum(2)
        val num3 = TmNum(3)
        val equal = EqOp(num1, num2)

        // TESTE:         if True then 2 else 3
        // RESTRIÇÕES:    bool=bool e int=int
        // SUBSTITUIÇÕES: -
        val restrictionsIf1 = typeConsColl(TmIf(TmBool(true), num2, num3),
                                            identTable(hashMapOf()), 0, mutableListOf(), true, false)
        Assert.assertEquals(mutableListOf<String>(), unify(restrictionsIf1.second, mutableListOf()))

        // TESTE:         if 1==2 then false else true
        // RESTRIÇÕES:    bool=bool e bool=bool
        // SUBSTITUIÇÕES: -
        val restrictionsIf2 = typeConsColl(TmIf(equal, TmBool(false), TmBool(true)),
                                            identTable(hashMapOf()), 0, mutableListOf(), true, false)
        Assert.assertEquals(mutableListOf<String>(), unify(restrictionsIf2.second, mutableListOf()))
    }

    @Test(expected = UnifyFail::class)
    fun ifErrorTests() {
        val num1 = TmNum(1)
        val num2 = TmNum(2)
        val num3 = TmNum(3)

        // TESTE:         if 1 then 2 else 3
        // RESTRIÇÕES:    int=bool e int=int
        // SUBSTITUIÇÕES: - (UnifyFail())
        val restrictionsIf1 = typeConsColl(TmIf(num1, num2, num3),
                                    identTable(hashMapOf()), 0, mutableListOf(), true, false)
        unify(restrictionsIf1.second, mutableListOf())

        // TESTE:         if true then false else 2
        // RESTRIÇÕES:    bool=bool e bool=int
        // SUBSTITUIÇÕES: - (UnifyFail())
        val restrictionsIf2 = typeConsColl(TmIf(TmBool(true), TmBool(false), num2),
                                    identTable(hashMapOf()), 0, mutableListOf(), true, false)
        unify(restrictionsIf2.second, mutableListOf())
    }

    @Test
    fun letTests() {
        val num1 = TmNum(1)
        val num2 = TmNum(2)
        val num3 = TmNum(3)
        val x = TmVar("x")
        val y = TmVar("y")

        // TESTE:         let x: int = 2 in x+x
        // RESTRIÇÕES:    int=int (3x)
        // SUBSTITUIÇÕES: -
        val addCommand = AddOp(x, x)
        val restrictionsLet2 = typeConsColl(TmLet(x, TyInt(), num1, addCommand),
                                            identTable(hashMapOf()), 0, mutableListOf(), false, true)
        Assert.assertEquals(mutableListOf<String>(), unify(restrictionsLet2.second, mutableListOf()))

        // TESTE:         let y: int = 2 in let x: int = 3 in x*y
        // RESTRIÇÕES:    int=int (muitas vezes)
        // SUBSTITUIÇÕES: -
        val multCommand = MulOp(x, y)
        val restrictionsMultiplication = typeConsColl(TmLet(x, TyInt(), num2, TmLet(y, TyInt(), num3, multCommand)),
                                            identTable(hashMapOf()), 0, mutableListOf(), false, true)
        Assert.assertEquals(mutableListOf<String>(), unify(restrictionsMultiplication.second, mutableListOf()))
    }

    @Test(expected = UnifyFail::class)
    fun letErrorTests() {
        val num1 = TmNum(1)
        val num2 = TmNum(2)
        val x = TmVar("x")
        val y = TmVar("y")

        // TESTE:         let x: int = 2 in x+True
        // RESTRIÇÕES:    int=int (3x)
        // SUBSTITUIÇÕES: - (UnifyFail())
        val addCommand = AddOp(x, TmBool(true))
        val restrictionsAdd = typeConsColl(TmLet(x, TyInt(), num1, addCommand),
                                    identTable(hashMapOf()), 0, mutableListOf(), false, true)
        unify(restrictionsAdd.second, mutableListOf())

        // TESTE:         let y: int = 2 in let x: int = True in x*y
        // RESTRIÇÕES:    int=int (muitas vezes)
        // SUBSTITUIÇÕES: - (UnifyFail())
        val multCommand = MulOp(x, y)
        val restrictionsMult = typeConsColl(TmLet(x, TyInt(), num2, TmLet(y, TyInt(), TmBool(true), multCommand)),
                                    identTable(hashMapOf()), 0, mutableListOf(), false, true)
        unify(restrictionsMult.second, mutableListOf())
    }

    @Test
    fun fnTests() {
        // TESTE:         fn x: int->bool => x = x
        // SUBSTITUIÇÕES: -
        val x = TmVar("x")
        val equal = EqOp(x, x)
        val restrictionsFnEqual = typeConsColl(TmFn(x, TyInt(), equal),
                                    identTable(hashMapOf()), 0, mutableListOf(), false, true)
        Assert.assertEquals(mutableListOf<String>(), unify(restrictionsFnEqual.second, mutableListOf()))
    }

    @Test(expected = UnifyFail::class)
    fun fnTestsError() {
        // TESTE:         fn x: int->bool => True = x
        // SUBSTITUIÇÕES: -
        val x = TmVar("x")
        val equal = EqOp(x, TmBool(true))
        val restrictionsFnEqual = typeConsColl(TmFn(x, TyInt(), equal),
                identTable(hashMapOf()), 0, mutableListOf(), false, true)
        unify(restrictionsFnEqual.second, mutableListOf())
    }

    @Test()
    fun headTests() {
        // TESTE:         hd(list), sendo list = (12, -32)
        // SUBSTITUIÇÕES: X0 list = int list
        val numList = TmList(Vnum(12), TmList(Vnum(-32), TmNil()))
        val restrictionHead1 = typeConsColl(TmHd(numList),
                identTable(hashMapOf()), 0, mutableListOf(), false, true)
        Assert.assertEquals(mutableListOf("X0 list=int list"), unify(restrictionHead1.second, mutableListOf()))

        // TESTE:         hd(list), sendo list = (false, true, false)
        // SUBSTITUIÇÕES: X0 list = bool list
        val boolList = TmList(Vbool(false), TmList(Vbool(true), TmList(Vbool(false), TmNil())))
        val restrictionHead2 = typeConsColl(TmHd(boolList),
                identTable(hashMapOf()), 0, mutableListOf(), false, true)
        Assert.assertEquals(mutableListOf("X0 list=bool list"), unify(restrictionHead2.second, mutableListOf()))
    }

    @Test()
    fun tailTests() {
        // TESTE:         tl(list), sendo list = (12, -32)
        // SUBSTITUIÇÕES: X0 list = int list
        val numList = TmList(Vnum(12), TmList(Vnum(-32), TmNil()))
        val restrictionTai11 = typeConsColl(TmTl(numList),
                identTable(hashMapOf()), 0, mutableListOf(), false, true)
        Assert.assertEquals(mutableListOf("X0 list=int list"), unify(restrictionTai11.second, mutableListOf()))

        // TESTE:         tl(list), sendo list = (false, true, false)
        // SUBSTITUIÇÕES: X0 list = bool list
        val boolList = TmList(Vbool(false), TmList(Vbool(true), TmList(Vbool(false), TmNil())))
        val restrictionTail2 = typeConsColl(TmTl(boolList),
                identTable(hashMapOf()), 0, mutableListOf(), false, true)
        Assert.assertEquals(mutableListOf("X0 list=bool list"), unify(restrictionTail2.second, mutableListOf()))
    }

    @Test
    fun isEmptyTests() {
        // TESTE:         isempty(list), sendo list = ()
        // SUBSTITUIÇÕES: X1 list = X0 list
        val emptyList = TmNil()
        val restrictionIsEmpty1 = typeConsColl(TmIsEmpty(emptyList),
                identTable(hashMapOf()), 0, mutableListOf(), false, true)
        Assert.assertEquals(mutableListOf("X1 list=X0 list"), unify(restrictionIsEmpty1.second, mutableListOf()))

        // TESTE:         isempty(list), sendo list = (true)
        // SUBSTITUIÇÕES: X0 list = bool list

        val boolList = TmList(Vbool(true), TmNil())
        val restrictionIsEmpty2 = typeConsColl(TmIsEmpty(boolList),
                identTable(hashMapOf()), 0, mutableListOf(), false, true)
        Assert.assertEquals(mutableListOf("X0 list=bool list"), unify(restrictionIsEmpty2.second, mutableListOf()))
    }
    
    @Test
    fun letRecTests() {
        val restrictions : MutableList<String>
        val list3 = TmList(Vnum(12), TmNil())
        val funcName = TmVar("teste")
        val intLista = TyList(TyInt())
        val entryName = TmVar("y")
        val boolTeste = TyBool()
        val corpo = TmIf(TmIsEmpty(entryName), TmBool(true), TmApp(funcName, TmTl(entryName)))
        val e22 = TmApp(funcName, list3)
        val letRec3  = TmLetRec(funcName, intLista, boolTeste, entryName, corpo, e22)
        restrictions = typeConsColl(letRec3 , identTable(hashMapOf()), 0, mutableListOf(), false, true).second
        unify(restrictions, mutableListOf())
    }


    /* TESTES ESTÁTICOS*/

    // Esses testes apenas confirmam que a unify não aceita algo errado ou não deixa de aceitar algo correto

    @Test
    fun staticFirstCaseTests() { // S = T
        Assert.assertEquals(mutableListOf<String>(), unify(mutableListOf("int=int"), mutableListOf()))
        Assert.assertEquals(mutableListOf<String>(), unify(mutableListOf("bool=bool", "int=int", "bool=bool"), mutableListOf()))
    }

    @Test
    fun staticSecondCaseTests() { // S = X e X não pertence a T
        Assert.assertEquals(mutableListOf("X1=int"), unify(mutableListOf("X1=int"), mutableListOf()))
        Assert.assertEquals(mutableListOf("X1=int"), unify(mutableListOf("(X1->bool)=(int->bool)"), mutableListOf()))
        Assert.assertEquals(mutableListOf("X1=int", "X2=bool"), unify(mutableListOf("(X1->X2)=(int->bool)"), mutableListOf()))
        Assert.assertEquals(mutableListOf("X1=int", "X2=X3"), unify(mutableListOf("(X1->X2)=(int->X3)"), mutableListOf()))
        Assert.assertEquals(mutableListOf("X3=int", "X1=int", "X2=int"), unify(mutableListOf("(X1->X2)=(int->X3)", "X3=int"), mutableListOf()))
        Assert.assertEquals(mutableListOf("X4=int", "X5=bool", "X6=int", "X3=bool", "X1=int", "X2=int"),
                unify(mutableListOf("((X1->X2)->X3)=((int->X4)->X5)", "X4=X6", "X5=bool", "X6=int"), mutableListOf()))
        Assert.assertEquals(mutableListOf("X1=int"), unify(mutableListOf("X1 List=int List"), mutableListOf()))
        Assert.assertEquals(mutableListOf("X1=int", "X2=int"), unify(mutableListOf("X1 List=X2 List", "X2=int"), mutableListOf()))
        Assert.assertEquals(mutableListOf("X2=int", "X1=int"), unify(mutableListOf("(X2->X1 List)=(int->int List)"), mutableListOf()))
    }

    @Test
    fun staticThirdCaseTests() { // T = X e X não pertence a S
        Assert.assertEquals(mutableListOf("X1=int"), unify(mutableListOf("int=X1"), mutableListOf()))
        Assert.assertEquals(mutableListOf("X1=int"), unify(mutableListOf("(int->bool)=(X1->bool)"), mutableListOf()))
        Assert.assertEquals(mutableListOf("X1=int", "X2=bool"), unify(mutableListOf("(int->bool)=(X1->X2)"), mutableListOf()))
        Assert.assertEquals(mutableListOf("X1=int", "X2=X3"), unify(mutableListOf("(int->X2)=(X1->X3)"), mutableListOf()))
        Assert.assertEquals(mutableListOf("X3=int", "X1=int", "X2=int"), unify(mutableListOf("(int->X3)=(X1->X2)", "int=X3"), mutableListOf()))
        Assert.assertEquals(mutableListOf("X4=int", "X5=bool", "X6=int", "X3=bool", "X1=int", "X2=int"),
                unify(mutableListOf("((int->X4)->X5)=((X1->X2)->X3)", "X4=X6", "bool=X5", "int=X6"), mutableListOf()))
    }

    @Test
    fun staticFourthCaseTests() { // T = T1->T2 e W = S1->S2
        Assert.assertEquals(mutableListOf<String>(), unify(mutableListOf("(int->int)=(int->int)"), mutableListOf()))
        Assert.assertEquals(mutableListOf<String>(), unify(mutableListOf("(int->int)=(int->int)", "((bool->int)->bool)=((bool->int)->bool)"), mutableListOf()))
        Assert.assertEquals(mutableListOf<String>(), unify(mutableListOf("((((int->bool)->int)->bool)->int)=((((int->bool)->int)->bool)->int)"), mutableListOf()))
    }


    @Test
    fun staticListTest() {
        Assert.assertEquals(mutableListOf("X1 list=int list"), unify(mutableListOf("int list=X1 list"), mutableListOf()))
        Assert.assertEquals(mutableListOf("X1 list=int list"), unify(mutableListOf("X1 list=int list"), mutableListOf()))
    }

    // Testes de erro

    @Test(expected =  UnifyFail::class)
    fun staticErrorCaseTests1() {
        unify(mutableListOf("int=bool"), mutableListOf())
    }

    @Test(expected =  UnifyFail::class)
    fun staticErrorCaseTests2() {
        unify(mutableListOf("X1=(int->X1)"), mutableListOf())
    }

    @Test(expected =  UnifyFail::class)
    fun staticErrorCaseTests3() {
        unify(mutableListOf("int=(int->X1)"), mutableListOf())
    }

    @Test(expected =  UnifyFail::class)
    fun staticErrorCaseTests4() {
        unify(mutableListOf("(int->bool)=(int->int)"), mutableListOf())
    }

    @Test(expected =  UnifyFail::class)
    fun staticErrorCaseTests5() {
        unify(mutableListOf("int=int List"), mutableListOf())
    }

    @Test(expected =  UnifyFail::class)
    fun staticErrorCaseTests6() {
        unify(mutableListOf("int List=int"), mutableListOf())
    }


    // Testes para funções complexas (com muitos ->)
    @Test
    fun staticComplexFunctionsTest() {
        Assert.assertEquals(mutableListOf("X3=bool", "X1=int", "X2=int"), unify(mutableListOf("((X1->X2)->X3)=((int->int)->bool)"), mutableListOf()))
        Assert.assertEquals(mutableListOf("X1=(int->int)", "X2=int"), unify(mutableListOf("(X1->X2)=((int->int)->int)"), mutableListOf()))
        Assert.assertEquals(mutableListOf("X3=int", "X1=(int->int)", "X2=int"), unify(mutableListOf("((X1->X2)->X3)=(((int->int)->int)->int)"), mutableListOf()))
        Assert.assertEquals(mutableListOf("X3=bool", "X1=int", "X2=int"), unify(mutableListOf("((X1->X2)->X3)=((int->int)->bool)"), mutableListOf()))
    }


    // Testes para o coletor de partes de função
    @Test
    fun staticFunctionPartsCollectorTest() {
        Assert.assertEquals(mutableListOf("(int->int)", "int"), collectFunctionParts("((int->int)->int)"))
        Assert.assertEquals(mutableListOf("((int->int)->int)", "bool"), collectFunctionParts("(((int->int)->int)->bool)"))
        Assert.assertEquals(mutableListOf("((int->int)->int)", "(int->bool)"), collectFunctionParts("(((int->int)->int)->(int->bool))"))
        Assert.assertEquals(mutableListOf("((int->int)->int)", "((bool->bool)->int)"), collectFunctionParts("(((int->int)->int)->((bool->bool)->int))"))
        Assert.assertEquals(mutableListOf("int->int", "int"), collectFunctionParts("(int->int->int)"))
        Assert.assertEquals(mutableListOf<String>(), collectFunctionParts("int"))
    }

}