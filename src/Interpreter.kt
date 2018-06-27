import java.util.*

class Interpreter {

    private var implicit : Boolean = true

    private fun interpret(){
        if (this.implicit) println("Implicit interpreter of L1++") else println("Explicit interpreter of L1++")
        val parser = if(this.implicit) ImplicitParser() else ExplicitParser()
        val emptEnv = Env(hashMapOf())
        while (true)
        {
            print(">> ")
            try {
                val typedExpr: String? = readLine()
                if (typedExpr != null)
                {
                    val term = parser.fromString(typedExpr)
                    val type = typeInfer(term, identTable(hashMapOf()), this.implicit)

                    val result:String = toString(bigStep(term, emptEnv))
                    println("Type = $type")
                    println("Result = $result")
                }

            }catch(e : NoRuleApplies){

                println("NoRuleApplies: " + e.message)
            }catch(e : UnifyFail){
                println("Type infer failed")
            }catch(e: IdentNotDefined){
                println("Variable not defined")
            }catch(e : SintaxError){
                println("Sintax error: " + e.message)

            }catch(e : ParserError){
                println("Parsing error")
            }
        }
    }

    fun main()
    {
        println("L1++ Interpreter : Please choose the configuration\n\t1 - Implicit L1++\n\t2 - Explicit L1++")
        var interpreterCalled = false
        val option = readLine()
        while(!interpreterCalled){
            when(option){
                "1" -> {
                    this.implicit = true
                    interpret()
                    interpreterCalled = true
                }
                "2" -> {
                    this.implicit = false
                    interpret()
                    interpreterCalled = true
                }
                else -> {
                    println("Please select one of the following:\n\t1 - Implicit L1++\n\t2 - Explicit L1++")

                }


            }
        }
    }
}

fun main(args: Array<String>){
    val interp = Interpreter()
    interp.main()
}