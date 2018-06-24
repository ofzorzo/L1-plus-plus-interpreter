import org.junit.Assert
import org.junit.Test

class ParserTest {

    val impParser = ImplicitParser()
    val expParser = ExplicitParser()

    val parsers = listOf(expParser, impParser)


    @Test
    fun testValues(){

        for(p in parsers) {
            Assert.assertEquals(p.fromString("2"), TmNum(2))
            Assert.assertEquals(p.fromString("-13"), TmNum(-13))
            Assert.assertEquals(p.fromString("true"), TmBool(true))
            Assert.assertEquals(p.fromString("false"), TmBool(false))
            Assert.assertTrue(p.fromString("raise") is TmRaise)
            Assert.assertTrue(p.fromString("nil") is TmNil)
            Assert.assertTrue(p.fromString("[2,3,4,5]") is TmList)
        }
    }

    @Test
    fun testIf(){
        for(p in parsers){
            Assert.assertEquals(p.fromString("if true then 2 else 3"), TmIf(TmBool(true), TmNum(2), TmNum(3)))
            Assert.assertEquals(p.fromString("if if true then true else false then 2 else 3"),
                    TmIf(TmIf(TmBool(true), TmBool(true), TmBool(false)), TmNum(2), TmNum(3)))
        }
    }
    @Test
    fun testFn(){
        //EXPLICITAMENTE TIPADA
        val exp1 = expParser.fromString("fn x:int=>x")
        Assert.assertTrue(exp1 is TmFn)
        Assert.assertEquals((exp1 as TmFn).x,TmVar("x"))
        Assert.assertTrue(exp1.t is TyInt)
        Assert.assertEquals(exp1.e, TmVar("x"))

        val exp2 = expParser.fromString("fn x:bool=>if x then 2 else 3")
        Assert.assertTrue(exp2 is TmFn)
        Assert.assertEquals((exp2 as TmFn).x,TmVar("x"))
        Assert.assertTrue(exp2.t is TyBool)
        Assert.assertEquals(exp2.e, TmIf(TmVar("x"), TmNum(2), TmNum(3)))

        //IMPLICITAMENTE TIPADA
        val imp1 = impParser.fromString("fn x=>x")
        Assert.assertTrue(imp1 is TmFn)
        Assert.assertEquals((imp1 as TmFn).x,TmVar("x"))
        Assert.assertTrue(imp1.t is TyUnknown)
        Assert.assertEquals(imp1.e, TmVar("x"))

        val imp2 = impParser.fromString("fn x=>if x then 2 else 3")
        Assert.assertTrue(imp2 is TmFn)
        Assert.assertEquals((imp2 as TmFn).x,TmVar("x"))
        Assert.assertTrue(imp2.t is TyUnknown)
        Assert.assertEquals(imp2.e, TmIf(TmVar("x"), TmNum(2), TmNum(3)))

    }

    @Test
    fun testListOp(){
        for (p in parsers){
            //ISEMPTY
            val isempt0 = p.fromString("isempty [1]")
            Assert.assertTrue(isempt0 is TmIsEmpty)
            Assert.assertTrue((isempt0 as TmIsEmpty).e is TmList)
            val l:TmList = (isempt0.e as TmList)
            Assert.assertEquals(l.h, Vnum(1))
            Assert.assertTrue(l.t is TmNil)
            val isempt1 = p.fromString("isempty nil")
            Assert.assertTrue(isempt1 is TmIsEmpty)
            Assert.assertTrue((isempt1 as TmIsEmpty).e is TmNil)
            //HD
            val hd1 = p.fromString("hd [true,false]")
            Assert.assertTrue(hd1 is TmHd)
            Assert.assertTrue((hd1 as TmHd).e is TmList)
            val l2: TmList = (hd1.e as TmList)
            Assert.assertEquals(l2.h, Vbool(true))
            Assert.assertTrue(l2.t is TmList)
            val l3 : TmList = l2.t as TmList
            Assert.assertEquals(l3.h, Vbool(false))
            Assert.assertTrue(l3.t is TmNil)
            //TL
            val tl1 = p.fromString("tl [true,false]")
            Assert.assertTrue(tl1 is TmTl)
            Assert.assertTrue((tl1 as TmTl).e is TmList)
            val l4: TmList = (tl1.e as TmList)
            Assert.assertEquals(l4.h, Vbool(true))
            Assert.assertTrue(l4.t is TmList)
            val l5 : TmList = l4.t as TmList
            Assert.assertEquals(l5.h, Vbool(false))
            Assert.assertTrue(l5.t is TmNil)
        }
    }
    @Test
    fun testTry(){
        for(p in parsers){
            Assert.assertEquals(p.fromString("try 0 with 2"), TmTryWith(TmNum(0), TmNum(2)))
            Assert.assertEquals(p.fromString("try if true then 1 else 2 with 5"), TmTryWith(TmIf(TmBool(true), TmNum(1), TmNum(2)), TmNum(5)))
            val try1 = p.fromString("try raise with false")
            Assert.assertTrue(try1 is TmTryWith)
            Assert.assertTrue((try1 as TmTryWith).e1 is TmRaise)
            Assert.assertEquals(try1.e2, TmBool(false))
        }
    }

    @Test
    fun testLet(){
        //IMPLICITAMENTE TIPADA
        val implet1 = impParser.fromString("let x=true in if x then 1 else 2")
        Assert.assertTrue(implet1 is TmLet)
        Assert.assertTrue((implet1 as TmLet).t is TyUnknown)
        Assert.assertEquals(implet1.x, TmVar("x"))
        Assert.assertEquals(implet1.e1, TmBool(true))
        Assert.assertEquals(implet1.e2, TmIf(TmVar("x"), TmNum(1), TmNum(2)))

        val impletrec = impParser.fromString("let rec f=(fn x=>isempty x) in f")
        Assert.assertTrue(impletrec is TmLetRec)
        Assert.assertTrue((impletrec as TmLetRec).fin is TyUnknown)
        Assert.assertTrue(impletrec.fout is TyUnknown)
        Assert.assertEquals(impletrec.f, TmVar("f"))
        Assert.assertEquals(impletrec.x, TmVar("x"))
        Assert.assertEquals(impletrec.e1, TmIsEmpty(TmVar("x")))
        Assert.assertEquals(impletrec.e2, TmVar("f"))
        //EXPLICITAMENTE TIPADA

        val explet = expParser.fromString("let x:bool=true in if x then 1 else 2")
        Assert.assertTrue(explet is TmLet)
        Assert.assertTrue((explet as TmLet).t is TyBool)
        Assert.assertEquals(explet.x, TmVar("x"))
        Assert.assertEquals(explet.e1, TmBool(true))
        Assert.assertEquals(explet.e2, TmIf(TmVar("x"), TmNum(1), TmNum(2)))

        val expletrec = expParser.fromString("let rec f:int->bool=(fn x:int=>true) in f")
        Assert.assertTrue(expletrec is TmLetRec)
        Assert.assertTrue((expletrec as TmLetRec).fin is TyInt)
        Assert.assertTrue(expletrec.fout is TyBool)
        Assert.assertEquals(expletrec.f, TmVar("f"))
        Assert.assertEquals(expletrec.x, TmVar("x"))
        Assert.assertEquals(expletrec.e1, TmBool(true))
        Assert.assertEquals(expletrec.e2, TmVar("f"))

    }

    @Test
    fun opTest(){
        for(p in parsers)
        {
            //SIMPLE OP
            Assert.assertEquals(p.fromString("2+2"), AddOp(TmNum(2), TmNum(2)))
            Assert.assertEquals(p.fromString("2-23"), SubOp(TmNum(2), TmNum(23)))
            Assert.assertEquals(p.fromString("12*2"), MulOp(TmNum(12), TmNum(2)))
            Assert.assertEquals(p.fromString("52 div 2"), DivOp(TmNum(52), TmNum(2)))
            Assert.assertEquals(p.fromString("2<32"), SmOp(TmNum(2), TmNum(32)))
            Assert.assertEquals(p.fromString("2<=2"), SeOp(TmNum(2), TmNum(2)))
            Assert.assertEquals(p.fromString("2==2"), EqOp(TmNum(2), TmNum(2)))
            Assert.assertEquals(p.fromString("2>=2"), GeOp(TmNum(2), TmNum(2)))
            Assert.assertEquals(p.fromString("2>2"), GrOp(TmNum(2), TmNum(2)))
            Assert.assertEquals(p.fromString("2!=2"), NeqOp(TmNum(2), TmNum(2)))
            Assert.assertEquals(p.fromString("true and true"), AndOp(TmBool(true), TmBool(true)))
            Assert.assertEquals(p.fromString("true or false"), OrOp(TmBool(true), TmBool(false)))
            Assert.assertEquals(p.fromString("not true"), NotOp(TmBool(true)))
            Assert.assertEquals(p.fromString("2 2"), TmApp(TmNum(2), TmNum(2)))

            //OPS COM TERMOS // TESTES PARA SOMA, OS OUTROS DEVEM FUNCIONAR SIMILARMENTE
            Assert.assertEquals(p.fromString("if true then 1 else 2 + 4"), TmIf(TmBool(true), TmNum(1), AddOp(TmNum(2), TmNum(4))))
            Assert.assertEquals(p.fromString("(if true then 1 else 2) + 4"), AddOp(TmIf(TmBool(true) , TmNum(1), TmNum(2)), TmNum(4)))

            val catExpr = p.fromString("[1,2]::[1,2]")
            Assert.assertTrue(catExpr is TmCat)
            Assert.assertTrue((catExpr as TmCat).e1 is TmList)
            val firstL = catExpr.e1 as TmList
            Assert.assertEquals(firstL.h, Vnum(1))
            Assert.assertTrue(firstL.t is TmList)
            Assert.assertEquals((firstL.t as TmList).h, Vnum(2))
            Assert.assertTrue(firstL.t.t is TmNil)
            val secondL = catExpr.e2 as TmList
            Assert.assertEquals(secondL.h, Vnum(1))
            Assert.assertTrue(secondL.t is TmList)
            Assert.assertEquals((secondL.t as TmList).h, Vnum(2))
            Assert.assertTrue(secondL.t.t is TmNil)

        }

    }
    @Test
    fun testTypes(){
        //uso let para testar os tipos

        val type1 = expParser.fromString("let x:int=2 in x")
        Assert.assertTrue((type1 as TmLet).t is TyInt)
        val type2 = expParser.fromString("let x:bool=2 in x")
        Assert.assertTrue((type2 as TmLet).t is TyBool)
        val type3 = expParser.fromString("let x:int list=2 in x")
        Assert.assertTrue((type3 as TmLet).t is TyList)
        Assert.assertTrue((type3.t as TyList).T is TyInt)
        val type4 = expParser.fromString("let x:int->bool->int=2 in x")
        Assert.assertTrue((type4 as TmLet).t is TyFn)
        Assert.assertTrue((type4.t as TyFn).inp is TyInt)
        Assert.assertTrue(type4.t.out is TyFn)
        Assert.assertTrue((type4.t.out as TyFn).inp is TyBool)
        Assert.assertTrue((type4.t.out).out is TyInt)
        val type5 = expParser.fromString("let x:int->(bool->bool)=2 in x")
        Assert.assertTrue((type5 as TmLet).t is TyFn)
        Assert.assertTrue((type5.t as TyFn).inp is TyInt)
        Assert.assertTrue(type5.t.out is TyFn)
        Assert.assertTrue((type5.t.out as TyFn).inp is TyBool)
        Assert.assertTrue((type5.t.out).out is TyBool)
        val type6 = expParser.fromString("let x:(int->bool)->bool=2 in x")
        Assert.assertTrue((type6 as TmLet).t is TyFn)
        Assert.assertTrue((type6.t as TyFn).out is TyBool)
        Assert.assertTrue(type6.t.inp is TyFn)
        Assert.assertTrue((type6.t.inp as TyFn).inp is TyInt)
        Assert.assertTrue((type6.t.inp).out is TyBool)

    }

}

