import org.junit.Assert
import org.junit.Test

class TypeInferTests {


    /* Testes para o applySubs */
    @Test
    fun applySubsTests() {
        Assert.assertEquals("int", applySubs(mutableListOf("X0=int"), "X0"))
        Assert.assertEquals("bool", applySubs(mutableListOf("X0=bool"), "X0"))
        Assert.assertEquals("int list", applySubs(mutableListOf("X0=int"), "X0 list"))
        Assert.assertEquals("bool list", applySubs(mutableListOf("X0=bool"), "X0 list"))
        Assert.assertEquals("int", applySubs(mutableListOf("X0=int", "X1=bool", "X2=int"), "X0"))
        Assert.assertEquals("bool", applySubs(mutableListOf("X0=int", "X1=bool", "X2=int"), "X1"))
        Assert.assertEquals("int->int", applySubs(mutableListOf("X0=int", "X1=int"), "X0->X1"))
        Assert.assertEquals("int list->bool list", applySubs(mutableListOf("X0=int", "X1=int", "X2=bool"),
                "X0 list->X2 list"))
        Assert.assertEquals("int list->int", applySubs(mutableListOf("X0=int", "X1=int"), "X0 list->X1"))
        Assert.assertEquals("int list->bool", applySubs(mutableListOf("X0=int", "X1=bool"), "X0 list->X1"))
        Assert.assertEquals("int", applySubs(mutableListOf("X0=int", "X1=bool", "X2=bool", "X3=bool"), "X0"))
        Assert.assertEquals("bool", applySubs(mutableListOf("X0=bool", "X1=int", "X2=bool"), "X0"))
    }

    @Test(expected = UnifyFail::class)
    fun applySubsErrorTests() {
        applySubs(mutableListOf("X0"), "X0")
    }

    @Test
    fun implicitTypeInferTests() {
        typeInferTests(false)
    }

    @Test
    fun explicitTypeInferTests() {
        typeInferTests(false)
    }

    fun typeInferTests(implicit : Boolean) {

        // Expressão = 1
        Assert.assertEquals("int", typeInfer(TmNum(1), identTable(hashMapOf()), implicit))

        // Expressão = true
        Assert.assertEquals("bool", typeInfer(TmBool(true), identTable(hashMapOf()), implicit))

        // Expressão = false
        Assert.assertEquals("bool", typeInfer(TmBool(false), identTable(hashMapOf()), implicit))

        // Expressão = not false
        Assert.assertEquals("bool", typeInfer(NotOp(TmBool(false)), identTable(hashMapOf()), implicit))

        // Expressão = not 1 == 2
        Assert.assertEquals("bool", typeInfer(NotOp(EqOp(TmNum(1), TmNum(2))), identTable(hashMapOf()), implicit))

        // Expressão = 3 == 1
        Assert.assertEquals("bool", typeInfer(EqOp(TmNum(3), TmNum(1)), identTable(hashMapOf()), implicit))

        // Expressão = if true then 1 else 3
        Assert.assertEquals("int", typeInfer(TmIf(TmBool(true), TmNum(1), TmNum(3)),
                identTable(hashMapOf()), implicit))

        // Expressão = if true then true else false
        Assert.assertEquals("bool", typeInfer(TmIf(TmBool(true), TmBool(true), TmBool(false)),
                identTable(hashMapOf()), implicit))

        // Expressão = if (3 == 2) then true else false
        Assert.assertEquals("bool", typeInfer(TmIf(EqOp(TmNum(3), TmNum(2)), TmBool(true), TmBool(false)),
                identTable(hashMapOf()), implicit))

        // Expressão = 3 - 2
        Assert.assertEquals("int", typeInfer(SubOp(TmNum(3), TmNum(2)), identTable(hashMapOf()),
                implicit))

        // Expressão = 3 + 2
        Assert.assertEquals("int", typeInfer(AddOp(TmNum(3), TmNum(2)), identTable(hashMapOf()), implicit))

        // Expressão = head(cons(3, cons(4, nil)))
        Assert.assertEquals("int", typeInfer(TmHd(TmList(Vnum(3), TmList(Vnum(4), TmNil()))),
                identTable(hashMapOf()), implicit))

        // Expressão = head(cons(3, nil))
        Assert.assertEquals("int", typeInfer(TmHd(TmList(Vnum(3), TmNil())), identTable(hashMapOf()), implicit))

        // Expressão = tail(cons(3, cons(4, nil)))
        Assert.assertEquals("int list", typeInfer(TmTl(TmList(Vnum(3), TmList(Vnum(4), TmNil()))),
                identTable(hashMapOf()), implicit))

        // Expressão = tail(cons(3, nil))
        Assert.assertEquals("int list", typeInfer(TmTl(TmList(Vnum(3), TmNil())),
                identTable(hashMapOf()), implicit))

        // Expressão = cons(false, nil)
        Assert.assertEquals("bool list", typeInfer(TmList(Vbool(false), TmNil()),
                identTable(hashMapOf()), implicit))

        // Expressão = isempty(cons(3, nil))
        Assert.assertEquals("bool", typeInfer(TmIsEmpty(TmList(Vnum(3), TmNil())),
                identTable(hashMapOf()), implicit))

        // Expressão = isempty(nil)
        Assert.assertEquals("bool", typeInfer(TmIsEmpty(TmNil()), identTable(hashMapOf()), implicit))

        // Expressão = fn x:int => x == 3
        Assert.assertEquals("(int->bool)", typeInfer(TmFn(TmVar("x"), TyInt(), EqOp(TmVar("x"), TmNum(3))),
                identTable(hashMapOf()), implicit))

        // Expressão = fn x:int => x + 3
        Assert.assertEquals("(int->int)", typeInfer(TmFn(TmVar("x"), TyInt(), AddOp(TmVar("x"), TmNum(3))),
                identTable(hashMapOf()), implicit))

        // Expressão = (fn x:int => x == 3) 2
        Assert.assertEquals("bool", typeInfer(TmApp(TmFn(TmVar("x"), TyInt(), EqOp(TmVar("x"), TmNum(3))), TmNum(2)),
                identTable(hashMapOf()), implicit))

        // Expressão = (fn x:int => x + 3) 2
        Assert.assertEquals("int", typeInfer(TmApp(TmFn(TmVar("x"), TyInt(), AddOp(TmVar("x"), TmNum(3))), TmNum(2)),
                identTable(hashMapOf()), implicit))

        // Expressão = let x:int = 3 in x + 2
        Assert.assertEquals("int", typeInfer(TmLet(TmVar("x"), TyInt(), TmNum(3), AddOp(TmVar("x"), TmNum(2))),
                identTable(hashMapOf()), implicit))

        // Expressão = let x:int = 3 in x == 2
        Assert.assertEquals("bool", typeInfer(TmLet(TmVar("x"), TyInt(), TmNum(3), EqOp(TmVar("x"), TmNum(2))),
                identTable(hashMapOf()), implicit))

        // Expressão = let x:int = (4 - 1) in x + 2
        Assert.assertEquals("int", typeInfer(TmLet(TmVar("x"), TyInt(), SubOp(TmNum(4), TmNum(1)), AddOp(TmVar("x"), TmNum(2))),
                identTable(hashMapOf()), implicit))

        // Expressão = let x:int = 3 == 2 in if x then 1 else 2
        Assert.assertEquals("int", typeInfer(TmLet(TmVar("x"), TyBool(), EqOp(TmNum(3), TmNum(2)),
                TmIf(TmVar("x"), TmNum(1), TmNum(2))),
                identTable(hashMapOf()), implicit))

        // Fotmato let rec
        // TmLetRec(val f: TmVar, val fin : Type, val fout : Type, val x: TmVar, val e1 : Term, val e2:Term) : Term()
        //  let rec f:fin -> fout =
        //          fn x:fin => e1
        //  in e2

        // Expressão =  let rec fat:int->int =
        //                  fn x:int => if x = 0 then 1 else x*fat(x-1)
        //              in fat(5)
        val fat_x = TmLetRec(TmVar("fat"), TyInt(), TyInt(), TmVar("x"),
                TmIf(EqOp(TmVar("x"), TmNum(0)),
                        TmNum(1),
                        MulOp(TmVar("x"), TmApp(TmVar("fat"), SubOp(TmVar("x"), TmNum(1))))),
                TmApp(TmVar("fat"), TmNum(5)))
        Assert.assertEquals("int", typeInfer(fat_x, identTable(hashMapOf()), implicit))

        // Expressão =  let rec sum:int->int =
        //                  fn k:int => if k < 7 then k+sum(k+1) else 0
        //              in sum(0)
        val sum_k = TmLetRec(TmVar("sum"), TyInt(), TyInt(), TmVar("k"),
                TmIf(GrOp(TmVar("k"), TmNum(7)),
                        AddOp(TmVar("k"), TmApp(TmVar("sum"), AddOp(TmVar("k"), TmNum(1)))),
                        TmNum(0)),
                TmApp(TmVar("sum"), TmNum(0)))
        Assert.assertEquals("int", typeInfer(sum_k, identTable(hashMapOf()), implicit))

        // Expressão = try 2 + 1 with raise
        Assert.assertEquals("int", typeInfer(TmTryWith(AddOp(TmNum(2), TmNum(1)), TmRaise()),
                identTable(hashMapOf()), implicit))

        // Expressão =  try
        //                  let rec sum:int->int =
        //                      fn k:int => if k < 7 then k+sum(k+1) else 0
        //                  in sum(0)
        //              with raise
        Assert.assertEquals("int", typeInfer(TmTryWith(sum_k, TmRaise()),
                identTable(hashMapOf()), implicit))

        Assert.assertEquals("bool list", typeInfer(TmCat(TmBool(true), TmNil()),
                identTable(hashMapOf()), implicit))

        Assert.assertEquals("int list", typeInfer(TmCat(TmNum(1), TmCat(TmNum(2), TmNil())),
                identTable(hashMapOf()), implicit))

        Assert.assertEquals("X0 list", typeInfer(TmNil(), identTable(hashMapOf()), implicit))

    }

    // Expressão: 1 + true
    @Test(expected = UnifyFail::class)
    fun equalsErrorTest() {
        typeInfer(EqOp(TmNum(1), TmBool(true)), identTable(hashMapOf()), true)
    }

    // Expressão: if true then 1 else true
    @Test(expected = UnifyFail::class)
    fun ifErrorTest1() {
        typeInfer(TmIf(TmBool(true), TmNum(1), TmBool(true)), identTable(hashMapOf()), true)
    }

    // Expressão: if 3 then 1 else 1
    @Test(expected = UnifyFail::class)
    fun ifErrorTest2() {
        typeInfer(TmIf(TmNum(3), TmNum(1), TmNum(1)), identTable(hashMapOf()), true)
    }

    // Expressão: not 1
    @Test(expected = UnifyFail::class)
    fun notErrorTest() {
        typeInfer(NotOp(TmNum(1)), identTable(hashMapOf()), true)
    }

    // Expressão: fn x:bool => x+1
    @Test(expected = UnifyFail::class)
    fun fnErrorTest1() {
        val x = TmVar("x")
        val n1 = TmNum(1)
        typeInfer(TmFn(x, TyBool(), AddOp(x, n1)),
                identTable(hashMapOf()), false)
    }

    // Expressão: fn x:int => x+false
    @Test(expected = UnifyFail::class)
    fun fnErrorTest2() {
        typeInfer(TmFn(TmVar("x"), TyInt(), AddOp(TmVar("x"), TmBool(false))),
                identTable(hashMapOf()), true)
    }

    // Expressão: let x:int = 3 in x + false
    @Test(expected = UnifyFail::class)
    fun letErrorTest() {
        typeInfer(TmLet(TmVar("x"), TyInt(), TmNum(3), AddOp(TmVar("x"), TmBool(false))),
                identTable(hashMapOf()), true)
    }


    // Expressão:

    /* Testes para o separador de partes da substituição */
    @Test
    fun separateTests() {
        Assert.assertEquals(Pair("X0", "int"), separateSubstitution("X0=int"))
        Assert.assertEquals(Pair("X1", "int"), separateSubstitution("X1=int"))
        Assert.assertEquals(Pair("X0", "X1"), separateSubstitution("X0=X1"))
    }

}