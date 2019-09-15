import org.junit.Assert
import org.junit.Test

class ToStringTest {
    //TESTES DE toString
    //a função tem como objetivo converter uma expressão da linguagem para uma string que será impressa
    private val True = TmBool(true)
    private val False = TmBool(false)

    @Test
    fun boolTest() {

        Assert.assertEquals(toString(True), "true")
        Assert.assertEquals(toString(False), "false")
    }

    private val num2 = TmNum(2)
    private val num3 = TmNum(3)
    private val num12 = TmNum(12)
    private val num_32 = TmNum(-32)
    @Test
    fun numTest() {
        Assert.assertEquals(toString(num_32), "-32")
        Assert.assertEquals(toString(num12), "12")

    }
    private val raise = TmRaise()

    @Test
    fun raiseTest() {
        Assert.assertEquals(toString(raise), "raise")
    }
    private val list1 = TmList(Vnum(12), TmList(Vnum(-32), TmNil()))
    private val emptyList = TmList(Vnil(), TmNil())
    private val list2 = TmList(Vnum(12), TmNil())
    @Test
    fun listTest() {
        Assert.assertEquals(toString(list1), "[12,-32]")
        Assert.assertEquals(toString(emptyList), "[]")
        Assert.assertEquals(toString(list2), "[12]")

    }
    private val nil = TmNil()

    @Test
    fun nilTest() {
        Assert.assertEquals(toString(nil), "nil")
    }
    private val tVar = TmVar("willEhUmaVar")

    @Test
    fun varTest() {
        Assert.assertEquals(toString(tVar), "willEhUmaVar")
    }

    private val add = AddOp(num2, num2)
    private val isEmptyNil = TmIsEmpty(TmNil())
    private val if1 = TmIf(True, num2, num3)
    private val if2 = TmIf(False, add, num3)
    private val if3 = TmIf(isEmptyNil, num2, num3)

    @Test
    fun ifTest() {
        Assert.assertEquals(toString(if1), "if true then 2 else 3")
        Assert.assertEquals(toString(if2), "if false then 2 + 2 else 3")
        Assert.assertEquals(toString(if3), "if isempty nil then 2 else 3")

    }

    private val doubleFn = TmFn(TmVar("x"), TyInt(), AddOp(TmVar("x"), TmVar("x")))
    private val ifFn = TmFn(TmVar("b"), TyBool(), TmIf(TmVar("b"), TmNum(2), TmNum(3)))

    @Test
    fun fnTest() {
        Assert.assertEquals(toString(doubleFn), "fn x:int=>x + x")
        Assert.assertEquals(toString(ifFn), "fn b:bool=>if b then 2 else 3")

    }

    private val app1 = TmApp(ifFn, True)
    private val app2 = TmApp(doubleFn, num12)
    @Test
    fun appTest() {
        Assert.assertEquals(toString(app1), "(fn b:bool=>if b then 2 else 3) (true)")
        Assert.assertEquals(toString(app2), "(fn x:int=>x + x) (12)")
    }

    private val defVar = TmVar("x")
    private val intTy = TyInt()
    private val addX = AddOp(defVar, defVar)
    private val let1 = TmLet(defVar, intTy, num2, addX)


    @Test
    fun letTest()
    {
        Assert.assertEquals(toString(let1), "let x:int=2 in x + x")

    }

    private val fVar = TmVar("f")
    private val letRec1 = TmLetRec(fVar, intTy, intTy, defVar, addX, TmApp(fVar, num2))
    @Test
    fun letRecTest() {
        Assert.assertEquals(toString(letRec1), "let rec f:int->int = (fn x:int=>x + x) in (f) (2)")
    }


    private val hd = TmHd(list1)
    private val tl = TmTl(list1)
    private val isEmp = TmIsEmpty(list1)
    private val catt = TmCat(list1, list2)
    @Test
    fun listOpTest(){
        Assert.assertEquals(toString(hd), "hd [12,-32]")
        Assert.assertEquals(toString(tl), "tl [12,-32]")
        Assert.assertEquals(toString(isEmp), "isempty [12,-32]")
        Assert.assertEquals(toString(catt), "[12,-32]::[12]")
    }

    private val tryWith = TmTryWith(raise, add)
    @Test
    fun tryWithTest(){
        Assert.assertEquals(toString(tryWith), "try raise with 2 + 2")
    }


    private val sub = SubOp(num2, num2)
    private val mul = MulOp(num2, num3)
    private val div = DivOp(num12, num2)
    @Test
    fun opTest(){
        Assert.assertEquals(toString(add), "2 + 2")
        Assert.assertEquals(toString(sub), "2 - 2")
        Assert.assertEquals(toString(mul), "2 * 3")
        Assert.assertEquals(toString(div), "12 / 2")

        Assert.assertEquals(toString(AndOp(True, True)), "true and true")
        Assert.assertEquals(toString(OrOp(True, False)), "true or false")
        Assert.assertEquals(toString(NotOp(True)), "not true")

        Assert.assertEquals(toString(GrOp(num2, num3)), "2 > 3")
        Assert.assertEquals(toString(GeOp(num2, num3)), "2 >= 3")
        Assert.assertEquals(toString(SmOp(num12, num3)), "12 < 3")
        Assert.assertEquals(toString(SeOp(num2, num3)), "2 <= 3")
        Assert.assertEquals(toString(EqOp(num2, num2)), "2 == 2")
        Assert.assertEquals(toString(NeqOp(num2, num12)), "2 != 12")


    }
}