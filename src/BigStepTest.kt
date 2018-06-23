import org.junit.Assert
import org.junit.Test


class BigStepTest {

    private val emptEnv = Env(hashMapOf())

    private val True = TmBool(true)
    private val False = TmBool(false)

    private val num2 = TmNum(2)
    private val num3 = TmNum(3)
    private val num12 = TmNum(12)
    private val num_32 = TmNum(-32)
    private val raise = TmRaise()

    private val list1 = TmList(Vnum(12), TmList(Vnum(-32), TmNil()))
    private val emptyList = TmList(Vnil(), TmNil())
    private val list2 = TmList(Vnum(12), TmNil())

    private val nil = TmNil()
    @Test
    fun valueAndRaiseTest() {

        Assert.assertEquals(bigStep(True, emptEnv), Vbool(true))
        Assert.assertEquals(bigStep(False, emptEnv), Vbool(false))

        Assert.assertEquals(bigStep(num_32, emptEnv), Vnum(-32))
        Assert.assertEquals(bigStep(num12, emptEnv), Vnum(12))

        Assert.assertTrue(bigStep(raise, emptEnv) is Raise)

        val list1V = bigStep(list1, emptEnv)
        Assert.assertTrue(list1V is Vlist)
        Assert.assertEquals((list1V as Vlist).h, (Vnum(12)))

        val list1T = list1V.t
        Assert.assertTrue(list1T is Vlist)
        Assert.assertEquals((list1T as Vlist).h, (Vnum(-32)))
        Assert.assertTrue(list1T.t is Vnil)

        val nilnil = bigStep(emptyList, emptEnv)

        Assert.assertTrue(nilnil is Vlist)
        Assert.assertTrue((nilnil as Vlist).h is Vnil)
        Assert.assertTrue(nilnil.t is Vnil)

        val list2V = bigStep(list2, emptEnv)
        Assert.assertTrue(list2V is Vlist)
        Assert.assertEquals((list2V as Vlist).h, (Vnum(12)))
        Assert.assertTrue(list2V.t is Vnil)

        Assert.assertTrue(bigStep(nil, emptEnv) is Vnil)


    }


    private val defVar = TmVar("x")
    private val xEnv = Env(hashMapOf(Pair(defVar, Vnum(2))))
    @Test
    fun varTest() {
        Assert.assertEquals(bigStep(defVar, xEnv), Vnum(2))

        //somente não falha se for a excessão gerada
        try {
            bigStep(defVar, emptEnv)
            Assert.fail()
        }catch (e : IdentNotDefined){

        }
        catch(e : Exception){
            Assert.fail()
        }

    }

    private val add = AddOp(num2, num2)
    private val isEmptyNil = TmIsEmpty(TmNil())
    private val if1 = TmIf(True, num2, num3)
    private val if2 = TmIf(False, add, num3)
    private val if3 = TmIf(isEmptyNil, num2, num3)

    @Test
    fun ifTest() {
        Assert.assertEquals(bigStep(if1, emptEnv), Vnum(2))
        Assert.assertEquals(bigStep(if2, emptEnv), Vnum(3))
        Assert.assertEquals(bigStep(if3, emptEnv), Vnum(2))
    }

    private val doubleFn = TmFn(TmVar("x"), TyInt(), AddOp(TmVar("x"), TmVar("x")))
    private val ifFn = TmFn(TmVar("b"), TyBool(), TmIf(TmVar("b"), TmNum(2), TmNum(3)))

    /*private val f = TmVar("f")
    private val inteiro = TyInt()
    private val inteiroParaBool = TyFn(TyInt(), TyBool())
    private val y = TmVar("y")
    private val e1 = EqOp(f, f)

    private val fn1 = TmFn(TmVar("x"), inteiro, e1)*/

    @Test
    fun fnTest() {
        Assert.assertEquals(bigStep(doubleFn, emptEnv), VClosure(doubleFn.x, doubleFn.e, emptEnv))
        //Assert.assertEquals(bigStep(fn1, emptEnv), VClosure(fn1.x, fn1.e, emptEnv))
        Assert.assertEquals(bigStep(ifFn, emptEnv), VClosure(ifFn.x, ifFn.e, emptEnv))


    }

    private val app1 = TmApp(ifFn, True)
    private val app2 = TmApp(doubleFn, num12)
    @Test
    fun appTest() {
        Assert.assertEquals(bigStep(app1, emptEnv), Vnum(2))
        Assert.assertEquals(bigStep(app2, emptEnv), Vnum(24))
    }

    private val intTy = TyInt()
    private val addX = AddOp(defVar, defVar)
    private val let1 = TmLet(defVar, intTy, num2, addX)

    @Test
    fun letTest()
    {
        Assert.assertEquals(bigStep(let1, emptEnv), Vnum(4))

    }

    private val fVar = TmVar("fat")
    private val fatBody = TmIf(SeOp(defVar, TmNum(0)), TmNum(1), MulOp(defVar, TmApp(fVar, SubOp(defVar, TmNum(1)))))

    private val letRec1 = TmLetRec(fVar, intTy, intTy, defVar, fatBody, TmApp(fVar, TmNum(5)))
    @Test
    fun letRecTest() {
        Assert.assertEquals(bigStep(letRec1, emptEnv), Vnum(120))
        /*val letRec2 = TmLetRec(TmVar("f"), TyInt(), TyFn(TyInt(), TyBool()), TmVar("y"), EqOp(num2, num2), TmVar("y"))
        Assert.assertEquals(bigStep(letRec2, emptEnv), Vnum(120))*/
    }


    private val hd = TmHd(list1)
    private val tl = TmTl(list1)
    private val isEmp = TmIsEmpty(list1)
    private val catt = TmCat(list1, list2)
    @Test
    fun listOpTest(){
        Assert.assertEquals(bigStep(hd, emptEnv), Vnum(12))
        val tailTerm = bigStep(tl, emptEnv)
        Assert.assertTrue(tailTerm is Vlist)
        Assert.assertEquals((tailTerm as Vlist).h, Vnum(-32))
        Assert.assertTrue(tailTerm.t is Vnil)


        Assert.assertEquals(bigStep(isEmp, emptEnv), Vbool(false))
        Assert.assertEquals(bigStep(TmIsEmpty(nil), emptEnv), Vbool(true))

        val catTerm = bigStep(catt, emptEnv)
        Assert.assertTrue(catTerm is Vlist)
        Assert.assertEquals((catTerm as Vlist).h, Vnum(12))
        var tail = catTerm.t
        Assert.assertTrue(tail is Vlist)
        Assert.assertEquals((tail as Vlist).h, Vnum(-32))
        tail = tail.t
        Assert.assertTrue(tail is Vlist)
        Assert.assertEquals((tail as Vlist).h, Vnum(12))
        Assert.assertTrue(tail.t is Vnil)


    }

    private val tryWith = TmTryWith(raise, add)
    @Test
    fun tryWithTest(){
        Assert.assertEquals(bigStep(tryWith, emptEnv), Vnum(4))
        Assert.assertEquals(bigStep(TmTryWith(SubOp(TmNum(7), TmNum(1)), TmNum(2)), emptEnv), Vnum(6))
    }


    private val sub = SubOp(num2, num2)
    private val mul = MulOp(num2, num3)
    private val div = DivOp(num12, num2)
    @Test
    fun opTest(){
        Assert.assertEquals(bigStep(add, emptEnv), Vnum(4))
        Assert.assertEquals(bigStep(sub, emptEnv), Vnum(0))
        Assert.assertEquals(bigStep(mul, emptEnv), Vnum(6))
        Assert.assertEquals(bigStep(div, emptEnv), Vnum(6))
        Assert.assertTrue(bigStep(DivOp(num2, TmNum(0)), emptEnv) is Raise) // div por 0

        Assert.assertEquals(bigStep(AndOp(True, True), emptEnv), Vbool(true))
        Assert.assertEquals(bigStep(OrOp(True, False), emptEnv), Vbool(true))
        Assert.assertEquals(bigStep(NotOp(True), emptEnv), Vbool(false))

        Assert.assertEquals(bigStep(GrOp(num2, num3), emptEnv), Vbool(false))
        Assert.assertEquals(bigStep(GeOp(num2, num3), emptEnv),Vbool(false))
        Assert.assertEquals(bigStep(SmOp(num12, num3), emptEnv), Vbool(false))
        Assert.assertEquals(bigStep(SeOp(num2, num3), emptEnv), Vbool(true))
        Assert.assertEquals(bigStep(EqOp(num2, num2), emptEnv), Vbool(true))
        Assert.assertEquals(bigStep(NeqOp(num2, num12), emptEnv), Vbool(true))


    }
}