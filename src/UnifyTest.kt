import org.junit.Assert
import org.junit.Test

class UnifyTest {
    private val True = TmBool(true)
    private val equal1 = EqOp(TmNum(1), TmNum(2))
    private val num2 = TmNum(2)
    private val num3 = TmNum(3)
    private val if1 = TmIf(True, num2, num3)
    private val if2 = TmIf(equal1, num2, num3)


    @Test
    fun letRecTests() {
        val restrictions : MutableList<String>
        val substitutions : MutableList<String>
        val list4 = TmNil()
        val list3 = TmList(Vnum(12), TmNil())
        val funcName = TmVar("teste")
        val intLista = TyList(TyInt())
        val entryName = TmVar("y")
        val boolTeste = TyBool()
        val corpo = TmIf(TmIsEmpty(entryName), TmBool(true), TmApp(funcName, TmTl(entryName)))
        val e22 = TmApp(funcName, list3)
        val letRec3  = TmLetRec(funcName, intLista, boolTeste, entryName, corpo, e22)
        var ident2 = identTable(hashMapOf())
        restrictions = typeConsColl(letRec3 , ident2, 0, mutableListOf(), false, true).second
        print("\nRESTRS: " + restrictions + "\n")
        substitutions = unify(restrictions, mutableListOf())
        print("\nSUBS: " + substitutions + "\n")
    }

    @Test
    fun firstCaseTests() { // S = T
        val ident = identTable(hashMapOf())
        val constraintsIf1 = typeConsColl(if1, ident, 0, mutableListOf(), false, true)
        Assert.assertEquals(mutableListOf<String>(), unify(constraintsIf1.second, mutableListOf()))
        val constraintsIf2 = typeConsColl(if2, ident, 0, mutableListOf(), false, true)
        Assert.assertEquals(mutableListOf<String>(), unify(constraintsIf2.second, mutableListOf()))
    }

    // Alguns testes estáticos
    // Esses testes apenas confirmam que a unify não aceita algo errado ou não deixa de aceitar algo correto

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
    fun listTest() {
        Assert.assertEquals(mutableListOf("X1 list=int list"), unify(mutableListOf("int list=X1 list"), mutableListOf()))
        Assert.assertEquals(mutableListOf("X1 list=int list"), unify(mutableListOf("X1 list=int list"), mutableListOf()))
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
    fun complexFunctionsTest() {
        Assert.assertEquals(mutableListOf("X3=bool", "X1=int", "X2=int"), unify(mutableListOf("((X1->X2)->X3)=((int->int)->bool)"), mutableListOf()))
        Assert.assertEquals(mutableListOf("X1=(int->int)", "X2=int"), unify(mutableListOf("(X1->X2)=((int->int)->int)"), mutableListOf()))
        Assert.assertEquals(mutableListOf("X3=int", "X1=(int->int)", "X2=int"), unify(mutableListOf("((X1->X2)->X3)=(((int->int)->int)->int)"), mutableListOf()))
        Assert.assertEquals(mutableListOf("X3=bool", "X1=int", "X2=int"), unify(mutableListOf("((X1->X2)->X3)=((int->int)->bool)"), mutableListOf()))
    }


    // Testes para o coletor de partes de função
    @Test
    fun functionPartsCollectorTest() {
        Assert.assertEquals(mutableListOf("(int->int)", "int"), collectFunctionParts("((int->int)->int)"))
        Assert.assertEquals(mutableListOf("((int->int)->int)", "bool"), collectFunctionParts("(((int->int)->int)->bool)"))
        Assert.assertEquals(mutableListOf("((int->int)->int)", "(int->bool)"), collectFunctionParts("(((int->int)->int)->(int->bool))"))
        Assert.assertEquals(mutableListOf("((int->int)->int)", "((bool->bool)->int)"), collectFunctionParts("(((int->int)->int)->((bool->bool)->int))"))
        Assert.assertEquals(mutableListOf("int->int", "int"), collectFunctionParts("(int->int->int)"))
        Assert.assertEquals(mutableListOf<String>(), collectFunctionParts("int"))
    }

}